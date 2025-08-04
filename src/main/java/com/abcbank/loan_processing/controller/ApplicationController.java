package com.abcbank.loan_processing.controller;

import com.abcbank.loan_processing.DTO.LoanApplication;
import com.abcbank.loan_processing.entity.Application;
import com.abcbank.loan_processing.entity.EmploymentDetails;
import com.abcbank.loan_processing.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
public class ApplicationController {

    @PostMapping("/application")
    public void submitApplication(@RequestBody LoanApplication loanApplication){
        User user = loanApplication.getUser();
        Application application = loanApplication.getApplication();
        EmploymentDetails employmentDetails = loanApplication.getEmploymentDetails();
    }
}
