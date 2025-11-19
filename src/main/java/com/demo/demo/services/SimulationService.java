package com.demo.demo.services;

import com.demo.demo.entities.Simulation;
import com.demo.demo.entities.StatutSimulation;

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
    // FIX : Tour humain vs IA (trade humain → contre IA + score)
    Map<String, Object> playIaMove(Integer simulationId, Map<String, Object> humanTrade); // Retourne {"human":..., "ia":..., "score":...}
    void runForexAnalysisInSimulation(Integer simulationId);// MODIF : Trigger forex JSON/gains
    Map<String, Object> getUpdatedTempsRestant(Integer simulationId);
    List<Simulation> getSimulationsByStatus(StatutSimulation statut);
    Map<String, Object> getYahooLivePrices();           // Nouveau
    List<Map<String, Object>> getYahooCandles(String pair); // Nouveau
    // NOUVEAU : Pour secteur TECHNOLOGIE (Alpha Vantage)
    Map<String, Object> getAlphaLivePrices();  // Prix live + % change pour AAPL/MSFT/GOOGL
    List<Map<String, Object>> getAlphaCandles(String symbol);  // 5 dernières candles 1min pour un symbol
    // NOUVEAU : Pour secteur ÉNERGIE (Finnhub)
    Map<String, Object> getFinnhubLivePrices();  // Prix live + % change pour XOM/CVX/etc.
    List<Map<String, Object>> getFinnhubCandles(String symbol);  // 5 dernières candles 1min
    //matier premiere
    // NOUVEAU : Pour matières premières (Twelve Data)
    Map<String, Object> getTwelveDataLivePrices();  // Prix live + % change pour CL/GC/etc.
    List<Map<String, Object>> getTwelveDataCandles(String symbol);  // 5 dernières candles 1min
    //immobillier
    // NOUVEAU : Pour immobilier (Alpha Vantage, réutilise clé)
    Map<String, Object> getAlphaRealEstateLivePrices();  // Prix live + % change pour PLD/AMT/etc.
    List<Map<String, Object>> getAlphaRealEstateCandles(String symbol);  // 5 dernières candles 1min
    void refreshGarchLiveForSimulation(Simulation simulation);
}