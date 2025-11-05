package com.demo.demo.serviceimplement;

import com.demo.demo.entities.*;
import com.demo.demo.repository.CarnetOrdreRepository;
import com.demo.demo.repository.SimulationRepository;
import com.demo.demo.services.CarnetOrdreService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable; // FIX : Import correct
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.logging.Logger; // Pour logs simples

@Service
@Transactional
public class CarnetOrdreServiceImpl implements CarnetOrdreService {

    @Autowired
    private CarnetOrdreRepository carnetOrdreRepository;

    @Autowired
    private SimulationRepository simulationRepository;

    private final ObjectMapper mapper = new ObjectMapper(); // AJOUT : Pour GARCH JSON.

    private static final Logger logger = Logger.getLogger(CarnetOrdreServiceImpl.class.getName()); // Log simple

    @Override
    public CarnetOrdre createCarnetOrdre(Integer simulationId, CarnetOrdre carnetOrdre) {
        // Vérif simulation
        Optional<Simulation> simulationOpt = simulationRepository.findById(simulationId);
        if (simulationOpt.isEmpty()) {
            throw new IllegalArgumentException("Simulation non trouvée avec ID : " + simulationId); // 404
        }
        Simulation simulation = simulationOpt.get();
        if (simulation.getStatutSimulation() != StatutSimulation.EXECUTEE) {
            throw new IllegalArgumentException("Simulation doit être exécutée"); // 400
        }

        // Vérifs input
        if (carnetOrdre.getQuantite() <= 0) {
            throw new IllegalArgumentException("Quantité doit être positive"); // 400
        }
        if (carnetOrdre.getTypeT() == null) {
            throw new IllegalArgumentException("TypeT (ACHAT/VENTE) obligatoire"); // 400
        }

        // NOUVEAU : Set modeValidation par défaut si null
        if (carnetOrdre.getModeValidation() == null) {
            carnetOrdre.setModeValidation(ModeValidation.MANUEL); // Défaut manuel
        }

        carnetOrdre.setSimulation(simulation);
        carnetOrdre.setDateCreation(LocalDateTime.now());
        carnetOrdre.setStatusOrdre(statusOrdre.EN_ATTENTE);
        carnetOrdre.setDateExecution(null);
        carnetOrdre.setPrixAchat(null);
        carnetOrdre.setPrixVente(null);
        carnetOrdre.setGain(0.0);
        carnetOrdre.setRisque(0.0f);

        return carnetOrdreRepository.save(carnetOrdre);
    }

