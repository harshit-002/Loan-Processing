package com.abcbank.loan_processing.controller;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/public")
@RestController
public class healthCheck {
    @GetMapping("/healthCheck")
    public String healthCheck(){
        return "healthCheck passed";
    }
}
