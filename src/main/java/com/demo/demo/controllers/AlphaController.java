package com.demo.demo.controllers;

import com.demo.demo.services.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alpha")  // Ou /api/yahoo/tech si tu fusionnes
@CrossOrigin(origins = "http://localhost:4200")  // Comme ton Yahoo
public class AlphaController {

    @Autowired
    private SimulationService simulationService;

    // Live prices pour TECH
    @GetMapping("/live-tech")
    public Map<String, Object> getAlphaLivePrices() {
        return simulationService.getAlphaLivePrices();
    }


    // Candles pour un symbol TECH
    @GetMapping("/candles-tech/{symbol}")
    public List<Map<String, Object>> getAlphaCandles(@PathVariable String symbol) {
        return simulationService.getAlphaCandles(symbol);
    }
    // Dans AlphaController (ajoute ces 2 méthodes)

    @GetMapping("/live-real-estate")
    public Map<String, Object> getAlphaRealEstateLivePrices() {
        return simulationService.getAlphaRealEstateLivePrices();
    }

    @GetMapping("/candles-real-estate/{symbol}")
    public List<Map<String, Object>> getAlphaRealEstateCandles(@PathVariable String symbol) {
        return simulationService.getAlphaRealEstateCandles(symbol);
    }

}