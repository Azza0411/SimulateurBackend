// src/main/java/com/demo/demo/controllers/YahooController.java

package com.demo.demo.controllers;

import com.demo.demo.services.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/yahoo")
@CrossOrigin(origins = "http://localhost:4200")
public class YahooController {

    @Autowired
    private SimulationService simulationService;

    @GetMapping("/live")
    public Map<String, Object> getLivePrices() {
        return simulationService.getYahooLivePrices();
    }

    @GetMapping("/candles/{pair}")
    public List<Map<String, Object>> getCandles(@PathVariable String pair) {
        return simulationService.getYahooCandles(pair);
    }
    // === NOUVELLE ROUTE DÉDIÉE AU FOREX (SÉCURISÉE) ===
    @GetMapping("/forex/live")
    public Map<String, Map<String, Double>> getForexLivePrices() {
        List<String> forexPairs = List.of("EURUSD=X", "GBPUSD=X", "USDJPY=X", "AUDUSD=X", "USDCAD=X");
        Map<String, Map<String, Double>> result = new LinkedHashMap<>();

        Map<String, Object> rawData = simulationService.getYahooLivePrices(); // Réutilise ton service

        for (String pair : forexPairs) {
            Object raw = rawData.get(pair);
            double price = 1.08;
            double changePct = 0.0;

            if (raw instanceof Map<?, ?> map) {
                Object p = map.get("price");
                Object c = map.get("changePct");
                price = (p instanceof Number) ? ((Number) p).doubleValue() : 1.08;
                changePct = (c instanceof Number) ? ((Number) c).doubleValue() : 0.0;
            }

            // SÉCURITÉ : prix Forex entre 0.1 et 1000
            if (pair.contains("EURUSD") && (price < 0.5 || price > 2.0)) price = 1.08345;
            if (pair.contains("GBPUSD") && (price < 0.5 || price > 2.0)) price = 1.23567;
            if (pair.contains("USDJPY") && (price < 50 || price > 300)) price = 151.32;
            if (pair.contains("AUDUSD") && (price < 0.3 || price > 1.5)) price = 0.65432;
            if (pair.contains("USDCAD") && (price < 0.8 || price > 2.0)) price = 1.37210;

            result.put(pair.replace("=X", ""), Map.of("price", price, "changePct", changePct));
        }

        return result;
    }

    // === NOUVELLE ROUTE CANDLES FOREX (SÉCURISÉE) ===
    @GetMapping("/forex/candles/{pair}")
    public List<Map<String, Object>> getForexCandles(@PathVariable String pair) {
        String fullPair = pair + "=X";
        List<String> allowed = List.of("EURUSD=X", "GBPUSD=X", "USDJPY=X", "AUDUSD=X", "USDCAD=X");
        if (!allowed.contains(fullPair)) {
            throw new IllegalArgumentException("Paire Forex non autorisée: " + pair);
        }
        return simulationService.getYahooCandles(fullPair);
    }

}