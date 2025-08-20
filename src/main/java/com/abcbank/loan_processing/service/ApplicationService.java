package com.abcbank.loan_processing.service;

import com.abcbank.loan_processing.dto.*;
import com.abcbank.loan_processing.entity.*;
import com.abcbank.loan_processing.repository.*;
import com.abcbank.loan_processing.util.Mapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.*;

@Service
public class ApplicationService {
    private static final Logger logger = LoggerFactory.getLogger(LoanApplicationRetryService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployementDetailsRepository employementDetailsRepository;

    @Autowired
    private LoanInfoRepository loanInfoRepository;

    @Autowired
    private Mapper mapper;

    @Autowired
    private MlService mlService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public ResponseEntity<ApiResponse<String>> submitApplication(LoanApplication loanApplication) {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, "Not authenticated", null));
            }
            String accUsername = auth.getName();
            Account userAccount = accountRepository.findByUsername(accUsername)
                    .orElseThrow(() -> new IllegalStateException("Account not found: " + accUsername));

            User incomingUser            = Optional.ofNullable(loanApplication.getUser()).orElseGet(User::new);
            LoanInfo incomingLoanInfo    = Optional.ofNullable(loanApplication.getLoanInfo()).orElseGet(LoanInfo::new);
            EmploymentDetails incomingEd = Optional.ofNullable(loanApplication.getEmploymentDetails()).orElseGet(EmploymentDetails::new);

            User existing = userAccount.getUser();
            if (existing == null) {                 // first application for this account
                existing = new User();
                existing.setAccount(userAccount);
            }

            if(! incomingUser.getSsnNumber().equals(userAccount.getSsnNumber())){
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, "SSN number does not match our records. Enter your ssn number", null));
            }

            existing.updateFrom(incomingUser);

            // EmploymentDetails: update-if-exists, else create
            if (existing.getEmploymentDetails() != null) {
                incomingEd.setId(existing.getEmploymentDetails().getId());
            }
            existing.setEmploymentDetails(incomingEd);
            incomingEd.setUser(existing);

            // LoanInfo: create a new loan record for a new submission
            incomingLoanInfo.setId(null);
            incomingLoanInfo.setUser(existing);
            incomingLoanInfo.setLoanApplicationDate(LocalDate.now());
            MLPredictionResponseDTO MlApiResponse = mlService.getStatusFromModel(existing,incomingLoanInfo,incomingEd);

            String declineReason = "None";
            if(MlApiResponse.getDeclineReasons()!=null){
                declineReason = objectMapper.writeValueAsString(MlApiResponse.getDeclineReasons());
            }
            incomingLoanInfo.setRetryCount(1);
            existing.setScore(MlApiResponse.getScore().intValue());
            incomingLoanInfo.setStatus((String) MlApiResponse.getDecision());
            incomingLoanInfo.setDeclineReason(declineReason);

            if (existing.getLoanInfos() == null) {
                existing.setLoanInfos(new ArrayList<>());
            }
            existing.getLoanInfos().add(incomingLoanInfo);

            userRepository.save(existing);
            logger.info("Saved loan application in DB");
            return ResponseEntity.ok(new ApiResponse<>(true, "Application submitted successfully", null));
        } catch (Exception e) {
            logger.error("Error saving loan application.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Internal Server Error: Unable to submit application", null));
        }
    }

    public ResponseEntity<ApiResponse<List<ApplicationSummaryDTO>>> getAllApplications() {
        try {
            SecurityContext context = SecurityContextHolder.getContext();
            String accUsername = context.getAuthentication().getName();
            Optional<Account> userAccountOpt = accountRepository.findByUsername(accUsername);

            if(userAccountOpt.isPresent()){
                Long currUserId = userAccountOpt.get().getUser().getId();
                List<ApplicationSummaryDTO> applicationList = loanInfoRepository.findAllApplicationSummary(currUserId);
                logger.info("Fetched applications for username: {}", accUsername);
                return ResponseEntity.ok(new ApiResponse<>(true, "Applications fetched successfully", applicationList));
            }
            else{
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, "Account does not exist with username"+accUsername, null));
            }
        } catch (Exception e) {
            logger.info("Something went wrong fetching all applications.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Internal Server Error: Please try again later", null));
        }
    }

    public ResponseEntity<ApiResponse<LoanApplicationDTO>> getApplicationById(Long loanInfoId) {
        try{
            SecurityContext context = SecurityContextHolder.getContext();
            String accUsername = context.getAuthentication().getName();
            Optional<Account> userAccountOpt = accountRepository.findByUsername(accUsername);

            if(userAccountOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, "Account does not exist with username"+accUsername, null));
            }
            User currUserDetails = userAccountOpt.get().getUser();
            List<LoanInfo> match = currUserDetails.getLoanInfos().stream().filter(loanInfo -> loanInfo.getId().equals(loanInfoId)).toList();

            if(!match.isEmpty()){
                Optional<LoanInfo> loanInfoOpt = loanInfoRepository.findLoanInfoById(loanInfoId);

                if (loanInfoOpt.isPresent()) {
                    LoanInfo loanInfo = loanInfoOpt.get();
                    User user = loanInfo.getUser();
                    EmploymentDetails employmentDetails = user.getEmploymentDetails();

                    LoanInfoDTO loanInfoDTO = mapper.toLoanInfoDTO(loanInfo);
                    UserDTO userDTO = mapper.toUserDTO(user);
                    EmploymentDetailsDTO employmentDetailsDTO = mapper.toEmploymentDetailsDTO(employmentDetails);

                    LoanApplicationDTO loanApplicationDTO = new LoanApplicationDTO(userDTO, loanInfoDTO,employmentDetailsDTO);
                    logger.info("Fetched application for: {}",accUsername);
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
            logger.error("Something went wrong fetching application with id :{}",loanInfoId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false,"Internal Server Error: Please try again later",null));
        }
    }


}
