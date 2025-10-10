package com.demo.demo.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name="Actif")
public class Actif {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
private int id;
    private int codeTicket;
    private String nom;
    @Enumerated(EnumType.STRING)

    private typeActif typeActif;
    @Enumerated(EnumType.STRING)

    private SecteurActif SecteurActif;
    private float prixNominal;
    private float volatilite_histo;
    private String description;
    private Float prixUpdate;
    // 🔹 Relation Many-to-Many vers Transaction (bidirectionnelle)
    @ManyToMany(mappedBy = "actifs")
    private Set<Transaction> transactions = new HashSet<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCodeTicket() {
        return codeTicket;
    }

    public void setCodeTicket(int codeTicket) {
        this.codeTicket = codeTicket;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public com.demo.demo.entities.typeActif getTypeActif() {
        return typeActif;
    }

    public void setTypeActif(com.demo.demo.entities.typeActif typeActif) {
        this.typeActif = typeActif;
    }

    public com.demo.demo.entities.SecteurActif getSecteurActif() {
        return SecteurActif;
    }

    public void setSecteurActif(com.demo.demo.entities.SecteurActif secteurActif) {
        SecteurActif = secteurActif;
    }

    public float getPrixNominal() {
        return prixNominal;
    }

    public void setPrixNominal(float prixNominal) {
        this.prixNominal = prixNominal;
    }

    public float getVolatilite_histo() {
        return volatilite_histo;
    }

    public void setVolatilite_histo(float volatilite_histo) {
        this.volatilite_histo = volatilite_histo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Float getPrixUpdate() {
        return prixUpdate;
    }

    public void setPrixUpdate(Float prixUpdate) {
        this.prixUpdate = prixUpdate;
    }
}
