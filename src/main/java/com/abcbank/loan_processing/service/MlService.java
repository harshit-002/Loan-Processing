package com.abcbank.loan_processing.service;
import com.abcbank.loan_processing.dto.MLPredictionRequestDTO;
import com.abcbank.loan_processing.dto.MLPredictionResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class MlService {
    private static final Logger logger = LoggerFactory.getLogger(MlService.class);

    private final String mlApiUrl = "http://localhost:8080/api/public/predict";

    public MLPredictionResponseDTO getPrediction(MLPredictionRequestDTO requestDto) {
        // Create RestTemplate with 4-second timeout
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(4000);
        requestFactory.setReadTimeout(4000);
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
