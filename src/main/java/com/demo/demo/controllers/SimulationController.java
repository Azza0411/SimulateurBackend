package com.demo.demo.controllers;

import com.demo.demo.entities.Simulation;
import com.demo.demo.services.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/simulations")
public class SimulationController {
    @Autowired
    private SimulationService simulationService;
    @PostMapping
    public ResponseEntity<Simulation> createSimulation(@RequestBody Simulation simulation) {
        return ResponseEntity.ok(simulationService.createSimulation(simulation));
    }
    // Récupérer une simulation par ID
    @GetMapping("/{id}")
    public ResponseEntity<Simulation> getSimulationById(@PathVariable Integer id) {
        try {
            Simulation simulation = simulationService.getSimulationById(id);
            return ResponseEntity.ok(simulation);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(null); // 404 Not Found si non trouvée
        }
    }

    // Récupérer toutes les simulations
    @GetMapping
    public ResponseEntity<List<Simulation>> getAllSimulations() {
        List<Simulation> simulations = simulationService.getAllSimulations();
        return ResponseEntity.ok(simulations);
    }

    // Mettre à jour une simulation
    @PutMapping("/{id}")
    public ResponseEntity<Simulation> updateSimulation(@PathVariable Integer id, @RequestBody Simulation details) {
        try {
            Simulation updatedSimulation = simulationService.updateSimulation(id, details);
            return ResponseEntity.ok(updatedSimulation);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(null); // 404 si non trouvée
        }
    }

    // Supprimer une simulation
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSimulation(@PathVariable Integer id) {
        simulationService.deleteSimulation(id);
        return ResponseEntity.noContent().build(); // 204 No Content


    }

    // Démarrer une simulation
    @PostMapping("/{id}/start")
    public ResponseEntity<Simulation> startSimulation(@PathVariable Integer id) {
        try {
            Simulation startedSimulation = simulationService.startSimulation(id);
            return ResponseEntity.ok(startedSimulation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null); // 400 si conditions nest pas en attente
        }
    }

    // Terminer une simulation
    @PostMapping("/{id}/end")
    public ResponseEntity<Simulation> endSimulation(@PathVariable Integer id) {
        try {
            Simulation endedSimulation = simulationService.endSimulation(id);
            return ResponseEntity.ok(endedSimulation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null); // 400 si conditions non remplies
        }
    }
}
