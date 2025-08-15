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
import org.springframework.transaction.annotation.Transactional;
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
    
    public Map<String,Object> getStatusFromModel(User user, LoanInfo loanInfo, EmploymentDetails employmentDetails){
        double[] features = featureExtracter.extractFeaturesFromApplication(loanInfo,employmentDetails,user);
        Map<String, Object> mlResponse = mlService.getPrediction(features);
        Integer score = (Integer) mlResponse.get("score");
        String declineReason = (String) mlResponse.get("declineReason");

        if(score==-1) return Map.of("score", score,"status","Pending","declineReason","Pending");

        if(score>700) return  Map.of("score", score,"status","Approved","declineReason",declineReason);
        return  Map.of("score", score,"status","Declined","declineReason",declineReason);
    }

    @Transactional
    public ResponseEntity<ApiResponse<String>> submitApplication(LoanApplication loanApplication) {
        try {
            // 1) Auth & account
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, "Not authenticated", null));
            }
            String accUsername = auth.getName();
            Account userAccount = accountRepository.findByUsername(accUsername)
                    .orElseThrow(() -> new IllegalStateException("Account not found: " + accUsername));

            // 2) Defensive null-safe extraction from DTOs
            User incomingUser            = Optional.ofNullable(loanApplication.getUser()).orElseGet(User::new);
            LoanInfo incomingLoanInfo    = Optional.ofNullable(loanApplication.getLoanInfo()).orElseGet(LoanInfo::new);
            EmploymentDetails incomingEd = Optional.ofNullable(loanApplication.getEmploymentDetails()).orElseGet(EmploymentDetails::new);

            // 3) Attach/prepare aggregate root
            User existing = userAccount.getUser();
            if (existing == null) {                 // first application for this account
                existing = new User();
                existing.setAccount(userAccount);
            }

            // 4) Copy/update simple fields (don’t clobber relationships)
            existing.updateFrom(incomingUser);      // your method that copies primitives

            // 5) EmploymentDetails: update-if-exists, else create
            if (existing.getEmploymentDetails() != null) {
                // keep same row, just update values
                incomingEd.setId(existing.getEmploymentDetails().getId());
            }
            existing.setEmploymentDetails(incomingEd);
            incomingEd.setUser(existing);

            // 6) LoanInfo: always create a new loan record for a new submission
            // (avoid trusting client-provided IDs like 0)
            incomingLoanInfo.setId(null);
            incomingLoanInfo.setUser(existing);
            incomingLoanInfo.setLoanApplicationDate(LocalDate.now()); // server truth

            if (existing.getLoanInfos() == null) {
                existing.setLoanInfos(new ArrayList<>());
            }
            existing.getLoanInfos().add(incomingLoanInfo);

            // 7) Scoring/model
            Map<String,Object> statusMap = getStatusFromModel(existing, incomingLoanInfo, incomingEd);
            existing.setScore((Integer) statusMap.get("score"));
            incomingLoanInfo.setStatus((String) statusMap.get("status"));
            incomingLoanInfo.setDeclineReason((String) statusMap.get("declineReason"));

            // 8) Persist root (requires cascade on child relations)
            userRepository.save(existing);

            return ResponseEntity.ok(new ApiResponse<>(true, "Application submitted successfully", null));
        } catch (Exception e) {
            e.printStackTrace();
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
