package com.demo.demo.serviceimplement;
import static com.demo.demo.config.Config.*;
import static com.demo.demo.entities.MarketType.TECH;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.demo.demo.config.Config;
import com.demo.demo.entities.MarketType;
import com.demo.demo.entities.ModeSimulation;
import com.demo.demo.entities.Simulation;
import com.demo.demo.entities.StatutSimulation;
import com.demo.demo.repository.SimulationRepository;
import com.demo.demo.services.SimulationService;
import com.demo.demo.services.TradingAgentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import com.opencsv.CSVWriter;  // FIX : Résout symbol CSVWriter (OpenCSV 5.9)
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.Optional;
import java.lang.Math;  // Pour sqrt

@Service
public class SimulationServiceImpl implements SimulationService {
    private static final Logger logger = LoggerFactory.getLogger(SimulationServiceImpl.class); // Équiv. logging Python

    @Autowired
    private SimulationRepository simulationRepository;
    @Autowired
     private TradingAgentService tradingAgentService; //pour ia adverssaire
    @Autowired
    private RestTemplate restTemplate; // Pour fetch Yahoo RT (équiv. yfinance)


    @Value("${app.trading.export.dir}")
    private String exportDir; // Dossier export CSV (équiv. Config.EXPORT_DIR)
    private final ObjectMapper mapper = new ObjectMapper();  // FIX : Mapper partagé (efficace)

    @Override
    public Simulation createSimulation(Simulation simulation) {
        // Capital obligatoire
        if (simulation.getCapital() == null || simulation.getCapital() <= 0) {
            throw new RuntimeException("Le capital doit être positif");
        }

        // MarketType obligatoire
        if (simulation.getMarketType() == null) {
            throw new RuntimeException("marketType obligatoire (FOREX, TECH, ENERGIE, etc.)");
        }

        // Description obligatoire
        if (simulation.getDescription() == null || simulation.getDescription().trim().isEmpty()) {
            throw new RuntimeException("La description de la salle est obligatoire");
        }
        simulation.setDescription(simulation.getDescription().trim());

        // Mode par défaut
        if (simulation.getModeSimulation() == null) {
            simulation.setModeSimulation(ModeSimulation.MONOJOUEUR);
        }

        // Durée par défaut
        if (simulation.getDureeJeuMinutes() == null || simulation.getDureeJeuMinutes() < 1) {
            simulation.setDureeJeuMinutes(3);
        }

        // Assets autorisés
        if (simulation.getAllowedAssets() == null || simulation.getAllowedAssets().trim().isEmpty()) {
            simulation.setAllowedAssets("ALL");
        }

        // Actif courant auto
        if (simulation.getCurrentAsset() == null || simulation.getCurrentAsset().trim().isEmpty()) {
            simulation.setCurrentAsset(getDefaultHotAsset(simulation.getMarketType()));
        }

        simulation.setCapitalActuel(simulation.getCapital());
        simulation.setStatutSimulation(StatutSimulation.EN_ATTENTE);
        simulation.setIaAdversaireActive(false);
        simulation.setScoreIaVsUser(0.0f);
        simulation.setHistoriqueTrades("[]");
        simulation.setAnalyseResultats("{}");
        simulation.setDernierTradeIa("{}");

        return simulationRepository.save(simulation);
    }
    private String getDefaultHotAsset(MarketType marketType) {
        return switch (marketType) {
            case TECH -> getRandomFrom(TECH_SYMBOLS);
            case ENERGIE -> getRandomFrom(ENERGY_SYMBOLS);
            case MATIERE_PREMIERE -> getRandomFrom(COMMODITY_SYMBOLS); // "GC" propre
            case IMMOBILIER -> getRandomFrom(REAL_ESTATE_SYMBOLS);
            case FOREX -> getRandomFrom(PAIRS);
        };
    }
    private String getRandomFrom(List<String> list) {
        if (list == null || list.isEmpty()) return "EURUSD=X";
        Random rand = new Random();
        return list.get(rand.nextInt(list.size()));
    }

    private String getRandomFrom(List<String> list, String... priorities) {
        Random rand = new Random();
        if (!list.isEmpty() && rand.nextDouble() < 0.7 && priorities.length > 0) {
            return priorities[rand.nextInt(priorities.length)];
        }
        return getRandomFrom(list);
    }

    @Override
    public Simulation getSimulationById(Integer id) {
        Optional<Simulation> optional = simulationRepository.findById(id);
        if (optional.isEmpty()) {
            throw new RuntimeException("Simulation non trouvée avec ID : " + id);
        }
        Simulation sim = optional.get();

        // CORRECTION SANS APPEL À getUpdatedTempsRestant() → ÉVITE LA BOUCLE
        if (sim.getStatutSimulation() == StatutSimulation.EXECUTEE && sim.getDateFin() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(sim.getDateFin())) {
                sim.setStatutSimulation(StatutSimulation.TERMINEE);
                sim.setTempsRestantSecondes(0);
                simulationRepository.save(sim);
            } else {
                long restant = Duration.between(now, sim.getDateFin()).getSeconds();
                sim.setTempsRestantSecondes((int) Math.max(0, restant));
            }
        }

