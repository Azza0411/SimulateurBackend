package com.demo.demo.controllers;

import com.demo.demo.services.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/twelvedata")
@CrossOrigin(origins = "http://localhost:4200")
public class TwelveDataController {

    @Autowired
    private SimulationService simulationService;

    @GetMapping("/live-commodities")
    public Map<String, Object> getTwelveDataLivePrices() {
        return simulationService.getTwelveDataLivePrices();
    }

    @GetMapping("/candles-commodities/{symbol}")
    public List<Map<String, Object>> getTwelveDataCandles(@PathVariable String symbol) {
        return simulationService.getTwelveDataCandles(symbol);
    }
}