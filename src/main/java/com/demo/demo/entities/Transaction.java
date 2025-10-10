package com.demo.demo.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name="transaction")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Enumerated(EnumType.STRING)
private typeT typeT;
    private int quantiteActif;
    private Long prixTransaction;
    @Enumerated(EnumType.STRING)
    private typeTransaction typeTransaction;
    @Enumerated(EnumType.STRING)
    private statusTransaction statusTransaction;
    private float risque;
    private Long prixCible;
    private float frais;
    private float gainPerte;
    private Date dateCreation;
    private Date dateExecution;
    // 🔹 Relation Many-to-One vers Simulation
    @ManyToOne
    @JoinColumn(name = "simulation_id")
    private Simulation simulation;
    // 🔹 Relation Many-to-Many vers Actif
    @ManyToMany
    @JoinTable(
            name = "transaction_actif",
            joinColumns = @JoinColumn(name = "transaction_id"),
            inverseJoinColumns = @JoinColumn(name = "actif_id")
    )
    private Set<Actif> actifs = new HashSet<>();
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

    public int getQuantiteActif() {
        return quantiteActif;
    }

    public void setQuantiteActif(int quantiteActif) {
        this.quantiteActif = quantiteActif;
    }

    public Long getPrixTransaction() {
        return prixTransaction;
    }

    public void setPrixTransaction(Long prixTransaction) {
        this.prixTransaction = prixTransaction;
    }

    public com.demo.demo.entities.typeTransaction getTypeTransaction() {
        return typeTransaction;
    }

    public void setTypeTransaction(com.demo.demo.entities.typeTransaction typeTransaction) {
        this.typeTransaction = typeTransaction;
    }

    public com.demo.demo.entities.statusTransaction getStatusTransaction() {
        return statusTransaction;
    }

    public void setStatusTransaction(com.demo.demo.entities.statusTransaction statusTransaction) {
        this.statusTransaction = statusTransaction;
    }

    public float getRisque() {
        return risque;
    }

    public void setRisque(float risque) {
        this.risque = risque;
    }

    public Long getPrixCible() {
        return prixCible;
    }

    public void setPrixCible(Long prixCible) {
        this.prixCible = prixCible;
    }

    public float getFrais() {
        return frais;
    }

    public void setFrais(float frais) {
        this.frais = frais;
    }

    public float getGainPerte() {
        return gainPerte;
    }

    public void setGainPerte(float gainPerte) {
        this.gainPerte = gainPerte;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Date getDateExecution() {
        return dateExecution;
    }

    public void setDateExecution(Date dateExecution) {
        this.dateExecution = dateExecution;
    }
}
