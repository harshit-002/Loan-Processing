package com.abcbank.loan_processing.controller;

import com.abcbank.loan_processing.dto.MLPredictionRequestDTO;
import com.abcbank.loan_processing.dto.MLPredictionResponseDTO;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("/api/public")
@RestController
public class healthCheck {
    @GetMapping("/healthCheck")
    public String healthCheck(){
        return "healthCheck passed";
    }

    @PostMapping("/predict")
    public MLPredictionResponseDTO getPrediction(@RequestBody MLPredictionRequestDTO requestDTO){
        MLPredictionResponseDTO mockResponse = new MLPredictionResponseDTO(
                750,
                "Approved",
                "none"
        );
        System.out.println("request to ml api: "+ requestDTO);
//        try {
//            // Simulate processing delay — 5 seconds
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
        return mockResponse;
    }
}
