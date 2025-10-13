package com.demo.demo.entities;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
//@Data
@Table(name="Module")
public class CoursModule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String titreModule;
    private String description;
    @Enumerated(EnumType.STRING)
    private statusModule statusModule;
    @Column(name = "image")
    private String image;
    @Column(name = "video")  // Ajout du champ pour la vidéo
    private String video;
    @Enumerated(EnumType.STRING)
    private niveau niveau;
    /// /
    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lesson> lessons;
    // 🔹 Relation Many-to-Many inverse avec UserEntity
    @ManyToMany(mappedBy = "modules")
    private Set<UserEntity> users= new HashSet<>();;

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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public com.demo.demo.entities.niveau getNiveau() {
        return niveau;
    }

    public void setNiveau(com.demo.demo.entities.niveau niveau) {
        this.niveau = niveau;
    }

    public List<Lesson> getLessons() {
        return lessons;
    }

    public void setLessons(List<Lesson> lessons) {
        this.lessons = lessons;
    }

    public Set<UserEntity> getUsers() {
        return users;
    }

    public void setUsers(Set<UserEntity> users) {
        this.users = users;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public com.demo.demo.entities.statusModule getStatusModule() {
        return statusModule;
    }

    public void setStatusModule(com.demo.demo.entities.statusModule statusModule) {
        this.statusModule = statusModule;
    }



}
