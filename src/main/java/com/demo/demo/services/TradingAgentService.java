package com.demo.demo.services;

public interface TradingAgentService {
    //prompt Gemini pour calcule risques/contre-trade
    String getAdversaryMove(String userTrade, String asset, Integer simulationId);
    //Appel Finnhub pour prix live (JSON simple).
    String getMarketData(String asset);

}
