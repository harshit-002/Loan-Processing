package com.abcbank.loan_processing.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class ApplicationSummaryDTO {
    private Long id;
    private String name;
    private String loanPurpose;
    private BigDecimal loanAmount;
    private LocalDate LoanApplicationDate;
    private String status;

    public ApplicationSummaryDTO(Long id, String name, String status, LocalDate loanApplicationDate,String loanPurpose,BigDecimal loanAmount) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.LoanApplicationDate = loanApplicationDate;
        this.loanPurpose = loanPurpose;
        this.loanAmount = loanAmount;
    }
}
