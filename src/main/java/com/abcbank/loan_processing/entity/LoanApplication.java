package com.abcbank.loan_processing.entity;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplication {
    private User user;
    private LoanInfo loanInfo;
    private EmploymentDetails employmentDetails;
}
