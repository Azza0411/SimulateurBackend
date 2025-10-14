package com.demo.demo.controllers;

import com.demo.demo.entities.CarnetOrdre;
import com.demo.demo.services.CarnetOrdreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/carnet-ordre")
@CrossOrigin(origins = "*")  // Pour tests frontend ; retire en prod
public class CarnetOrdreController {
    @Autowired
    private CarnetOrdreService carnetOrdreService;
    // CREATE : 200 avec body structuré
    @PostMapping("/simulation/{simulationId}")
    public ResponseEntity<Map<String, Object>> createCarnetOrdre(@PathVariable Integer simulationId,
                                                                 @RequestBody CarnetOrdre carnetOrdre) {
        try {
            CarnetOrdre created = carnetOrdreService.createCarnetOrdre(simulationId, carnetOrdre);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "CarnetOrdre créé avec succès");
            response.put("data", created);
            return ResponseEntity.ok(response);  // 200 + body
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);  // 400 + body
        }
    }
    // READ by ID (GET /api/carnet-ordre/{id})
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCarnetOrdreById(@PathVariable Integer id) {
        try {
            CarnetOrdre ordre = carnetOrdreService.getCarnetOrdreById(id);
            Map<String, Object> response = new HashMap<>();
            response.put("data", ordre);
            return ResponseEntity.ok(response);  // 200
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(404).body(error);  // 404
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCarnetOrdre(@PathVariable Integer id,
                                                                 @RequestBody CarnetOrdre details) {
        try {
            CarnetOrdre updated = carnetOrdreService.updateCarnetOrdre(id, details);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "CarnetOrdre mis à jour");
            response.put("data", updated);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return e.getMessage().contains("non trouvé") ? ResponseEntity.status(404).body(error) : ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCarnetOrdre(@PathVariable Integer id) {
        try {
            carnetOrdreService.deleteCarnetOrdre(id);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "CarnetOrdre supprimé avec succès");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(404).body(error);
        }
    }
    // récupérer la liste des ordres en attente (statusOrdre = EN_ATTENTE)
    @GetMapping("/simulation/{simulationId}/pending")
    public ResponseEntity<Map<String, Object>> getPendingBySimulationId(@PathVariable Integer simulationId) {
        try {
            List<CarnetOrdre> pending = carnetOrdreService.getPendingBySimulationId(simulationId);
            Map<String, Object> response = new HashMap<>();
            response.put("data", pending);
            response.put("count", pending.size());
            return ResponseEntity.ok(response);  // 200
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(404).body(error);  // 404
        }
    }
    /*
    // récupère les ordres en attente ((match automatiquement les ordres ACHAT))
    @PostMapping("/simulation/{simulationId}/match")
    public ResponseEntity<Map<String, Object>> matchOrders(@PathVariable Integer simulationId,
                                                           @RequestParam Double prixMarche) {
        try {
            List<CarnetOrdre> matched = carnetOrdreService.matchOrders(simulationId, prixMarche);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Matching exécuté – " + matched.size() + " ordres matchés");
            response.put("data", matched);
            return ResponseEntity.ok(response);  // 200
        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);  // 400
        }
    }
*/
}