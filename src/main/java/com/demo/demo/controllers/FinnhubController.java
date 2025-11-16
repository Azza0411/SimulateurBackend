package com.demo.demo.controllers;

import com.demo.demo.services.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finhub")
@CrossOrigin(origins = "http://localhost:4200")
public class FinnhubController {

    @Autowired
    private SimulationService simulationService;

    @GetMapping("/live-energy")
    public Map<String, Object> getFinnhubLivePrices() {
        return simulationService.getFinnhubLivePrices();
    }

    @GetMapping("/candles-energy/{symbol}")
    public List<Map<String, Object>> getFinnhubCandles(@PathVariable String symbol) {
        return simulationService.getFinnhubCandles(symbol);
    }
}