package com.abcbank.loan_processing.dto;
import com.abcbank.loan_processing.common.Address;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class EmploymentDetailsDTO {
    private Long id;
    private String employerName;
    private Integer experienceYears;
    private Integer experienceMonths;
    private BigDecimal annualSalary;
    private String designation;
    private Address address;
}
