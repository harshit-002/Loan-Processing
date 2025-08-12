package com.abcbank.loan_processing.service;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class MlService {

    private final String mlApiUrl = "http://localhost:8080/api/predict";

    public Map<String, Object> getPrediction(double[] features) {
        // Create RestTemplate with 4-second timeout
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(4000); // 4 sec connect timeout
        requestFactory.setReadTimeout(4000);    // 4 sec read timeout
        RestTemplate timeoutRestTemplate = new RestTemplate(requestFactory);

        Map<String, Object> requestBody = Map.of("features", features);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response =
                    timeoutRestTemplate.postForEntity(mlApiUrl, requestEntity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
        } catch (ResourceAccessException e) {
            System.out.println("ML API timeout (over 4 sec), returning default score -1");
        } catch (Exception e) {
            System.out.println("Error calling ML API: " + e.getMessage());
        }

        // Default response if timeout or error
        return Map.of("score", -1,"declineReason","none");
    }
}
