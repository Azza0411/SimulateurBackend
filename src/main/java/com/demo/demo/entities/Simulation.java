package com.demo.demo.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name="similation")
public class Simulation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private int id;
    @Enumerated(EnumType.STRING)
private typeSimulation typeSimulation;
    private String description;
    private Duration duration;
    @Enumerated(EnumType.STRING)
    private difficulte difficulte;
private Float capital;
private Float vitesseExecution;
private Float volatiliteMarche;
private Float volumeEchange;
    @Enumerated(EnumType.STRING)
private regleSimulation regleSimulation ;
    @Enumerated(EnumType.STRING)

    private ModeSimulation ModeSimulation;
    /// nv attributs//////////////////
    private LocalDateTime dateDebut; //Timestamp de début pour suivre l'état en temps réel.

    private LocalDateTime dateFin;
    private Float gainTotal; //  Gain total cumulé des ordres exécutés.
    private Float risqueMaxAcceptable; // Explication : Seuil de risque global (nouveau).
    private Float facteurTempsEcoule; // Explication : Temps simulé écoulé (nouveau).
    private Float capitalActuel; // Explication : Capital restant (nouveau).
    private Float capitalParUser;
    @Enumerated(EnumType.STRING)

    private StatutSimulation statutSimulation;
    ;
    // 🔹 Many-to-Many avec UserEntity
    @ManyToMany(mappedBy = "simulationsParticipees")
    private Set<UserEntity> users = new HashSet<>();
    // 🔹 One-to-Many pour les transactions
    @OneToMany(mappedBy = "simulation", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Transaction> transactions = new HashSet<>();

    // 🔹 One-to-Many pour les Carnets d’Ordre
    // Une Simulation peut avoir plusieurs Carnets d’Ordre
    @JsonIgnore
    @OneToMany(mappedBy = "simulation", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CarnetOrdre> carnetsOrdre = new HashSet<>();
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public com.demo.demo.entities.typeSimulation getTypeSimulation() {
        return typeSimulation;
    }

    public void setTypeSimulation(com.demo.demo.entities.typeSimulation typeSimulation) {
        this.typeSimulation = typeSimulation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Duration getDuration() {
        return duration;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }

    public Float getGainTotal() {
        return gainTotal;
    }

    public void setGainTotal(Float gainTotal) {
        this.gainTotal = gainTotal;
    }

    public Float getRisqueMaxAcceptable() {
        return risqueMaxAcceptable;
    }

    public void setRisqueMaxAcceptable(Float risqueMaxAcceptable) {
        this.risqueMaxAcceptable = risqueMaxAcceptable;
    }

    public Float getFacteurTempsEcoule() {
        return facteurTempsEcoule;
    }

    public void setFacteurTempsEcoule(Float facteurTempsEcoule) {
        this.facteurTempsEcoule = facteurTempsEcoule;
    }

    public Float getCapitalActuel() {
        return capitalActuel;
    }

    public void setCapitalActuel(Float capitalActuel) {
        this.capitalActuel = capitalActuel;
    }

    public Float getCapitalParUser() {
        return capitalParUser;
    }

    public void setCapitalParUser(Float capitalParUser) {
        this.capitalParUser = capitalParUser;
    }

    public StatutSimulation getStatutSimulation() {
        return statutSimulation;
    }

    public void setStatutSimulation(StatutSimulation statutSimulation) {
        this.statutSimulation = statutSimulation;
    }

    public Set<UserEntity> getUsers() {
        return users;
    }

    public void setUsers(Set<UserEntity> users) {
        this.users = users;
    }

    public Set<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(Set<Transaction> transactions) {
        this.transactions = transactions;
    }

    public Set<CarnetOrdre> getCarnetsOrdre() {
        return carnetsOrdre;
    }

    public void setCarnetsOrdre(Set<CarnetOrdre> carnetsOrdre) {
        this.carnetsOrdre = carnetsOrdre;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public com.demo.demo.entities.difficulte getDifficulte() {
        return difficulte;
    }

    public void setDifficulte(com.demo.demo.entities.difficulte difficulte) {
        this.difficulte = difficulte;
    }

    public Float getCapital() {
        return capital;
    }

    public void setCapital(Float capital) {
        this.capital = capital;
    }

    public Float getVitesseExecution() {
        return vitesseExecution;
    }

    public void setVitesseExecution(Float vitesseExecution) {
        this.vitesseExecution = vitesseExecution;
    }

    public Float getVolatiliteMarche() {
        return volatiliteMarche;
    }

    public void setVolatiliteMarche(Float volatiliteMarche) {
        this.volatiliteMarche = volatiliteMarche;
    }

    public Float getVolumeEchange() {
        return volumeEchange;
    }

    public void setVolumeEchange(Float volumeEchange) {
        this.volumeEchange = volumeEchange;
    }

    public com.demo.demo.entities.regleSimulation getRegleSimulation() {
        return regleSimulation;
    }

    public void setRegleSimulation(com.demo.demo.entities.regleSimulation regleSimulation) {
        this.regleSimulation = regleSimulation;
    }

    public com.demo.demo.entities.ModeSimulation getModeSimulation() {
        return ModeSimulation;
    }

    public void setModeSimulation(com.demo.demo.entities.ModeSimulation modeSimulation) {
        ModeSimulation = modeSimulation;
    }
}