        sim.setIaAdversaireActive(Optional.ofNullable(sim.getIaAdversaireActive()).orElse(false));
        return sim;
    }

    @Override
    public List<Simulation> getAllSimulations() {
        return simulationRepository.findAll();
    }

    @Override
    public Simulation updateSimulation(Integer id, Simulation details) {
        Simulation simulation = simulationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Simulation non trouvée avec ID : " + id));

        // SEULEMENT modifiable si EN_ATTENTE
        if (simulation.getStatutSimulation() != StatutSimulation.EN_ATTENTE) {
            throw new RuntimeException("Modification impossible : la simulation n'est plus en attente (statut actuel : " + simulation.getStatutSimulation() + ")");
        }

        // === TOUT EST AUTORISÉ TANT QUE C'EST EN_ATTENTE ===

        // Description (obligatoire et nettoyée)
        if (details.getDescription() != null) {
            if (details.getDescription().trim().isEmpty()) {
                throw new RuntimeException("La description ne peut pas être vide");
            }
            simulation.setDescription(details.getDescription().trim());
        }

        // Capital
        if (details.getCapital() != null) {
            if (details.getCapital() <= 0) {
                throw new RuntimeException("Le capital doit être positif");
            }
            simulation.setCapital(details.getCapital());
            simulation.setCapitalActuel(details.getCapital());
        }

        // Durée du match
        if (details.getDureeJeuMinutes() != null) {
            if (details.getDureeJeuMinutes() < 1 || details.getDureeJeuMinutes() > 30) {
                throw new RuntimeException("La durée doit être entre 1 et 30 minutes");
            }
            simulation.setDureeJeuMinutes(details.getDureeJeuMinutes());
        }

        // Changement de marché → on change l'actif par défaut
        if (details.getMarketType() != null) {
            simulation.setMarketType(details.getMarketType());
            simulation.setCurrentAsset(getDefaultHotAsset(details.getMarketType()));
        }

        // Forcer un actif spécifique
        if (details.getCurrentAsset() != null && !details.getCurrentAsset().trim().isEmpty()) {
            simulation.setCurrentAsset(details.getCurrentAsset().toUpperCase().trim());
        }

        // Liste d'actifs autorisés
        if (details.getAllowedAssets() != null && !details.getAllowedAssets().trim().isEmpty()) {
            simulation.setAllowedAssets(details.getAllowedAssets().toUpperCase().trim());
        }

        return simulationRepository.save(simulation);
    }


    @Override
    public void deleteSimulation(Integer id) {
        logger.info("Tentative de suppression de l'ID : {}", id);
        simulationRepository.deleteById(id);
    }
  /// ///////////////////////////////////////////////////////////////////////
    @Override
    public Simulation startSimulation(Integer id) {
        Simulation simulation = simulationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Simulation non trouvée"));

        if (simulation.getStatutSimulation() != StatutSimulation.EN_ATTENTE) {
            throw new RuntimeException("La simulation doit être en attente pour démarrer");
        }

        Integer dureeMinutes = simulation.getDureeJeuMinutes();
        LocalDateTime debut = LocalDateTime.now();
        simulation.setDateDebut(debut);
        simulation.setDateFin(debut.plusMinutes(dureeMinutes));
        simulation.setTempsRestantSecondes(dureeMinutes * 60);

        simulation.setStatutSimulation(StatutSimulation.EXECUTEE);

        // Lancement du GARCH live pour cette salle
        refreshGarchLiveForSimulation(simulation);
        simulation.setGarchLastUpdate(LocalDateTime.now());

        runForexAnalysisInSimulation(id);

        if (simulation.getModeSimulation() == ModeSimulation.MONOJOUEUR) {
            tradingAgentService.activateIaAdversaire(id);
        }

        return simulationRepository.save(simulation);
    }

    // ===================================================================
    // GARCH LIVE POUR LE JOUEUR UNIQUEMENT
    // ===================================================================

    @Scheduled(fixedRate = 30000) // Toutes les 30 secondes
    public void refreshAllGarchLive() {
        List<Simulation> active = simulationRepository.findByStatutSimulation(StatutSimulation.EXECUTEE);
        LocalDateTime now = LocalDateTime.now();

        for (Simulation sim : active) {
            if (sim.getGarchLastUpdate() == null || Duration.between(sim.getGarchLastUpdate(), now).getSeconds() > 25) {
                refreshGarchLiveForSimulation(sim);
                sim.setGarchLastUpdate(now);
                simulationRepository.save(sim);
            }
        }
    }
    public void refreshGarchLiveForSimulation(Simulation sim) {
        MarketType market = sim.getMarketType();
        List<String> symbols = switch (market) {
            case FOREX -> Config.PAIRS;
            case TECH -> Config.TECH_SYMBOLS;
            case ENERGIE -> Config.ENERGY_SYMBOLS;
            case MATIERE_PREMIERE -> Config.COMMODITY_SYMBOLS.stream().map(s -> s + "=F").toList();
            case IMMOBILIER -> Config.REAL_ESTATE_SYMBOLS;
        };

        Map<String, Object> bestSignal = Map.of("raison", "Analyse en cours...");
        double bestScore = 0;

        for (String symbol : symbols) {
            try {
                List<Map<String, Object>> data = getData(symbol, "1h");
                if (data == null || data.size() < 25) continue;

                double[] closes = data.stream().mapToDouble(r -> (Double) r.get("Close")).toArray();
                double price = closes[closes.length - 1];
                double prev = closes[closes.length - 2];
                double change = (price - prev) / prev * 100;
                double volScore = assessVolatility(closes);

                double ema10 = calculateEMA(closes, 10);
                double ema30 = calculateEMA(closes, 30);
                boolean bullish = price > ema10 && ema10 > ema30;

                String pred = bullish ? "HAUSSIER" : "BAISSIER";
                String force = volScore > 0.7 ? "TRÈS FORTE" : volScore < 0.4 ? "FAIBLE" : "MODÉRÉE";

                String raison = String.format("%s %.2f$ (%+.2f%%) → %s %s",
                        symbol.replace("=F", ""), price, change, pred, force);

                double score = bullish ? volScore : (1 - volScore);
                if (score > bestScore) {
                    bestScore = score;
                    bestSignal = Map.of(
                            "symbol", symbol.replace("=F", ""),
                            "price", String.format("%.2f", price),
                            "change", String.format("%+.2f%%", change),
                            "prediction", pred,
                            "force", force,
                            "conseil", bullish ? "ACHAT recommandé" : "VENTE ou ATTENDRE",
                            "raison", raison
                    );
                }
            } catch (Exception ignored) {}
        }

        Map<String, Object> garchJson = Map.of(
                "market", market.name(),
                "best_signal", bestSignal,
                "update", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        );

        try {
            sim.setGarchLivePrediction(mapper.writeValueAsString(garchJson));
        } catch (Exception e) {
            sim.setGarchLivePrediction("{}");
        }
    }
    @Override
    public Simulation endSimulation(Integer id) {
        Simulation sim = getSimulationById(id); // Utilise la version corrigée

        if (sim.getStatutSimulation() != StatutSimulation.EXECUTEE) {
            throw new RuntimeException("La simulation doit être en cours pour être terminée");
        }

        sim.setStatutSimulation(StatutSimulation.TERMINEE);
        sim.setDateFin(LocalDateTime.now());
        return simulationRepository.save(sim);
    }

    @Override
    public void activateIaAdversaire(Integer simulationId) {
        tradingAgentService.activateIaAdversaire(simulationId);

    }

    @Override
    public String getRealTimeIaResponse(Integer simulationId, String userTrade, String asset) {
        return tradingAgentService.getRealTimeIaResponse(simulationId, userTrade, asset);    }
