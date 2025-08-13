package com.abcbank.loan_processing.controller;

import com.abcbank.loan_processing.entity.Account;
import com.abcbank.loan_processing.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AccountService accountService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Account account) {
        return accountService.register(account);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        request.getSession().invalidate();
        return ResponseEntity.ok("Logged out successfully");
    }
}
