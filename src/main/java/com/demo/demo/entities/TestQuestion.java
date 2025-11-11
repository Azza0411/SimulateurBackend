// src/main/java/com/demo/demo/entities/TestQuestion.java
package com.demo.demo.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "test_question")
public class TestQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_text")
    private String questionText;

    private String category;

    @Column(name = "correct_answer")
    private String correctAnswer;

    @Column(name = "is_active")
    private Boolean isActive;

    private String option1;
    private String option2;
    private String option3;
    private String option4;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public String getOption1() { return option1; }
    public void setOption1(String option1) { this.option1 = option1; }

    public String getOption2() { return option2; }
    public void setOption2(String option2) { this.option2 = option2; }

    public String getOption3() { return option3; }
    public void setOption3(String option3) { this.option3 = option3; }

    public String getOption4() { return option4; }
    public void setOption4(String option4) { this.option4 = option4; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public String[] getOptions() {
        return new String[]{option1, option2, option3, option4};
    }

    public int getCorrectAnswerIndex() {
        if (correctAnswer == null) return -1;
        if (correctAnswer.equals(option1)) return 0;
        if (correctAnswer.equals(option2)) return 1;
        if (correctAnswer.equals(option3)) return 2;
        if (correctAnswer.equals(option4)) return 3;
        return -1;
    }
}