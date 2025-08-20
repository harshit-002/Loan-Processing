package com.abcbank.loan_processing.service;

import com.abcbank.loan_processing.entity.Account;
import com.abcbank.loan_processing.entity.User;
import com.abcbank.loan_processing.repository.AccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    @InjectMocks
    private AccountService accountService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
    }

    @Test
    void testRegister_UserAlreadyExists() {
        Account account = new Account();
        account.setSsnNumber("123");
        account.setUsername("user1");
        when(accountRepository.findBySsnNumber("123")).thenReturn(Optional.of(account));

        ResponseEntity<String> response = accountService.register(request, account);

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("User already exists"));
    }

    @Test
    void testRegister_UsernameAlreadyExists() {
        Account account = new Account();
        account.setSsnNumber("123");
        account.setUsername("user1");
        when(accountRepository.findBySsnNumber("123")).thenReturn(Optional.empty());
        when(accountRepository.findByUsername("user1")).thenReturn(Optional.of(account));

        ResponseEntity<String> response = accountService.register(request, account);

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("Username already exists"));
    }

    @Test
    void testRegister_Success() {
        Account account = new Account();
        account.setSsnNumber("123");
        account.setUsername("user1");
        account.setPassword("password");
        when(accountRepository.findBySsnNumber("123")).thenReturn(Optional.empty());
        when(accountRepository.findByUsername("user1")).thenReturn(Optional.empty());
        when(request.getSession(true)).thenReturn(session);

        ResponseEntity<String> response = accountService.register(request, account);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Account created", response.getBody());
        verify(accountRepository, times(1)).save(any(Account.class));
        verify(session, times(1)).setAttribute(eq(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY), any());
    }
}