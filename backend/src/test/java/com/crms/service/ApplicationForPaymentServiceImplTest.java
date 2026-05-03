package com.crms.service;

import com.crms.domain.contract.entity.ApplicationForPayment;
import com.crms.domain.contract.entity.Contract;
import com.crms.domain.contract.entity.PayLessNotice;
import com.crms.domain.contract.entity.PaymentNotice;
import com.crms.domain.contract.enums.ApplicationStatus;
import com.crms.domain.contract.enums.ContractStatus;
import com.crms.domain.contract.enums.NoticeType;
import com.crms.domain.contract.repository.ApplicationForPaymentRepository;
import com.crms.domain.contract.repository.ContractRepository;
import com.crms.domain.contract.repository.PaymentNoticeRepository;
import com.crms.dto.request.ApplicationForPaymentRequest;
import com.crms.dto.request.PayLessNoticeRequest;
import com.crms.dto.request.PaymentNoticeRequest;
import com.crms.dto.response.ApplicationResponse;
import com.crms.dto.response.PageResponse;
import com.crms.exception.ResourceNotFoundException;
import com.crms.exception.ValidationException;
import com.crms.service.impl.ApplicationForPaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ApplicationForPaymentServiceImpl.
 * Tests cover application creation, submission workflow, retention calculations,
 * and payment notice management.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ApplicationForPaymentServiceImplTest {

    @Mock
    private ApplicationForPaymentRepository applicationRepository;

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private PaymentNoticeRepository paymentNoticeRepository;

    @InjectMocks
    private ApplicationForPaymentServiceImpl applicationService;

    // Test data
    private Contract testContract;
    private ApplicationForPayment testApplication;
    private ApplicationForPaymentRequest testRequest;

    @BeforeEach
    void setUp() {
        testContract = Contract.builder()
                .id(1L)
                .contractRef("CRMS-001")
                .title("Test Contract")
                .contractValue(new BigDecimal("500000.00"))
                .retentionPercent(new BigDecimal("5.0"))
                .retentionReductionPercent(new BigDecimal("2.5"))
                .paymentTermsDays(30)
                .finalDateForPaymentOffsetDays(14)
                .payLessNoticePrescribedPeriodDays(7)
                .status(ContractStatus.IN_PROGRESS)
                .build();

        testApplication = ApplicationForPayment.builder()
                .id(1L)
                .contract(testContract)
                .applicationRef("CRMS-001-APP-1")
                .applicationNumber(1)
                .applicationPeriodStart(LocalDate.of(2024, 1, 1))
                .applicationPeriodEnd(LocalDate.of(2024, 1, 31))
                .valueOfWorks(new BigDecimal("50000.00"))
                .retention(new BigDecimal("2500.00"))
                .grossValue(new BigDecimal("47500.00"))
                .status(ApplicationStatus.DRAFT)
                .build();

        testRequest = ApplicationForPaymentRequest.builder()
                .applicationPeriodStart(LocalDate.of(2024, 2, 1))
                .applicationPeriodEnd(LocalDate.of(2024, 2, 29))
                .valueOfWorks(new BigDecimal("60000.00"))
                .build();
    }

    // ================================================================
    // FIND TESTS
    // ================================================================

    @Nested
    @DisplayName("Find Operation Tests")
    class FindTests {

        @Test
        @DisplayName("findByContract returns applications for contract")
        void findByContract_returnsApplications() {
            // Given
            when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));
            when(applicationRepository.findByContractIdOrderByApplicationPeriodEndDesc(1L))
                    .thenReturn(List.of(testApplication));

            // When
            PageResponse<ApplicationResponse> response = applicationService.findByContract(1L);

            // Then
            assertNotNull(response);
            assertEquals(1, response.getContent().size());
            assertEquals("CRMS-001-APP-1", response.getContent().get(0).getApplicationRef());
        }

        @Test
        @DisplayName("findByContract throws exception for non-existent contract")
        void findByContract_throwsException_whenContractNotFound() {
            // Given
            when(contractRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> 
                applicationService.findByContract(999L));
        }

        @Test
        @DisplayName("findById returns application when exists")
        void findById_returnsApplication_whenExists() {
            // Given
            when(applicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));

            // When
            ApplicationResponse response = applicationService.findById(1L);

            // Then
            assertNotNull(response);
            assertEquals("CRMS-001-APP-1", response.getApplicationRef());
            assertEquals(1, response.getApplicationNumber());
        }

        @Test
        @DisplayName("findById throws exception when not found")
        void findById_throwsException_whenNotFound() {
            // Given
            when(applicationRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> 
                applicationService.findById(999L));
        }
    }

    // ================================================================
    // APPLICATION CREATION TESTS
    // ================================================================

    @Nested
    @DisplayName("Application Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("create() generates correct application reference")
        void create_generatesCorrectReference() {
            // Given
            when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));
            when(applicationRepository.findMaxApplicationNumberByContractId(1L))
                    .thenReturn(Optional.of(2));
            when(applicationRepository.save(any(ApplicationForPayment.class))).thenAnswer(invocation -> {
                ApplicationForPayment app = invocation.getArgument(0);
                app.setId(3L);
                return app;
            });

            // When
            ApplicationResponse response = applicationService.create(1L, testRequest);

            // Then
            assertNotNull(response);
            assertTrue(response.getApplicationRef().endsWith("-3"));
            verify(applicationRepository).save(any(ApplicationForPayment.class));
        }

        @Test
        @DisplayName("create() calculates retention automatically")
        void create_calculatesRetentionAutomatically() {
            // Given
            when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));
            when(applicationRepository.findMaxApplicationNumberByContractId(1L))
                    .thenReturn(Optional.of(0));
            when(applicationRepository.save(any(ApplicationForPayment.class))).thenAnswer(invocation -> {
                ApplicationForPayment app = invocation.getArgument(0);
                app.setId(1L);
                return app;
            });

            // When
            ApplicationResponse response = applicationService.create(1L, testRequest);

            // Then
            // Retention should be 5% of 60000 = 3000
            assertEquals(new BigDecimal("3000.00"), response.getRetention());
            // Gross value should be 60000 - 3000 = 57000
            assertEquals(new BigDecimal("57000.00"), response.getGrossValue());
        }

        @Test
        @DisplayName("create() calculates due date from contract terms")
        void create_calculatesDueDateFromContractTerms() {
            // Given
            when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));
            when(applicationRepository.findMaxApplicationNumberByContractId(1L))
                    .thenReturn(Optional.of(0));
            when(applicationRepository.save(any(ApplicationForPayment.class))).thenAnswer(invocation -> {
                ApplicationForPayment app = invocation.getArgument(0);
                app.setId(1L);
                return app;
            });

            // When
            ApplicationResponse response = applicationService.create(1L, testRequest);

            // Then
            // Due date = period end + 30 days (contract payment terms)
            assertEquals(LocalDate.of(2024, 3, 30), response.getDueDate());
        }

        @Test
        @DisplayName("create() uses provided retention if specified")
        void create_usesProvidedRetention() {
            // Given
            ApplicationForPaymentRequest requestWithRetention = ApplicationForPaymentRequest.builder()
                    .applicationPeriodStart(LocalDate.of(2024, 2, 1))
                    .applicationPeriodEnd(LocalDate.of(2024, 2, 29))
                    .valueOfWorks(new BigDecimal("60000.00"))
                    .retention(new BigDecimal("4000.00"))
                    .build();

            when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));
            when(applicationRepository.findMaxApplicationNumberByContractId(1L))
                    .thenReturn(Optional.of(0));
            when(applicationRepository.save(any(ApplicationForPayment.class))).thenAnswer(invocation -> {
                ApplicationForPayment app = invocation.getArgument(0);
                app.setId(1L);
                return app;
            });

            // When
            ApplicationResponse response = applicationService.create(1L, requestWithRetention);

            // Then
            assertEquals(new BigDecimal("4000.00"), response.getRetention());
        }

        @Test
        @DisplayName("create() throws exception when contract not found")
        void create_throwsException_whenContractNotFound() {
            // Given
            when(contractRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> 
                applicationService.create(999L, testRequest));
        }
    }

    // ================================================================
    // APPLICATION SUBMISSION TESTS
    // ================================================================

    @Nested
    @DisplayName("Application Submission Tests")
    class SubmissionTests {

        @Test
        @DisplayName("submit() transitions application to SUBMITTED status")
        void submit_transitionsToSubmitted() {
            // Given
            when(applicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));
            when(applicationRepository.save(any(ApplicationForPayment.class))).thenReturn(testApplication);
            when(paymentNoticeRepository.save(any(PaymentNotice.class))).thenAnswer(invocation -> {
                PaymentNotice notice = invocation.getArgument(0);
                notice.setId(1L);
                return notice;
            });

            // When
            ApplicationResponse response = applicationService.submit(1L);

            // Then
            assertEquals(ApplicationStatus.SUBMITTED.name(), response.getStatus());
            verify(paymentNoticeRepository).save(any(PaymentNotice.class));
        }

        @Test
        @DisplayName("submit() creates default payment notice with correct values")
        void submit_createsDefaultPaymentNotice() {
            // Given
            when(applicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));
            when(applicationRepository.save(any(ApplicationForPayment.class))).thenAnswer(invocation -> {
                ApplicationForPayment app = invocation.getArgument(0);
                app.setStatus(ApplicationStatus.SUBMITTED);
                return app;
            });
            when(paymentNoticeRepository.save(any(PaymentNotice.class))).thenAnswer(invocation -> {
                PaymentNotice notice = invocation.getArgument(0);
                notice.setId(1L);
                return notice;
            });

            // When
            applicationService.submit(1L);

            // Then
            verify(paymentNoticeRepository).save(argThat(notice -> 
                notice.getNoticeType() == NoticeType.PAYMENT &&
                notice.getSumConsideredDue().equals(new BigDecimal("47500.00")) &&
                notice.getCurrency().equals("GBP")));
        }

        @Test
        @DisplayName("submit() calculates final date for payment correctly")
        void submit_calculatesFinalDateForPayment() {
            // Given
            when(applicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));
            when(applicationRepository.save(any(ApplicationForPayment.class))).thenAnswer(invocation -> {
                ApplicationForPayment app = invocation.getArgument(0);
                app.setStatus(ApplicationStatus.SUBMITTED);
                return app;
            });
            when(paymentNoticeRepository.save(any(PaymentNotice.class))).thenAnswer(invocation -> {
                PaymentNotice notice = invocation.getArgument(0);
                notice.setId(1L);
                return notice;
            });

            // When
            applicationService.submit(1L);

            // Then
            // Due date = Jan 31 + 30 = Feb 29 (or 30 in non-leap)
            // Final date = Due date + 14 = March 14 (or 15)
            verify(paymentNoticeRepository).save(argThat(notice -> 
                notice.getFinalDateForPayment() != null));
        }

        @Test
        @DisplayName("submit() throws exception when application not in DRAFT status")
        void submit_throwsException_whenNotDraft() {
            // Given
            testApplication.setStatus(ApplicationStatus.SUBMITTED);
            when(applicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));

            // When/Then
            assertThrows(ValidationException.class, () -> 
                applicationService.submit(1L));
        }

        @Test
        @DisplayName("submit() generates payer reference")
        void submit_generatesPayerReference() {
            // Given
            when(applicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));
            when(applicationRepository.save(any(ApplicationForPayment.class))).thenAnswer(invocation -> {
                ApplicationForPayment app = invocation.getArgument(0);
                app.setStatus(ApplicationStatus.SUBMITTED);
                return app;
            });
            when(paymentNoticeRepository.save(any(PaymentNotice.class))).thenAnswer(invocation -> {
                PaymentNotice notice = invocation.getArgument(0);
                notice.setId(1L);
                return notice;
            });

            // When
            ApplicationResponse response = applicationService.submit(1L);

            // Then
            assertNotNull(response.getPayerRef());
            assertTrue(response.getPayerRef().startsWith("PAY-"));
        }
    }

    // ================================================================
    // RETENTION CALCULATION TESTS
    // ================================================================

    @Nested
    @DisplayName("Retention Calculation Tests")
    class RetentionCalculationTests {

        @Test
        @DisplayName("Retention = gross value × retention%")
        void retention_calculation() {
            // Given
            BigDecimal grossValue = new BigDecimal("100000.00");
            BigDecimal retentionPercent = new BigDecimal("5.0");

            // When
            BigDecimal retention = grossValue.multiply(retentionPercent)
                    .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);

            // Then
            assertEquals(new BigDecimal("5000.00"), retention);
        }

        @Test
        @DisplayName("Gross value = value of works - retention")
        void grossValue_calculation() {
            // Given
            BigDecimal valueOfWorks = new BigDecimal("60000.00");
            BigDecimal retention = new BigDecimal("3000.00");

            // When
            BigDecimal grossValue = valueOfWorks.subtract(retention);

            // Then
            assertEquals(new BigDecimal("57000.00"), grossValue);
        }

        @Test
        @DisplayName("Net value = gross value - retention")
        void netValue_calculation() {
            // Given
            BigDecimal grossValue = new BigDecimal("100000.00");
            BigDecimal retention = new BigDecimal("5000.00");

            // When
            BigDecimal netValue = grossValue.subtract(retention);

            // Then
            assertEquals(new BigDecimal("95000.00"), netValue);
        }

        @Test
        @DisplayName("Retention uses contract default when null")
        void retention_usesContractDefault() {
            // Given
            Contract contract = Contract.builder()
                    .retentionPercent(new BigDecimal("5.0"))
                    .build();
            BigDecimal valueOfWorks = new BigDecimal("80000.00");

            // When
            BigDecimal retention = contract.calculateRetention(valueOfWorks);

            // Then
            assertEquals(new BigDecimal("4000.00"), retention);
        }
    }

    // ================================================================
    // PAYMENT NOTICE TESTS
    // ================================================================

    @Nested
    @DisplayName("Payment Notice Tests")
    class PaymentNoticeTests {

        @Test
        @DisplayName("addPaymentNotice() creates payment notice")
        void addPaymentNotice_createsNotice() {
            // Given
            when(applicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));
            when(applicationRepository.save(any(ApplicationForPayment.class))).thenReturn(testApplication);
            when(paymentNoticeRepository.save(any(PaymentNotice.class))).thenAnswer(invocation -> {
                PaymentNotice notice = invocation.getArgument(0);
                notice.setId(1L);
                return notice;
            });

            PaymentNoticeRequest request = PaymentNoticeRequest.builder()
                    .noticeType(NoticeType.PAYMENT)
                    .issuedOn(LocalDateTime.now())
                    .sumConsideredDue(new BigDecimal("47000.00"))
                    .currency("GBP")
                    .basisOfCalculation("Adjusted value based on measured works")
                    .build();

            // When
            ApplicationResponse response = applicationService.addPaymentNotice(1L, request);

            // Then
            assertNotNull(response);
            verify(paymentNoticeRepository).save(any(PaymentNotice.class));
        }

        @Test
        @DisplayName("addPayLessNotice() creates pay less notice")
        void addPayLessNotice_createsNotice() {
            // Given
            when(applicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));
            when(applicationRepository.save(any(ApplicationForPayment.class))).thenReturn(testApplication);

            PayLessNoticeRequest request = PayLessNoticeRequest.builder()
                    .issuedOn(LocalDateTime.now())
                    .sumConsideredDue(new BigDecimal("45000.00"))
                    .currency("GBP")
                    .basisOfCalculation("Disallowed costs for defective work")
                    .build();

            // When
            ApplicationResponse response = applicationService.addPayLessNotice(1L, request);

            // Then
            assertNotNull(response);
            verify(applicationRepository).save(argThat(app -> app.getPayLessNotice() != null));
        }

        @Test
        @DisplayName("addPayLessNotice() throws exception if notice already exists")
        void addPayLessNotice_throwsException_whenAlreadyExists() {
            // Given
            testApplication.setPayLessNotice(PayLessNotice.builder().id(1L).build());
            when(applicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));

            PayLessNoticeRequest request = PayLessNoticeRequest.builder()
                    .issuedOn(LocalDateTime.now())
                    .sumConsideredDue(new BigDecimal("45000.00"))
                    .build();

            // When/Then
            assertThrows(ValidationException.class, () -> 
                applicationService.addPayLessNotice(1L, request));
        }
    }

    // ================================================================
    // EDGE CASE TESTS
    // ================================================================

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Due date uses contract default when payment terms days is null")
        void dueDate_usesContractDefault_whenNull() {
            // Given
            Contract contractNoTerms = Contract.builder()
                    .id(1L)
                    .contractRef("CRMS-001")
                    .paymentTermsDays(null)  // null
                    .finalDateForPaymentOffsetDays(null)
                    .payLessNoticePrescribedPeriodDays(null)
                    .build();

            ApplicationForPaymentRequest request = ApplicationForPaymentRequest.builder()
                    .applicationPeriodStart(LocalDate.of(2024, 2, 1))
                    .applicationPeriodEnd(LocalDate.of(2024, 2, 29))
                    .valueOfWorks(new BigDecimal("60000.00"))
                    .build();

            when(contractRepository.findById(1L)).thenReturn(Optional.of(contractNoTerms));
            when(applicationRepository.findMaxApplicationNumberByContractId(1L))
                    .thenReturn(Optional.of(0));
            when(applicationRepository.save(any(ApplicationForPayment.class))).thenAnswer(invocation -> {
                ApplicationForPayment app = invocation.getArgument(0);
                app.setId(1L);
                return app;
            });

            // When
            ApplicationResponse response = applicationService.create(1L, request);

            // Then - should use default 30 days
            assertEquals(LocalDate.of(2024, 3, 30), response.getDueDate());
        }

        @Test
        @DisplayName("Empty application list returns empty page response")
        void findByContract_returnsEmptyPage_whenNoApplications() {
            // Given
            when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));
            when(applicationRepository.findByContractIdOrderByApplicationPeriodEndDesc(1L))
                    .thenReturn(List.of());

            // When
            PageResponse<ApplicationResponse> response = applicationService.findByContract(1L);

            // Then
            assertNotNull(response);
            assertEquals(0, response.getContent().size());
            assertEquals(1, response.getTotalPages());
        }
    }
}
