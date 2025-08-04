package com.abcbank.loan_processing.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api")
@RestController
public class healthCheck {
    @GetMapping("/healthCheck")
    public String healthCheck(){
        return "healthCheck passed";
    }
}
