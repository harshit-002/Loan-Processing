package com.abcbank.loan_processing.service;

import com.abcbank.loan_processing.entity.Account;
import com.abcbank.loan_processing.entity.User;
import com.abcbank.loan_processing.repository.AccountRepository;
import com.abcbank.loan_processing.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
    @Autowired
    AccountRepository accountRepository;

    @Autowired
    UserRepository userRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    public ResponseEntity<String> register(Account account){
         if (accountRepository.findByUsername(account.getUsername()).isPresent()) {
                return ResponseEntity.badRequest().body("Username already exists");
            }

            account.setPassword(bCryptPasswordEncoder.encode(account.getPassword()));
            account.setPassword(account.getPassword());

            User user = new User();
            account.setUser(user);
            user.setAccount(account);

            accountRepository.save(account);
            return ResponseEntity.ok("Account created");
        }
}
