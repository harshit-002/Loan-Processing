package com.abcbank.loan_processing.service;

import com.abcbank.loan_processing.entity.Account;
import com.abcbank.loan_processing.entity.User;
import com.abcbank.loan_processing.repository.AccountRepository;
import com.abcbank.loan_processing.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {
    @Autowired
    AccountRepository accountRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    public ResponseEntity<String> register(HttpServletRequest request, Account account){
        if( accountRepository.findBySsnNumber(account.getSsnNumber()).isPresent()){
            return ResponseEntity.badRequest().body("User already exists with ssn: "+ account.getSsnNumber());
        }
         if (accountRepository.findByUsername(account.getUsername()).isPresent()) {
                return ResponseEntity.badRequest().body("Username already exists");
         }

        account.setPassword(bCryptPasswordEncoder.encode(account.getPassword()));

        User user = new User();
        account.setUser(user);
        user.setAccount(account);

        accountRepository.save(account);
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER")
        );
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(account.getUsername(),null,authorities);
        SecurityContextHolder.getContext().setAuthentication(authToken);

        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());
        return ResponseEntity.ok("Account created");
        }
}
