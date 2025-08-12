package com.abcbank.loan_processing.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestMapping("/api")
@RestController
public class healthCheck {
    @GetMapping("/healthCheck")
    public String healthCheck(){
        return "healthCheck passed";
    }

    @PostMapping("/predict")
    public Map<String, Object> getPrediction(){
        Map<String, Object> mockResponse = Map.of(
                "score", 700,
                "declineReason","poor u, lol"
        );
        try {
            // Simulate processing delay — 5 seconds
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return mockResponse;
    }
}
