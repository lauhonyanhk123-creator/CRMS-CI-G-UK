package com.crms.service.impl;

import com.crms.domain.company.entity.Company;
import com.crms.domain.subcontractor.entity.CISReturn;
import com.crms.domain.subcontractor.entity.CISReturnLine;
import com.crms.domain.subcontractor.entity.CISVerification;
import com.crms.domain.subcontractor.enums.CisReturnStatus;
import com.crms.domain.subcontractor.enums.CisVerificationStatus;
import com.crms.domain.subcontractor.repository.CISReturnLineRepository;
import com.crms.domain.subcontractor.repository.CISReturnRepository;
import com.crms.domain.subcontractor.repository.CISVerificationRepository;
import com.crms.exception.ResourceNotFoundException;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CISServiceImpl}.
 *
 * <p>Covers return generation, submission, and payment statement generation.
 * All repository dependencies are mocked; no Spring context is loaded.</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CISServiceImpl")
class CISServiceImplTest {

    @Mock
    private CISReturnRepository cisReturnRepository;

    @Mock
    private CISReturnLineRepository cisReturnLineRepository;

    @Mock
    private CISVerificationRepository cisVerificationRepository;

    @InjectMocks
    private CISServiceImpl cisService;

    // Shared fixtures
    private Company testCompany;
    private CISVerification testVerification;

