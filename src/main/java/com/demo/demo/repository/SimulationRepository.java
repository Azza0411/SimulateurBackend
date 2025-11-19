package com.demo.demo.repository;

import com.demo.demo.entities.Simulation;
import com.demo.demo.entities.StatutSimulation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SimulationRepository extends JpaRepository<Simulation, Integer> {
    /*Optional<Simulation> findByTypeSimulationAndDescriptionAndDateDebut(typeSimulation typeSimulation, String description, LocalDateTime dateDebut);
    // AJOUT FIX : Méthode pour scheduling (query JPQL sur enum statut)
    @Query("SELECT s FROM Simulation s WHERE s.statutSimulation = ?1")
    List<Simulation> findByStatutSimulation(StatutSimulation statut);}
*/
    List<Simulation> findByStatutSimulation(StatutSimulation statutSimulation);}