package com.abcbank.loan_processing.entity;

import com.abcbank.loan_processing.common.Address;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
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

}
