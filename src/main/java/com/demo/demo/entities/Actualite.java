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
    @Column(name = "image")
    private String image;
    @Column(name = "video")
    private String video;
    @Enumerated(EnumType.STRING)

    private CategorieActualite categorieActualite;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Date getDatePublication() {
        return datePublication;
    }

    public void setDatePublication(Date datePublication) {
        this.datePublication = datePublication;
    }

    public Sentiment getSentiment() {
        return sentiment;
    }

    public void setSentiment(Sentiment sentiment) {
        this.sentiment = sentiment;
    }

    public String getImpact() {
        return impact;
    }

    public void setImpact(String impact) {
        this.impact = impact;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public CategorieActualite getCategorieActualite() {
        return categorieActualite;
    }

    public void setCategorieActualite(CategorieActualite categorieActualite) {
        this.categorieActualite = categorieActualite;
    }

    public Set<UserEntity> getUsers() {
        return users;
    }

    public void setUsers(Set<UserEntity> users) {
        this.users = users;
    }

    // 🔹 Many-to-Many avec UserEntity
        @ManyToMany(mappedBy = "actualitesConsultees")
      private Set<UserEntity> users = new HashSet<>();

}
