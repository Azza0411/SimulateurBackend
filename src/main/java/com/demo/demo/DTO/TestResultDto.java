// src/main/java/com/demo/demo/DTO/TestResultDto.java
package com.demo.demo.DTO;

public class TestResultDto {
    private String level;
    private int technique;
    private int psycho;
    private int experience;
    private double tsi;
    private String profile;
    private String badge;
    private String strengths;
    private String weaknesses;

    // Getters & Setters
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public int getTechnique() { return technique; }
    public void setTechnique(int technique) { this.technique = technique; }

    public int getPsycho() { return psycho; }
    public void setPsycho(int psycho) { this.psycho = psycho; }

    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }

    public double getTsi() { return tsi; }
    public void setTsi(double tsi) { this.tsi = tsi; }

    public String getProfile() { return profile; }
    public void setProfile(String profile) { this.profile = profile; }

    public String getBadge() { return badge; }
    public void setBadge(String badge) { this.badge = badge; }

    public String getStrengths() { return strengths; }
    public void setStrengths(String strengths) { this.strengths = strengths; }

    public String getWeaknesses() { return weaknesses; }
    public void setWeaknesses(String weaknesses) { this.weaknesses = weaknesses; }
}