/*
// FIX : Tour humain vs IA - VERSION FINALE RÉELLE
@Override
public Map<String, Object> playIaMove(Integer simulationId, Map<String, Object> humanTrade) {
    Simulation sim = simulationRepository.findById(simulationId).orElseThrow();

    // === VÉRIFIE TEMPS VIA NOUVELLE MÉTHODE (auto-fin) ===
    Map<String, Object> tempsInfo = getUpdatedTempsRestant(simulationId);
    if ((Boolean) tempsInfo.get("fini")) {
        throw new RuntimeException("TEMPS ÉCOULÉ ! Le match est terminé.");
    }
    int tempsActuel = (Integer) tempsInfo.get("tempsRestant");
    sim.setTempsRestantSecondes(tempsActuel);

    // === VÉRIFIE IA ACTIVE ===
    if (Boolean.FALSE.equals(Optional.ofNullable(sim.getIaAdversaireActive()).orElse(false))
            || sim.getModeSimulation() != ModeSimulation.MONOJOUEUR) {
        throw new RuntimeException("IA adversaire non active (mode MONOJOUEUR seulement)");
    }

    String asset = (String) humanTrade.get("asset");

    // === TRADE HUMAIN → JSON pour Gemini ===
    String userTradeJson;
    try {
        userTradeJson = mapper.writeValueAsString(humanTrade);
    } catch (JsonProcessingException e) {
        logger.error("JSON write error for humanTrade: {}", e.getMessage());
        throw new RuntimeException("Invalid human trade JSON");
    }

    // === APPEL À GEMINI ===
    String iaResponse = tradingAgentService.getRealTimeIaResponse(simulationId, userTradeJson, asset);

    // === PARSE RÉPONSE IA ===
    Map<String, Object> iaTrade;
    try {
        iaTrade = mapper.readValue(iaResponse, Map.class);
    } catch (JsonProcessingException e) {
        logger.error("JSON parse error for IA response: {}", e.getMessage());
        iaTrade = fallbackIaTrade();
    }

    // === AJOUT CRUCIAL : on garde l'asset pour le calcul du score réel ===
    iaTrade.put("asset", asset);
    try {
        sim.setDernierTradeIa(mapper.writeValueAsString(iaTrade));
    } catch (JsonProcessingException e) {
        sim.setDernierTradeIa("{}");
    }
    simulationRepository.save(sim);
    // === FIN AJOUT CRUCIAL ===

    // === PnL Humain (tu peux le rendre réel plus tard si tu veux) ===
    Float pnlHuman = (float) (Math.random() * 100 - 50);

    // === MISE À JOUR DU SCORE RÉEL (utilise le prix live Yahoo) ===
    tradingAgentService.updateScoreIaVsUser(simulationId, pnlHuman);

    // === HISTORIQUE DES TOURS ===
    List<Map<String, Object>> historique;
    try {
        historique = mapper.readValue(sim.getHistoriqueTrades(), List.class);
    } catch (JsonProcessingException e) {
        historique = new ArrayList<>();
    }

    Map<String, Object> tour = Map.of(
            "tour", historique.size() + 1,
            "human", humanTrade,
            "ia", iaTrade
    );
    historique.add(tour);

    try {
        sim.setHistoriqueTrades(mapper.writeValueAsString(historique));
    } catch (JsonProcessingException e) {
        sim.setHistoriqueTrades("[]");
    }
    simulationRepository.save(sim);

    // === RÉPONSE AU FRONT ===
    Map<String, Object> response = new HashMap<>();
    response.put("tour", tour);
    response.put("scoreIaVsUser", sim.getScoreIaVsUser());

    return response;
}
    private Map<String, Object> fallbackIaTrade() {
        return Map.of(
                "tradeType", "ACHAT",
                "quantity", 100.0,
                "price", 1.08,
                "stopLoss", 1.06,
                "takeProfit", 1.09,
                "reason", "Fallback : Contre-trade basique (risque 2%)"
        );}
*/
@Override
public Map<String, Object> playIaMove(Integer simulationId, Map<String, Object> humanTrade) {
    Simulation sim = simulationRepository.findById(simulationId)
            .orElseThrow(() -> new RuntimeException("Simulation non trouvée"));

    // === 1. GESTION DU TEMPS ===
    Map<String, Object> tempsInfo = getUpdatedTempsRestant(simulationId);
    if (Boolean.TRUE.equals(tempsInfo.get("fini"))) {
        throw new RuntimeException("⏰ TEMPS ÉCOULÉ ! Le match est terminé.");
    }
    int tempsRestant = (Integer) tempsInfo.get("tempsRestant");
    sim.setTempsRestantSecondes(tempsRestant);

    // === 2. VÉRIF MODE + IA ACTIVE ===
    if (!ModeSimulation.MONOJOUEUR.equals(sim.getModeSimulation()) ||
            Boolean.FALSE.equals(Optional.ofNullable(sim.getIaAdversaireActive()).orElse(false))) {
        throw new RuntimeException("Mode IA adversaire non activé");
    }

    String asset = (String) humanTrade.get("asset");

    // === 3. GEMINI – PROMPT CLASSIQUE (l'IA ne voit PAS GARCH) ===
    String iaResponseJson;
    try {
        String humanTradeJson = mapper.writeValueAsString(humanTrade);
        iaResponseJson = tradingAgentService.getRealTimeIaResponse(simulationId, humanTradeJson, asset);
    } catch (JsonProcessingException e) {
        throw new RuntimeException("Erreur sérialisation trade humain");
    }

    // === 4. PARSE RÉPONSE IA + FALLBACK ===
    Map<String, Object> iaTrade;
    try {
        iaTrade = mapper.readValue(iaResponseJson, new TypeReference<Map<String, Object>>() {});
    } catch (Exception e) {
        logger.warn("Gemini réponse invalide → fallback activé");
        iaTrade = fallbackIaTrade();
    }
    iaTrade.put("asset", asset);

    try {
        sim.setDernierTradeIa(mapper.writeValueAsString(iaTrade));
    } catch (JsonProcessingException e) {
        sim.setDernierTradeIa("{}");
    }

    // === 5. PRIX LIVE + CALCUL PnL RÉELS ===
    Map<String, Object> livePrices = getYahooLivePrices();
    double prixActuel = 145.0; // fallback safe
    if (livePrices.containsKey(asset)) {
        prixActuel = ((Number) ((Map<?, ?>) livePrices.get(asset)).get("price")).doubleValue();
    }

    // PnL Humain
    String typeHuman = (String) humanTrade.get("type");
    double prixHuman = ((Number) humanTrade.get("prix")).doubleValue();
    double qtyHuman = ((Number) humanTrade.get("quantite")).doubleValue();
    double pnlHumanTour = "ACHAT".equalsIgnoreCase(typeHuman)
            ? (prixActuel - prixHuman) * qtyHuman
            : (prixHuman - prixActuel) * qtyHuman;

    // PnL IA
    String typeIa = (String) iaTrade.get("type");
    double prixIa = ((Number) iaTrade.get("prix")).doubleValue();
    double qtyIa = ((Number) iaTrade.get("quantite")).doubleValue();
    double pnlIaTour = "ACHAT".equalsIgnoreCase(typeIa)
            ? (prixActuel - prixIa) * qtyIa
            : (prixIa - prixActuel) * qtyIa;

    // === 6. SCORE GLOBAL ===
    float ancienScore = sim.getScoreIaVsUser() != null ? sim.getScoreIaVsUser() : 0f;
    float nouveauScore = ancienScore + (float) (pnlIaTour - pnlHumanTour);
    sim.setScoreIaVsUser(nouveauScore);

    // === 7. HISTORIQUE DES TOURS ===
    List<Map<String, Object>> historique = new ArrayList<>();
    try {
        historique = mapper.readValue(sim.getHistoriqueTrades(), new TypeReference<List<Map<String, Object>>>() {});
    } catch (Exception ignored) {}

    Map<String, Object> tour = new LinkedHashMap<>();
    tour.put("tour", historique.size() + 1);
    tour.put("human", humanTrade);
    tour.put("ia", iaTrade);
    historique.add(tour);

    try {
        sim.setHistoriqueTrades(mapper.writeValueAsString(historique));
    } catch (JsonProcessingException e) {
        sim.setHistoriqueTrades("[]");
    }

    simulationRepository.save(sim);

    // === 8. GARCH LIVE – UNIQUEMENT POUR LE JOUEUR (super pouvoir secret !) ===
    Map<String, Object> garchPlayerPower = Map.of("visible", false);

    if (sim.getGarchLivePrediction() != null && !sim.getGarchLivePrediction().equals("{}")) {
        try {
            Map<String, Object> live = mapper.readValue(sim.getGarchLivePrediction(), new TypeReference<>() {});
            Map<String, Object> best = (Map<String, Object>) live.get("best_signal");

            garchPlayerPower = Map.of(
                    "visible", true,
                    "titre", "🔥 TON AVANTAGE SECRET GARCH (l’IA est aveugle !)",
                    "marche", sim.getMarketType().name(),
                    "actif", best.getOrDefault("symbol", "N/A"),
                    "prix", best.getOrDefault("price", "?"),
                    "variation", best.getOrDefault("change", "0%"),
                    "prediction", best.getOrDefault("prediction", "NEUTRE") + " " + best.getOrDefault("force", ""),
                    "conseil", best.getOrDefault("conseil", "Observe..."),
                    "update", live.getOrDefault("update", "N/A")
            );
        } catch (Exception ignored) {}
    }

    // === 9. RÉPONSE FRONT ===
    String resultatTour = pnlHumanTour > 0
            ? "🟢 TOUR GAGNANT ! +" + String.format("%.2f $", pnlHumanTour)
            : pnlHumanTour < 0
            ? "🔴 Tour perdu " + String.format("%.2f $", pnlHumanTour)
            : "🟡 Tour neutre 0.00 $";

    String leader = nouveauScore > 0
            ? "🤖 IA devant de " + String.format("%.2f $", Math.abs(nouveauScore))
            : nouveauScore < 0
            ? "🚀💪 TU ES DEVANT de " + String.format("%.2f $", Math.abs(nouveauScore))
            : "⚔️ ÉGALITÉ PARFAITE";

    Map<String, Object> response = new HashMap<>();
    response.put("tour", tour);
    response.put("pnlTour", resultatTour);
    response.put("pnlHuman", String.format("%.2f $", pnlHumanTour));
    response.put("pnlIa", String.format("%.2f $", pnlIaTour));
    response.put("scoreIaVsUser", nouveauScore);
    response.put("leader", leader);
    response.put("tempsRestantSecondes", tempsRestant);
    response.put("garchPlayerPower", garchPlayerPower); // ← TON SUPER POUVOIR SECRET !

    return response;
}
    // ===================================================================
// FALLBACK (maintenant dans la même classe → plus d’erreur !)
// ===================================================================
    private Map<String, Object> fallbackIaTrade() {
        Random r = new Random();
        String type = r.nextBoolean() ? "ACHAT" : "VENTE";
        double basePrice = 140 + r.nextDouble() * 20;
        double qty = 50 + r.nextInt(500);
        double sl = type.equals("ACHAT") ? basePrice * 0.97 : basePrice * 1.03;
        double tp = type.equals("ACHAT") ? basePrice * 1.06 : basePrice * 0.94;

        return Map.of(
                "type", type,
                "quantite", Math.round(qty),
                "prix", Math.round(basePrice * 100.0) / 100.0,
                "stopLoss", Math.round(sl * 100.0) / 100.0,
                "takeProfit", Math.round(tp * 100.0) / 100.0,
                "raison", "Fallback sécurisé : contre-trade conservateur"
        );
    }
    // (runForexAnalysisInSimulation, updateActiveSimulations, getData, cleanData, resampleTo4h, getFallbackData inchangées - copiez de précédent)

    @Override
    public void runForexAnalysisInSimulation(Integer simulationId) {
        // (Code inchangé de précédent)
        Simulation simulation = getSimulationById(simulationId);
        logger.info("🔄 Analyse des devises en cours pour simu #{} - {}", simulationId, LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));

        new File(exportDir).mkdirs();

        Map<String, List<Map<String, Object>>> allSignals = new HashMap<>();
        for (String pair : Config.PAIRS) {
            List<Map<String, Object>> pairSignals = new ArrayList<>();
            for (String timeframe : Config.TIMEFRAMES) {
                try {
                    List<Map<String, Object>> data = getData(pair, timeframe);
                    if (data != null && data.size() > 15) {
                        Map<String, Object> indicators = calculateIndicators(data);
                        Map<String, Object> signal = generateSignal(indicators, timeframe);
                        pairSignals.add(signal);
                    }
                    Thread.sleep(500);
                } catch (InterruptedException | RuntimeException e) {
                    logger.warn("Erreur pour paire {} timeframe {}: {}", pair, timeframe, e.getMessage());
                }
            }
            allSignals.put(pair, pairSignals);
        }

        exportToCsv(allSignals, simulationId);

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        dashboard.put("iteration", simulation.getId());
        dashboard.put("signals", allSignals);
        dashboard.put("consensus", getPairConsensusAll(allSignals));

        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonDashboard = mapper.writeValueAsString(dashboard);
            simulation.setAnalyseResultats(jsonDashboard);
            simulationRepository.save(simulation);
            logger.info("🎯 Dashboard JSON sauvegardé pour simu #{}", simulationId);
        } catch (Exception e) {
            logger.error("Erreur génération JSON: {}", e.getMessage());
            simulation.setAnalyseResultats("{}");
            simulationRepository.save(simulation);
        }
    }

    @Override
    public Map<String, Object> getUpdatedTempsRestant(Integer simulationId) {
        // CHARGE DIRECTEMENT SANS PASSER PAR getSimulationById
        Simulation sim = simulationRepository.findById(simulationId)
                .orElseThrow(() -> new RuntimeException("Simulation non trouvée"));

        LocalDateTime now = LocalDateTime.now();
        if (sim.getDateFin() == null) {
            return Map.of("tempsRestant", 0, "fini", true, "dureeMinutes", sim.getDureeJeuMinutes());
        }

        long restantMillis = Duration.between(now, sim.getDateFin()).toMillis();
        int tempsRestant = (int) Math.max(0, restantMillis / 1000);
        boolean fini = tempsRestant == 0;

        if (fini && sim.getStatutSimulation() == StatutSimulation.EXECUTEE) {
            sim.setStatutSimulation(StatutSimulation.TERMINEE);
            sim.setTempsRestantSecondes(0);
            simulationRepository.save(sim);
        } else if (!fini) {
            sim.setTempsRestantSecondes(tempsRestant);
            simulationRepository.save(sim);
        }

        return Map.of("tempsRestant", tempsRestant, "fini", fini, "dureeMinutes", sim.getDureeJeuMinutes());
    }

    @Override
    public List<Simulation> getSimulationsByStatus(StatutSimulation statut) {
        return simulationRepository.findByStatutSimulation(statut);    }
