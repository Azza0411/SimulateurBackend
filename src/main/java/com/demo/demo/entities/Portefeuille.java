package com.demo.demo.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name="Portefeuille")
public class Portefeuille {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
private int id;
    private Long solde;
    private Float risque;
    private Float valeurNette;
    @Enumerated(EnumType.STRING)
private typePortefeuille typePortefeuille;
    // 🔹 Relation One-to-One pour portefeuille individuel
    @OneToOne(mappedBy = "portefeuilleIndividuel", cascade = CascadeType.ALL)
    private UserEntity userIndividuel;

    // 🔹 Relation Many-to-Many pour portefeuille partagé
    @ManyToMany(mappedBy = "portefeuillesPartages")
    private Set<UserEntity> usersPartages = new HashSet<>();
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Long getSolde() {
        return solde;
    }

    public void setSolde(Long solde) {
        this.solde = solde;
    }

    public Float getRisque() {
        return risque;
    }

    public void setRisque(Float risque) {
        this.risque = risque;
    }

    public Float getValeurNette() {
        return valeurNette;
    }

    public void setValeurNette(Float valeurNette) {
        this.valeurNette = valeurNette;
    }
}