    @BeforeEach
    void setUp() {
        testCompany = Company.builder()
                .id(10L)
                .name("Acme Subcontracting Ltd")
                .utr("1234567890")
                .build();

        testVerification = CISVerification.builder()
                .id(1L)
                .company(testCompany)
                .verificationRef("VER-001")
                .status(CisVerificationStatus.VERIFIED)
                .rate(new BigDecimal("20"))
                .build();

        // Default lenient stubs — individual test classes can override
        when(cisReturnRepository.save(any(CISReturn.class)))
                .thenAnswer(inv -> {
                    CISReturn c = inv.getArgument(0);
                    if (c.getId() == null) c.setId(1L);
                    return c;
                });
        when(cisReturnLineRepository.saveAll(any()))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    // =========================================================================
    // generateReturn
    // =========================================================================

    @Nested
    @DisplayName("generateReturn()")
    class GenerateReturn {

        @Test
        @DisplayName("throws IllegalArgumentException when taxMonth is null")
        void nullTaxMonth_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> cisService.generateReturn(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid tax month format");

            verifyNoInteractions(cisReturnRepository, cisVerificationRepository);
        }

        @Test
        @DisplayName("throws IllegalArgumentException when taxMonth is in MM-YYYY format")
        void wrongOrderFormat_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> cisService.generateReturn("04-2025"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid tax month format");
        }

        @Test
        @DisplayName("throws IllegalArgumentException when taxMonth is a plain word")
        void wordFormat_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> cisService.generateReturn("April-2025"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid tax month format");
        }

        @Test
        @DisplayName("returns existing return details when a return already exists for the month")
        void existingReturn_returnsExistingReturnMap() {
            CISReturn existing = CISReturn.builder()
                    .id(42L)
                    .taxMonth("2025-04")
                    .status(CisReturnStatus.DRAFT)
                    .build();

            when(cisReturnRepository.findByTaxMonth("2025-04"))
                    .thenReturn(Optional.of(existing));

            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) cisService.generateReturn("2025-04");

            assertThat(result.get("returnId")).isEqualTo(42L);
            assertThat(result.get("status")).isEqualTo(CisReturnStatus.DRAFT.name());
            assertThat(result.get("message").toString()).contains("already exists");
            verify(cisReturnRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws IllegalStateException when no VERIFIED verifications are found")
        void noVerifications_throwsIllegalStateException() {
            when(cisReturnRepository.findByTaxMonth("2025-04")).thenReturn(Optional.empty());
            when(cisVerificationRepository.findByStatus(CisVerificationStatus.VERIFIED))
                    .thenReturn(Collections.emptyList());

            assertThatThrownBy(() -> cisService.generateReturn("2025-04"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("No valid CIS verifications");
        }

        @Test
        @DisplayName("saves a new DRAFT return and its lines when valid verifications exist")
        void validVerifications_savesDraftReturn() {
            when(cisReturnRepository.findByTaxMonth("2025-04")).thenReturn(Optional.empty());
            when(cisVerificationRepository.findByStatus(CisVerificationStatus.VERIFIED))
                    .thenReturn(List.of(testVerification));

            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) cisService.generateReturn("2025-04");

            assertThat(result.get("taxMonth")).isEqualTo("2025-04");
            assertThat(result.get("status")).isEqualTo(CisReturnStatus.DRAFT.name());
            assertThat(result.get("message").toString()).contains("generated successfully");
            verify(cisReturnRepository).save(any(CISReturn.class));
            verify(cisReturnLineRepository).saveAll(any());
        }

        @Test
        @DisplayName("skips verification entries whose company is null when building return lines")
        void verificationWithNullCompany_lineCountIsZero() {
            CISVerification noCompany = CISVerification.builder()
                    .id(2L)
                    .verificationRef("VER-002")
                    .status(CisVerificationStatus.VERIFIED)
                    .rate(new BigDecimal("20"))
                    // company intentionally left null
                    .build();

            when(cisReturnRepository.findByTaxMonth("2025-04")).thenReturn(Optional.empty());
            when(cisVerificationRepository.findByStatus(CisVerificationStatus.VERIFIED))
                    .thenReturn(List.of(noCompany));

            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) cisService.generateReturn("2025-04");

            assertThat(result.get("lineCount")).isEqualTo(0);
        }

        @Test
        @DisplayName("applies default 20% CIS rate when verification rate is null")
        void nullRate_doesNotThrow_usesDefaultRate() {
            CISVerification nullRateVerification = CISVerification.builder()
                    .id(3L)
                    .company(testCompany)
                    .verificationRef("VER-003")
                    .status(CisVerificationStatus.VERIFIED)
                    .rate(null)
                    .build();

            when(cisReturnRepository.findByTaxMonth("2025-04")).thenReturn(Optional.empty());
            when(cisVerificationRepository.findByStatus(CisVerificationStatus.VERIFIED))
                    .thenReturn(List.of(nullRateVerification));

            // Should not throw; default 20% rate is applied
            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) cisService.generateReturn("2025-04");

            assertThat(result).isNotNull();
            assertThat(result.get("lineCount")).isEqualTo(1);
        }
    }

    // =========================================================================
    // submitReturn
    // =========================================================================

    @Nested
    @DisplayName("submitReturn()")
    class SubmitReturn {

        @Test
        @DisplayName("throws ResourceNotFoundException when return ID does not exist")
        void notFound_throwsResourceNotFoundException() {
            when(cisReturnRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cisService.submitReturn(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }

        @Test
        @DisplayName("throws IllegalStateException when return has no lines")
        void emptyLines_throwsIllegalStateException() {
            // @Builder.Default gives an empty ArrayList, so no lines to add
            CISReturn emptyReturn = CISReturn.builder()
                    .id(100L)
                    .taxMonth("2025-04")
                    .status(CisReturnStatus.DRAFT)
                    .build();

            when(cisReturnRepository.findById(100L)).thenReturn(Optional.of(emptyReturn));

            assertThatThrownBy(() -> cisService.submitReturn(100L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("empty");
        }

        @Test
        @DisplayName("returns already-submitted message without saving when already submitted")
        void alreadySubmitted_returnsMessageWithoutSave() {
            CISReturn submittedReturn = buildReturnWithLine(CisReturnStatus.SUBMITTED);
            when(cisReturnRepository.findById(100L)).thenReturn(Optional.of(submittedReturn));

            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) cisService.submitReturn(100L);

            assertThat(result.get("message").toString()).contains("already submitted");
            verify(cisReturnRepository, never()).save(any());
        }

        @Test
        @DisplayName("sets status to SUBMITTED and stores an HMRC receipt ref on valid return")
        void validDraftReturn_setsSubmittedAndHmrcRef() {
            CISReturn draftReturn = buildReturnWithLine(CisReturnStatus.DRAFT);
            when(cisReturnRepository.findById(100L)).thenReturn(Optional.of(draftReturn));

            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) cisService.submitReturn(100L);

            assertThat(result.get("status")).isEqualTo(CisReturnStatus.SUBMITTED.name());
            assertThat(result.get("hmrcReceiptRef").toString()).startsWith("HMRC-");
            assertThat(result.get("submittedAt")).isNotNull();
            assertThat(draftReturn.getStatus()).isEqualTo(CisReturnStatus.SUBMITTED);
            assertThat(draftReturn.getSubmittedAt()).isNotNull();
            verify(cisReturnRepository).save(draftReturn);
        }

        @Test
        @DisplayName("calculates and includes gross, deduction and net totals in the response")
        void validDraftReturn_includesCalculatedTotals() {
            CISReturn draftReturn = buildReturnWithLine(CisReturnStatus.DRAFT);
            when(cisReturnRepository.findById(100L)).thenReturn(Optional.of(draftReturn));

            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) cisService.submitReturn(100L);

            assertThat(result).containsKeys("totalGross", "totalDeduction", "totalNet");
            assertThat((BigDecimal) result.get("totalGross"))
                    .isGreaterThanOrEqualTo(BigDecimal.ZERO);
        }

        // Helper – builds a return with one fully-populated line
        private CISReturn buildReturnWithLine(CisReturnStatus status) {
            CISReturn cisReturn = CISReturn.builder()
                    .id(100L)
                    .taxMonth("2025-04")
                    .status(status)
                    .submittedAt(status == CisReturnStatus.SUBMITTED || status == CisReturnStatus.ACCEPTED
                            ? java.time.LocalDateTime.now() : null)
                    .hmrcReceiptRef(status == CisReturnStatus.SUBMITTED || status == CisReturnStatus.ACCEPTED
                            ? "HMRC-TEST-001" : null)
                    .build();

            CISReturnLine line = CISReturnLine.builder()
                    .id(1L)
                    .cisReturn(cisReturn)
                    .subcontractor(testCompany)
                    .grossPaid(new BigDecimal("1000.00"))
                    .cisRate(new BigDecimal("20"))
                    .deduction(new BigDecimal("200.00"))
                    .netPaid(new BigDecimal("800.00"))
                    .build();

            cisReturn.getCisReturnLines().add(line);
            return cisReturn;
        }
    }

    // =========================================================================
    // generatePaymentStatements
    // =========================================================================

    @Nested
    @DisplayName("generatePaymentStatements()")
    class GeneratePaymentStatements {

        @Test
        @DisplayName("throws ResourceNotFoundException when return ID does not exist")
        void notFound_throwsResourceNotFoundException() {
            when(cisReturnRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cisService.generatePaymentStatements(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }

        @Test
        @DisplayName("throws IllegalStateException when return has no lines")
        void emptyLines_throwsIllegalStateException() {
            CISReturn emptyReturn = CISReturn.builder()
                    .id(100L)
                    .taxMonth("2025-04")
                    .status(CisReturnStatus.SUBMITTED)
                    .build();

            when(cisReturnRepository.findById(100L)).thenReturn(Optional.of(emptyReturn));

            assertThatThrownBy(() -> cisService.generatePaymentStatements(100L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("No return lines");
        }

        @Test
        @DisplayName("returns one payment statement per return line with correct subcontractor details")
        void validLines_returnsOneStatementPerLine() {
            CISReturn returnWithLines = buildReturnWithLine();
            when(cisReturnRepository.findById(100L)).thenReturn(Optional.of(returnWithLines));

            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) cisService.generatePaymentStatements(100L);

            assertThat(result.get("returnId")).isEqualTo(100L);
            assertThat(result.get("taxMonth")).isEqualTo("2025-04");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> statements =
                    (List<Map<String, Object>>) result.get("statements");
            assertThat(statements).hasSize(1);

            Map<String, Object> stmt = statements.get(0);
            assertThat(stmt.get("subcontractorId")).isEqualTo(10L);
            assertThat(stmt.get("subcontractorName")).isEqualTo("Acme Subcontracting Ltd");
            assertThat(stmt.get("subcontractorUtr")).isEqualTo("1234567890");
            assertThat(stmt.get("grossPaid")).isEqualTo(new BigDecimal("500.00"));
            assertThat(stmt.get("cisDeduction")).isEqualTo(new BigDecimal("100.00"));
            assertThat(stmt.get("netPaid")).isEqualTo(new BigDecimal("400.00"));
        }

        @Test
        @DisplayName("includes a summary block with correct aggregate totals")
        void validLines_summaryContainsAggregatedTotals() {
            CISReturn returnWithLines = buildReturnWithLine();
            when(cisReturnRepository.findById(100L)).thenReturn(Optional.of(returnWithLines));

            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) cisService.generatePaymentStatements(100L);

            @SuppressWarnings("unchecked")
            Map<String, Object> summary = (Map<String, Object>) result.get("summary");
            assertThat(summary.get("totalGross")).isEqualTo(new BigDecimal("500.00"));
            assertThat(summary.get("totalDeduction")).isEqualTo(new BigDecimal("100.00"));
            assertThat(summary.get("totalNet")).isEqualTo(new BigDecimal("400.00"));
            assertThat(summary.get("subcontractorCount")).isEqualTo(1);
        }

        @Test
        @DisplayName("aggregates totals correctly across multiple return lines")
        void multipleLines_aggregatesTotals() {
            Company company2 = Company.builder()
                    .id(11L)
                    .name("Beta Works Ltd")
                    .utr("9876543210")
                    .build();

            CISReturn cisReturn = CISReturn.builder()
                    .id(100L)
                    .taxMonth("2025-04")
                    .status(CisReturnStatus.SUBMITTED)
                    .build();

            CISReturnLine line1 = CISReturnLine.builder()
                    .id(1L).cisReturn(cisReturn).subcontractor(testCompany)
                    .grossPaid(new BigDecimal("1000.00")).deduction(new BigDecimal("200.00"))
                    .netPaid(new BigDecimal("800.00")).cisRate(new BigDecimal("20")).build();
            CISReturnLine line2 = CISReturnLine.builder()
                    .id(2L).cisReturn(cisReturn).subcontractor(company2)
                    .grossPaid(new BigDecimal("500.00")).deduction(new BigDecimal("150.00"))
                    .netPaid(new BigDecimal("350.00")).cisRate(new BigDecimal("30")).build();

            cisReturn.getCisReturnLines().add(line1);
            cisReturn.getCisReturnLines().add(line2);

            when(cisReturnRepository.findById(100L)).thenReturn(Optional.of(cisReturn));

            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) cisService.generatePaymentStatements(100L);

            @SuppressWarnings("unchecked")
            Map<String, Object> summary = (Map<String, Object>) result.get("summary");
            assertThat(summary.get("totalGross")).isEqualTo(new BigDecimal("1500.00"));
            assertThat(summary.get("totalDeduction")).isEqualTo(new BigDecimal("350.00"));
            assertThat(summary.get("totalNet")).isEqualTo(new BigDecimal("1150.00"));
            assertThat(summary.get("subcontractorCount")).isEqualTo(2);
        }

        @Test
        @DisplayName("uses 'N/A' placeholder when subcontractor UTR is null")
        void nullUtr_usesFallbackNaString() {
            Company noUtrCompany = Company.builder()
                    .id(20L)
                    .name("No UTR Ltd")
                    .build(); // utr intentionally left null

            CISReturn cisReturn = CISReturn.builder()
                    .id(100L)
                    .taxMonth("2025-04")
                    .status(CisReturnStatus.SUBMITTED)
                    .build();

            CISReturnLine line = CISReturnLine.builder()
                    .id(3L).cisReturn(cisReturn).subcontractor(noUtrCompany)
                    .grossPaid(BigDecimal.ZERO).deduction(BigDecimal.ZERO)
                    .netPaid(BigDecimal.ZERO).cisRate(new BigDecimal("20")).build();
            cisReturn.getCisReturnLines().add(line);

            when(cisReturnRepository.findById(100L)).thenReturn(Optional.of(cisReturn));

            @SuppressWarnings("unchecked")
            Map<String, Object> result = (Map<String, Object>) cisService.generatePaymentStatements(100L);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> statements =
                    (List<Map<String, Object>>) result.get("statements");
            assertThat(statements.get(0).get("subcontractorUtr")).isEqualTo("N/A");
        }

        // Helper – returns a submitted CISReturn with one fully-populated line
        private CISReturn buildReturnWithLine() {
            CISReturn cisReturn = CISReturn.builder()
                    .id(100L)
                    .taxMonth("2025-04")
                    .status(CisReturnStatus.SUBMITTED)
                    .build();

            CISReturnLine line = CISReturnLine.builder()
                    .id(1L)
                    .cisReturn(cisReturn)
                    .subcontractor(testCompany)
                    .grossPaid(new BigDecimal("500.00"))
                    .cisRate(new BigDecimal("20"))
                    .deduction(new BigDecimal("100.00"))
                    .netPaid(new BigDecimal("400.00"))
                    .build();

            cisReturn.getCisReturnLines().add(line);
            return cisReturn;
        }
    }
}
