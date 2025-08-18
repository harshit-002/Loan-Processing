package com.abcbank.loan_processing.service;

import com.abcbank.loan_processing.entity.Account;
import com.abcbank.loan_processing.entity.AccountPrincipal;
import com.abcbank.loan_processing.repository.AccountRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Component
@Service
public class AccountDetailsServiceImpl implements UserDetailsService {

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
            System.out.println("exception: "+e.getMessage());
            throw new UsernameNotFoundException("account not found");
        }
    }
}