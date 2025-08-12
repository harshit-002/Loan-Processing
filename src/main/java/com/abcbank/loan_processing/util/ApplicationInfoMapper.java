package com.abcbank.loan_processing.util;

import com.abcbank.loan_processing.dto.LoanInfoDTO;
import com.abcbank.loan_processing.dto.EmploymentDetailsDTO;
import com.abcbank.loan_processing.dto.UserDTO;
import com.abcbank.loan_processing.entity.LoanInfo;
import com.abcbank.loan_processing.entity.EmploymentDetails;
import com.abcbank.loan_processing.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ApplicationInfoMapper {

    public UserDTO toUserDTO(User user) {
        if (user == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setMiddleName(user.getMiddleName());
        dto.setLastName(user.getLastName());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setMaritalStatus(user.getMaritalStatus());
        dto.setSsnNumber(user.getSsnNumber());
        dto.setScore(user.getScore());
        dto.setAddress(user.getAddress());
        dto.setContactInfo(user.getContactInfo());
        return dto;
    }

    public LoanInfoDTO toLoanInfoDTO(LoanInfo loanInfo) {
        if (loanInfo == null) {
            return null;
        }
        LoanInfoDTO dto = new LoanInfoDTO();
        dto.setId(loanInfo.getId());
        dto.setLoanAmount(loanInfo.getLoanAmount());
        dto.setLoanPurpose(loanInfo.getLoanPurpose());
        dto.setApplicationDate(loanInfo.getLoanApplicationDate());
        dto.setDescription(loanInfo.getDescription());
        dto.setStatus(loanInfo.getStatus());
        dto.setDeclineReason(loanInfo.getDeclineReason());
        return dto;
    }

    public EmploymentDetailsDTO toEmploymentDetailsDTO(EmploymentDetails employmentDetails) {
        if (employmentDetails == null) {
            return null;
        }
        EmploymentDetailsDTO dto = new EmploymentDetailsDTO();
        dto.setId(employmentDetails.getId());
        dto.setEmployerName(employmentDetails.getEmployerName());
        dto.setExperienceYears(employmentDetails.getExperienceYears());
        dto.setExperienceMonths(employmentDetails.getExperienceMonths());
        dto.setAnnualSalary(employmentDetails.getAnnualSalary());
        dto.setDesignation(employmentDetails.getDesignation());
        dto.setAddress(employmentDetails.getAddress());
        return dto;
    }
}