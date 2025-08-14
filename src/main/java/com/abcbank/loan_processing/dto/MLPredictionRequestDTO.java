package com.abcbank.loan_processing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@Data
public class MLPredictionRequestDTO {
    private String ssnNumber;
    private BigDecimal loanAmount;
    private String loanPurpose;
    private String description;
    private Integer experienceYears;
    private BigDecimal annualSalary;
}
