package com.demo.demo.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name="Similation")
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
    // 🔹 Many-to-Many avec UserEntity
    @ManyToMany(mappedBy = "simulationsParticipees")
    private Set<UserEntity> users = new HashSet<>();
    // 🔹 One-to-Many pour les transactions
    @OneToMany(mappedBy = "simulation", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Transaction> transactions = new HashSet<>();


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
