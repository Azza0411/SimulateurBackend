package com.demo.demo.repository;// src/main/java/com/demo/demo/repositories/TestQuestionRepository.java


import com.demo.demo.entities.TestQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TestQuestionRepository extends JpaRepository<TestQuestion, Long> {
    List<TestQuestion> findByIsActiveTrue();
}