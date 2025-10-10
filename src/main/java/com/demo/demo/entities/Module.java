package com.demo.demo.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Table(name="Module")
public class Module {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String titreModule;
    private String description;
    @Enumerated(EnumType.STRING)
    private statusModule statusModule;
    @Enumerated(EnumType.STRING)
    private niveau niveau;
    /// /
    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lesson> lessons;
    // 🔹 Relation Many-to-Many inverse avec UserEntity
    @ManyToMany(mappedBy = "modules")
    private Set<UserEntity> users;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitreModule() {
        return titreModule;
    }

    public void setTitreModule(String titreModule) {
        this.titreModule = titreModule;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }




    public com.demo.demo.entities.statusModule getStatusModule() {
        return statusModule;
    }

    public void setStatusModule(com.demo.demo.entities.statusModule statusModule) {
        this.statusModule = statusModule;
    }



}
