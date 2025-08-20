package com.abcbank.loan_processing.service;

import com.abcbank.loan_processing.entity.Account;
import com.abcbank.loan_processing.entity.AccountPrincipal;
import com.abcbank.loan_processing.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Component
@Service
public class AccountDetailsServiceImpl implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(LoanApplicationRetryService.class);

    private AccountRepository accountRepository;

    public AccountDetailsServiceImpl(AccountRepository accountRepository){
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Optional<Account> currAccount = accountRepository.findByUsername(username);
            if(currAccount.isPresent()){
                return new AccountPrincipal(currAccount.get());
            }
            throw new UsernameNotFoundException("Account not found with username: " + username);
        }
        catch (UsernameNotFoundException e){
            logger.error("Error occurred loading user{}", e.getMessage());
            throw new UsernameNotFoundException("account not found");
        }
    }
}