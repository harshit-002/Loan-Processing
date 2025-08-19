package com.abcbank.loan_processing.service;

import com.abcbank.loan_processing.dto.*;
import com.abcbank.loan_processing.entity.*;
import com.abcbank.loan_processing.repository.*;
import com.abcbank.loan_processing.util.Mapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApplicationServiceTest {

    @InjectMocks
    private ApplicationService applicationService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private EmployementDetailsRepository employementDetailsRepository;
    @Mock
    private LoanInfoRepository loanInfoRepository;
    @Mock
    private Mapper mapper;
    @Mock
    private MlService mlService;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
    }

    @Test
    void testSubmitApplication_Success() throws Exception {
        // Setup mocks and input
        LoanApplication loanApplication = new LoanApplication();
        Account account = new Account();
        account.setSsnNumber("123");
        User user = new User();
        user.setSsnNumber("123");
        account.setUser(user);

        // System.out.println("\n\n\n\n\n\n\n\n\n||||||||||\n\n\n\n\n\n\n\n\n\n\n\n\n\n");

        when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(account));
        loanApplication.setUser(user);

        MLPredictionResponseDTO mlResponse = new MLPredictionResponseDTO();
        mlResponse.setScore(BigDecimal.valueOf(100.0));
        mlResponse.setDecision("APPROVED");
        mlResponse.setDeclineReasons(null);

        when(mlService.getStatusFromModel(any(), any(), any())).thenReturn(mlResponse);

        ResponseEntity<ApiResponse<String>> response = applicationService.submitApplication(loanApplication);

        assertTrue(response.getBody().isSuccess());
        assertEquals("Application submitted successfully", response.getBody().getMessage());
    }

    @Test
    void testGetAllApplications_Success() {
        Account account = new Account();
        User user = new User();
        user.setId(1L);
        account.setUser(user);

        //  System.out.println("\n\n\n\n\n\n\n\n\n||||||||||\n\n\n\n\n\n\n\n\n\n\n\n\n\n");

        when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(account));
        when(loanInfoRepository.findAllApplicationSummary(1L)).thenReturn(new ArrayList<>());

        ResponseEntity<ApiResponse<List<ApplicationSummaryDTO>>> response = applicationService.getAllApplications();

        assertTrue(response.getBody().isSuccess());
        assertEquals("Applications fetched successfully", response.getBody().getMessage());
    }

    @Test
    void testGetApplicationById_Success() {
        Account account = new Account();
        User user = new User();
        user.setId(1L);
        account.setUser(user);

        LoanInfo loanInfo = new LoanInfo();
        loanInfo.setId(2L);
        loanInfo.setUser(user);

        user.setLoanInfos(List.of(loanInfo));

        //  System.out.println("\n\n\n\n\n\n\n\n\n||||||||||\n\n\n\n\n\n\n\n\n\n\n\n\n\n");


        when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(account));
        when(loanInfoRepository.findLoanInfoById(2L)).thenReturn(Optional.of(loanInfo));
        when(mapper.toLoanInfoDTO(any())).thenReturn(new LoanInfoDTO());
        when(mapper.toUserDTO(any())).thenReturn(new UserDTO());
        when(mapper.toEmploymentDetailsDTO(any())).thenReturn(new EmploymentDetailsDTO());

        ResponseEntity<ApiResponse<LoanApplicationDTO>> response = applicationService.getApplicationById(2L);

        assertTrue(response.getBody().isSuccess());
        assertEquals("Application found", response.getBody().getMessage());
    }
}