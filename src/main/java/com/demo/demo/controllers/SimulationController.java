package com.demo.demo.controllers;

import com.demo.demo.entities.Simulation;
import com.demo.demo.entities.StatutSimulation;
import com.demo.demo.services.SimulationService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/simulations")

@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*",methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},allowCredentials = "true")
public class SimulationController {
    private static final Logger logger = LoggerFactory.getLogger(SimulationController.class);
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
            logger.warn("UPDATE REJETÉ (400) - ID: {} | Erreur: {}", id, e.getMessage());
// === CORRECTION : 400 pour erreur logique, 404 seulement si non trouvée ===
            if (e.getMessage().contains("non trouvée")) {
                return ResponseEntity.status(404).body(null);
            } else {
                return ResponseEntity.status(400).body(null); // Erreur de validation
            }        }
    }

    // Supprimer une simulation
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSimulation(@PathVariable Integer id) {
        simulationService.deleteSimulation(id);
        return ResponseEntity.noContent().build(); // 204 No Content


    }
    // === AJOUT : COMPTE À REBOURS AUTO (poll front + auto-fin) ===
    @GetMapping("/{id}/temps")
    public ResponseEntity<Map<String, Object>> getTempsRestant(@PathVariable Integer id) {
        try {
            Map<String, Object> tempsInfo = simulationService.getUpdatedTempsRestant(id);
            return ResponseEntity.ok(tempsInfo);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage(), "tempsRestant", 0, "fini", true));
        }
    }
    // === FIN AJOUT ===
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
    /**
     * Récupérer le JSON dashboard forex (analyse + consensus, "photo" du tableau Python).
     */
    @GetMapping("/{id}/analyse")
    public ResponseEntity<String> getForexAnalysis(@PathVariable Integer id) {
        try {
            Simulation sim = simulationService.getSimulationById(id);
            // Force update si besoin (re-lance analyse)
            simulationService.runForexAnalysisInSimulation(id);
            return ResponseEntity.ok(sim.getAnalyseResultats()); // JSON string du dashboard
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("{}"); // JSON vide si erreur
        }
    }
    // Endpoint pour tour humain vs IA (test clé)
    @PostMapping("/{id}/ia-move")
    public ResponseEntity<Map<String, Object>> playIaMove(@PathVariable Integer id, @RequestBody Map<String, Object> humanTrade) {
        try {
            Map<String, Object> response = simulationService.playIaMove(id, humanTrade);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }

    }
    /*
    @GetMapping("/status/{statut}")
    public ResponseEntity<List<Simulation>> getSimulationsByStatus(@PathVariable String statut) {
        try {
            StatutSimulation statutEnum = StatutSimulation.valueOf(statut.toUpperCase());
            List<Simulation> simulations = simulationService.getSimulationsByStatus(statutEnum);
            return ResponseEntity.ok(simulations);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
*/
    @GetMapping("/status/{statut}")
    public ResponseEntity<List<Simulation>> getSimulationsByStatus(@PathVariable String statut) {
        logger.info("🔍 Requête status: {}", statut);  // Log pour tracer
        try {
            StatutSimulation statutEnum = StatutSimulation.valueOf(statut.toUpperCase().trim());
            List<Simulation> simulations = simulationService.getSimulationsByStatus(statutEnum);
            logger.info("📊 {} simus pour {}", simulations.size(), statutEnum);
            return ResponseEntity.ok(simulations);
        } catch (IllegalArgumentException e) {
            logger.warn("❌ Statut invalide: {}", statut);
            return ResponseEntity.badRequest().body(List.of());
        } catch (Exception e) {
            logger.error("💥 Erreur: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(List.of());
        }
    }}