    @Override
    public CarnetOrdre getCarnetOrdreById(Integer id) {
        Optional<CarnetOrdre> optional = carnetOrdreRepository.findById(id);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("CarnetOrdre non trouvé avec ID : " + id); // 404
        }
        return optional.get();
    }

    @Override
    public CarnetOrdre updateCarnetOrdre(Integer id, CarnetOrdre details) {
        CarnetOrdre carnetOrdre = getCarnetOrdreById(id);
        if (details.getTypeT() != null) carnetOrdre.setTypeT(details.getTypeT());
        if (details.getQuantite() > 0) carnetOrdre.setQuantite(details.getQuantite());
        if (details.getPrixLimite() != null) carnetOrdre.setPrixLimite(details.getPrixLimite());
        // NOUVEAU : Gère SL/TP dans update
        if (details.getStopLoss() != null) carnetOrdre.setStopLoss(details.getStopLoss());
        if (details.getTakeProfit() != null) carnetOrdre.setTakeProfit(details.getTakeProfit());
        if (details.getStatusOrdre() != null) carnetOrdre.setStatusOrdre(details.getStatusOrdre());
        if (details.getModeValidation() != null) carnetOrdre.setModeValidation(details.getModeValidation());
        if (details.getRisque() != null) carnetOrdre.setRisque(details.getRisque());

        return carnetOrdreRepository.save(carnetOrdre);
    }

    @Override
    public void deleteCarnetOrdre(Integer id) {
        getCarnetOrdreById(id);
        System.out.println("Tentative de suppression de l'ID : " + id);
        carnetOrdreRepository.deleteById(id);
    }

    // NOUVEAU : Implémente executeOrdre (ÉTAPE 1 MVP)
    @Override
    public CarnetOrdre executeOrdre(Integer id, Double prixMarcheActuel) {
        CarnetOrdre ordre = getCarnetOrdreById(id); // Get + throw si pas trouvé
        if (ordre.getStatusOrdre() != statusOrdre.EN_ATTENTE) {
            throw new IllegalArgumentException("Ordre doit être EN_ATTENTE pour exécution (actuel: " + ordre.getStatusOrdre() + ")");
        }

        // Set exécution
        ordre.setStatusOrdre(statusOrdre.EXECUTEE);
        ordre.setDateExecution(LocalDateTime.now());

        // Set prix basé sur type (pour futur calcul gain lors de sortie)
        if (ordre.getTypeT() == typeT.ACHAT) {
            ordre.setPrixAchat(prixMarcheActuel); // Prix d'entrée
        } else if (ordre.getTypeT() == typeT.VENTE) {
            ordre.setPrixVente(prixMarcheActuel);
        }

        // Calcule et set gain/risque (tes méthodes privées, updatées ci-dessous)
        ordre.setGain(calculateGain(ordre));
        ordre.setRisque(calculateRisque(ordre));

        // Log pour debug
        logger.info("Ordre " + id + " exécuté à " + prixMarcheActuel + ", gain: " + ordre.getGain() + ", risque: " + ordre.getRisque());

        return carnetOrdreRepository.save(ordre); // Persiste et retourne
    }

    @Override
    public List<CarnetOrdre> getPendingBySimulationId(Integer simulationId) {
        if (!simulationRepository.existsById(simulationId)) {
            throw new IllegalArgumentException("Simulation non trouvée avec ID : " + simulationId);
        }
        return carnetOrdreRepository.findPendingBySimulationId(simulationId, statusOrdre.EN_ATTENTE);
    }

    // AMÉLIORÉ : matchOrders (appelle vraiment execute, gère croisement + partiel basique)
    @Override
    public List<CarnetOrdre> matchOrders(Integer simulationId, Double prixMarche) {
        if (!simulationRepository.existsById(simulationId)) {
            throw new IllegalArgumentException("Simulation non trouvée avec ID : " + simulationId);
        }

        List<CarnetOrdre> pending = getPendingBySimulationId(simulationId);
        if (pending.isEmpty()) {
            return new ArrayList<>(); // Rien à matcher
        }

        List<CarnetOrdre> matched = new ArrayList<>();

        // Trie buys (BID : desc prix pour meilleur d'abord) et sells (ASK : asc prix)
        List<CarnetOrdre> buys = pending.stream()
                .filter(o -> o.getTypeT() == typeT.ACHAT)
                .sorted(Comparator.comparing(CarnetOrdre::getPrixLimite).reversed()) // Meilleur BID haut
                .collect(Collectors.toList());

        List<CarnetOrdre> sells = pending.stream()
                .filter(o -> o.getTypeT() == typeT.VENTE)
                .sorted(Comparator.comparing(CarnetOrdre::getPrixLimite)) // Meilleur ASK bas
                .collect(Collectors.toList());

        // Boucle simple pour matcher (pour MVP : premier croisement possible)
        for (CarnetOrdre buy : buys) {
            if (buy.getStatusOrdre() != statusOrdre.EN_ATTENTE) continue; // Déjà matché

            for (CarnetOrdre sell : sells) {
                if (sell.getStatusOrdre() != statusOrdre.EN_ATTENTE) continue;

                // Check croisement
                if (buy.getPrixLimite() >= prixMarche && sell.getPrixLimite() <= prixMarche) {
                    // Exécute (prixMarche comme prix exec)
                    CarnetOrdre executedBuy = executeOrdre(buy.getId(), prixMarche);
                    CarnetOrdre executedSell = executeOrdre(sell.getId(), prixMarche);

                    matched.add(executedBuy);
                    matched.add(executedSell);

                    logger.info("Matché buy #" + buy.getId() + " avec sell #" + sell.getId() + " à " + prixMarche);

                    break; // Un match par buy pour MVP simple
                }
            }
        }

        return matched;
    }

    // AMÉLIORÉ : calculateGain (gère entrée/sortie + fees basique)
    private Double calculateGain(CarnetOrdre ordre) {
        Double entryPrice = (ordre.getTypeT() == typeT.ACHAT ? ordre.getPrixAchat() : ordre.getPrixVente());
        if (entryPrice == null) return 0.0; // Pas encore exécuté

        // Pour MVP : Gain "provisoire" basé sur prixLimite comme sortie (ajuste pour TP/SL plus tard)
        Double exitPrice = ordre.getPrixLimite(); // Placeholder ; futur : TP ou SL hit
        Double brut = (exitPrice - entryPrice) * ordre.getQuantite();

        // Fees simples 0.1%
        Double fees = Math.abs(brut) * 0.001;
        return brut - fees;
    }

    // Ton calculateRisque inchangé (bon pour vol simu)
    private Float calculateRisque(CarnetOrdre ordre) {
        Simulation sim = ordre.getSimulation();
        if (sim.getVolatiliteMarche() != null) {
            return (float) (ordre.getQuantite() * sim.getVolatiliteMarche() * 0.01);
        }
        return 0.0f;
    }
}