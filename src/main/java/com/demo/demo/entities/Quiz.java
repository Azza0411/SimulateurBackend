package com.demo.demo.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Duration;
import java.util.List;

@Entity
@Data
@Table(name="Quiz")
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
private String quizTitre;
private String quizDescription;
private float quizScore;
private int score;
private int nbQuestionsValidee;
    private Duration duration;
    @ManyToOne
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;
    // 🔹 Relation One-to-Many vers Questions
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Questions> questions;
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuizTitre() {
        return quizTitre;
    }

    public void setQuizTitre(String quizTitre) {
        this.quizTitre = quizTitre;
    }

    public String getQuizDescription() {
        return quizDescription;
    }

    public void setQuizDescription(String quizDescription) {
        this.quizDescription = quizDescription;
    }

    public float getQuizScore() {
        return quizScore;
    }

    public void setQuizScore(float quizScore) {
        this.quizScore = quizScore;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getNbQuestionsValidee() {
        return nbQuestionsValidee;
    }

    public void setNbQuestionsValidee(int nbQuestionsValidee) {
        this.nbQuestionsValidee = nbQuestionsValidee;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }
}
