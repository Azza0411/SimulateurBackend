package com.demo.demo;

import com.demo.demo.entities.ModeSimulation;
import com.demo.demo.entities.Simulation;
import com.demo.demo.entities.StatutSimulation;
import com.demo.demo.repository.SimulationRepository;
import com.demo.demo.services.SimulationService;
import com.demo.demo.services.TradingAgentService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")  // Utilise application-test.properties (H2 DB)
@Transactional  // Rollback DB après test
class SimulationServiceTest {
    @Autowired
    private SimulationService simulationService;
    @Autowired
    private TradingAgentService tradingAgentService;
    @Autowired
    private SimulationRepository simulationRepository;

    @Test
    void testCreateAndStartMonoWithIa() {
        Simulation sim = new Simulation();
        sim.setModeSimulation(ModeSimulation.MONOJOUEUR);
        sim.setCapital(10000.0f);
        sim.setDescription("Test Duel IA");
        sim = simulationService.createSimulation(sim);
        assertEquals(StatutSimulation.EN_ATTENTE, sim.getStatutSimulation());
        assertNotNull(sim.getId());

        sim = simulationService.startSimulation(sim.getId());
        assertEquals(StatutSimulation.EXECUTEE, sim.getStatutSimulation());
        assertTrue(sim.getIaAdversaireActive());  // IA activée
        assertNotEquals("{}", sim.getAnalyseResultats());  // Analyse forex
    }

    @Test
    void testPlayIaMove() {
        // Crée et start simu MONO
        Simulation sim = new Simulation();
        sim.setModeSimulation(ModeSimulation.MONOJOUEUR);
        sim.setCapital(10000.0f);
        sim = simulationService.createSimulation(sim);
        simulationService.startSimulation(sim.getId());

        Map<String, Object> humanTrade = Map.of(
                "tradeType", "ACHAT",
                "quantity", 100,
                "price", 1.08,
                "asset", "EURUSD=X"
        );
        Map<String, Object> response = simulationService.playIaMove(sim.getId(), humanTrade);
        assertNotNull(response.get("tour"));
        assertTrue((Double) ((Map) ((Map) response.get("tour")).get("ia")).get("quantity") > 0);  // IA répond
        assertNotNull(response.get("scoreIaVsUser"));  // Score mis à jour

        // Vérifie DB
        Simulation updated = simulationService.getSimulationById(sim.getId());
        assertTrue(updated.getScoreIaVsUser() != 0.0f);
        assertTrue(updated.getHistoriqueTrades().contains("tour"));  // Historique OK
    }

    @Test
    void testEndSimulation() {
        Simulation sim = new Simulation();
        sim.setCapital(10000.0f);
        sim = simulationService.createSimulation(sim);
        simulationService.startSimulation(sim.getId());  // EXECUTEE
        simulationService.endSimulation(sim.getId());  // ANNULEE
        assertEquals(StatutSimulation.ANNULEE, simulationService.getSimulationById(sim.getId()).getStatutSimulation());
    }
}
