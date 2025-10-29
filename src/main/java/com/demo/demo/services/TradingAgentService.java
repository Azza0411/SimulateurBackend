package com.demo.demo.services;

public interface TradingAgentService {
    void activateIaAdversaire(Integer simulationId);
    String getRealTimeIaResponse(Integer simulationId, String userTrade, String asset);
    Float updateScoreIaVsUser(Integer simulationId, Float pnlHuman);
}
