package com.abcbank.loan_processing.repository;

import com.abcbank.loan_processing.entity.LoanInfo;
import com.abcbank.loan_processing.dto.ApplicationSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanInfoRepository extends JpaRepository<LoanInfo,Long> {
    @Query("SELECT new com.abcbank.loan_processing.dto.ApplicationSummary(" +
            "l.id, CONCAT(u.firstName,' ', COALESCE(u.middleName, ''),' ', u.lastName), l.status, l.loanApplicationDate) " +
            "FROM LoanInfo l JOIN l.user u"+
    " WHERE u.id =:id ")
    List<ApplicationSummary> findAllApplicationSummary(@Param("id") Long id);

    Optional<LoanInfo> findLoanInfoById(Long id);

    List<LoanInfo> findByStatus(String status);
}
