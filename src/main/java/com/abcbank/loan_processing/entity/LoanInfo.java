package com.abcbank.loan_processing.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@ToString(exclude = {"user"})
public class LoanInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal loanAmount;
    private String loanPurpose;
    private LocalDate loanApplicationDate;

    private String description;

    private String status = "Pending";

    private String declineReason;
    private int retryCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
