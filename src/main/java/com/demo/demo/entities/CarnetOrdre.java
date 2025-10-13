package com.demo.demo.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name="CarnetOrdre ")
public class CarnetOrdre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
private int id;
    @Enumerated(EnumType.STRING)
    private typeT typeT;
    @Enumerated(EnumType.STRING)
private typeTransaction typeTransaction;
    @Enumerated(EnumType.STRING)
    private statusOrdre statusOrdre;
    private double quantite;

    // Null si market
    private Double prixLimite;

    private LocalDateTime dateCreation;
    private LocalDateTime dateExecution;

    private Double prixAchat; // si typeOrdre = ACHAT
    private Double prixVente; // si typeOrdre = VENTE

    private Double gain;
}
