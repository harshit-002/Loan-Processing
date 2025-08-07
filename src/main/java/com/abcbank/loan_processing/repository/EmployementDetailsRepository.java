package com.abcbank.loan_processing.repository;

import com.abcbank.loan_processing.entity.EmploymentDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployementDetailsRepository extends JpaRepository<EmploymentDetails,Long> {
}
