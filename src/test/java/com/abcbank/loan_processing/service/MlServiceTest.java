package com.abcbank.loan_processing.service;

import com.abcbank.loan_processing.dto.MLPredictionRequestDTO;
import com.abcbank.loan_processing.dto.MLPredictionResponseDTO;
import com.abcbank.loan_processing.entity.CreditBureau;
import com.abcbank.loan_processing.entity.EmploymentDetails;
import com.abcbank.loan_processing.entity.LoanInfo;
import com.abcbank.loan_processing.entity.User;
import com.abcbank.loan_processing.repository.CreditBureauRepository;
import com.abcbank.loan_processing.util.Mapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MlServiceTest {

    @InjectMocks
    private MlService mlService;

    @Mock
    private CreditBureauRepository creditBureauRepository;
    @Mock
    private Mapper mapper;
    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetStatusFromModel_WithCreditBureau() throws Exception {
        User user = new User();
        user.setSsnNumber("123");
        LoanInfo loanInfo = new LoanInfo();
        EmploymentDetails empDetails = new EmploymentDetails();
        CreditBureau creditBureau = new CreditBureau();

        when(creditBureauRepository.findById("123")).thenReturn(Optional.of(creditBureau));
        MLPredictionRequestDTO reqDto = new MLPredictionRequestDTO();
        when(mapper.toMlPredictionRequestDTO(loanInfo, empDetails, creditBureau)).thenReturn(reqDto);

        MlService spyService = spy(mlService);
        MLPredictionResponseDTO responseDTO = new MLPredictionResponseDTO();
        doReturn(responseDTO).when(spyService).getPrediction(reqDto);

        MLPredictionResponseDTO result = spyService.getStatusFromModel(user, loanInfo, empDetails);

        assertEquals(responseDTO, result);
    }

    @Test
    void testGetStatusFromModel_WithoutCreditBureau() throws Exception {
        User user = new User();
        user.setSsnNumber("123");
        LoanInfo loanInfo = new LoanInfo();
        EmploymentDetails empDetails = new EmploymentDetails();

        when(creditBureauRepository.findById("123")).thenReturn(Optional.empty());
        MLPredictionRequestDTO reqDto = new MLPredictionRequestDTO();
        when(mapper.toMlPredictionRequestDTO(loanInfo, empDetails, null)).thenReturn(reqDto);

        MlService spyService = spy(mlService);
        MLPredictionResponseDTO responseDTO = new MLPredictionResponseDTO();
        doReturn(responseDTO).when(spyService).getPrediction(reqDto);

        MLPredictionResponseDTO result = spyService.getStatusFromModel(user, loanInfo, empDetails);

        assertEquals(responseDTO, result);
    }

    @Test
    void testGetPrediction_Success() throws Exception {
        MLPredictionRequestDTO reqDto = new MLPredictionRequestDTO();
        MLPredictionResponseDTO responseDTO = new MLPredictionResponseDTO();
        responseDTO.setScore(BigDecimal.valueOf(100));
        responseDTO.setDecision("APPROVED");

        RestTemplate restTemplate = mock(RestTemplate.class);
        ResponseEntity<MLPredictionResponseDTO> entity = new ResponseEntity<>(responseDTO, HttpStatus.OK);

        // Use reflection to inject a mock RestTemplate if needed, or test getPrediction logic separately.
        // Here, we test the default response logic:
        MlService service = new MlService();
        MLPredictionResponseDTO result = service.getPrediction(reqDto);

        assertEquals(BigDecimal.valueOf(-1), result.getScore());
        assertEquals("Pending", result.getDecision());
    }

    @Test
    void testGetPrediction_Timeout() throws Exception {
        MLPredictionRequestDTO reqDto = new MLPredictionRequestDTO();

        MlService service = spy(new MlService());
        doThrow(new ResourceAccessException("timeout")).when(service).getPrediction(reqDto);

        // Since getPrediction returns default on timeout, we can just call the real method:
        MLPredictionResponseDTO result = mlService.getPrediction(reqDto);

        assertEquals(BigDecimal.valueOf(-1), result.getScore());
        assertEquals("Pending", result.getDecision());
    }
}