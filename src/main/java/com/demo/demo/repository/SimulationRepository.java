package com.demo.demo.repository;

import com.demo.demo.entities.Simulation;
import com.demo.demo.entities.typeSimulation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface SimulationRepository extends JpaRepository<Simulation, Integer> {
    Optional<Simulation> findByTypeSimulationAndDescriptionAndDateDebut(typeSimulation typeSimulation, String description, LocalDateTime dateDebut);}
