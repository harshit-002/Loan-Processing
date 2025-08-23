package com.abcbank.loan_processing.controller;

import com.abcbank.loan_processing.dto.ApiResponse;
import com.abcbank.loan_processing.dto.ApplicationSummaryDTO;
import com.abcbank.loan_processing.dto.LoanApplicationDTO;
import com.abcbank.loan_processing.entity.LoanApplication;
import com.abcbank.loan_processing.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @PostMapping("/application")
    public ResponseEntity<ApiResponse<String>> submitApplication(@RequestBody LoanApplication loanApplication){
        return applicationService.submitApplication(loanApplication);
    }

    @GetMapping("/application")
    public ResponseEntity<ApiResponse<List<ApplicationSummaryDTO>>> getAllApplications(){
        return applicationService.getAllApplications();
    }

    @GetMapping("/application/{id}")
    public ResponseEntity<ApiResponse<LoanApplicationDTO>> getApplicationById(@PathVariable String id){
        return applicationService.getApplicationById(Long.parseLong(id));
    }
}
