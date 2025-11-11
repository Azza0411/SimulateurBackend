// src/main/java/com/demo/demo/DTO/TestSubmitDto.java
package com.demo.demo.DTO;

import java.util.Map;

public class TestSubmitDto {
    private Map<Long, Integer> answers;
    public Map<Long, Integer> getAnswers() { return answers; }
    public void setAnswers(Map<Long, Integer> answers) { this.answers = answers; }
}