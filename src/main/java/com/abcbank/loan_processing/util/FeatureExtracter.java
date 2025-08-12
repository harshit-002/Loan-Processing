package com.abcbank.loan_processing.util;

import com.abcbank.loan_processing.entity.EmploymentDetails;
import com.abcbank.loan_processing.entity.LoanInfo;
import com.abcbank.loan_processing.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;

@Component
public class FeatureExtracter {
    public double[] extractFeaturesFromApplication(LoanInfo loanInfo, EmploymentDetails employmentDetails, User user) {
        LocalDate dob = user.getDateOfBirth();
        int age = Period.between(dob, LocalDate.now()).getYears();

        int employmentDuration = employmentDetails.getExperienceYears();

        int previousLoans = user.getLoanInfos() == null ? 0 : user.getLoanInfos().size();

        return new double[] {
                loanInfo.getLoanAmount().doubleValue(),
                age,
                employmentDetails.getAnnualSalary().doubleValue(),
                employmentDuration,
                previousLoans
        };
    }

}
