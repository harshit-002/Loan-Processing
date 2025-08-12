package com.abcbank.loan_processing.controller;

import com.abcbank.loan_processing.entity.Account;
import com.abcbank.loan_processing.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    @Autowired
    AccountService accountService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Account account) {
        return accountService.register(account);
    }

}
