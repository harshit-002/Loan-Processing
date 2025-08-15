//package com.abcbank.loan_processing.service;
//
//import com.abcbank.loan_processing.common.ApiResponse;
//import com.abcbank.loan_processing.dto.*;
//import com.abcbank.loan_processing.entity.*;
//import com.abcbank.loan_processing.repository.AccountRepository;
//import com.abcbank.loan_processing.repository.LoanInfoRepository;
//import com.abcbank.loan_processing.repository.EmployementDetailsRepository;
//import com.abcbank.loan_processing.repository.UserRepository;
//import com.abcbank.loan_processing.util.ApplicationInfoMapper;
//import com.abcbank.loan_processing.util.FeatureExtracter;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.stereotype.Service;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//@Service
//public class DummyApplicationService {
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private EmployementDetailsRepository employementDetailsRepository;
//
//    @Autowired
//    private LoanInfoRepository loanInfoRepository;
//
//    @Autowired
//    private ApplicationInfoMapper applicationInfoMapper;
//
//    @Autowired
//    private FeatureExtracter featureExtracter;
//
//    @Autowired
//    private MlService mlService;
//
//    @Autowired
//    private AccountRepository accountRepository;
//
//    public Map<String,Object> getStatusFromModel(User user, LoanInfo loanInfo, EmploymentDetails employmentDetails){
//        double[] features = featureExtracter.extractFeaturesFromApplication(loanInfo,employmentDetails,user);
//        Map<String, Object> mlResponse = mlService.getPrediction(features);
//        Integer score = (Integer) mlResponse.get("score");
//        String declineReason = (String) mlResponse.get("declineReason");
//
//        if(score==-1) return Map.of("score", score,"status","Pending","declineReason","Pending");
//
//        if(score>700) return  Map.of("score", score,"status","Approved","declineReason",declineReason);
//        return  Map.of("score", score,"status","Declined","declineReason",declineReason);
//    }
//
//    public ResponseEntity<ApiResponse<String>> submitApplication(LoanApplication loanApplication) {
//        try {
//            SecurityContext context = SecurityContextHolder.getContext();
//            String accUsername = context.getAuthentication().getName();
//            Optional<Account> userAccount = accountRepository.findByUsername(accUsername);
//
//            User userPersonalDetails = loanApplication.getUser();
//            LoanInfo loanInfo = loanApplication.getLoanInfo();
//            EmploymentDetails employmentDetails = loanApplication.getEmploymentDetails();
//
//            loanInfo.setLoanApplicationDate(LocalDate.now());
//
//            if(userAccount.isPresent()) {
//                Long userId = userAccount.get().getUser().getId();
//                userPersonalDetails.setId(userId);
//            }
//
//            String userSsn = userPersonalDetails.getSsnNumber();
//            Optional<User> existingUserOpt = userRepository.findBySsnNumber(userSsn);
//
//
//
//            if (existingUserOpt.isPresent()) {
//                User existingUser = existingUserOpt.get();
//                Long existingEmploymentId = existingUser.getEmploymentDetails().getId();
//                employmentDetails.setId(existingEmploymentId);
//
//                existingUser.updateFrom(userPersonalDetails);
//                existingUser.setEmploymentDetails(employmentDetails);
//                employmentDetails.setUser(existingUser);
//
//                loanInfo.setUser(existingUser);
//                existingUser.getLoanInfos().add(loanInfo);
//
//                Map statusMap = getStatusFromModel(existingUser,loanInfo,employmentDetails);
//                existingUser.setScore((Integer)statusMap.get("score"));
//                loanInfo.setStatus((String) statusMap.get("status"));
//                loanInfo.setDeclineReason((String)statusMap.get("declineReason"));
//
//                userRepository.save(existingUser);
//            } else {
//                Map statusMap = (getStatusFromModel(userPersonalDetails,loanInfo,employmentDetails));
//                userPersonalDetails.setScore((Integer)statusMap.get("score"));
//                loanInfo.setStatus((String) statusMap.get("status"));
//                loanInfo.setDeclineReason((String)statusMap.get("declineReason"));
//
//                userPersonalDetails.setEmploymentDetails(employmentDetails);
//                employmentDetails.setUser(userPersonalDetails);
//
//                userPersonalDetails.setLoanInfos(List.of(loanInfo));
//                loanInfo.setUser(userPersonalDetails);
//
//                userRepository.save(userPersonalDetails);
//            }
//            return ResponseEntity.ok(new ApiResponse<>(true, "Application submitted successfully", null));
//
//        } catch (Exception e) {
//            System.out.println(e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ApiResponse<>(false, "Internal Server Error: Unable to submit application", null));
//        }
//    }
//
//    public ResponseEntity<ApiResponse<List<ApplicationSummary>>> getAllApplications() {
//        try {
//            List<ApplicationSummary> applicationList = loanInfoRepository.findAllApplicationSummary();
//            return ResponseEntity.ok(new ApiResponse<>(true, "Applications fetched successfully", applicationList));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ApiResponse<>(false, "Internal Server Error: Please try again later", null));
//        }
//    }
//
//    public ResponseEntity<ApiResponse<LoanApplicationDTO>> getApplicationById(Long id) {
//        try{
//            Optional<LoanInfo> loanInfoOpt = loanInfoRepository.findLoanInfoById(id);
//
//            if (loanInfoOpt.isPresent()) {
//                LoanInfo loanInfo = loanInfoOpt.get();
//                User user = loanInfo.getUser();
//                EmploymentDetails employmentDetails = user.getEmploymentDetails();
//
//                LoanInfoDTO loanInfoDTO = applicationInfoMapper.toLoanInfoDTO(loanInfo);
//                UserDTO userDTO = applicationInfoMapper.toUserDTO(user);
//                EmploymentDetailsDTO employmentDetailsDTO = applicationInfoMapper.toEmploymentDetailsDTO(employmentDetails);
//
//                LoanApplicationDTO loanApplicationDTO = new LoanApplicationDTO(userDTO, loanInfoDTO,employmentDetailsDTO);
//                return ResponseEntity.ok(new ApiResponse<>(true,"Application found",loanApplicationDTO));
//            }
//            else{
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false,"Application not found",null));
//            }
//        }
//        catch (Exception e){
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false,"Internal Server Error: Please try again later",null));
//        }
//    }
//
//
//}
