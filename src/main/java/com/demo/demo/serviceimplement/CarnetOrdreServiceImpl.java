package com.demo.demo.serviceimplement;

import com.demo.demo.entities.*;
import com.demo.demo.repository.CarnetOrdreRepository;
import com.demo.demo.repository.SimulationRepository;
import com.demo.demo.services.CarnetOrdreService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;  // ← FIX : Import SPRING DATA (supprime java.awt.print.Pageable si présent)import java.time.LocalDateTime;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CarnetOrdreServiceImpl implements CarnetOrdreService {
    @Autowired
    private CarnetOrdreRepository carnetOrdreRepository;
    @Autowired
    private SimulationRepository simulationRepository;
    private final ObjectMapper mapper = new ObjectMapper(); // AJOUT : Pour GARCH JSON.
    @Override
    public CarnetOrdre createCarnetOrdre(Integer simulationId, CarnetOrdre carnetOrdre) {
        // Vérif simulation
        Optional<Simulation> simulationOpt = simulationRepository.findById(simulationId);
        if (simulationOpt.isEmpty()) {
            throw new IllegalArgumentException("Simulation non trouvée avec ID : " + simulationId);  // 404
        }
        Simulation simulation = simulationOpt.get();
        if (simulation.getStatutSimulation() != StatutSimulation.EXECUTEE) {
            throw new IllegalArgumentException("Simulation doit être exécutée");  // 400
        }
        // Vérifs input
        if (carnetOrdre.getQuantite() <= 0) {
            throw new IllegalArgumentException("Quantité doit être positive");  // 400
        }
        if (carnetOrdre.getTypeT() == null) {
            throw new IllegalArgumentException("TypeT (ACHAT/VENTE) obligatoire");  // 400
        }
      /*  // Unicité optionnelle
        Optional<CarnetOrdre> existing = carnetOrdreRepository.findBySimulationIdAndTypeT(simulationId, carnetOrdre.getTypeT());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Ordre dupliqué pour ce type");  // 400
        }
        // Init*/
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
            throw new IllegalArgumentException("CarnetOrdre non trouvé avec ID : " + id);  // 404
        }
        return optional.get();
    }


    @Override
    public CarnetOrdre updateCarnetOrdre(Integer id, CarnetOrdre details) {
        CarnetOrdre carnetOrdre = getCarnetOrdreById(id);
        if (details.getTypeT() != null) carnetOrdre.setTypeT(details.getTypeT());
        if (details.getQuantite() > 0) carnetOrdre.setQuantite(details.getQuantite());
        if (details.getPrixLimite() != null) carnetOrdre.setPrixLimite(details.getPrixLimite());
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

    @Override
    public CarnetOrdre executeOrdre(Integer id, Double prixMarcheActuel) {
        return null;
    }

    @Override
    public List<CarnetOrdre> getPendingBySimulationId(Integer simulationId) {
        if (!simulationRepository.existsById(simulationId)) {
            throw new IllegalArgumentException("Simulation non trouvée avec ID : " + simulationId);
        }
        return carnetOrdreRepository.findPendingBySimulationId(simulationId, statusOrdre.EN_ATTENTE);
    }

    @Override
    public List<CarnetOrdre> matchOrders(Integer simulationId, Double prixMarche) {
        if (!simulationRepository.existsById(simulationId)) {
            throw new IllegalArgumentException("Simulation non trouvée avec ID : " + simulationId);
        }
        List<CarnetOrdre> pending = getPendingBySimulationId(simulationId);
        List<CarnetOrdre> matched = new ArrayList<>();
        pending.sort(Comparator.comparing(CarnetOrdre::getDateCreation));  // FIFO
        for (CarnetOrdre buy : pending.stream().filter(o -> o.getTypeT() == typeT.ACHAT).collect(Collectors.toList())) {
            for (CarnetOrdre sell : pending.stream().filter(o -> o.getTypeT() == typeT.VENTE).collect(Collectors.toList())) {
                if (buy.getPrixLimite() <= prixMarche && sell.getPrixLimite() >= prixMarche) {
                    executeOrdre(buy.getId(), prixMarche);
                    executeOrdre(sell.getId(), prixMarche);
                    matched.add(buy);
                    matched.add(sell);
                    break;
                }
            }
        }
        return matched;
    }

    private Double calculateGain(CarnetOrdre ordre) {
        Double prixAchatPrev = ordre.getPrixAchat() != null ? ordre.getPrixAchat() : 0.0;
        if (ordre.getTypeT() == typeT.VENTE && ordre.getPrixVente() != null) {
            return (ordre.getPrixVente() - prixAchatPrev) * ordre.getQuantite();
        }
        return 0.0;
    }

    private Float calculateRisque(CarnetOrdre ordre) {
        Simulation sim = ordre.getSimulation();
        if (sim.getVolatiliteMarche() != null) {
            return (float) (ordre.getQuantite() * sim.getVolatiliteMarche() * 0.01);
        }
        return 0.0f;
    }
}