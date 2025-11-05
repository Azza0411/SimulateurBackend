// src/main/java/com/demo/demo/controllers/YahooController.java

package com.demo.demo.controllers;

import com.demo.demo.services.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
}