package com.demo.demo.services;

import com.demo.demo.entities.Simulation;

import java.util.List;
import java.util.Map;

public interface

SimulationService {
    Simulation createSimulation(Simulation simulation);
    Simulation getSimulationById(Integer id);
    List<Simulation> getAllSimulations();
    Simulation updateSimulation(Integer id, Simulation details);
    void deleteSimulation(Integer id);
    Simulation startSimulation(Integer id); // Démarre la simulation (passe à EXECUTEE)
    Simulation endSimulation(Integer id);   // Termine ou annule la simulation
    // Nouvelles pour IA temps réel en MONO
    void activateIaAdversaire(Integer simulationId); // Active IA en salle
    String getRealTimeIaResponse(Integer simulationId, String userTrade, String asset); // Réponse IA live
    void runForexAnalysisInSimulation(Integer simulationId);}// MODIF : Trigger forex JSON/gains