package com.demo.demo.controllers;

import com.demo.demo.entities.Actif;
import com.demo.demo.services.ActifService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/actifs")
public class ActifController {

    @Autowired
    private ActifService actifService;

    @PostMapping
    public ResponseEntity<Actif> createActif(@RequestBody Actif actif) {
        Actif createdActif = actifService.createActif(actif);
        return ResponseEntity.ok(createdActif);
    }

    @GetMapping
    public List<Actif> getAllActifs() {
        return actifService.getAllActifs();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Actif> getActifById(@PathVariable Integer id) {
        Optional<Actif> actif = actifService.getActifById(id);
        return actif.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Actif> updateActif(@PathVariable Integer id, @RequestBody Actif actifDetails) {
        Actif updatedActif = actifService.updateActif(id, actifDetails);
        return ResponseEntity.ok(updatedActif);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActif(@PathVariable Integer id) {
        actifService.deleteActif(id);
        return ResponseEntity.noContent().build();
    }
}