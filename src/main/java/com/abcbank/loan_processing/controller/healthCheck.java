package com.abcbank.loan_processing.controller;

import com.abcbank.loan_processing.dto.MLPredictionResponseDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestMapping("/api/public")
@RestController
public class healthCheck {
    @GetMapping("/healthCheck")
    public String healthCheck(){
        return "healthCheck passed";
    }

    @PostMapping("/predict")
    public MLPredictionResponseDTO getPrediction(){
        MLPredictionResponseDTO mockResponse = new MLPredictionResponseDTO(
                750,
                "Approved",
                "none"
        );
//        try {
//            // Simulate processing delay — 5 seconds
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
        return mockResponse;
    }
}
