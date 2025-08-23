package com.abcbank.loan_processing.controller;

import com.abcbank.loan_processing.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    @Autowired
    AccountService accountService;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentAccountUsername() {
        SecurityContext context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();
        return ResponseEntity.ok(Map.of("username", username));
    }

}
