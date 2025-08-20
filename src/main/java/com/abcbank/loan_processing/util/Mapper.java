package com.abcbank.loan_processing.util;

import com.abcbank.loan_processing.dto.LoanInfoDTO;
import com.abcbank.loan_processing.dto.EmploymentDetailsDTO;
import com.abcbank.loan_processing.dto.MLPredictionRequestDTO;
import com.abcbank.loan_processing.dto.UserDTO;
import com.abcbank.loan_processing.entity.CreditBureau;
import com.abcbank.loan_processing.entity.LoanInfo;
import com.abcbank.loan_processing.entity.EmploymentDetails;
import com.abcbank.loan_processing.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class Mapper {
    private static final Map<String, String> PURPOSE_MAP = Map.of(
            "Home Loan", "Home_Loan",
            "Personal Loan", "Personal_Loan",
            "Debt", "Debt",
            "Education Loan", "Education_Loan"
    );

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

    public MLPredictionRequestDTO toMlPredictionRequestDTO(LoanInfo loanInfo, EmploymentDetails employmentDetails, CreditBureau creditBureau){
        MLPredictionRequestDTO dto = new MLPredictionRequestDTO();

        dto.setLoanAmnt(loanInfo.getLoanAmount());
        dto.setDesc(loanInfo.getDescription());
        dto.setPurpose(PURPOSE_MAP.get(loanInfo.getLoanPurpose()));
        dto.setEmpLength(employmentDetails.getExperienceYears());
        dto.setAnnualInc(employmentDetails.getAnnualSalary());

        if(creditBureau == null) {
            String defaultEarliestCrLine = "2015-01-20";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate date = LocalDate.parse(defaultEarliestCrLine, formatter);
            creditBureau = new CreditBureau("",0,0,120,130,3,0,0L,0.1,9,date);
        }

        dto.setDelinq2yrs(creditBureau.getDelinq2yrs());
        dto.setInqLast6mths(creditBureau.getInqLast6mths());
        dto.setMthsSinceLastDelinq(creditBureau.getMthsSinceLastDelinq());
        dto.setMthsSinceLastRecord(creditBureau.getMthsSinceLastRecord());
        dto.setOpenAcc(creditBureau.getOpenAcc());
        dto.setPubRec(creditBureau.getPubRec());
        dto.setRevolBal(creditBureau.getRevolBal());
        dto.setRevolUtil(creditBureau.getRevolUtil());
        dto.setTotalAcc(creditBureau.getTotalAcc());
        dto.setEarliestCrLine(creditBureau.getEarliestCrLine());

        return dto;
    }
}