package com.abcbank.loan_processing.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class EmploymentDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String employerName;
    private Integer experienceYears;
    private Integer experienceMonths;
    private BigDecimal annualSalary;
    private String designation;

    @Embedded
    private Address address;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

}
