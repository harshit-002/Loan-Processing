package com.abcbank.loan_processing.repository;

import com.abcbank.loan_processing.entity.CreditBureau;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditBureauRepository extends JpaRepository<CreditBureau,String> {
}
