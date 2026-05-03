package com.crms.service.impl;

import com.crms.domain.contract.entity.ApplicationForPayment;
import com.crms.domain.contract.entity.Contract;
import com.crms.domain.contract.enums.ApplicationStatus;
import com.crms.domain.contract.enums.ContractStatus;
import com.crms.domain.contract.repository.ApplicationForPaymentRepository;
import com.crms.domain.contract.repository.ContractRepository;
import com.crms.domain.contract.repository.PayLessNoticeRepository;
import com.crms.domain.contract.repository.PaymentNoticeRepository;
import com.crms.domain.company.entity.Company;
import com.crms.domain.tender.entity.Tender;
import com.crms.dto.request.ApplicationForPaymentRequest;
import com.crms.dto.response.ApplicationResponse;
import com.crms.exception.ResourceNotFoundException;
import com.crms.exception.ValidationException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ApplicationForPayment VAT reverse charge functionality.
 * Tests reverse charge calculation per UK construction industry VAT rules.
 * Reverse charge applies when:
 * - Subcontractor is VAT registered (has vatNumber)
 * - Contract value >= £85,000 VAT threshold
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ApplicationForPaymentVatReverseChargeTest {

    @Mock
    private ApplicationForPaymentRepository applicationRepository;

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private PaymentNoticeRepository paymentNoticeRepository;

    @Mock
    private PayLessNoticeRepository payLessNoticeRepository;

    @InjectMocks
    private ApplicationForPaymentServiceImpl applicationService;

    private Contract contractWithVatCompany;
    private Contract contractWithoutVatCompany;
    private Contract contractBelowThreshold;

    @BeforeEach
    void setUp() {
        // Contract with VAT-registered client above threshold
        Company vatCompany = Company.builder()
                .id(1L)
                .name("VAT Registered Client")
                .vatNumber("GB123456789")
                .build();

        Tender tenderWithVat = Tender.builder()
                .id(1L)
                .title("Test Tender")
                .client(vatCompany)
                .build();

        contractWithVatCompany = Contract.builder()
                .id(1L)
                .contractRef("CRMS-001")
                .title("Contract with VAT Client")
                .contractValue(new BigDecimal("500000.00")) // Above threshold
                .paymentTermsDays(30)
                .tender(tenderWithVat)
                .status(ContractStatus.IN_PROGRESS)
                .build();

        // Contract without VAT-registered client
        Company nonVatCompany = Company.builder()
                .id(2L)
                .name("Non-VAT Client")
                .vatNumber(null) // No VAT number
                .build();

        Tender tenderWithoutVat = Tender.builder()
                .id(2L)
                .title("Test Tender 2")
                .client(nonVatCompany)
                .build();

        contractWithoutVatCompany = Contract.builder()
                .id(2L)
                .contractRef("CRMS-002")
                .title("Contract without VAT Client")
                .contractValue(new BigDecimal("500000.00"))
                .paymentTermsDays(30)
                .tender(tenderWithoutVat)
                .status(ContractStatus.IN_PROGRESS)
                .build();

        // Contract below VAT threshold
        Company belowThresholdCompany = Company.builder()
                .id(3L)
                .name("Below Threshold Client")
                .vatNumber("GB987654321")
                .build();

        Tender tenderBelowThreshold = Tender.builder()
                .id(3L)
                .title("Test Tender 3")
                .client(belowThresholdCompany)
                .build();

        contractBelowThreshold = Contract.builder()
                .id(3L)
                .contractRef("CRMS-003")
                .title("Contract Below Threshold")
                .contractValue(new BigDecimal("50000.00")) // Below £85,000
                .paymentTermsDays(30)
                .tender(tenderBelowThreshold)
                .status(ContractStatus.IN_PROGRESS)
                .build();
    }

    @Nested
    @DisplayName("VAT Reverse Charge Calculation Tests")
    class VatReverseChargeCalculationTests {

        @Test
        @DisplayName("reverseCharge is true when client is VAT registered and contract above threshold")
        void reverseCharge_true_whenVatRegisteredAndAboveThreshold() {
            // Given
            when(contractRepository.findById(1L)).thenReturn(Optional.of(contractWithVatCompany));
            when(applicationRepository.findMaxApplicationNumberByContractId(1L)).thenReturn(Optional.empty());
            when(applicationRepository.save(any(ApplicationForPayment.class))).thenAnswer(invocation -> {
                ApplicationForPayment app = invocation.getArgument(0);
                app.setId(1L);
                return app;
            });

            ApplicationForPaymentRequest request = ApplicationForPaymentRequest.builder()
                    .applicationPeriodStart(LocalDate.of(2024, 1, 1))
                    .applicationPeriodEnd(LocalDate.of(2024, 1, 31))
                    .valueOfWorks(new BigDecimal("50000.00"))
                    .build();

            // When
            ApplicationResponse response = applicationService.create(1L, request);

            // Then
            assertTrue(response.getReverseCharge());
        }

        @Test
        @DisplayName("reverseCharge is false when client has no VAT number")
        void reverseCharge_false_whenNoVatNumber() {
            // Given
            when(contractRepository.findById(2L)).thenReturn(Optional.of(contractWithoutVatCompany));
            when(applicationRepository.findMaxApplicationNumberByContractId(2L)).thenReturn(Optional.empty());
            when(applicationRepository.save(any(ApplicationForPayment.class))).thenAnswer(invocation -> {
                ApplicationForPayment app = invocation.getArgument(0);
                app.setId(2L);
                return app;
            });

            ApplicationForPaymentRequest request = ApplicationForPaymentRequest.builder()
                    .applicationPeriodStart(LocalDate.of(2024, 1, 1))
                    .applicationPeriodEnd(LocalDate.of(2024, 1, 31))
                    .valueOfWorks(new BigDecimal("50000.00"))
                    .build();

            // When
            ApplicationResponse response = applicationService.create(2L, request);

            // Then
            assertFalse(response.getReverseCharge());
        }

        @Test
        @DisplayName("reverseCharge is false when contract value below threshold")
        void reverseCharge_false_whenBelowThreshold() {
            // Given
            when(contractRepository.findById(3L)).thenReturn(Optional.of(contractBelowThreshold));
            when(applicationRepository.findMaxApplicationNumberByContractId(3L)).thenReturn(Optional.empty());
            when(applicationRepository.save(any(ApplicationForPayment.class))).thenAnswer(invocation -> {
                ApplicationForPayment app = invocation.getArgument(0);
                app.setId(3L);
                return app;
            });

            ApplicationForPaymentRequest request = ApplicationForPaymentRequest.builder()
                    .applicationPeriodStart(LocalDate.of(2024, 1, 1))
                    .applicationPeriodEnd(LocalDate.of(2024, 1, 31))
                    .valueOfWorks(new BigDecimal("5000.00"))
                    .build();

            // When
            ApplicationResponse response = applicationService.create(3L, request);

            // Then
            assertFalse(response.getReverseCharge());
        }

        @Test
        @DisplayName("reverseCharge is false when VAT number is blank")
        void reverseCharge_false_whenVatNumberBlank() {
            // Given
            Company blankVatCompany = Company.builder()
                    .id(4L)
                    .name("Blank VAT Client")
                    .vatNumber("   ") // Whitespace only
                    .build();

            Tender tender = Tender.builder()
                    .id(4L)
                    .client(blankVatCompany)
                    .build();

            Contract contract = Contract.builder()
                    .id(4L)
                    .contractRef("CRMS-004")
                    .contractValue(new BigDecimal("500000.00"))
                    .paymentTermsDays(30)
                    .tender(tender)
                    .build();

            when(contractRepository.findById(4L)).thenReturn(Optional.of(contract));
            when(applicationRepository.findMaxApplicationNumberByContractId(4L)).thenReturn(Optional.empty());
            when(applicationRepository.save(any(ApplicationForPayment.class))).thenAnswer(invocation -> {
                ApplicationForPayment app = invocation.getArgument(0);
                app.setId(4L);
                return app;
            });

            ApplicationForPaymentRequest request = ApplicationForPaymentRequest.builder()
                    .applicationPeriodStart(LocalDate.of(2024, 1, 1))
                    .applicationPeriodEnd(LocalDate.of(2024, 1, 31))
                    .valueOfWorks(new BigDecimal("50000.00"))
                    .build();

            // When
            ApplicationResponse response = applicationService.create(4L, request);

            // Then
            assertFalse(response.getReverseCharge());
        }

        @Test
        @DisplayName("reverseCharge is false when contract has no tender")
        void reverseCharge_false_whenNoTender() {
            // Given
            Contract contractNoTender = Contract.builder()
                    .id(5L)
                    .contractRef("CRMS-005")
                    .contractValue(new BigDecimal("500000.00"))
                    .tender(null) // No tender
                    .build();

            when(contractRepository.findById(5L)).thenReturn(Optional.of(contractNoTender));
            when(applicationRepository.findMaxApplicationNumberByContractId(5L)).thenReturn(Optional.empty());
            when(applicationRepository.save(any(ApplicationForPayment.class))).thenAnswer(invocation -> {
                ApplicationForPayment app = invocation.getArgument(0);
                app.setId(5L);
                return app;
            });

            ApplicationForPaymentRequest request = ApplicationForPaymentRequest.builder()
                    .applicationPeriodStart(LocalDate.of(2024, 1, 1))
                    .applicationPeriodEnd(LocalDate.of(2024, 1, 31))
                    .valueOfWorks(new BigDecimal("50000.00"))
                    .build();

            // When
            ApplicationResponse response = applicationService.create(5L, request);

            // Then
            assertFalse(response.getReverseCharge());
        }

        @Test
        @DisplayName("reverseCharge is false when tender has no client")
        void reverseCharge_false_whenTenderNoClient() {
            // Given
            Tender tenderNoCompany = Tender.builder()
                    .id(6L)
                    .client(null) // No client
                    .build();

            Contract contractNoCompany = Contract.builder()
                    .id(6L)
                    .contractRef("CRMS-006")
                    .contractValue(new BigDecimal("500000.00"))
                    .tender(tenderNoCompany)
                    .build();

            when(contractRepository.findById(6L)).thenReturn(Optional.of(contractNoCompany));
            when(applicationRepository.findMaxApplicationNumberByContractId(6L)).thenReturn(Optional.empty());
            when(applicationRepository.save(any(ApplicationForPayment.class))).thenAnswer(invocation -> {
                ApplicationForPayment app = invocation.getArgument(0);
                app.setId(6L);
                return app;
            });

            ApplicationForPaymentRequest request = ApplicationForPaymentRequest.builder()
                    .applicationPeriodStart(LocalDate.of(2024, 1, 1))
                    .applicationPeriodEnd(LocalDate.of(2024, 1, 31))
                    .valueOfWorks(new BigDecimal("50000.00"))
                    .build();

            // When
            ApplicationResponse response = applicationService.create(6L, request);

            // Then
            assertFalse(response.getReverseCharge());
        }
    }

    @Nested
    @DisplayName("VAT Threshold Boundary Tests")
    class VatThresholdBoundaryTests {

        @Test
        @DisplayName("reverseCharge is true at exactly £85,000 threshold")
        void reverseCharge_true_atExactly85000() {
            // Given
            Company vatCompany = Company.builder()
                    .id(7L)
                    .vatNumber("GB111111111")
                    .build();

            Tender tender = Tender.builder()
                    .id(7L)
                    .client(vatCompany)
                    .build();

            Contract contract = Contract.builder()
                    .id(7L)
                    .contractRef("CRMS-007")
                    .contractValue(new BigDecimal("85000.00")) // Exactly at threshold
                    .paymentTermsDays(30)
                    .tender(tender)
                    .build();

            when(contractRepository.findById(7L)).thenReturn(Optional.of(contract));
            when(applicationRepository.findMaxApplicationNumberByContractId(7L)).thenReturn(Optional.empty());
            when(applicationRepository.save(any(ApplicationForPayment.class))).thenAnswer(invocation -> {
                ApplicationForPayment app = invocation.getArgument(0);
                app.setId(7L);
                return app;
            });

            ApplicationForPaymentRequest request = ApplicationForPaymentRequest.builder()
                    .applicationPeriodStart(LocalDate.of(2024, 1, 1))
                    .applicationPeriodEnd(LocalDate.of(2024, 1, 31))
                    .valueOfWorks(new BigDecimal("10000.00"))
                    .build();

            // When
            ApplicationResponse response = applicationService.create(7L, request);

            // Then - >= threshold triggers reverse charge
            assertTrue(response.getReverseCharge());
        }

        @Test
        @DisplayName("reverseCharge is false just below £85,000 threshold")
        void reverseCharge_false_below85000() {
            // Given
            Company vatCompany = Company.builder()
                    .id(8L)
                    .vatNumber("GB222222222")
                    .build();

            Tender tender = Tender.builder()
                    .id(8L)
                    .client(vatCompany)
                    .build();

            Contract contract = Contract.builder()
                    .id(8L)
                    .contractRef("CRMS-008")
                    .contractValue(new BigDecimal("84999.99")) // Just below threshold
                    .paymentTermsDays(30)
                    .tender(tender)
                    .build();

            when(contractRepository.findById(8L)).thenReturn(Optional.of(contract));
            when(applicationRepository.findMaxApplicationNumberByContractId(8L)).thenReturn(Optional.empty());
            when(applicationRepository.save(any(ApplicationForPayment.class))).thenAnswer(invocation -> {
                ApplicationForPayment app = invocation.getArgument(0);
                app.setId(8L);
                return app;
            });

            ApplicationForPaymentRequest request = ApplicationForPaymentRequest.builder()
                    .applicationPeriodStart(LocalDate.of(2024, 1, 1))
                    .applicationPeriodEnd(LocalDate.of(2024, 1, 31))
                    .valueOfWorks(new BigDecimal("10000.00"))
                    .build();

            // When
            ApplicationResponse response = applicationService.create(8L, request);

            // Then - just below threshold
            assertFalse(response.getReverseCharge());
        }

        @Test
        @DisplayName("reverseCharge handles null contract value")
        void reverseCharge_handlesNullContractValue() {
            // Given
            Company vatCompany = Company.builder()
                    .id(9L)
                    .vatNumber("GB333333333")
                    .build();

            Tender tender = Tender.builder()
                    .id(9L)
                    .client(vatCompany)
                    .build();

            Contract contract = Contract.builder()
                    .id(9L)
                    .contractRef("CRMS-009")
                    .contractValue(null) // Null contract value
                    .paymentTermsDays(30)
                    .tender(tender)
                    .build();

            when(contractRepository.findById(9L)).thenReturn(Optional.of(contract));
            when(applicationRepository.findMaxApplicationNumberByContractId(9L)).thenReturn(Optional.empty());
            when(applicationRepository.save(any(ApplicationForPayment.class))).thenAnswer(invocation -> {
                ApplicationForPayment app = invocation.getArgument(0);
                app.setId(9L);
                return app;
            });

            ApplicationForPaymentRequest request = ApplicationForPaymentRequest.builder()
                    .applicationPeriodStart(LocalDate.of(2024, 1, 1))
                    .applicationPeriodEnd(LocalDate.of(2024, 1, 31))
                    .valueOfWorks(new BigDecimal("10000.00"))
                    .build();

            // When
            ApplicationResponse response = applicationService.create(9L, request);

            // Then - null value doesn't exceed threshold
            assertFalse(response.getReverseCharge());
        }
    }

    @Nested
    @DisplayName("Pay-Less Notice Deadline in Response")
    class PayLessNoticeDeadlineInResponseTests {

        @Test
        @DisplayName("ApplicationResponse includes pay-less notice deadline")
        void response_includesPayLessNoticeDeadline() {
            // Given
            ApplicationForPayment application = ApplicationForPayment.builder()
                    .id(1L)
                    .contract(contractWithVatCompany)
                    .applicationRef("CRMS-001-APP-1")
                    .applicationNumber(1)
                    .applicationPeriodStart(LocalDate.of(2024, 1, 1))
                    .applicationPeriodEnd(LocalDate.of(2024, 1, 31))
                    .valueOfWorks(new BigDecimal("50000.00"))
                    .retention(new BigDecimal("2500.00"))
                    .status(ApplicationStatus.SUBMITTED)
                    .submittedDate(LocalDate.of(2024, 2, 15))
                    .reverseCharge(true)
                    .build();

            when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

            // When
            ApplicationResponse response = applicationService.findById(1L);

            // Then
            assertNotNull(response.getPayLessNoticeDeadline());
            // Deadline should be 5 days before submitted date
            assertEquals(LocalDate.of(2024, 2, 10), response.getPayLessNoticeDeadline());
        }

        @Test
        @DisplayName("ApplicationResponse includes deadline status")
        void response_includesDeadlineStatus() {
            // Given
            ApplicationForPayment application = ApplicationForPayment.builder()
                    .id(1L)
                    .contract(contractWithVatCompany)
                    .applicationRef("CRMS-001-APP-1")
                    .applicationNumber(1)
                    .applicationPeriodStart(LocalDate.of(2024, 1, 1))
                    .applicationPeriodEnd(LocalDate.of(2024, 1, 31))
                    .valueOfWorks(new BigDecimal("50000.00"))
                    .status(ApplicationStatus.SUBMITTED)
                    .submittedDate(LocalDate.of(2024, 2, 15))
                    .reverseCharge(true)
                    .build();

            when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

            // When
            ApplicationResponse response = applicationService.findById(1L);

            // Then
            assertNotNull(response.getDeadlineStatus());
        }
    }
}
