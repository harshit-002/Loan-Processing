package com.abcbank.loan_processing.service;

import com.abcbank.loan_processing.dto.MLPredictionRequestDTO;
import com.abcbank.loan_processing.dto.MLPredictionResponseDTO;
import com.abcbank.loan_processing.entity.EmploymentDetails;
import com.abcbank.loan_processing.entity.LoanInfo;
import com.abcbank.loan_processing.entity.User;
import com.abcbank.loan_processing.repository.LoanInfoRepository;
import com.abcbank.loan_processing.repository.UserRepository;
import com.abcbank.loan_processing.util.FeatureExtracter;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class LoanApplicationRetryService {
    private static final Logger logger = LoggerFactory.getLogger(LoanApplicationRetryService.class);

    private static final int MAX_RETRY_COUNT= 3;

    @Autowired
    private LoanInfoRepository loanInfoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FeatureExtracter featureExtracter;

    @Autowired
    private MlService mlService;

    public MLPredictionResponseDTO getStatusFromModel(User user, LoanInfo loanInfo){
        EmploymentDetails empDetails = user.getEmploymentDetails();
        MLPredictionRequestDTO req = new MLPredictionRequestDTO(
                user.getSsnNumber(),loanInfo.getLoanAmount(),loanInfo.getLoanPurpose(),loanInfo.getDescription(),empDetails.getExperienceYears(),empDetails.getAnnualSalary());
        MLPredictionResponseDTO mlApiResponse = mlService.getPrediction(req);
        return mlApiResponse;
    }
    @Transactional
//    @Scheduled(fixedRate = 60000)
    public void retryPendingApplications(){
        logger.info("Retyring pending applications, Time: ", LocalDate.now());
        List<LoanInfo> pendingList = loanInfoRepository.findByStatus("Pending");

        for(LoanInfo loan : pendingList){
            if(loan.getRetryCount() > MAX_RETRY_COUNT){
                continue;
            }
            try{
                User currUserDetails = loan.getUser();
                EmploymentDetails currUserEmpDetails = currUserDetails.getEmploymentDetails();

                MLPredictionResponseDTO MlApiResponse = getStatusFromModel(currUserDetails,loan);

                currUserDetails.setScore((Integer)MlApiResponse.getScore());
                loan.setStatus((String) MlApiResponse.getStatus());
                loan.setDeclineReason((String)MlApiResponse.getDeclineReason());

                loan.setRetryCount(loan.getRetryCount()+1);

                userRepository.save(currUserDetails);
                loanInfoRepository.save(loan);
            }
            catch (Exception e) {
                System.out.println("exception occured: "+e.getMessage());
                loan.setRetryCount(loan.getRetryCount() + 1);
                loanInfoRepository.save(loan);
            }
        }
    }
}
