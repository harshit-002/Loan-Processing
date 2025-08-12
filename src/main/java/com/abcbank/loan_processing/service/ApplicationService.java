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
import com.abcbank.loan_processing.util.FeatureExtracter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private FeatureExtracter featureExtracter;

    @Autowired
    private MlService mlService;
    
    public Map<String,Object> getStatusFromModel(User user, LoanInfo loanInfo, EmploymentDetails employmentDetails){
        double[] features = featureExtracter.extractFeaturesFromApplication(loanInfo,employmentDetails,user);
        Map<String, Object> mlResponse = mlService.getPrediction(features);
        Integer score = (Integer) mlResponse.get("score");
        String declineReason = (String) mlResponse.get("declineReason");

        if(score==-1) return Map.of("score", score,"status","Pending","declineReason","Pending");

        if(score>700) return  Map.of("score", score,"status","Approved","declineReason",declineReason);
        return  Map.of("score", score,"status","Declined","declineReason",declineReason);
    }

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

                Map statusMap = getStatusFromModel(existingUser,loanInfo,employmentDetails);
                existingUser.setScore((Integer)statusMap.get("score"));
                loanInfo.setStatus((String) statusMap.get("status"));
                loanInfo.setDeclineReason((String)statusMap.get("declineReason"));

                userRepository.save(existingUser);
            } else {
                Map statusMap = (getStatusFromModel(user,loanInfo,employmentDetails));
                user.setScore((Integer)statusMap.get("score"));
                loanInfo.setStatus((String) statusMap.get("status"));
                loanInfo.setDeclineReason((String)statusMap.get("declineReason"));

                user.setEmploymentDetails(employmentDetails);
                employmentDetails.setUser(user);

                user.setLoanInfos(List.of(loanInfo));
                loanInfo.setUser(user);

                userRepository.save(user);
            }
            return ResponseEntity.ok(new ApiResponse<>(true, "Application submitted successfully", null));

        } catch (Exception e) {
            System.out.println(e);
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
