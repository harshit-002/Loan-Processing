package com.abcbank.loan_processing.service;

import com.abcbank.loan_processing.dto.MLPredictionResponseDTO;
import com.abcbank.loan_processing.entity.EmploymentDetails;
import com.abcbank.loan_processing.entity.LoanInfo;
import com.abcbank.loan_processing.entity.User;
import com.abcbank.loan_processing.repository.LoanInfoRepository;
import com.abcbank.loan_processing.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LoanApplicationRetryServiceTest {

    @InjectMocks
    private LoanApplicationRetryService retryService;

    @Mock
    private LoanInfoRepository loanInfoRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MlService mlService;
    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRetryPendingApplications_RetriesAndUpdates() throws Exception {
        LoanInfo loan = new LoanInfo();
        loan.setStatus("Pending");
        loan.setRetryCount(1);
        User user = new User();
        EmploymentDetails emp = new EmploymentDetails();
        loan.setUser(user);
        user.setEmploymentDetails(emp);

        MLPredictionResponseDTO mlResponse = new MLPredictionResponseDTO();
        mlResponse.setScore(BigDecimal.valueOf(100.0));
        mlResponse.setDecision("APPROVED");
        mlResponse.setDeclineReasons(null);

        when(loanInfoRepository.findByStatus("Pending")).thenReturn(List.of(loan));
        when(mlService.getStatusFromModel(user, loan, emp)).thenReturn(mlResponse);

        retryService.retryPendingApplications();

        verify(userRepository, times(1)).save(user);
        verify(loanInfoRepository, times(1)).save(loan);
        assertEquals("APPROVED", loan.getStatus());
        assertEquals(2, loan.getRetryCount());
    }

    @Test
    void testRetryPendingApplications_MaxRetryCount() throws Exception {
        LoanInfo loan = new LoanInfo();
        loan.setStatus("Pending");
        loan.setRetryCount(4); // Exceeds MAX_RETRY_COUNT
        when(loanInfoRepository.findByStatus("Pending")).thenReturn(List.of(loan));

        retryService.retryPendingApplications();

        verify(mlService, never()).getStatusFromModel(any(), any(), any());
        verify(userRepository, never()).save(any());
        verify(loanInfoRepository, never()).save(loan);
    }

    @Test
    void testRetryPendingApplications_ExceptionHandling() throws Exception {
        LoanInfo loan = new LoanInfo();
        loan.setStatus("Pending");
        loan.setRetryCount(1);
        User user = new User();
        EmploymentDetails emp = new EmploymentDetails();
        loan.setUser(user);
        user.setEmploymentDetails(emp);

        when(loanInfoRepository.findByStatus("Pending")).thenReturn(List.of(loan));
        when(mlService.getStatusFromModel(any(), any(), any())).thenThrow(new RuntimeException("ML error"));

        retryService.retryPendingApplications();

        verify(loanInfoRepository, times(1)).save(loan);
        assertEquals(2, loan.getRetryCount());
    }
}