package com.demo.demo.serviceimplement;

import com.demo.demo.entities.Simulation;
import com.demo.demo.entities.StatutSimulation;
import com.demo.demo.repository.SimulationRepository;
import com.demo.demo.services.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SimulationServiceImpl implements SimulationService {
    @Autowired
    private SimulationRepository simulationRepository;
    @Override
    public Simulation createSimulation(Simulation simulation) {
        if (simulation.getCapital() == null || simulation.getCapital() <= 0) {
            throw new RuntimeException("Le capital doit être positif");
        }
        // Vérification de l'existence d'une simulation avec les mêmes typeSimulation, description, et dateDebut
        // Si dateDebut est null (cas initial), on vérifie uniquement typeSimulation et description
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
        return simulationRepository.findAll();    }

    @Override
    public Simulation updateSimulation(Integer id, Simulation details) {
// Réactualiser l'entité pour éviter les problèmes de cache ou de référence partagée
        Simulation simulation = simulationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Simulation non trouvée avec ID : " + id));        if (details.getDescription() != null) simulation.setDescription(details.getDescription());
        // Mettre à jour uniquement les champs fournis dans 'details'
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
        if (details.getStatutSimulation() != null) simulation.setStatutSimulation(details.getStatutSimulation()); // Autoriser la mise à jour du statut si nécessaire

        return simulationRepository.save(simulation);    }

    @Override
    public void deleteSimulation(Integer id) {
        System.out.println("Tentative de suppression de l'ID : " + id);
        simulationRepository.deleteById(id); // Suppression directe par ID
    }

    @Override
    public Simulation startSimulation(Integer id) {
        // Réactualiser l'entité pour éviter les problèmes de cache
        Simulation simulation = simulationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Simulation non trouvée avec ID : " + id));
        if (simulation.getStatutSimulation() != StatutSimulation.EN_ATTENTE) {
            throw new RuntimeException("La simulation doit être en attente pour démarrer");
        }
        simulation.setStatutSimulation(StatutSimulation.EXECUTEE);
        simulation.setDateDebut(LocalDateTime.now());
        return simulationRepository.save(simulation); // Sauvegarde uniquement cette instance
    }
    @Override
    public Simulation endSimulation(Integer id) {
        // Réactualiser l'entité pour éviter les problèmes de cache
        Simulation simulation = simulationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Simulation non trouvée avec ID : " + id));
        if (simulation.getStatutSimulation() != StatutSimulation.EXECUTEE) {
            throw new RuntimeException("La simulation doit être en cours pour être terminée");
        }
        simulation.setStatutSimulation(StatutSimulation.ANNULEE);
        simulation.setDateFin(LocalDateTime.now());
        return simulationRepository.save(simulation); // Sauvegarde uniquement cette instance
    }
}
