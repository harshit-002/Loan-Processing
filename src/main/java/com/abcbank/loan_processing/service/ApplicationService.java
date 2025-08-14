package com.abcbank.loan_processing.service;

import com.abcbank.loan_processing.common.ApiResponse;
import com.abcbank.loan_processing.dto.*;
import com.abcbank.loan_processing.entity.*;
import com.abcbank.loan_processing.repository.AccountRepository;
import com.abcbank.loan_processing.repository.LoanInfoRepository;
import com.abcbank.loan_processing.repository.EmployementDetailsRepository;
import com.abcbank.loan_processing.repository.UserRepository;
import com.abcbank.loan_processing.util.ApplicationInfoMapper;
import com.abcbank.loan_processing.util.FeatureExtracter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.*;

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

    @Autowired
    private FeatureExtracter featureExtracter;

    @Autowired
    private MlService mlService;

    @Autowired
    private AccountRepository accountRepository;

    public MLPredictionResponseDTO getStatusFromModel(User user, LoanInfo loanInfo,EmploymentDetails empDetails){
        MLPredictionRequestDTO req = new MLPredictionRequestDTO(
                user.getSsnNumber(),loanInfo.getLoanAmount(),loanInfo.getLoanPurpose(),loanInfo.getDescription(),empDetails.getExperienceYears(),empDetails.getAnnualSalary());
        MLPredictionResponseDTO mlApiResponse = mlService.getPrediction(req);

        return mlApiResponse;
    }

    public ResponseEntity<ApiResponse<String>> submitApplication(LoanApplication loanApplication) {
        try {
            SecurityContext context = SecurityContextHolder.getContext();
            String accUsername = context.getAuthentication().getName();
            Optional<Account> userAccountOpt = accountRepository.findByUsername(accUsername);

            User userPersonalDetails = loanApplication.getUser();
            LoanInfo loanInfo = loanApplication.getLoanInfo();
            EmploymentDetails employmentDetails = loanApplication.getEmploymentDetails();

            loanInfo.setLoanApplicationDate(LocalDate.now());
            Account userAccount = userAccountOpt.get();
            User existingUserDetails = userAccount.getUser();

            Long userId = existingUserDetails.getId();
            userPersonalDetails.setId(userId);
            userPersonalDetails.setAccount(userAccount);

            MLPredictionResponseDTO MlApiResponse = getStatusFromModel(userPersonalDetails,loanInfo,employmentDetails);

            loanInfo.setRetryCount(1);
            userPersonalDetails.setScore((Integer)MlApiResponse.getScore());
            loanInfo.setStatus((String) MlApiResponse.getStatus());
            loanInfo.setDeclineReason((String)MlApiResponse.getDeclineReason());

            if(existingUserDetails.getSsnNumber()==null){
                userPersonalDetails.setEmploymentDetails(employmentDetails);
                employmentDetails.setUser(userPersonalDetails);
                if (userPersonalDetails.getLoanInfos() == null) {
                    userPersonalDetails.setLoanInfos(new ArrayList<>());
                }
                userPersonalDetails.getLoanInfos().add(loanInfo);
                loanInfo.setUser(userPersonalDetails);

                userRepository.save(userPersonalDetails);
                return ResponseEntity.ok(new ApiResponse<>(true, "Application submitted successfully", null));

            }
            else{
                Long existingEmploymentId = existingUserDetails.getEmploymentDetails().getId();
                employmentDetails.setId(existingEmploymentId);

                existingUserDetails.updateFrom(userPersonalDetails);
                existingUserDetails.setEmploymentDetails(employmentDetails);
                employmentDetails.setUser(existingUserDetails);

                loanInfo.setUser(existingUserDetails);
                existingUserDetails.getLoanInfos().add(loanInfo);
                userRepository.save(existingUserDetails);
                return ResponseEntity.ok(new ApiResponse<>(true, "Application submitted successfully", null));
            }
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Internal Server Error: Unable to submit application", null));
        }
    }

    public ResponseEntity<ApiResponse<List<ApplicationSummary>>> getAllApplications() {
        try {
            SecurityContext context = SecurityContextHolder.getContext();
            String accUsername = context.getAuthentication().getName();
            Optional<Account> userAccountOpt = accountRepository.findByUsername(accUsername);

            if(userAccountOpt.isPresent()){
                Long currUserId = userAccountOpt.get().getUser().getId();
                List<ApplicationSummary> applicationList = loanInfoRepository.findAllApplicationSummary(currUserId);
                return ResponseEntity.ok(new ApiResponse<>(true, "Applications fetched successfully", applicationList));
            }
            else{
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, "Account does not exist with username"+accUsername, null));

            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Internal Server Error: Please try again later", null));
        }
    }

    public ResponseEntity<ApiResponse<LoanApplicationDTO>> getApplicationById(Long loanInfoId) {
        try{
            SecurityContext context = SecurityContextHolder.getContext();
            String accUsername = context.getAuthentication().getName();
            Optional<Account> userAccountOpt = accountRepository.findByUsername(accUsername);

            User currUserDetails = userAccountOpt.get().getUser();
            List<LoanInfo> match = currUserDetails.getLoanInfos().stream().filter(loanInfo -> loanInfo.getId().equals(loanInfoId)).toList();

            if(!match.isEmpty()){
                Optional<LoanInfo> loanInfoOpt = loanInfoRepository.findLoanInfoById(loanInfoId);

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
            else{
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(false,"Access denied",null));
            }
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false,"Internal Server Error: Please try again later",null));
        }
    }


}
