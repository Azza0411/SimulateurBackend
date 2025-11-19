package com.demo.demo.controllers;

import com.demo.demo.entities.MarketType;
import com.demo.demo.entities.Simulation;
import com.demo.demo.entities.StatutSimulation;
import com.demo.demo.repository.SimulationRepository;
import com.demo.demo.services.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.demo.demo.config.Config.*;

@RestController
@RequestMapping("/api/trading")
@CrossOrigin(origins = "http://localhost:4200")
public class TradingDashboardController {

    @Autowired
    private SimulationService simulationService;

    @Autowired
    private SimulationRepository simulationRepository;

    @GetMapping("/dashboard/{simulationId}")
    public Map<String, Object> getDashboard(@PathVariable Integer simulationId) {
        Simulation sim = simulationService.getSimulationById(simulationId);

        Map<String, Object> response = new HashMap<>();
        response.put("simulationId", sim.getId());
        response.put("marketType", sim.getMarketType().name());
        response.put("statut", sim.getStatutSimulation().name());

        String displayedAsset = sim.getCurrentAsset();
        if (sim.getMarketType() == MarketType.FOREX) {
            displayedAsset = displayedAsset + "=X";
        }
        response.put("currentAsset", displayedAsset); // GC sans =F, EURUSD avec =X

        response.put("tempsRestant", sim.getTempsRestantSecondes() != null ? sim.getTempsRestantSecondes() : 0);
        response.put("iaActive", sim.getIaAdversaireActive());

        List<String> availableAssets = getDisplayAssets(sim.getMarketType());
        response.put("availableAssets", availableAssets);

        // ON UTILISE LE BON ENDPOINT SELON LE MARCHÉ
        Map<String, Object> priceData;
        List<Map<String, Object>> candles;

        switch (sim.getMarketType()) {
            case FOREX:
            case TECH:
                Map<String, Object> yahooPrices = simulationService.getYahooLivePrices();
                priceData = (Map<String, Object>) yahooPrices.getOrDefault(sim.getCurrentAsset(), Map.of("price", 0.0, "changePct", 0.0));
                candles = simulationService.getYahooCandles(sim.getCurrentAsset());
                break;

            case ENERGIE:
                Map<String, Object> finnhubPrices = simulationService.getFinnhubLivePrices();
                priceData = (Map<String, Object>) finnhubPrices.getOrDefault(sim.getCurrentAsset(), Map.of("price", 0.0, "changePct", 0.0));
                candles = simulationService.getFinnhubCandles(sim.getCurrentAsset());
                break;

            case MATIERE_PREMIERE:
                Map<String, Object> twelvePrices = simulationService.getTwelveDataLivePrices();
                priceData = (Map<String, Object>) twelvePrices.getOrDefault(sim.getCurrentAsset(), Map.of("price", 0.0, "changePct", 0.0));
                candles = simulationService.getTwelveDataCandles(sim.getCurrentAsset());
                break;

            case IMMOBILIER:
                Map<String, Object> alphaPrices = simulationService.getAlphaRealEstateLivePrices();
                priceData = (Map<String, Object>) alphaPrices.getOrDefault(sim.getCurrentAsset(), Map.of("price", 0.0, "changePct", 0.0));
                candles = simulationService.getAlphaRealEstateCandles(sim.getCurrentAsset());
                break;

            default:
                priceData = Map.of("price", 0.0, "changePct", 0.0);
                candles = new ArrayList<>();
        }

        response.put("currentPrice", priceData.get("price"));
        response.put("changePct", priceData.get("changePct"));
        response.put("candles", candles);

        return response;
    }

    @PutMapping("/change-asset/{simulationId}")
    public ResponseEntity<Simulation> changeAsset(@PathVariable Integer simulationId, @RequestBody Map<String, String> body) {
        Simulation sim = simulationService.getSimulationById(simulationId);

        if (sim.getStatutSimulation() != StatutSimulation.EN_ATTENTE) {
            throw new RuntimeException("Impossible : le duel a déjà commencé");
        }

        String asset = body.get("asset");
        if (asset == null || asset.trim().isEmpty()) {
            throw new RuntimeException("Actif manquant");
        }

        String cleanAsset = asset.toUpperCase().trim()
                .replace("=F", "")
                .replace("=X", "");

        sim.setCurrentAsset(cleanAsset);

        return ResponseEntity.ok(simulationRepository.save(sim));
    }

    private List<String> getDisplayAssets(MarketType marketType) {
        return switch (marketType) {
            case FOREX -> PAIRS.stream().map(s -> s.replace("=X", "")).toList();
            case TECH -> TECH_SYMBOLS;
            case ENERGIE -> ENERGY_SYMBOLS;
            case MATIERE_PREMIERE -> COMMODITY_SYMBOLS;
            case IMMOBILIER -> REAL_ESTATE_SYMBOLS;
        };
    }
}