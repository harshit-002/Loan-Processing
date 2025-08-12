package com.abcbank.loan_processing.dto;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LoanInfoDTO {
    private Long id;
    private BigDecimal loanAmount;
    private String loanPurpose;
    private LocalDate applicationDate;
    private String description;
    private String status;
    private String declineReason;
}
