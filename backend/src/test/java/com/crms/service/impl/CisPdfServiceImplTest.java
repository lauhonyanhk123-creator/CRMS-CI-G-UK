package com.crms.service.impl;

import com.crms.domain.company.entity.Company;
import com.crms.domain.subcontractor.entity.CISReturn;
import com.crms.domain.subcontractor.entity.CISReturnLine;
import com.crms.domain.subcontractor.enums.CisReturnStatus;
import com.crms.domain.subcontractor.repository.CISReturnRepository;
import com.crms.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CisPdfServiceImpl")
class CisPdfServiceImplTest {

    @Mock
    private CISReturnRepository cisReturnRepository;

    @InjectMocks
    private CisPdfServiceImpl cisPdfService;

    private Company testCompany;

    @BeforeEach
    void setUp() {
        testCompany = Company.builder()
                .id(10L)
                .name("Acme Groundworks Ltd")
                .utr("1234567890")
                .build();
    }

    private CISReturn buildReturn(CisReturnStatus status) {
        CISReturn r = CISReturn.builder()
                .id(1L)
                .taxMonth("2025-04")
                .status(status)
                .build();

        CISReturnLine line = CISReturnLine.builder()
                .id(1L)
                .cisReturn(r)
                .subcontractor(testCompany)
                .grossPaid(new BigDecimal("5000.00"))
                .cisRate(new BigDecimal("20"))
                .deduction(new BigDecimal("1000.00"))
                .netPaid(new BigDecimal("4000.00"))
                .build();
        r.getCisReturnLines().add(line);
        return r;
    }

    @Nested
    @DisplayName("generatePaymentDeductionStatement()")
    class Generate {

        @Test
        @DisplayName("throws ResourceNotFoundException when return does not exist")
        void notFound_throws() {
            when(cisReturnRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cisPdfService.generatePaymentDeductionStatement(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("throws IllegalStateException when return has no lines")
        void noLines_throws() {
            CISReturn empty = CISReturn.builder().id(2L).taxMonth("2025-04")
                    .status(CisReturnStatus.DRAFT).build();
            when(cisReturnRepository.findById(2L)).thenReturn(Optional.of(empty));

            assertThatThrownBy(() -> cisPdfService.generatePaymentDeductionStatement(2L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("No return lines");
        }

        @Test
        @DisplayName("returns non-empty byte array for a valid DRAFT return")
        void draft_returnsPdfBytes() {
            when(cisReturnRepository.findById(1L)).thenReturn(Optional.of(buildReturn(CisReturnStatus.DRAFT)));

            byte[] pdf = cisPdfService.generatePaymentDeductionStatement(1L);

            assertThat(pdf).isNotEmpty();
        }

        @Test
        @DisplayName("PDF starts with the PDF magic bytes %%PDF")
        void pdf_hasMagicHeader() {
            when(cisReturnRepository.findById(1L)).thenReturn(Optional.of(buildReturn(CisReturnStatus.DRAFT)));

            byte[] pdf = cisPdfService.generatePaymentDeductionStatement(1L);

            // PDF files always begin with %PDF
            String header = new String(pdf, 0, 4);
            assertThat(header).isEqualTo("%PDF");
        }

        @Test
        @DisplayName("PDF is larger than 2 KB (not a trivially empty document)")
        void pdf_hasSubstantialSize() {
            when(cisReturnRepository.findById(1L)).thenReturn(Optional.of(buildReturn(CisReturnStatus.DRAFT)));

            byte[] pdf = cisPdfService.generatePaymentDeductionStatement(1L);

            assertThat(pdf.length).isGreaterThan(2048);
        }

        @Test
        @DisplayName("works for a SUBMITTED return with receipt ref and submission timestamp")
        void submitted_includesReceiptRef() {
            CISReturn submitted = buildReturn(CisReturnStatus.SUBMITTED);
            submitted.setHmrcReceiptRef("HMRC-1-" + System.currentTimeMillis());
            submitted.setSubmittedAt(LocalDateTime.now());
            when(cisReturnRepository.findById(1L)).thenReturn(Optional.of(submitted));

            byte[] pdf = cisPdfService.generatePaymentDeductionStatement(1L);

            assertThat(pdf).isNotEmpty();
            assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
        }

        @Test
        @DisplayName("handles subcontractor with null UTR gracefully")
        void nullUtr_noException() {
            Company noUtr = Company.builder().id(20L).name("No UTR Ltd").build();
            CISReturn r = CISReturn.builder().id(3L).taxMonth("2025-04").status(CisReturnStatus.DRAFT).build();
            CISReturnLine line = CISReturnLine.builder()
                    .id(2L).cisReturn(r).subcontractor(noUtr)
                    .grossPaid(new BigDecimal("1000.00")).cisRate(new BigDecimal("20"))
                    .deduction(new BigDecimal("200.00")).netPaid(new BigDecimal("800.00"))
                    .build();
            r.getCisReturnLines().add(line);
            when(cisReturnRepository.findById(3L)).thenReturn(Optional.of(r));

            assertThatNoException().isThrownBy(() -> cisPdfService.generatePaymentDeductionStatement(3L));
        }

        @Test
        @DisplayName("handles multiple lines and produces a single PDF document")
        void multipleLines_singlePdf() {
            Company company2 = Company.builder().id(11L).name("Beta Civils Ltd").utr("9876543210").build();
            CISReturn r = CISReturn.builder().id(4L).taxMonth("2025-04").status(CisReturnStatus.DRAFT).build();

            CISReturnLine line1 = CISReturnLine.builder()
                    .id(1L).cisReturn(r).subcontractor(testCompany)
                    .grossPaid(new BigDecimal("3000.00")).cisRate(new BigDecimal("20"))
                    .deduction(new BigDecimal("600.00")).netPaid(new BigDecimal("2400.00")).build();
            CISReturnLine line2 = CISReturnLine.builder()
                    .id(2L).cisReturn(r).subcontractor(company2)
                    .grossPaid(new BigDecimal("2000.00")).cisRate(new BigDecimal("30"))
                    .deduction(new BigDecimal("600.00")).netPaid(new BigDecimal("1400.00")).build();
            r.getCisReturnLines().add(line1);
            r.getCisReturnLines().add(line2);
            when(cisReturnRepository.findById(4L)).thenReturn(Optional.of(r));

            byte[] pdf = cisPdfService.generatePaymentDeductionStatement(4L);

            assertThat(pdf).isNotEmpty();
            assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
        }
    }
}
