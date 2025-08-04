package com.abcbank.loan_processing.entity;


import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal loanAmount;
    private String loanPurpose;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String scoreStatus = "PENDING"; // optional status for async scoring

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Getters and Setters
}
