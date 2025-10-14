package com.demo.demo.services;

import com.demo.demo.entities.CarnetOrdre;
import org.springframework.data.domain.Page;

import java.awt.print.Pageable;
import java.util.List;

public interface CarnetOrdreService {
    CarnetOrdre createCarnetOrdre(Integer simulationId, CarnetOrdre carnetOrdre);
    CarnetOrdre getCarnetOrdreById(Integer id);
    //Page<CarnetOrdre> getCarnetsOrdreBySimulationId(Integer simulationId, Pageable pageable);  // Paginated
    CarnetOrdre updateCarnetOrdre(Integer id, CarnetOrdre details);
    void deleteCarnetOrdre(Integer id);
    CarnetOrdre executeOrdre(Integer id, Double prixMarcheActuel);
    List<CarnetOrdre> getPendingBySimulationId(Integer simulationId);  // En attente
    List<CarnetOrdre> matchOrders(Integer simulationId, Double prixMarche);  // Idéal : Matching auto
}