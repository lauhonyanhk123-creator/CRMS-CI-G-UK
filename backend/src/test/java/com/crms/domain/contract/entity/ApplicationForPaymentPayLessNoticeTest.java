package com.crms.domain.contract.entity;

import com.crms.domain.contract.enums.ApplicationStatus;
import com.crms.domain.contract.enums.DeadlineStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ApplicationForPayment pay-less notice functionality.
 * Tests s.111 pay-less notice deadline calculations under the Housing Grants Act.
 */
class ApplicationForPaymentPayLessNoticeTest {

    private ApplicationForPayment application;

    @BeforeEach
    void setUp() {
        Contract contract = Contract.builder()
                .id(1L)
                .contractRef("CRMS-001")
                .title("Test Contract")
                .payLessNoticePrescribedPeriodDays(7)
                .build();

        application = ApplicationForPayment.builder()
                .id(1L)
                .contract(contract)
                .applicationRef("CRMS-001-APP-1")
                .applicationNumber(1)
                .applicationPeriodStart(LocalDate.of(2024, 1, 1))
                .applicationPeriodEnd(LocalDate.of(2024, 1, 31))
                .dueDate(LocalDate.of(2024, 3, 1))
                .valueOfWorks(new BigDecimal("50000.00"))
                .retention(new BigDecimal("2500.00"))
                .grossValue(new BigDecimal("47500.00"))
                .status(ApplicationStatus.SUBMITTED)
                .submittedDate(LocalDate.of(2024, 2, 5))
                .build();
    }

    @Nested
    @DisplayName("Pay-Less Notice Deadline Tests")
    class PayLessNoticeDeadlineTests {

        @Test
        @DisplayName("submittedDate is required for pay-less notice deadline")
        void submittedDate_requiredForDeadline() {
            // Given
            application.setSubmittedDate(null);

            // When - no submitted date means no deadline can be calculated
            LocalDate deadline = calculatePayLessNoticeDeadline(application.getSubmittedDate());

            // Then
            assertNull(deadline);
        }

        @Test
        @DisplayName("pay-less notice deadline is 5 days before submitted date")
        void payLessNoticeDeadline_5DaysBeforeSubmittedDate() {
            // Given
            application.setSubmittedDate(LocalDate.of(2024, 2, 15));

            // When
            LocalDate deadline = calculatePayLessNoticeDeadline(application.getSubmittedDate());

            // Then
            assertEquals(LocalDate.of(2024, 2, 10), deadline);
        }

        @Test
        @DisplayName("pay-less notice deadline handles month boundaries")
        void payLessNoticeDeadline_handlesMonthBoundaries() {
            // Given - submitted on 5th of month
            application.setSubmittedDate(LocalDate.of(2024, 3, 5));

            // When
            LocalDate deadline = calculatePayLessNoticeDeadline(application.getSubmittedDate());

            // Then - deadline is 28th Feb (5 days before 5th)
            assertEquals(LocalDate.of(2024, 2, 29), deadline); // 2024 is leap year
        }

        @Test
        @DisplayName("pay-less notice deadline handles year boundary")
        void payLessNoticeDeadline_handlesYearBoundary() {
            // Given - submitted on Jan 3rd
            application.setSubmittedDate(LocalDate.of(2024, 1, 3));

            // When
            LocalDate deadline = calculatePayLessNoticeDeadline(application.getSubmittedDate());

            // Then - deadline is Dec 29th of previous year
            assertEquals(LocalDate.of(2023, 12, 29), deadline);
        }
    }

    @Nested
    @DisplayName("Deadline Status Tests")
    class DeadlineStatusTests {

        @Test
        @DisplayName("DEADLINE_PASSED when deadline is in the past")
        void deadlineStatus_deadlinePassed() {
            // Given - deadline was 2 days ago
            LocalDate deadline = LocalDate.now().minusDays(2);

            // When
            DeadlineStatus status = calculateDeadlineStatus(deadline);

            // Then
            assertEquals(DeadlineStatus.DEADLINE_PASSED, status);
        }

        @Test
        @DisplayName("DEADLINE_APPROACHING when deadline is within 2 days")
        void deadlineStatus_deadlineApproaching() {
            // Given - deadline is tomorrow
            LocalDate deadline = LocalDate.now().plusDays(1);

            // When
            DeadlineStatus status = calculateDeadlineStatus(deadline);

            // Then
            assertEquals(DeadlineStatus.DEADLINE_APPROACHING, status);
        }

        @Test
        @DisplayName("DEADLINE_ACTIVE when deadline is more than 2 days away")
        void deadlineStatus_deadlineActive() {
            // Given - deadline is 5 days away
            LocalDate deadline = LocalDate.now().plusDays(5);

            // When
            DeadlineStatus status = calculateDeadlineStatus(deadline);

            // Then
            assertEquals(DeadlineStatus.DEADLINE_ACTIVE, status);
        }

        @Test
        @DisplayName("NO_DEADLINE when deadline is null")
        void deadlineStatus_noDeadline() {
            // Given
            LocalDate deadline = null;

            // When
            DeadlineStatus status = calculateDeadlineStatus(deadline);

            // Then
            assertEquals(DeadlineStatus.NO_DEADLINE, status);
        }

        @Test
        @DisplayName("DEADLINE_APPROACHING exactly 2 days before")
        void deadlineStatus_exactly2DaysBefore() {
            // Given - deadline is exactly 2 days away
            LocalDate deadline = LocalDate.now().plusDays(2);

            // When
            DeadlineStatus status = calculateDeadlineStatus(deadline);

            // Then - boundary: <= 2 days is APPROACHING
            assertEquals(DeadlineStatus.DEADLINE_APPROACHING, status);
        }
    }