/*
@Override
public Map<String, Object> getYahooLivePrices() {
    Map<String, Object> result = new HashMap<>();

    // === FOREX + TECH ENSEMBLE ===
    List<String> allSymbols = new ArrayList<>(Config.PAIRS);
    allSymbols.addAll(Config.TECH_SYMBOLS);

    for (String symbol : allSymbols) {
        try {
            String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + symbol +
                    "?range=1d&interval=1m";

            logger.info("Yahoo → {}", url);

            String jsonStr = restTemplate.getForObject(url, String.class);
            if (jsonStr == null || jsonStr.trim().isEmpty()) {
                logger.warn("Réponse vide pour {}", symbol);
                result.put(symbol, Map.of("price", 0.0, "changePct", 0.0));
                continue;
            }

            JsonNode root = mapper.readTree(jsonStr);
            JsonNode chart = root.path("chart");
            if (chart.isMissingNode()) {
                logger.warn("Pas de 'chart' dans la réponse pour {}", symbol);
                result.put(symbol, Map.of("price", 0.0, "changePct", 0.0));
                continue;
            }

            JsonNode resultNode = chart.path("result");
            if (resultNode.isMissingNode() || resultNode.size() == 0) {
                logger.warn("Pas de 'result' pour {}", symbol);
                result.put(symbol, Map.of("price", 0.0, "changePct", 0.0));
                continue;
            }

            JsonNode meta = resultNode.get(0).path("meta");
            double price = meta.path("regularMarketPrice").asDouble(0.0);
            double previousClose = meta.path("chartPreviousClose").asDouble(0.0);

            if (price <= 0 || previousClose <= 0) {
                logger.warn("Prix invalide pour {}: price={}, previousClose={}", symbol, price, previousClose);
                result.put(symbol, Map.of("price", 0.0, "changePct", 0.0));
                continue;
            }

            double changePct = ((price - previousClose) / previousClose) * 100;

            result.put(symbol, Map.of(
                    "price", round(price, symbol.contains("=X") ? 5 : 2),
                    "changePct", round(changePct, 2)
            ));

            logger.info("{} → price: {}, change: {}%", symbol, price, changePct);

        } catch (Exception e) {
            logger.error("Exception Yahoo {}: {}", symbol, e.getMessage(), e);
            result.put(symbol, Map.of("price", 0.0, "changePct", 0.0));
        }
    }
    return result;
}
*/
@Override
public Map<String, Object> getYahooLivePrices() {
    Map<String, Object> result = new HashMap<>();

    // ON CHARGE TOUS LES MARCHÉS POSSIBLES
    Set<String> allSymbols = new HashSet<>();

    // Forex (déjà avec =X)
    allSymbols.addAll(Config.PAIRS);

    // Tech
    allSymbols.addAll(Config.TECH_SYMBOLS);

    // Énergie
    allSymbols.addAll(Config.ENERGY_SYMBOLS);

    // Immobilier
    allSymbols.addAll(Config.REAL_ESTATE_SYMBOLS);

    // Matières premières : on ajoute =F automatiquement
    for (String s : Config.COMMODITY_SYMBOLS) {
        allSymbols.add(s + "=F");
    }

    for (String symbol : allSymbols) {
        try {
            String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + symbol + "?range=1d&interval=1m";
            logger.info("Yahoo → {}", url);

            String jsonStr = restTemplate.getForObject(url, String.class);
            if (jsonStr == null || jsonStr.trim().isEmpty()) {
                result.put(symbol, Map.of("price", 0.0, "changePct", 0.0));
                continue;
            }

            JsonNode root = mapper.readTree(jsonStr);
            JsonNode chart = root.path("chart");
            if (chart.isMissingNode()) {
                result.put(symbol, Map.of("price", 0.0, "changePct", 0.0));
                continue;
            }

            JsonNode resultNode = chart.path("result");
            if (resultNode.isMissingNode() || resultNode.size() == 0) {
                result.put(symbol, Map.of("price", 0.0, "changePct", 0.0));
                continue;
            }

            JsonNode meta = resultNode.get(0).path("meta");
            double price = meta.path("regularMarketPrice").asDouble(0.0);
            double previousClose = meta.path("chartPreviousClose").asDouble(0.0);

            if (price <= 0 || previousClose <= 0) {
                result.put(symbol, Map.of("price", 0.0, "changePct", 0.0));
                continue;
            }

            double changePct = ((price - previousClose) / previousClose) * 100;

            result.put(symbol, Map.of(
                    "price", round(price, symbol.contains("=X") ? 5 : 2),
                    "changePct", round(changePct, 2)
            ));

        } catch (Exception e) {
            logger.error("Exception Yahoo {}: {}", symbol, e.getMessage());
            result.put(symbol, Map.of("price", 0.0, "changePct", 0.0));
        }
    }
    return result;
}
    @Override
    public List<Map<String, Object>> getYahooCandles(String pair) {
        try {
            String url = String.format(
                    "https://query1.finance.yahoo.com/v8/finance/chart/%s?interval=5m&period=1d", pair
            );
            String jsonStr = restTemplate.getForObject(url, String.class);
            if (jsonStr != null && !jsonStr.contains("error")) {
                JsonNode root = mapper.readTree(jsonStr);
                JsonNode resultNode = root.path("chart").path("result").get(0);
                if (resultNode != null) {
                    JsonNode opens = resultNode.path("indicators").path("quote").get(0).path("open");
                    JsonNode highs = resultNode.path("indicators").path("quote").get(0).path("high");
                    JsonNode lows = resultNode.path("indicators").path("quote").get(0).path("low");
                    JsonNode closes = resultNode.path("indicators").path("quote").get(0).path("close");

                    List<Map<String, Object>> data = new ArrayList<>();
                    int start = Math.max(0, opens.size() - 5);
                    for (int i = start; i < opens.size(); i++) {
                        Map<String, Object> candle = new HashMap<>();
                        candle.put("Open", opens.get(i).asDouble(0));
                        candle.put("High", highs.get(i).asDouble(0));
                        candle.put("Low", lows.get(i).asDouble(0));
                        candle.put("Close", closes.get(i).asDouble(0));
                        data.add(candle);
                    }
                    return data;
                }
            }
        } catch (Exception e) {
            logger.warn("Erreur chandeliers {}: fallback", pair);
        }
        List<Map<String, Object>> fallback = getFallbackData(pair, "5m");
        return fallback.subList(0, Math.min(5, fallback.size()));

    }



    @Scheduled(fixedDelayString = "${app.forex.update-interval:300000}")
    public void updateActiveSimulations() {
        // FIX : Utilise maintenant la méthode ajoutée en repo
        List<Simulation> activeSimus = simulationRepository.findByStatutSimulation(StatutSimulation.EXECUTEE);
        for (Simulation simu : activeSimus) {
            runForexAnalysisInSimulation(simu.getId());
        }
        logger.info("⏳ Prochaine mise à jour dans 5 minutes...");
    }

    // (Autres helpers getData, cleanData, etc. inchangés)

    private List<Map<String, Object>> getData(String pair, String interval) {
        // (Inchangé)
        try {
            String yfInterval = "1h";
            String period = interval.equals("1h") ? "7d" : "30d";
            String url = String.format("https://query1.finance.yahoo.com/v8/finance/chart/%s?interval=%s&period=%s", pair, yfInterval, period);
            String jsonStr = restTemplate.getForObject(url, String.class);
            if (jsonStr != null && !jsonStr.contains("error")) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(jsonStr);
                JsonNode result = root.path("chart").path("result").get(0);
                if (result != null) {
                    JsonNode timestamps = result.path("timestamp");
                    JsonNode quote = result.path("indicators").path("quote").get(0);
                    JsonNode opens = quote.path("open"), highs = quote.path("high"), lows = quote.path("low"), closes = quote.path("close");

                    List<Map<String, Object>> data = new ArrayList<>();
                    for (int i = 0; i < timestamps.size(); i++) {
                        Map<String, Object> row = new HashMap<>();
                        row.put("Open", opens.get(i).asDouble(0));
                        row.put("High", highs.get(i).asDouble(0));
                        row.put("Low", lows.get(i).asDouble(0));
                        row.put("Close", closes.get(i).asDouble(0));
                        row.put("timestamp", LocalDateTime.ofEpochSecond(timestamps.get(i).asLong(0), 0, ZoneOffset.UTC).toString());
                        data.add(row);
                    }
                    data = cleanData(data);
                    if (interval.equals("4h")) data = resampleTo4h(data);
                    if (data.size() > 10) return data;
                }
            }
        } catch (Exception e) {
            logger.warn("Yahoo fetch échoué pour {}: utilisation fallback", pair);
        }
        return getFallbackData(pair, interval);
    }

    private List<Map<String, Object>> cleanData(List<Map<String, Object>> data) {
        // (Inchangé)
        return data.stream()
                .filter(row -> row.containsKey("Open") && row.containsKey("High") && row.containsKey("Low") && row.containsKey("Close"))
                .filter(row -> row.get("Open") != null && row.get("Close") != null)
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> resampleTo4h(List<Map<String, Object>> data1h) {
        // (Inchangé)
        List<Map<String, Object>> data4h = new ArrayList<>();
        for (int i = 0; i < data1h.size(); i += 4) {
            int endIdx = Math.min(i + 4, data1h.size());
            List<Map<String, Object>> chunk = data1h.subList(i, endIdx);
            if (chunk.size() == 4) {
                Map<String, Object> agg = new HashMap<>();
                agg.put("Open", chunk.get(0).get("Open"));
                agg.put("High", chunk.stream().mapToDouble(r -> (Double) r.get("High")).max().orElse(0.0));
                agg.put("Low", chunk.stream().mapToDouble(r -> (Double) r.get("Low")).min().orElse(0.0));
                agg.put("Close", chunk.get(chunk.size() - 1).get("Close"));
                agg.put("timestamp", chunk.get(0).get("timestamp"));
                data4h.add(agg);
            }
        }
        return data4h.isEmpty() ? data1h : data4h;
    }

    private List<Map<String, Object>> getFallbackData(String pair, String interval) {
        // (Inchangé)
        Map<String, Double> currentPrices = Map.of(
                "EURUSD=X", 1.0650, "USDJPY=X", 151.50, "GBPUSD=X", 1.2350,
                "EURJPY=X", 161.50, "AUDUSD=X", 0.6400, "USDCAD=X", 1.3750
        );
        double basePrice = currentPrices.getOrDefault(pair, 1.0650);
        int periods = 50;
        double[] trends = {-0.0002, 0.0, 0.0002};
        double trend = trends[(int) (Math.random() * 3)];
        double volatility = 0.0015;
        Random rand = new Random(System.currentTimeMillis());
        double[] returns = new double[periods];
        for (int i = 0; i < periods; i++) {
            returns[i] = rand.nextGaussian() * volatility + trend;
        }
        double[] prices = new double[periods];
        prices[0] = basePrice;
        for (int i = 1; i < periods; i++) {
            prices[i] = prices[i - 1] * (1 + returns[i]);
        }
        List<Map<String, Object>> data = new ArrayList<>();
        LocalDateTime end = LocalDateTime.now();
        int freqHours = interval.equals("4h") ? 4 : 1;
        for (int i = 0; i < periods; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("Open", prices[i] * 0.9998);
            row.put("High", prices[i] + Math.abs(returns[i]) * basePrice * 0.8);
            row.put("Low", prices[i] - Math.abs(returns[i]) * basePrice * 0.8);
            row.put("Close", prices[i]);
            row.put("timestamp", end.minusHours((periods - i) * freqHours).toString());
            data.add(row);
        }
        return data;
    }

    private Map<String, Object> calculateIndicators(List<Map<String, Object>> df) {
        // (Inchangé, appelle assessVolatility fixée)
        if (df.size() < 20) {
            return getNeutralIndicators();
        }
        double[] closes = df.stream().mapToDouble(row -> (Double) row.get("Close")).toArray();
        double currentPrice = closes[closes.length - 1];
        double prevPrice = closes[closes.length - 2];
        double emaShort = calculateEMA(closes, Config.EMA_SHORT);
        double emaLong = calculateEMA(closes, Config.EMA_LONG);
        double trendDirection = assessTrend(closes, emaShort, emaLong);
        double rsi = calculateRSI(closes);
        double[] macdRes = calculateMACD(closes);
        double macd = macdRes[0];
        double macdSignal = macdRes[1];
        double momentumScore = assessMomentum(rsi, macd, macdSignal);
        double volatilityScore = assessVolatility(closes);  // Utilise fix
        double compositeScore = trendDirection * 0.40 + momentumScore * 0.35 + volatilityScore * 0.25;

        Map<String, Object> indicators = new HashMap<>();
        indicators.put("current_price", currentPrice);
        indicators.put("price_change", ((currentPrice - prevPrice) / prevPrice) * 100);
        indicators.put("ema_short", emaShort);
        indicators.put("ema_long", emaLong);
        indicators.put("rsi", rsi);
        indicators.put("macd", macd);
        indicators.put("macd_signal", macdSignal);
        indicators.put("trend_score", trendDirection);
        indicators.put("momentum_score", momentumScore);
        indicators.put("composite_score", compositeScore);
        indicators.put("timestamp", LocalDateTime.now().toString());
        return indicators;
    }

    // (calculateEMA, calculateRSI, calculateMACD, assessTrend, assessMomentum inchangées)

    private double calculateEMA(double[] prices, int period) {
        double multiplier = 2.0 / (period + 1);
        double ema = prices[0];
        for (int i = 1; i < prices.length; i++) {
            ema = (prices[i] * multiplier) + (ema * (1 - multiplier));
        }
        return ema;
    }

    private double calculateRSI(double[] prices) {
        int period = Config.RSI_PERIOD;
        if (prices.length < period + 1) return 50.0;
        double[] deltas = new double[prices.length - 1];
        for (int i = 1; i < prices.length; i++) {
            deltas[i - 1] = prices[i] - prices[i - 1];
        }
        double[] gains = DoubleStream.of(deltas).map(d -> Math.max(d, 0)).toArray();
        double[] losses = DoubleStream.of(deltas).map(d -> Math.max(-d, 0)).toArray();
        int start = prices.length - period - 1;
        double avgGain = Arrays.stream(Arrays.copyOfRange(gains, start, gains.length)).average().orElse(0.0);
        double avgLoss = Arrays.stream(Arrays.copyOfRange(losses, start, losses.length)).average().orElse(0.0001);
        double rs = avgGain / avgLoss;
        return 100 - (100 / (1 + rs));
    }

    private double[] calculateMACD(double[] prices) {
        double emaFast = calculateEMA(prices, Config.MACD_FAST);
        double emaSlow = calculateEMA(prices, Config.MACD_SLOW);
        double macd = emaFast - emaSlow;
        double multiplier = 2.0 / (Config.MACD_SIGNAL + 1);
        double signal = macd;
        for (int i = 1; i < Config.MACD_SIGNAL; i++) {
            signal = (macd * multiplier) + (signal * (1 - multiplier));
        }
        return new double[]{macd, signal};
    }

    private double assessTrend(double[] prices, double emaShort, double emaLong) {
        double currentPrice = prices[prices.length - 1];
        double emaTrend = emaShort > emaLong ? 1.0 : -1.0;
        double priceVsEma = currentPrice > emaLong ? 1.0 : -1.0;
        double momentumTrend = 0.0;
        if (prices.length >= 6) {
            double recentChange = (prices[prices.length - 1] - prices[prices.length - 6]) / prices[prices.length - 6];
            if (recentChange > 0.001) momentumTrend = 1.0;
            else if (recentChange < -0.001) momentumTrend = -1.0;
        }
        return (emaTrend + priceVsEma + momentumTrend) / 3.0;
    }

    private double assessMomentum(double rsi, double macd, double macdSignal) {
        double rsiScore = 0.0;
        if (rsi > 65) rsiScore = -0.8;
        else if (rsi < 35) rsiScore = 0.8;
        else if (rsi > 55) rsiScore = -0.3;
        else if (rsi < 45) rsiScore = 0.3;
        double macdScore = macd > macdSignal ? 0.7 : -0.7;
        return (rsiScore + macdScore) / 2;
    }

    /**
     * assessVolatility : NOUVEAU GARCH(1,1) - Prédiction vol temps réel sur données fraîches Yahoo.
     * Utilise log-returns des DERNIÈRES 30 bougies (récent pour réactivité, comme RSI=14).
     * À chaque run: fetch Yahoo → closes fraîches → GARCH sur fenêtre récente → score 0-1.
     * Pour TOUTES paires (via boucle Config.PAIRS). Prédit σ_t future pour estimations (range ±2σ).
     */
    private double assessVolatility(double[] prices) {
        if (prices.length < 20) {  // Min pour init stable (comme avant)
            return 0.5;  // Neutre si données insuffisantes
        }

        // 1. Log-returns sur full closes (données fraîches de getData()/Yahoo)
        double[] returns = new double[prices.length - 1];
        for (int i = 1; i < prices.length; i++) {
            if (prices[i - 1] > 0) {  // Safe: évite log(0), rare en forex
                returns[i - 1] = Math.log(prices[i] / prices[i - 1]);
            } else {
                returns[i - 1] = 0.0;  // Fallback neutre
            }
        }

        // 2. Fenêtre RECENTE: 30 dernières (réactif temps réel ; tunable si besoin)
        int window = 30;  // Comme ton period=14, mais + pour GARCH mémoire
        int n = Math.min(window, returns.length);
        double[] recentReturns = Arrays.copyOfRange(returns, returns.length - n, returns.length);

        // 3. Moyenne returns (pour résidus ε)
        double meanReturn = DoubleStream.of(recentReturns).average().orElse(0.0);

        // 4. Init variance (échantillon simple sur fenêtre, >0)
        double[] epsilonInit = DoubleStream.of(recentReturns).map(r -> r - meanReturn).toArray();
        double initVar = DoubleStream.of(epsilonInit).map(e -> e * e).average().orElse(0.0001);

        // 5. Params GARCH fixes (adaptés forex: persistence haute pour trends longs)
        double omega = 0.0001;  // Variance fond (stable EURUSD etc.)
        double alpha = 0.1;     // Choc récent (10% impact spike)
        double beta = 0.85;     // Héritage passé (85%, <1 pour stabilité)

        // 6. Itération GARCH: update variance conditionnelle (prédit σ_t pour NEXT bougie)
        double var_t = initVar;
        for (int i = 1; i < recentReturns.length; i++) {
            double epsilon_t1 = recentReturns[i - 1] - meanReturn;  // Résidu lag-1
            var_t = omega + alpha * (epsilon_t1 * epsilon_t1) + beta * var_t;
        }
        double sigma_t = Math.sqrt(var_t);  // Vol prédite (e.g., 0.005 = 0.5%)

        // 7. Score 0-1 (comme AVANT: seuils forex pour composite/signaux)
        if (sigma_t > 0.008) return 0.8;  // Haute vol prédite → ATTENDRE (risque spike)
        if (sigma_t < 0.003) return 0.3;  // Basse → ACHAT/VENTE confiant
        return 0.5;  // Neutre
    }

    // (getNeutralIndicators, generateSignal, calculateConfidence, generateReason, getPairConsensusAll, getPairConsensus, exportToCsv inchangées - copiez de précédent)

    private Map<String, Object> getNeutralIndicators() {
        Map<String, Object> neutral = new HashMap<>();
        neutral.put("current_price", 1.0);
        neutral.put("price_change", 0.0);
        neutral.put("ema_short", 1.0);
        neutral.put("ema_long", 1.0);
        neutral.put("rsi", 50.0);
        neutral.put("macd", 0.0);
        neutral.put("macd_signal", 0.0);
        neutral.put("trend_score", 0.0);
        neutral.put("momentum_score", 0.0);
        neutral.put("composite_score", 0.0);
        neutral.put("timestamp", LocalDateTime.now().toString());
        return neutral;
    }

    private Map<String, Object> generateSignal(Map<String, Object> indicators, String timeframe) {
        // (Inchangé)
        double compositeScore = (Double) indicators.get("composite_score");
        double rsi = (Double) indicators.get("rsi");
        String signal;
        int confidence;
        String reason;
        if (compositeScore > 0.15 && rsi < 75) {
            signal = "ACHAT";
            confidence = calculateConfidence(indicators, "HAUSSIER");
            reason = generateReason(indicators, "HAUSSIER");
        } else if (compositeScore < -0.15 && rsi > 25) {
            signal = "VENTE";
            confidence = calculateConfidence(indicators, "BAISSIER");
            reason = generateReason(indicators, "BAISSIER");
        } else {
            signal = "ATTENDRE";
            confidence = 45 + (int) (Math.abs(compositeScore) * 20);
            reason = generateReason(indicators, "NEUTRE");
        }
        Map<String, Object> sig = new HashMap<>();
        sig.put("signal", signal);
        sig.put("confidence", confidence);
        sig.put("reason", reason);
        sig.put("timeframe", timeframe);
        sig.put("price", indicators.get("current_price"));
        sig.put("price_change_pct", indicators.get("price_change"));
        sig.put("composite_score", compositeScore);
        sig.put("rsi", rsi);
        sig.put("trend_strength", Math.abs((Double) indicators.get("trend_score")));
        sig.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        sig.putAll(indicators);
        return sig;
    }

    private int calculateConfidence(Map<String, Object> indicators, String direction) {
        // (Inchangé)
        int baseConfidence = 65;
        double trendScore = (Double) indicators.get("trend_score");
        double momentumScore = (Double) indicators.get("momentum_score");
        if ((direction.equals("HAUSSIER") && trendScore > 0 && momentumScore > 0) ||
                (direction.equals("BAISSIER") && trendScore < 0 && momentumScore < 0)) {
            baseConfidence += 20;
        }
        double rsi = (Double) indicators.get("rsi");
        if ((direction.equals("HAUSSIER") && rsi > 30 && rsi < 65) ||
                (direction.equals("BAISSIER") && rsi > 35 && rsi < 70)) {
            baseConfidence += 10;
        }
        return Math.min(90, baseConfidence);
    }

    private String generateReason(Map<String, Object> indicators, String direction) {
        // (Inchangé)
        List<String> reasons = new ArrayList<>();
        double trendScore = (Double) indicators.get("trend_score");
        double macd = (Double) indicators.get("macd");
        double macdSignal = (Double) indicators.get("macd_signal");
        double rsi = (Double) indicators.get("rsi");
        double priceChange = (Double) indicators.get("price_change");
        if (direction.equals("HAUSSIER")) {
            if (trendScore > 0) reasons.add("↗️");
            if (macd > macdSignal) reasons.add("MACD📈");
            reasons.add("RSI" + String.format("%.0f", rsi));
        } else if (direction.equals("BAISSIER")) {
            if (trendScore < 0) reasons.add("↘️");
            if (macd < macdSignal) reasons.add("MACD📉");
            reasons.add("RSI" + String.format("%.0f", rsi));
        } else {
            reasons.add("Équilibré");
        }
        if (Math.abs(priceChange) > 0.01) {
            reasons.add(String.format("%+.2f%%", priceChange));
        }
        return String.join(" ", reasons);
    }

    private Map<String, Object> getPairConsensusAll(Map<String, List<Map<String, Object>>> allSignals) {
        // (Inchangé)
        double totalBuy = 0, totalSell = 0;
        for (List<Map<String, Object>> signals : allSignals.values()) {
            Map<String, Object> consensus = getPairConsensus(signals);
            double conf = ((Integer) consensus.get("confidence")) / 100.0;
            if ("ACHAT".equals(consensus.get("signal"))) totalBuy += conf;
            else if ("VENTE".equals(consensus.get("signal"))) totalSell += conf;
        }
        String globalSignal = totalBuy > totalSell + 0.1 ? "ACHAT" : (totalSell > totalBuy + 0.1 ? "VENTE" : "ATTENDRE");
        int globalConf = (int) (Math.max(totalBuy, totalSell) * 100);
        Map<String, Object> global = new HashMap<>();
        global.put("signal", globalSignal);
        global.put("confidence", globalConf);
        return global;
    }

    private Map<String, Object> getPairConsensus(List<Map<String, Object>> signals) {
        // (Inchangé)
        if (signals.isEmpty()) {
            Map<String, Object> neutral = new HashMap<>();
            neutral.put("signal", "ATTENDRE");
            neutral.put("confidence", 0);
            return neutral;
        }
        double buyScore = 0, sellScore = 0;
        for (Map<String, Object> signal : signals) {
            double weight = "4h".equals(signal.get("timeframe")) ? 0.4 : 0.6;
            double confidence = ((Integer) signal.get("confidence")) / 100.0;
            if ("ACHAT".equals(signal.get("signal"))) buyScore += weight * confidence;
            else if ("VENTE".equals(signal.get("signal"))) sellScore += weight * confidence;
        }
        String sig;
        int conf;
        if (buyScore > sellScore + 0.1) {
            sig = "ACHAT";
            conf = (int) (buyScore * 100);
        } else if (sellScore > buyScore + 0.1) {
            sig = "VENTE";
            conf = (int) (sellScore * 100);
        } else {
            sig = "ATTENDRE";
            conf = Math.max(40, (int) ((buyScore + sellScore) * 50));
        }
        Map<String, Object> consensus = new HashMap<>();
        consensus.put("signal", sig);
        consensus.put("confidence", conf);
        return consensus;
    }

    private void exportToCsv(Map<String, List<Map<String, Object>>> allSignals, Integer simulationId) {
        // (Inchangé, utilise CSVWriter)
        String filename = exportDir + "multi_signals_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + "_" + simulationId + ".csv";
        try (CSVWriter writer = new CSVWriter(new FileWriter(filename))) {
            String[] header = {"timestamp", "pair", "timeframe", "signal", "confidence", "price", "rsi", "price_change_pct"};
            writer.writeNext(header);
            for (Map.Entry<String, List<Map<String, Object>>> entry : allSignals.entrySet()) {
                for (Map<String, Object> signal : entry.getValue()) {
                    writer.writeNext(new String[]{
                            (String) signal.get("timestamp"),
                            entry.getKey(),
                            (String) signal.get("timeframe"),
                            (String) signal.get("signal"),
                            signal.get("confidence").toString(),
                            signal.get("price").toString(),
                            signal.get("rsi").toString(),
                            signal.get("price_change_pct").toString()
                    });
                }
            }
            logger.info("💾 Export CSV: {}", filename);
        } catch (IOException e) {
            logger.error("Erreur export CSV: {}", e.getMessage());
        }
    }
    @Scheduled(fixedRateString = "${app.simulation.auto-end-check:30000}")
    public void autoEndExpiredSimulations() {
        List<Simulation> activeSims = simulationRepository.findByStatutSimulation(StatutSimulation.EXECUTEE);
        LocalDateTime now = LocalDateTime.now();

        for (Simulation sim : activeSims) {
            if (sim.getDateFin() != null && now.isAfter(sim.getDateFin())) {
                sim.setStatutSimulation(StatutSimulation.TERMINEE);
                sim.setTempsRestantSecondes(0);
                simulationRepository.save(sim);
                logger.info("Simulation #{} terminée automatiquement (temps écoulé)", sim.getId());
            }
        }
    }
    @Value("${app.alpha.key:PD3XA6I2Q0V90AYX}")  // Fallback 'demo' pour tests (remplace par vraie clé en prod)
    private String alphaKey;
/*
    // NOUVEAU : Prix live pour TECH (comme getYahooLivePrices, mais Alpha)
    @Override
    public Map<String, Object> getAlphaLivePrices() {
        Map<String, Object> result = new HashMap<>();
        for (String symbol : Config.TECH_SYMBOLS) {  // AAPL, MSFT, GOOGL
            try {
                String url = String.format(
                        "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=%s&apikey=%s",
                        symbol, alphaKey
                );
                String jsonStr = restTemplate.getForObject(url, String.class);
                if (jsonStr != null && jsonStr.contains("\"01. symbol\"")) {  // Succès (pas d'erreur API)
                    JsonNode root = mapper.readTree(jsonStr);
                    JsonNode quote = root.path("Global Quote");
                    if (quote != null && !quote.isMissingNode()) {
                        double price = quote.path("05. price").asDouble(0);
                        double open = quote.path("02. open").asDouble(0);
                        double changePct = open > 0 ? ((price - open) / open) * 100 : 0.0;
                        result.put(symbol, Map.of("price", price, "changePct", changePct));
                    } else {
                        result.put(symbol, Map.of("price", 150.0, "changePct", 0.0));  // Fallback AAPL-like
                    }
                } else {
                    result.put(symbol, Map.of("price", 150.0, "changePct", 0.0));
                }
            } catch (Exception e) {
                logger.warn("Erreur Alpha live pour {}: {}", symbol, e.getMessage());
                result.put(symbol, Map.of("price", 150.0, "changePct", 0.0));
            }
        }
        return result;
    }
*/
@Override
public Map<String, Object> getAlphaLivePrices() {
    return getYahooLivePrices(); // ← 1 ligne, réutilise ta méthode existante

}

    // === UTILITÉ ===
    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        return (double) Math.round(value * factor) / factor;
    }
    // NOUVEAU : Candles pour TECH (comme getYahooCandles, 5 dernières 1min)
    @Override
    public List<Map<String, Object>> getAlphaCandles(String symbol) {
        try {
            String url = String.format(
                    "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=%s&interval=1min&outputsize=compact&apikey=%s",
                    symbol, alphaKey
            );
            String jsonStr = restTemplate.getForObject(url, String.class);
            if (jsonStr != null && jsonStr.contains("\"Time Series (1min)\"")) {
                JsonNode root = mapper.readTree(jsonStr);
                JsonNode timeSeries = root.path("Time Series (1min)");
                if (timeSeries != null && timeSeries.isObject()) {
                    List<Map<String, Object>> data = new ArrayList<>();
                    // Récupère les 5 plus récentes (timeSeries est un objet {timestamp: {ohlc...}})
                    Iterator<Map.Entry<String, JsonNode>> it = timeSeries.fields();
                    List<JsonNode> recent = new ArrayList<>();
                    while (it.hasNext() && recent.size() < 5) {
                        recent.add(it.next().getValue());
                    }
                    Collections.reverse(recent);  // Trier récentes d'abord
                    for (JsonNode candle : recent) {
                        Map<String, Object> row = new HashMap<>();
                        row.put("Open", candle.path("1. open").asDouble(0));
                        row.put("High", candle.path("2. high").asDouble(0));
                        row.put("Low", candle.path("3. low").asDouble(0));
                        row.put("Close", candle.path("4. close").asDouble(0));
                        data.add(row);
                    }
                    return data;
                }
            }
        } catch (Exception e) {
            logger.warn("Erreur Alpha candles pour {}: fallback", symbol);
        }
        // Fallback : Mock data
        return getFallbackTechCandles(symbol);
    }


    // Helper privé pour fallback (ajoute-le)
    private List<Map<String, Object>> getFallbackTechCandles(String symbol) {
        double basePrice = symbol.equals("AAPL") ? 150.0 : (symbol.equals("MSFT") ? 300.0 : 120.0);
        List<Map<String, Object>> data = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < 5; i++) {
            double variation = (rand.nextDouble() - 0.5) * 0.02;  // ±1%
            double price = basePrice * (1 + variation);
            Map<String, Object> candle = new HashMap<>();
            candle.put("Open", price * 0.999);
            candle.put("High", price * 1.002);
            candle.put("Low", price * 0.998);
            candle.put("Close", price);
            data.add(candle);
        }
        return data;
    }
