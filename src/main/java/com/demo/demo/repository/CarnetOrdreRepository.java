package com.demo.demo.repository;

import com.demo.demo.entities.CarnetOrdre;
import com.demo.demo.entities.statusOrdre;
import com.demo.demo.entities.typeT;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CarnetOrdreRepository extends JpaRepository<CarnetOrdre, Integer> {
    // Custom pour liste par simulation (avec pagination)
    Page<CarnetOrdre> findBySimulationId(Integer simulationId, Pageable pageable);

    // Ordres en attente par simulation
    @Query("SELECT co FROM CarnetOrdre co WHERE co.simulation.id = :simulationId AND co.statusOrdre = :status")
    List<CarnetOrdre> findPendingBySimulationId(@Param("simulationId") Integer simulationId, @Param("status") statusOrdre status);

    // Par type et status
    @Query("SELECT co FROM CarnetOrdre co WHERE co.simulation.id = :simulationId AND co.typeT = :typeT AND co.statusOrdre = :status")
    List<CarnetOrdre> findBySimulationIdAndTypeAndStatus(@Param("simulationId") Integer simulationId,
                                                         @Param("typeT") typeT typeT,
                                                         @Param("status") statusOrdre status);

    // Unicité optionnelle (ex. : par simu et type)
    Optional<CarnetOrdre> findBySimulationIdAndTypeT(Integer simulationId, typeT typeT);
    // Idéal : Ordres ACHAT prêts à match (prix <= prixMarche)
    @Query("SELECT co FROM CarnetOrdre co WHERE co.simulation.id = :simulationId AND co.typeT = :typeT AND co.statusOrdre = :status AND co.prixLimite <= :prixMarche")
    List<CarnetOrdre> findReadyBids(@Param("simulationId") Integer simulationId, @Param("typeT") typeT typeT, @Param("status") statusOrdre status, @Param("prixMarche") Double prixMarche);

}
