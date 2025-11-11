// src/main/java/com/demo/demo/repository/TestResultRepository.java
        package com.demo.demo.repository;

import com.demo.demo.entities.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    Optional<TestResult> findByUserId(Long userId);
}