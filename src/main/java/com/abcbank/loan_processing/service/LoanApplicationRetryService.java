package com.abcbank.loan_processing.service;

import com.abcbank.loan_processing.dto.MLPredictionResponseDTO;
import com.abcbank.loan_processing.entity.EmploymentDetails;
import com.abcbank.loan_processing.entity.LoanInfo;
import com.abcbank.loan_processing.entity.User;
import com.abcbank.loan_processing.repository.LoanInfoRepository;
import com.abcbank.loan_processing.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LoanApplicationRetryService {
    private static final Logger logger = LoggerFactory.getLogger(LoanApplicationRetryService.class);

    private static final int MAX_RETRY_COUNT= 3;

    @Autowired
    private LoanInfoRepository loanInfoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MlService mlService;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    @Scheduled(fixedRate = 600000) // retry every 60 min
    public void retryPendingApplications(){
        logger.info("Re-tyring pending applications, Time: {}", LocalDateTime.now());
        List<LoanInfo> pendingList = loanInfoRepository.findByStatus("Pending");

        for(LoanInfo loan : pendingList){
            if(loan.getRetryCount() > MAX_RETRY_COUNT){
                continue;
            }
            try{
                User currUserDetails = loan.getUser();
                EmploymentDetails currUserEmpDetails = currUserDetails.getEmploymentDetails();

                MLPredictionResponseDTO MlApiResponse = mlService.getStatusFromModel(currUserDetails,loan,currUserEmpDetails);

                currUserDetails.setScore(MlApiResponse.getScore().intValue());
                loan.setStatus(MlApiResponse.getDecision());

                String declineReason = "None";
                if(MlApiResponse.getDeclineReasons()!=null){
                    declineReason = objectMapper.writeValueAsString(MlApiResponse.getDeclineReasons());
                }
                loan.setDeclineReason(declineReason);
                loan.setRetryCount(loan.getRetryCount()+1);

                userRepository.save(currUserDetails);
                loanInfoRepository.save(loan);
            }
            catch (Exception e) {
                System.out.println("exception occurred: "+e.getMessage());
                loan.setRetryCount(loan.getRetryCount() + 1);
                loanInfoRepository.save(loan);
            }
        }
    }
}
