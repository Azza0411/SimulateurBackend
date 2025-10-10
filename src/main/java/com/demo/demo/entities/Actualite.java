package com.demo.demo.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name="Actualite")

public class Actualite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
private int id;
    private String titre;
    private String contenu;
    private String source;
    private Date datePublication;
    @Enumerated(EnumType.STRING)

    private Sentiment sentiment;
    private String impact;
    @Enumerated(EnumType.STRING)

    private CategorieActualite categorieActualite;
    // 🔹 Many-to-Many avec UserEntity
        @ManyToMany(mappedBy = "actualitesConsultees")
      private Set<UserEntity> users = new HashSet<>();

}
