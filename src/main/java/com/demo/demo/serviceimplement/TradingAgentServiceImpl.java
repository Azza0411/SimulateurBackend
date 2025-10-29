package com.demo.demo.serviceimplement;

import com.demo.demo.entities.Simulation;
import com.demo.demo.repository.SimulationRepository;
import com.demo.demo.services.TradingAgentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class TradingAgentServiceImpl implements TradingAgentService {

    private static final Logger logger = LoggerFactory.getLogger(TradingAgentServiceImpl.class);

    @Autowired
    private SimulationRepository simulationRepository;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Autowired
    private RestTemplate restTemplate;

    // MODÈLE ACTIF & GRATUIT : gemini-1.5-flash + v1 + generateContent
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    public void activateIaAdversaire(Integer simulationId) {
        Simulation sim = simulationRepository.findById(simulationId).orElseThrow();
        sim.setIaAdversaireActive(true);
        sim.setScoreIaVsUser(0.0f);
        sim.setDernierTradeIa("{}");
        simulationRepository.save(sim);
    }

    @Override
    public String getRealTimeIaResponse(Integer simulationId, String userTrade, String asset) {
        logger.info("Appel IA Gemini pour simulation {}", simulationId);

        Simulation sim = simulationRepository.findById(simulationId).orElseThrow();
        double capital = sim.getCapitalActuel();

        String prompt = String.format(
                "Tu es un trader IA expert. L'humain a fait : %s sur %s. Capital IA : %.2f. " +
                        "Donne un contre-trade intelligent. Réponds UNIQUEMENT avec ce JSON : " +
                        "{\"type\":\"VENTE\",\"quantite\":800,\"prix\":1.0841,\"stopLoss\":1.0600,\"takeProfit\":1.1380,\"raison\":\"RSI 72, MACD baissier\"}",
                userTrade, asset, capital
        );

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", prompt))
                ))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            String url = GEMINI_URL + geminiApiKey;
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            String body = response.getBody();

            logger.info("Réponse Gemini : {}", body);

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> json = mapper.readValue(body, Map.class);
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) json.get("candidates");
            if (candidates == null || candidates.isEmpty()) return fallbackJson();

            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            String text = (String) parts.get(0).get("text");

            String cleanJson = text.replaceAll("```json|```", "").trim();

            sim.setDernierTradeIa(cleanJson);
            simulationRepository.save(sim);
            return cleanJson;

        } catch (Exception e) {
            logger.error("Erreur Gemini : {}", e.getMessage());
            return fallbackJson();
        }
    }

    private String fallbackJson() {
        return """
            {
              "type": "VENTE",
              "quantite": 500,
              "prix": 1.0840,
              "stopLoss": 1.0620,
              "takeProfit": 1.1350,
              "raison": "Fallback : contre-trade sécurisé"
            }
            """;
    }

    @Override
    public Float updateScoreIaVsUser(Integer simulationId, Float pnlHuman) {
        Simulation sim = simulationRepository.findById(simulationId).orElseThrow();
        float pnlIa = (float) (Math.random() * 200 - 100);
        float delta = pnlIa - pnlHuman;
        sim.setScoreIaVsUser(sim.getScoreIaVsUser() + delta);
        simulationRepository.save(sim);
        return sim.getScoreIaVsUser();
    }
}