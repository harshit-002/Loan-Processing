package com.abcbank.loan_processing.repository;

import com.abcbank.loan_processing.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account,String> {
    Optional<Account> findByUsername(String username);
}