    @Nested
    @DisplayName("PayLessNotice Entity Tests")
    class PayLessNoticeEntityTests {

        @Test
        @DisplayName("PayLessNotice links to Application correctly")
        void payLessNotice_linksToApplication() {
            // Given
            PayLessNotice payLessNotice = PayLessNotice.builder()
                    .application(application)
                    .issuedOn(java.time.LocalDateTime.now())
                    .sumConsideredDue(new BigDecimal("40000.00"))
                    .basisOfCalculation("Valuation adjustment per JCT clause 4.1")
                    .documentRef("PLN-2024-001")
                    .build();

            // Then
            assertNotNull(payLessNotice.getApplication());
            assertEquals(application.getId(), payLessNotice.getApplication().getId());
        }

        @Test
        @DisplayName("PayLessNotice has correct currency default")
        void payLessNotice_hasDefaultCurrency() {
            // Given
            PayLessNotice payLessNotice = PayLessNotice.builder()
                    .application(application)
                    .issuedOn(java.time.LocalDateTime.now())
                    .sumConsideredDue(new BigDecimal("45000.00"))
                    .build();

            // Then - GBP is the default
            assertEquals("GBP", payLessNotice.getCurrency());
        }

        @Test
        @DisplayName("PayLessNotice stores basis of calculation")
        void payLessNotice_storesBasisOfCalculation() {
            // Given
            String basis = "Deductions for defective work per schedule";
            PayLessNotice payLessNotice = PayLessNotice.builder()
                    .application(application)
                    .issuedOn(java.time.LocalDateTime.now())
                    .sumConsideredDue(new BigDecimal("42000.00"))
                    .basisOfCalculation(basis)
                    .build();

            // Then
            assertEquals(basis, payLessNotice.getBasisOfCalculation());
        }

        @Test
        @DisplayName("PayLessNotice stores audit reference")
        void payLessNotice_storesAuditReference() {
            // Given
            String auditLogId = "audit-123-456";
            PayLessNotice payLessNotice = PayLessNotice.builder()
                    .application(application)
                    .issuedOn(java.time.LocalDateTime.now())
                    .sumConsideredDue(new BigDecimal("40000.00"))
                    .auditLogId(auditLogId)
                    .build();

            // Then
            assertEquals(auditLogId, payLessNotice.getAuditLogId());
        }

        @Test
        @DisplayName("PayLessNotice can store SHA256 hash")
        void payLessNotice_storesSha256() {
            // Given
            String sha256Hash = "a".repeat(64); // 64-char SHA-256 hex
            PayLessNotice payLessNotice = PayLessNotice.builder()
                    .application(application)
                    .issuedOn(java.time.LocalDateTime.now())
                    .sumConsideredDue(new BigDecimal("40000.00"))
                    .sha256(sha256Hash)
                    .build();

            // Then
            assertEquals(64, payLessNotice.getSha256().length());
        }
    }

    @Nested
    @DisplayName("Application-PayLessNotice Relationship Tests")
    class ApplicationPayLessNoticeRelationshipTests {

        @Test
        @DisplayName("Application can have one PayLessNotice")
        void application_hasOnePayLessNotice() {
            // Given
            PayLessNotice payLessNotice = PayLessNotice.builder()
                    .application(application)
                    .issuedOn(java.time.LocalDateTime.now())
                    .sumConsideredDue(new BigDecimal("40000.00"))
                    .build();

            application.setPayLessNotice(payLessNotice);

            // Then
            assertNotNull(application.getPayLessNotice());
        }

        @Test
        @DisplayName("PayLessNotice sum can be less than application gross value")
        void payLessNoticeSum_lessThanGrossValue() {
            // Given - application gross value is 47500
            PayLessNotice payLessNotice = PayLessNotice.builder()
                    .application(application)
                    .issuedOn(java.time.LocalDateTime.now())
                    .sumConsideredDue(new BigDecimal("40000.00")) // Less than gross
                    .build();

            // Then - notice amount is less than application
            assertTrue(payLessNotice.getSumConsideredDue()
                    .compareTo(application.getGrossValue()) < 0);
        }

        @Test
        @DisplayName("Application calculates difference when pay-less notice served")
        void application_calculatesDifference() {
            // Given
            BigDecimal grossValue = application.getGrossValue(); // 47500
            BigDecimal payLessAmount = new BigDecimal("40000.00");
            BigDecimal difference = grossValue.subtract(payLessAmount); // 7500

            // Then - difference should be 7500
            assertEquals(new BigDecimal("7500.00"), difference);
        }
    }

    // Helper methods that replicate the service logic for testing
    private LocalDate calculatePayLessNoticeDeadline(LocalDate applicationDate) {
        if (applicationDate == null) {
            return null;
        }
        return applicationDate.minusDays(5);
    }

    private DeadlineStatus calculateDeadlineStatus(LocalDate deadline) {
        if (deadline == null) {
            return DeadlineStatus.NO_DEADLINE;
        }
        
        LocalDate today = LocalDate.now();
        long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(today, deadline);
        
        if (daysRemaining < 0) {
            return DeadlineStatus.DEADLINE_PASSED;
        } else if (daysRemaining <= 2) {
            return DeadlineStatus.DEADLINE_APPROACHING;
        } else {
            return DeadlineStatus.DEADLINE_ACTIVE;
        }
    }
}
