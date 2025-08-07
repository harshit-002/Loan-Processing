package com.abcbank.loan_processing.service;

import com.abcbank.loan_processing.common.ApiResponse;
import com.abcbank.loan_processing.dto.*;
import com.abcbank.loan_processing.entity.LoanInfo;
import com.abcbank.loan_processing.entity.EmploymentDetails;
import com.abcbank.loan_processing.entity.LoanApplication;
import com.abcbank.loan_processing.entity.User;
import com.abcbank.loan_processing.repository.LoanInfoRepository;
import com.abcbank.loan_processing.repository.EmployementDetailsRepository;
import com.abcbank.loan_processing.repository.UserRepository;
import com.abcbank.loan_processing.util.ApplicationInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ApplicationService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployementDetailsRepository employementDetailsRepository;

    @Autowired
    private LoanInfoRepository loanInfoRepository;

    @Autowired
    private ApplicationInfoMapper applicationInfoMapper;

    public ResponseEntity<ApiResponse<String>> submitApplication(LoanApplication loanApplication) {
        try {
            User user = loanApplication.getUser();
            LoanInfo loanInfo = loanApplication.getLoanInfo();
            EmploymentDetails employmentDetails = loanApplication.getEmploymentDetails();

            String userSsn = user.getSsnNumber();
            Optional<User> existingUserOpt = userRepository.findBySsnNumber(userSsn);

            loanInfo.setLoanApplicationDate(LocalDate.now());

            if (existingUserOpt.isPresent()) {
                User existingUser = existingUserOpt.get();
                Long existingEmploymentId = existingUser.getEmploymentDetails().getId();
                employmentDetails.setId(existingEmploymentId);

                existingUser.updateFrom(user);
                existingUser.setEmploymentDetails(employmentDetails);
                employmentDetails.setUser(existingUser);

                loanInfo.setUser(existingUser);
                existingUser.getLoanInfos().add(loanInfo);
                userRepository.save(existingUser);
            } else {
                user.setEmploymentDetails(employmentDetails);
                employmentDetails.setUser(user);

                user.setLoanInfos(List.of(loanInfo));
                loanInfo.setUser(user);

                userRepository.save(user);
            }

            return ResponseEntity.ok(new ApiResponse<>(true, "Application submitted successfully", null));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Internal Server Error: Unable to submit application", null));
        }
    }

    public ResponseEntity<ApiResponse<List<ApplicationSummary>>> getAllApplications() {
        try {
            List<ApplicationSummary> applicationList = loanInfoRepository.findAllApplicationSummary();
            return ResponseEntity.ok(new ApiResponse<>(true, "Applications fetched successfully", applicationList));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Internal Server Error: Please try again later", null));
        }
    }

    public ResponseEntity<ApiResponse<LoanApplicationDTO>> getApplicationById(Long id) {
        try{
            Optional<LoanInfo> loanInfoOpt = loanInfoRepository.findLoanInfoById(id);

            if (loanInfoOpt.isPresent()) {
                LoanInfo loanInfo = loanInfoOpt.get();
                User user = loanInfo.getUser();
                EmploymentDetails employmentDetails = user.getEmploymentDetails();

                LoanInfoDTO loanInfoDTO = applicationInfoMapper.toLoanInfoDTO(loanInfo);
                UserDTO userDTO = applicationInfoMapper.toUserDTO(user);
                EmploymentDetailsDTO employmentDetailsDTO = applicationInfoMapper.toEmploymentDetailsDTO(employmentDetails);

                LoanApplicationDTO loanApplicationDTO = new LoanApplicationDTO(userDTO, loanInfoDTO,employmentDetailsDTO);
                return ResponseEntity.ok(new ApiResponse<>(true,"Application found",loanApplicationDTO));
            }
            else{
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false,"Application not found",null));
            }
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false,"Internal Server Error: Please try again later",null));
        }
    }


}
