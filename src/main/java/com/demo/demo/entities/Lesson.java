package com.demo.demo.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name="Lesson")
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String title;
    private String description;
    private String image;
    @Enumerated(EnumType.STRING)

    private typeLesson typeLesson;
    private String lien;
    private int ordreLesson;
    @ManyToOne
    @JoinColumn(name = "module_id")
    private Module module;
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Quiz> quizzes;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public com.demo.demo.entities.typeLesson getTypeLesson() {
        return typeLesson;
    }

    public void setTypeLesson(com.demo.demo.entities.typeLesson typeLesson) {
        this.typeLesson = typeLesson;
    }

    public String getLien() {
        return lien;
    }

    public void setLien(String lien) {
        this.lien = lien;
    }

    public int getOrdreLesson() {
        return ordreLesson;
    }

    public void setOrdreLesson(int ordreLesson) {
        this.ordreLesson = ordreLesson;
    }
}
