package com.abcbank.loan_processing.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class ApplicationSummary {
    private Long id;
    private String name;
    private LocalDate loanApplicationDate;
    private String status;

    public ApplicationSummary(Long id, String name, String status, LocalDate loanApplicationDate) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.loanApplicationDate = loanApplicationDate;
    }
}
