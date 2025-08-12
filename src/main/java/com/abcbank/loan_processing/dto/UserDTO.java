package com.abcbank.loan_processing.dto;

import com.abcbank.loan_processing.common.Address;
import com.abcbank.loan_processing.common.ContactInfo;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UserDTO {
    private Long id;
    private String firstName;
    private String middleName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String maritalStatus;
    private String ssnNumber;
    private Integer score;
    private Address address;
    private ContactInfo contactInfo;
    private List<LoanInfoDTO> applications;
    private EmploymentDetailsDTO employmentDetails;
}