// src/main/java/com/demo/demo/entities/TestResult.java
package com.demo.demo.entities;

import jakarta.persistence.*;
import lombok.Data;
import com.demo.demo.entities.niveau;
import com.demo.demo.entities.badge;

@Entity
@Table(name = "test_results")
@Data
public class TestResult {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "tech_score") // Mappe à la colonne tech_score
    private int technique;

    @Column(name = "psy_score") // Mappe à la colonne psy_score
    private int psycho;

    @Column(name = "exp_score") // Mappe à la colonne exp_score
    private int experience;

    @Column(name = "tsi")
    private double tsi;

    @Column(name = "level")
    private niveau level;

    @Column(name = "badge")
    private badge badge;

    @Column(name = "profile")
    private String profile;

    @Column(name = "strengths")
    private String strengths;

    @Column(name = "weaknesses")
    private String weaknesses;

    @Column(columnDefinition = "TEXT")
    private String answers;

    // Getters & Setters gérés par @Data


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public int getTechnique() {
        return technique;
    }

    public void setTechnique(int technique) {
        this.technique = technique;
    }

    public int getPsycho() {
        return psycho;
    }

    public void setPsycho(int psycho) {
        this.psycho = psycho;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public double getTsi() {
        return tsi;
    }

    public void setTsi(double tsi) {
        this.tsi = tsi;
    }

    public niveau getLevel() {
        return level;
    }

    public void setLevel(niveau level) {
        this.level = level;
    }

    public com.demo.demo.entities.badge getBadge() {
        return badge;
    }

    public void setBadge(com.demo.demo.entities.badge badge) {
        this.badge = badge;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getStrengths() {
        return strengths;
    }

    public void setStrengths(String strengths) {
        this.strengths = strengths;
    }

    public String getWeaknesses() {
        return weaknesses;
    }

    public void setWeaknesses(String weaknesses) {
        this.weaknesses = weaknesses;
    }

    public String getAnswers() {
        return answers;
    }

    public void setAnswers(String answers) {
        this.answers = answers;
    }
}