package com.abcbank.loan_processing.service;
import com.abcbank.loan_processing.entity.CreditBureau;
import com.abcbank.loan_processing.dto.MLPredictionRequestDTO;
import com.abcbank.loan_processing.dto.MLPredictionResponseDTO;
import com.abcbank.loan_processing.entity.EmploymentDetails;
import com.abcbank.loan_processing.entity.LoanInfo;
import com.abcbank.loan_processing.entity.User;
import com.abcbank.loan_processing.repository.CreditBureauRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class MlService {
    private static final Logger logger = LoggerFactory.getLogger(MlService.class);
    @Autowired
    private CreditBureauRepository creditBureauRepository;

    private final String mlApiUrl = "http://localhost:8080/api/public/predict";

    public MLPredictionResponseDTO getStatusFromModel(User user, LoanInfo loanInfo, EmploymentDetails empDetails){
        Optional<CreditBureau> creditBureauDataOpt = creditBureauRepository.findById(user.getSsnNumber());
        CreditBureau creditBureauData = null;

        if(creditBureauDataOpt.isPresent()){
            creditBureauData = creditBureauDataOpt.get();
        }
        MLPredictionRequestDTO req = new MLPredictionRequestDTO(
                user.getSsnNumber(),loanInfo.getLoanAmount(),loanInfo.getLoanPurpose(),loanInfo.getDescription(),empDetails.getExperienceYears(),empDetails.getAnnualSalary(),creditBureauData);
        MLPredictionResponseDTO mlApiResponse = this.getPrediction(req);

        return mlApiResponse;
    }
    public MLPredictionResponseDTO getPrediction(MLPredictionRequestDTO requestDto) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(2000);
        requestFactory.setReadTimeout(2000);
        RestTemplate timeoutRestTemplate = new RestTemplate(requestFactory);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<MLPredictionRequestDTO> requestEntity = new HttpEntity<>(requestDto, headers);

        try {
            ResponseEntity<MLPredictionResponseDTO> response =
                    timeoutRestTemplate.postForEntity(
                            mlApiUrl,
                            requestEntity,
                            MLPredictionResponseDTO.class
                    );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.info("ML Api response recieved successfully");
                return response.getBody();
            }
        } catch (ResourceAccessException e) {
            logger.info("ML API timeout (over 4 sec), returning default response");
        } catch (Exception e) {
           logger.error("Error calling ML API: " + e.getMessage());
        }

        // Default response if timeout or error
        MLPredictionResponseDTO defaultResponse = new MLPredictionResponseDTO();
        defaultResponse.setScore(-1);
        defaultResponse.setDeclineReason("none");
        defaultResponse.setStatus("Pending");
        return defaultResponse;
    }
}
