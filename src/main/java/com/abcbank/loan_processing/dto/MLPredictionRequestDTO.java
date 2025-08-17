package com.abcbank.loan_processing.dto;

import com.abcbank.loan_processing.entity.CreditBureau;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@AllArgsConstructor
@Data
public class MLPredictionRequestDTO {
    private String ssnNumber;
    private BigDecimal loanAmount;
    private String loanPurpose;
    private String description;
    private Integer experienceYears;
    private BigDecimal annualSalary;

    private CreditBureau creditBureau;
}
