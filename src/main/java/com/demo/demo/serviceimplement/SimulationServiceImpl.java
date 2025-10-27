package com.demo.demo.serviceimplement;

import com.demo.demo.config.Config;
import com.demo.demo.entities.Simulation;
import com.demo.demo.entities.StatutSimulation;
import com.demo.demo.repository.SimulationRepository;
import com.demo.demo.services.SimulationService;
import com.demo.demo.services.TradingAgentService;
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
    // private final TradingAgentService tradingAgentService; // Commenté si non utilisé (comme original)
    @Autowired
    private RestTemplate restTemplate; // Pour fetch Yahoo RT (équiv. yfinance)

    @Value("${app.trading.export.dir}")
    private String exportDir; // Dossier export CSV (équiv. Config.EXPORT_DIR)

    // ... (Toutes les méthodes createSimulation, getSimulationById, getAllSimulations, updateSimulation, deleteSimulation, startSimulation, endSimulation, activateIaAdversaire, getRealTimeIaResponse inchangées - copiez de précédent)

    @Override
    public Simulation createSimulation(Simulation simulation) {
        // (Code inchangé)
        if (simulation.getCapital() == null || simulation.getCapital() <= 0) {
            throw new RuntimeException("Le capital doit être positif");
        }
        Optional<Simulation> existingSimulation = simulationRepository.findByTypeSimulationAndDescriptionAndDateDebut(
                simulation.getTypeSimulation(), simulation.getDescription(), simulation.getDateDebut());
        if (existingSimulation.isPresent()) {
            throw new RuntimeException("Une simulation avec le même type, description et date de début existe déjà : ID = " + existingSimulation.get().getId());
        }
        if (simulation.getStatutSimulation() == null) {
            simulation.setStatutSimulation(StatutSimulation.EN_ATTENTE);
        }
        simulation.setDateDebut(null);
        simulation.setDateFin(null);
        simulation.setGainTotal(0.0f);
        simulation.setCapitalActuel(simulation.getCapital());
        simulation.setFacteurTempsEcoule(0.0f);
        simulation.setAnalyseResultats("{}");
        return simulationRepository.save(simulation);
    }

    @Override
    public Simulation getSimulationById(Integer id) {
        Optional<Simulation> optional = simulationRepository.findById(id);
        if (optional.isEmpty()) {
            throw new RuntimeException("Simulation non trouvée avec ID : " + id);
        }
        return optional.get();
    }

    @Override
    public List<Simulation> getAllSimulations() {
        return simulationRepository.findAll();
    }

    @Override
    public Simulation updateSimulation(Integer id, Simulation details) {
        Simulation simulation = simulationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Simulation non trouvée avec ID : " + id));
        if (details.getDescription() != null) simulation.setDescription(details.getDescription());
        if (details.getDuration() != null) simulation.setDuration(details.getDuration());
        if (details.getDifficulte() != null) simulation.setDifficulte(details.getDifficulte());
        if (details.getCapital() != null && details.getCapital() > 0) simulation.setCapital(details.getCapital());
        if (details.getVitesseExecution() != null) simulation.setVitesseExecution(details.getVitesseExecution());
        if (details.getVolatiliteMarche() != null) simulation.setVolatiliteMarche(details.getVolatiliteMarche());
        if (details.getVolumeEchange() != null) simulation.setVolumeEchange(details.getVolumeEchange());
        if (details.getRegleSimulation() != null) simulation.setRegleSimulation(details.getRegleSimulation());
        if (details.getModeSimulation() != null) simulation.setModeSimulation(details.getModeSimulation());
        if (details.getRisqueMaxAcceptable() != null) simulation.setRisqueMaxAcceptable(details.getRisqueMaxAcceptable());
        if (details.getCapitalParUser() != null) simulation.setCapitalParUser(details.getCapitalParUser());
        if (details.getStatutSimulation() != null) simulation.setStatutSimulation(details.getStatutSimulation());
        return simulationRepository.save(simulation);
    }

    @Override
    public void deleteSimulation(Integer id) {
        logger.info("Tentative de suppression de l'ID : {}", id);
        simulationRepository.deleteById(id);
    }

    @Override
    public Simulation startSimulation(Integer id) {
        Simulation simulation = simulationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Simulation non trouvée avec ID : " + id));
        if (simulation.getStatutSimulation() != StatutSimulation.EN_ATTENTE) {
            throw new RuntimeException("La simulation doit être en attente pour démarrer");
        }
        simulation.setStatutSimulation(StatutSimulation.EXECUTEE);
        simulation.setDateDebut(LocalDateTime.now());
        runForexAnalysisInSimulation(id);
        return simulationRepository.save(simulation);
    }

    @Override
    public Simulation endSimulation(Integer id) {
        Simulation simulation = simulationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Simulation non trouvée avec ID : " + id));
        if (simulation.getStatutSimulation() != StatutSimulation.EXECUTEE) {
            throw new RuntimeException("La simulation doit être en cours pour être terminée");
        }
        simulation.setStatutSimulation(StatutSimulation.ANNULEE);
        simulation.setDateFin(LocalDateTime.now());
        return simulationRepository.save(simulation);
    }

    @Override
    public void activateIaAdversaire(Integer simulationId) {
        // Placeholder
    }

    @Override
    public String getRealTimeIaResponse(Integer simulationId, String userTrade, String asset) {
        return "";
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
     * assessVolatility : FIX - Calcul manuel std dev (pas de getStandardDeviation()).
     * Équiv. Python returns.rolling(14).std().iloc[-1].
     */
    private double assessVolatility(double[] prices) {
        double[] returns = new double[prices.length - 1];
        for (int i = 1; i < prices.length; i++) {
            returns[i - 1] = (prices[i] - prices[i - 1]) / prices[i - 1];
        }
        int period = 14;
        if (returns.length < period) return 0.5;
        double[] recentReturns = Arrays.copyOfRange(returns, returns.length - period, returns.length);
        double mean = DoubleStream.of(recentReturns).average().orElse(0.0);
        double variance = DoubleStream.of(recentReturns).map(r -> Math.pow(r - mean, 2)).average().orElse(0.0);
        double volatility = Math.sqrt(variance);  // FIX : Calcul manuel std dev
        if (volatility > 0.008) return 0.8;
        if (volatility < 0.003) return 0.3;
        return 0.5;
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
}