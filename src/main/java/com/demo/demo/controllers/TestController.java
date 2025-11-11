// src/main/java/com/demo/demo/controllers/TestController.java
package com.demo.demo.controllers;

import com.demo.demo.DTO.TestResultDto;
import com.demo.demo.DTO.TestSubmitDto;
import com.demo.demo.entities.TestQuestion;
import com.demo.demo.serviceimplement.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/testt")
@CrossOrigin(origins = "http://localhost:4200")
public class TestController {

    @Autowired
    private TestService testService;

    @GetMapping("/questions")
    public ResponseEntity<List<TestQuestion>> getQuestions() {
        System.out.println("Requête GET /test/questions reçue");
        try {
            List<TestQuestion> questions = testService.getAllQuestions();
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            System.err.println("Erreur GET /test/questions: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/submit")
    public ResponseEntity<TestResultDto> submit(@RequestBody TestSubmitDto dto) {
        System.out.println("Requête POST /test/submit reçue: " + dto);
        try {
            TestResultDto result = testService.submitTest(dto);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Erreur POST /test/submit: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}