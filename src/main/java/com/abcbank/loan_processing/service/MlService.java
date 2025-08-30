package com.abcbank.loan_processing.service;
import com.abcbank.loan_processing.entity.CreditBureau;
import com.abcbank.loan_processing.dto.MLPredictionRequestDTO;
import com.abcbank.loan_processing.dto.MLPredictionResponseDTO;
import com.abcbank.loan_processing.entity.EmploymentDetails;
import com.abcbank.loan_processing.entity.LoanInfo;
import com.abcbank.loan_processing.entity.User;
import com.abcbank.loan_processing.repository.CreditBureauRepository;
import com.abcbank.loan_processing.util.Mapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

@Service
public class MlService {
    private static final Logger logger = LoggerFactory.getLogger(MlService.class);
    @Autowired
    private CreditBureauRepository creditBureauRepository;

    @Autowired
    private Mapper mapper;
    @Autowired
    private ObjectMapper objectMapper;
    private final String mlApiUrl = "Ml model API";

    public MLPredictionResponseDTO getStatusFromModel(User user, LoanInfo loanInfo, EmploymentDetails empDetails) throws JsonProcessingException {
        Optional<CreditBureau> creditBureauDataOpt = creditBureauRepository.findById(user.getSsnNumber());
        CreditBureau creditBureauData = null;

        if(creditBureauDataOpt.isPresent()){
            creditBureauData = creditBureauDataOpt.get();
        }
        MLPredictionRequestDTO req = mapper.toMlPredictionRequestDTO(loanInfo,empDetails,creditBureauData);

        return this.getRandomPrediction(req);
    }

    public MLPredictionResponseDTO getRandomPrediction(MLPredictionRequestDTO requestDto){
        Random random = new Random();
        int randomScore = 400 + random.nextInt(501); // 400 + (0 to 500) = 400 to 900

        // Create response with random score
        MLPredictionResponseDTO response = new MLPredictionResponseDTO();
        response.setScore(BigDecimal.valueOf(randomScore));

        // Set decision based on score ranges
        String decision;
        List<MLPredictionResponseDTO.DeclineReason> declineReasons = new ArrayList<>();
        MLPredictionResponseDTO.DeclineReason demoDR = new MLPredictionResponseDTO.DeclineReason("none","none","none");

        if (randomScore >= 750) {
            decision = "Approved";
            declineReasons = null;
        } else if (randomScore >= 650) {
            decision = "Approved";
            declineReasons = null;
        } else if (randomScore >= 550) {
            decision = "Under Review";
            declineReasons.add(demoDR);
        } else {
            decision = "Declined";
            declineReasons.add(demoDR);
        }

        response.setDecision(decision);
        response.setDeclineReasons(declineReasons);

        logger.info("Generated random credit score: {} with decision: {}", randomScore, decision);

        return response;
    }

    public MLPredictionResponseDTO getPrediction(MLPredictionRequestDTO requestDto) throws JsonProcessingException {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(4000);
        requestFactory.setReadTimeout(4000);
        RestTemplate timeoutRestTemplate = new RestTemplate(requestFactory);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<MLPredictionRequestDTO> requestEntity = new HttpEntity<>(requestDto, headers);
        try {
            ResponseEntity<MLPredictionResponseDTO> Response =
                    timeoutRestTemplate.postForEntity(
                            mlApiUrl,
                            requestEntity,
                            MLPredictionResponseDTO.class
                    );
            if (Response.getStatusCode().is2xxSuccessful() && Response.getBody() != null) {
                logger.info("ML Api response received successfully{}", Response);
                return Response.getBody();
            }
        } catch (ResourceAccessException e) {
            logger.info("ML API timeout (over 4 sec), returning default response");
        } catch (Exception e) {
            logger.error("Error calling ML API: {}", e.getMessage());
        }

        // Default response if timeout or error
        MLPredictionResponseDTO defaultResponse = new MLPredictionResponseDTO();
        defaultResponse.setScore(BigDecimal.valueOf(-1));
        defaultResponse.setDeclineReasons(null);
        defaultResponse.setDecision("Pending");
        return defaultResponse;
    }
}
