package com.abcbank.loan_processing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoanApplicationDTO {
    private UserDTO user;
    private LoanInfoDTO loanInfo;
    private EmploymentDetailsDTO employmentDetails;
}

