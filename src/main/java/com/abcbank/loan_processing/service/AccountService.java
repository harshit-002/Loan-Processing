package com.abcbank.loan_processing.service;

import com.abcbank.loan_processing.dto.ApiResponse;
import com.abcbank.loan_processing.entity.Account;
import com.abcbank.loan_processing.entity.User;
import com.abcbank.loan_processing.repository.AccountRepository;
import com.abcbank.loan_processing.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    private static final Logger logger = LoggerFactory.getLogger(LoanApplicationRetryService.class);

    @Autowired
    AccountRepository accountRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    public ResponseEntity<ApiResponse<String>> register(HttpServletRequest request, Account account) {
        try {
            if (accountRepository.findBySsnNumber(account.getSsnNumber()).isPresent()) {
                return ResponseEntity.badRequest().body(
                        new ApiResponse<>(false, "User already exists with ssn: " + account.getSsnNumber(), null)
                );
            }
            if (accountRepository.findByUsername(account.getUsername()).isPresent()) {
                return ResponseEntity.badRequest().body(
                        new ApiResponse<>(false, "Username already exists", null)
                );
            }

            account.setPassword(bCryptPasswordEncoder.encode(account.getPassword()));

            User user = new User();
            account.setUser(user);
            user.setAccount(account);

            accountRepository.save(account);
            List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_USER")
            );
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(account.getUsername(), null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authToken);

            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext());
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Account created successfully", null)
            );
        } catch (Exception e) {
            logger.error("Error occurred during account registration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Internal Server Error: Unable to register account", null));
        }
    }
}
