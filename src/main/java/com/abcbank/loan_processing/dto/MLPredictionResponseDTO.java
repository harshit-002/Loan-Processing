package com.abcbank.loan_processing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MLPredictionResponseDTO {
    private Integer score;
    private String status;
    private String declineReason;
}

