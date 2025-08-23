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
import java.util.Optional;

@Service
public class MlService {
    private static final Logger logger = LoggerFactory.getLogger(MlService.class);
    @Autowired
    private CreditBureauRepository creditBureauRepository;

    @Autowired
    private Mapper mapper;
    @Autowired
    private ObjectMapper objectMapper;
    private final String mlApiUrl = "http://localhost:5000/api/predict";

    public MLPredictionResponseDTO getStatusFromModel(User user, LoanInfo loanInfo, EmploymentDetails empDetails) throws JsonProcessingException {
        Optional<CreditBureau> creditBureauDataOpt = creditBureauRepository.findById(user.getSsnNumber());
        CreditBureau creditBureauData = null;

        if(creditBureauDataOpt.isPresent()){
            creditBureauData = creditBureauDataOpt.get();
        }
        MLPredictionRequestDTO req = mapper.toMlPredictionRequestDTO(loanInfo,empDetails,creditBureauData);

        return this.getPrediction(req);
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