//energy

    // ... imports existants + import com.fasterxml.jackson.databind.JsonNode; import org.slf4j.Logger; etc.

    @Value("${app.finhub.key:d3oemkpr01quo6o40rogd3oemkpr01quo6o40rp0}")
    private String finhubKey;

    @Override
    public Map<String, Object> getFinnhubLivePrices() {
        Map<String, Object> result = new HashMap<>();

        for (String symbol : Config.ENERGY_SYMBOLS) {
            try {
                String url = String.format("https://finnhub.io/api/v1/quote?symbol=%s&token=%s", symbol, finhubKey);
                String jsonStr = restTemplate.getForObject(url, String.class);

                if (jsonStr != null && !jsonStr.contains("error") && jsonStr.contains("\"c\":")) {
                    JsonNode root = mapper.readTree(jsonStr);
                    double price = root.path("c").asDouble(0);
                    double changePct = root.path("pc").asDouble(0); // % change déjà calculé

                    if (price > 0) {
                        result.put(symbol, Map.of("price", price, "changePct", changePct));
                        continue; // OK → on garde Finnhub
                    }
                }
            } catch (Exception e) {
                logger.warn("Finnhub échoué pour {} → bascule sur Yahoo", symbol);
            }

            // === BASCULE SUR YAHOO SI FINNHUB ÉCHOUE ===
            try {
                Map<String, Object> yahooPrices = getYahooLivePrices(); // réutilise ta méthode Yahoo
                Map<String, Object> yahooData = (Map<String, Object>) yahooPrices.getOrDefault(symbol, Map.of("price", 0.0, "changePct", 0.0));
                result.put(symbol, yahooData);
            } catch (Exception ex) {
                result.put(symbol, Map.of("price", 0.0, "changePct", 0.0));
            }
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getFinnhubCandles(String symbol) {
        try {
            long to = System.currentTimeMillis() / 1000;
            long from = to - 300 * 60; // 5 min
            String url = String.format(
                    "https://finnhub.io/api/v1/stock/candle?symbol=%s&resolution=1&from=%d&to=%d&token=%s",
                    symbol, from, to, finhubKey
            );
            String jsonStr = restTemplate.getForObject(url, String.class);

            if (jsonStr != null && !jsonStr.contains("error")) {
                JsonNode root = mapper.readTree(jsonStr);
                int s = root.path("s").asInt();
                if (s == 0 && root.path("c").size() > 0) {
                    // Finnhub OK → on renvoie ses candles
                    List<Map<String, Object>> data = new ArrayList<>();
                    int count = root.path("t").size();
                    int start = Math.max(0, count - 5);
                    for (int i = start; i < count; i++) {
                        Map<String, Object> candle = new HashMap<>();
                        candle.put("Open", root.path("o").get(i).asDouble(0));
                        candle.put("High", root.path("h").get(i).asDouble(0));
                        candle.put("Low", root.path("l").get(i).asDouble(0));
                        candle.put("Close", root.path("c").get(i).asDouble(0));
                        data.add(candle);
                    }
                    return data;
                }
            }
        } catch (Exception e) {
            logger.warn("Finnhub candles échoué pour {} → bascule sur Yahoo", symbol);
        }

        // === BASCULE SUR YAHOO ===
        return getYahooCandles(symbol);
    }

    private List<Map<String, Object>> getFallbackEnergyCandles(String symbol) {
        double basePrice = symbol.equals("XOM") ? 80.0 : (symbol.equals("CVX") ? 120.0 : 100.0);
        List<Map<String, Object>> data = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < 5; i++) {
            double variation = (rand.nextDouble() - 0.5) * 0.015;  // ±0.75% (vol énergie)
            double price = basePrice * (1 + variation);
            Map<String, Object> candle = new HashMap<>();
            candle.put("Open", price * 0.999);
            candle.put("High", price * 1.0015);
            candle.put("Low", price * 0.9985);
            candle.put("Close", price);
            data.add(candle);
        }
        return data;
    }

// ... imports + import com.fasterxml.jackson.databind.JsonNode;
// === COMMODITIES VIA YAHOO (ILLIMITÉ) ===
// === COMMODITIES LIVE PRICES – CORRIGÉ ===
@Override
public Map<String, Object> getTwelveDataLivePrices() {
    Map<String, Object> result = new HashMap<>();

    Map<String, String> yahooMap = Map.of(
            "CL", "CL=F",   // Crude Oil
            "GC", "GC=F",   // Gold
            "SI", "SI=F",   // Silver
            "HG", "HG=F",   // Copper
            "NG", "NG=F",   // Natural Gas
            "ZB", "ZB=F"    // 30-Year Treasury Bond
    );

    for (String symbol : Config.COMMODITY_SYMBOLS) { // ← CORRIGÉ : COMMODITY_SYMBOLS
        String yahooSymbol = yahooMap.getOrDefault(symbol, symbol);
        try {
            String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + yahooSymbol +
                    "?range=1d&interval=1m";

            logger.info("Yahoo Commodities → {}", url);

            String jsonStr = restTemplate.getForObject(url, String.class);
            if (jsonStr == null || jsonStr.trim().isEmpty()) {
                logger.warn("Réponse vide pour {}", symbol);
                result.put(symbol, Map.of("price", 0.0, "changePct", 0.0));
                continue;
            }

            JsonNode root = mapper.readTree(jsonStr);
            JsonNode resultNode = root.path("chart").path("result");
            if (resultNode.isMissingNode() || resultNode.size() == 0) {
                logger.warn("Pas de 'result' pour {}", symbol);
                result.put(symbol, Map.of("price", 0.0, "changePct", 0.0));
                continue;
            }

            JsonNode meta = resultNode.get(0).path("meta");
            double price = meta.path("regularMarketPrice").asDouble(0.0);
            double previousClose = meta.path("chartPreviousClose").asDouble(0.0);

            if (price <= 0 || previousClose <= 0) {
                logger.warn("Prix invalide pour {}: price={}, previousClose={}", symbol, price, previousClose);
                result.put(symbol, Map.of("price", 0.0, "changePct", 0.0));
                continue;
            }

            double changePct = ((price - previousClose) / previousClose) * 100;

            result.put(symbol, Map.of(
                    "price", round(price, symbol.equals("GC") ? 1 : 2),
                    "changePct", round(changePct, 2)
            ));

            logger.info("{} → price: {}, change: {}%", symbol, price, changePct);

        } catch (Exception e) {
            logger.error("Exception Yahoo Commodities {}: {}", symbol, e.getMessage(), e);
            result.put(symbol, Map.of("price", 0.0, "changePct", 0.0));
        }
    }
    return result;
}
    @Override
    public List<Map<String, Object>> getTwelveDataCandles(String symbol) {
        String yahooSymbol = Map.of(
                "CL", "CL=F", "GC", "GC=F", "SI", "SI=F",
                "HG", "HG=F", "NG", "NG=F", "ZB", "ZB=F"
        ).getOrDefault(symbol, symbol);

        return getYahooCandles(yahooSymbol);
    }

    private List<Map<String, Object>> getFallbackCommodityCandles(String symbol) {
        double basePrice = symbol.equals("CL") ? 75.0 : (symbol.equals("GC") ? 2000.0 : 25.0);  // Pétrole/or/argent
        List<Map<String, Object>> data = new ArrayList<>();
        Random rand = new Random();
        for (int i = 0; i < 5; i++) {
            double variation = (rand.nextDouble() - 0.5) * 0.02;  // ±1% vol commodities
            double price = basePrice * (1 + variation);
            Map<String, Object> candle = new HashMap<>();
            candle.put("Open", price * 0.999);
            candle.put("High", price * 1.002);
            candle.put("Low", price * 0.998);
            candle.put("Close", price);
            data.add(candle);
        }
        return data;
    }
// ... imports existants + import com.fasterxml.jackson.databind.JsonNode;

    // === REMPLACE getAlphaRealEstateLivePrices() PAR ÇA ===
    @Override
    public Map<String, Object> getAlphaRealEstateLivePrices() {
        Map<String, Object> result = new HashMap<>();
        for (String symbol : Config.REAL_ESTATE_SYMBOLS) {
            try {
                String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + symbol +
                        "?range=1d&interval=1m";

                String jsonStr = restTemplate.getForObject(url, String.class);
                if (jsonStr == null || jsonStr.trim().isEmpty()) {
                    result.put(symbol, Map.of("price", 0.0, "changePct", 0.0));
                    continue;
                }

                JsonNode root = mapper.readTree(jsonStr);
                JsonNode resultNode = root.path("chart").path("result");
                if (resultNode.isMissingNode() || resultNode.size() == 0) {
                    result.put(symbol, Map.of("price", 0.0, "changePct", 0.0));
                    continue;
                }

                JsonNode meta = resultNode.get(0).path("meta");
                double price = meta.path("regularMarketPrice").asDouble(0.0);
                double previousClose = meta.path("chartPreviousClose").asDouble(0.0);

                if (price <= 0 || previousClose <= 0) {
                    result.put(symbol, Map.of("price", 0.0, "changePct", 0.0));
                    continue;
                }

                double changePct = ((price - previousClose) / previousClose) * 100;

                result.put(symbol, Map.of(
                        "price", round(price, 2),
                        "changePct", round(changePct, 2)
                ));

            } catch (Exception e) {
                logger.warn("Yahoo Real Estate {} échoué: {}", symbol, e.getMessage());
                result.put(symbol, Map.of("price", 0.0, "changePct", 0.0));
            }
        }
        return result;
    }
    @Override
    public List<Map<String, Object>> getAlphaRealEstateCandles(String symbol) {
        try {
            String url = String.format(
                    "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=%s&interval=1min&outputsize=compact&apikey=%s",
                    symbol, alphaKey
            );
            String jsonStr = restTemplate.getForObject(url, String.class);
            if (jsonStr != null && jsonStr.contains("\"Time Series (1min)\"")) {
                JsonNode root = mapper.readTree(jsonStr);
                JsonNode timeSeries = root.path("Time Series (1min)");
                if (timeSeries != null && timeSeries.isObject()) {
                    List<Map<String, Object>> data = new ArrayList<>();
                    Iterator<Map.Entry<String, JsonNode>> it = timeSeries.fields();
                    List<JsonNode> recent = new ArrayList<>();
                    while (it.hasNext() && recent.size() < 5) {
                        recent.add(it.next().getValue());
                    }
                    Collections.reverse(recent);  // Récentes d'abord
                    for (JsonNode candle : recent) {
                        Map<String, Object> row = new HashMap<>();
                        row.put("Open", candle.path("1. open").asDouble(0));
                        row.put("High", candle.path("2. high").asDouble(0));
                        row.put("Low", candle.path("3. low").asDouble(0));
                        row.put("Close", candle.path("4. close").asDouble(0));
                        data.add(row);
                    }
                    return data;
                }
            }
        } catch (Exception e) {
            logger.warn("Erreur Alpha real-estate candles pour {}: fallback", symbol);
        }
        return getFallbackRealEstateCandles(symbol);
    }

    private List<Map<String, Object>> getFallbackRealEstateCandles(String symbol) {
        return getYahooCandles(symbol); // ← RÉUTILISE TA MÉTHODE EXISTANTE
         }}