package com.abcbank.loan_processing.controller;

import com.abcbank.loan_processing.entity.LoanApplication;
import com.abcbank.loan_processing.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @PostMapping("/application")
    public ResponseEntity<?> submitApplication(@RequestBody LoanApplication loanApplication){
        return applicationService.submitApplication(loanApplication);
    }

    @GetMapping("/application")
    public ResponseEntity<?> getAllApplications(){
        return applicationService.getAllApplications();
    }

    @GetMapping("/application/{id}")
    public ResponseEntity<?> getApplicationById(@PathVariable String id){
        return applicationService.getApplicationById(Long.parseLong(id));
    }
}
