package com.demo.demo.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name="carnet-ordre ")
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
    private Float risque; // Risque associé à l'ordre (ajouté pour évaluation)
    @Enumerated(EnumType.STRING)
    private ModeValidation modeValidation; // Mode de validation (AUTOMATIQUE/MANUEL)
    // 🔹 Relation Many-to-One avec Simulation
    // Chaque Carnet d’Ordre appartient à une seule Simulation
    @ManyToOne
    @JoinColumn(name = "simulation_id") // colonne FK
    private Simulation simulation;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public com.demo.demo.entities.typeT getTypeT() {
        return typeT;
    }

    public void setTypeT(com.demo.demo.entities.typeT typeT) {
        this.typeT = typeT;
    }

    public com.demo.demo.entities.typeTransaction getTypeTransaction() {
        return typeTransaction;
    }

    public void setTypeTransaction(com.demo.demo.entities.typeTransaction typeTransaction) {
        this.typeTransaction = typeTransaction;
    }

    public com.demo.demo.entities.statusOrdre getStatusOrdre() {
        return statusOrdre;
    }

    public void setStatusOrdre(com.demo.demo.entities.statusOrdre statusOrdre) {
        this.statusOrdre = statusOrdre;
    }

    public double getQuantite() {
        return quantite;
    }

    public void setQuantite(double quantite) {
        this.quantite = quantite;
    }

    public Double getPrixLimite() {
        return prixLimite;
    }

    public void setPrixLimite(Double prixLimite) {
        this.prixLimite = prixLimite;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateExecution() {
        return dateExecution;
    }

    public void setDateExecution(LocalDateTime dateExecution) {
        this.dateExecution = dateExecution;
    }

    public Double getPrixAchat() {
        return prixAchat;
    }

    public void setPrixAchat(Double prixAchat) {
        this.prixAchat = prixAchat;
    }

    public Double getPrixVente() {
        return prixVente;
    }

    public void setPrixVente(Double prixVente) {
        this.prixVente = prixVente;
    }

    public Double getGain() {
        return gain;
    }

    public void setGain(Double gain) {
        this.gain = gain;
    }

    public Float getRisque() {
        return risque;
    }

    public void setRisque(Float risque) {
        this.risque = risque;
    }

    public ModeValidation getModeValidation() {
        return modeValidation;
    }

    public void setModeValidation(ModeValidation modeValidation) {
        this.modeValidation = modeValidation;
    }

    public Simulation getSimulation() {
        return simulation;
    }

    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
    }
}
