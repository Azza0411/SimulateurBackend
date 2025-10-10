package com.demo.demo.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name="Questions")
public class Questions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
    private String intitule;
    private String reponseCorrecte;
    @ElementCollection
    private List<String> options; // Liste dynamique d'options
    // 🔹 Relation Many-to-One vers Quiz
    @ManyToOne
    @JoinColumn(name = "quiz_id")  // clé étrangère
    private Quiz quiz;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIntitule() {
        return intitule;
    }

    public void setIntitule(String intitule) {
        this.intitule = intitule;
    }

    public String getReponseCorrecte() {
        return reponseCorrecte;
    }

    public void setReponseCorrecte(String reponseCorrecte) {
        this.reponseCorrecte = reponseCorrecte;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }
}
