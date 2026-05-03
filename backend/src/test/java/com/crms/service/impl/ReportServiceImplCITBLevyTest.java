package com.crms.service.impl;

import com.crms.domain.contract.entity.Contract;
import com.crms.domain.contract.enums.ContractStatus;
import com.crms.domain.contract.repository.ContractRepository;
import com.crms.domain.site.entity.Site;
import com.crms.domain.subcontractor.repository.CISReturnRepository;
import com.crms.domain.tender.entity.Tender;
import com.crms.domain.company.entity.Company;
import com.crms.domain.tender.repository.TenderRepository;
import com.crms.domain.operative.repository.TimesheetRepository;
import com.crms.dto.response.CITBLevyReport;
import com.crms.service.CvrService;
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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReportServiceImpl CITB Levy calculation.
 * CITB Levy = 0.5% of qualifying labour costs.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReportServiceImplCITBLevyTest {

    @Mock
    private CvrService cvrService;

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private CISReturnRepository cisReturnRepository;

    @Mock
    private TenderRepository tenderRepository;

    @Mock
    private TimesheetRepository timesheetRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    private Contract testContract;

    @BeforeEach
    void setUp() {
        // Setup test contract with site and labour value
        Site site = Site.builder()
                .id(1L)
                .name("Test Site")
                .build();

        Company client = Company.builder()
                .id(1L)
                .name("Test Client")
                .build();

        Tender tender = Tender.builder()
                .id(1L)
                .title("Test Tender")
                .client(client)
                .build();

        testContract = Contract.builder()
                .id(1L)
                .contractRef("CRMS-001")
                .title("Test Contract")
                .contractValue(new BigDecimal("500000.00"))
                .labourValue(new BigDecimal("100000.00"))
                .site(site)
                .tender(tender)
                .status(ContractStatus.IN_PROGRESS)
                .build();
    }

    @Nested
    @DisplayName("CITB Levy Monthly Period Tests")
    class CITBLevyMonthlyPeriodTests {

        @Test
        @DisplayName("getCITBLevy calculates levy using live wage data when available")
        void getCITBLevy_usesLiveWageData_whenAvailable() {
            // Given
            String period = "2024-01";
            BigDecimal liveWages = new BigDecimal("25000.00");
            
            when(contractRepository.findAll()).thenReturn(List.of(testContract));
            when(timesheetRepository.calculateTotalWagesBySiteAndPeriod(eq(1L), any(), any()))
                    .thenReturn(liveWages);

            // When
            Object result = reportService.getCITBLevy(period);

            // Then
            assertNotNull(result);
            assertTrue(result instanceof List);
            List<CITBLevyReport> reports = (List<CITBLevyReport>) result;
            assertEquals(1, reports.size());
            
            CITBLevyReport report = reports.get(0);
            assertEquals(new BigDecimal("25000.00"), report.getOperativeWages());
            assertTrue(report.isUsedLiveWageData());
            // CITB Levy = 25000 * 0.5% = 125.00
            assertEquals(new BigDecimal("125.00"), report.getCitbLevy());
        }

        @Test
        @DisplayName("getCITBLevy falls back to contract labour value when no timesheet data")
        void getCITBLevy_fallsBackToContractLabourValue() {
            // Given
            String period = "2024-01";
            
            when(contractRepository.findAll()).thenReturn(List.of(testContract));
            when(timesheetRepository.calculateTotalWagesBySiteAndPeriod(eq(1L), any(), any()))
                    .thenReturn(null);

            // When
            Object result = reportService.getCITBLevy(period);

            // Then
            List<CITBLevyReport> reports = (List<CITBLevyReport>) result;
            CITBLevyReport report = reports.get(0);
            
            assertEquals(new BigDecimal("100000.00"), report.getContractLabourValue());
            assertFalse(report.isUsedLiveWageData());
            // CITB Levy = 100000 * 0.5% = 500.00
            assertEquals(new BigDecimal("500.00"), report.getCitbLevy());
        }

        @Test
        @DisplayName("getCITBLevy handles zero operative wages")
        void getCITBLevy_handlesZeroOperativeWages() {
            // Given
            String period = "2024-01";
            
            when(contractRepository.findAll()).thenReturn(List.of(testContract));
            when(timesheetRepository.calculateTotalWagesBySiteAndPeriod(eq(1L), any(), any()))
                    .thenReturn(BigDecimal.ZERO);

            // When
            Object result = reportService.getCITBLevy(period);

            // Then
            List<CITBLevyReport> reports = (List<CITBLevyReport>) result;
            CITBLevyReport report = reports.get(0);
            
            assertFalse(report.isUsedLiveWageData());
            // Falls back to contract labour value
            assertEquals(new BigDecimal("500.00"), report.getCitbLevy());
        }
    }

    @Nested
    @DisplayName("CITB Levy Quarterly Period Tests")
    class CITBLevyQuarterlyPeriodTests {

        @Test
        @DisplayName("getCITBLevy parses Q1 format correctly")
        void getCITBLevy_parsesQ1Format() {
            // Given
            String period = "2024-Q1"; // January-March 2024
            
            when(contractRepository.findAll()).thenReturn(List.of(testContract));
            when(timesheetRepository.calculateTotalWagesBySiteAndPeriod(eq(1L), any(), any()))
                    .thenReturn(new BigDecimal("75000.00"));

            // When
            Object result = reportService.getCITBLevy(period);

            // Then
            List<CITBLevyReport> reports = (List<CITBLevyReport>) result;
            CITBLevyReport report = reports.get(0);
            
            assertEquals(LocalDate.of(2024, 1, 1), report.getPeriodStart());
            assertEquals(LocalDate.of(2024, 3, 31), report.getPeriodEnd());
            // CITB Levy = 75000 * 0.5% = 375.00
            assertEquals(new BigDecimal("375.00"), report.getCitbLevy());
        }

        @Test
        @DisplayName("getCITBLevy parses Q4 format correctly")
        void getCITBLevy_parsesQ4Format() {
            // Given
            String period = "2024-Q4"; // October-December 2024
            
            when(contractRepository.findAll()).thenReturn(List.of(testContract));
            when(timesheetRepository.calculateTotalWagesBySiteAndPeriod(eq(1L), any(), any()))
                    .thenReturn(new BigDecimal("60000.00"));

            // When
            Object result = reportService.getCITBLevy(period);

            // Then
            List<CITBLevyReport> reports = (List<CITBLevyReport>) result;
            CITBLevyReport report = reports.get(0);
            
            assertEquals(LocalDate.of(2024, 10, 1), report.getPeriodStart());
            assertEquals(LocalDate.of(2024, 12, 31), report.getPeriodEnd());
            assertEquals(new BigDecimal("300.00"), report.getCitbLevy());
        }
    }

    @Nested
    @DisplayName("CITB Levy Rate Calculation Tests")
    class CITBLevyRateCalculationTests {

        @Test
        @DisplayName("CITB Levy rate is exactly 0.5%")
        void citbLevyRate_is0Point5Percent() {
            // Given
            String period = "2024-01";
            
            when(contractRepository.findAll()).thenReturn(List.of(testContract));
            when(timesheetRepository.calculateTotalWagesBySiteAndPeriod(eq(1L), any(), any()))
                    .thenReturn(new BigDecimal("10000.00"));

            // When
            Object result = reportService.getCITBLevy(period);

            // Then
            List<CITBLevyReport> reports = (List<CITBLevyReport>) result;
            CITBLevyReport report = reports.get(0);
            
            assertEquals(new BigDecimal("0.005"), report.getLevyRate());
            assertEquals(new BigDecimal("50.00"), report.getCitbLevy());
        }

        @Test
        @DisplayName("CITB Levy rounds to 2 decimal places")
        void citbLevy_roundsToTwoDecimalPlaces() {
            // Given
            String period = "2024-01";
            // 10001 * 0.5% = 50.005 -> should round to 50.01
            when(contractRepository.findAll()).thenReturn(List.of(testContract));
            when(timesheetRepository.calculateTotalWagesBySiteAndPeriod(eq(1L), any(), any()))
                    .thenReturn(new BigDecimal("10001.00"));

            // When
            Object result = reportService.getCITBLevy(period);

            // Then
            List<CITBLevyReport> reports = (List<CITBLevyReport>) result;
            CITBLevyReport report = reports.get(0);
            
            assertEquals(new BigDecimal("50.01"), report.getCitbLevy());
        }

        @Test
        @DisplayName("CITB Levy handles large labour costs")
        void citbLevy_handlesLargeLabourCosts() {
            // Given
            String period = "2024-01";
            testContract.setLabourValue(new BigDecimal("1000000.00")); // 1 million
            
            when(contractRepository.findAll()).thenReturn(List.of(testContract));
            when(timesheetRepository.calculateTotalWagesBySiteAndPeriod(eq(1L), any(), any()))
                    .thenReturn(BigDecimal.ZERO);

            // When
            Object result = reportService.getCITBLevy(period);

            // Then
            List<CITBLevyReport> reports = (List<CITBLevyReport>) result;
            CITBLevyReport report = reports.get(0);
            
            // 1000000 * 0.5% = 5000.00
            assertEquals(new BigDecimal("5000.00"), report.getCitbLevy());
        }

        @Test
        @DisplayName("CITB Levy handles zero labour costs")
        void citbLevy_handlesZeroLabourCosts() {
            // Given
            String period = "2024-01";
            testContract.setLabourValue(BigDecimal.ZERO);
            
            when(contractRepository.findAll()).thenReturn(List.of(testContract));
            when(timesheetRepository.calculateTotalWagesBySiteAndPeriod(eq(1L), any(), any()))
                    .thenReturn(BigDecimal.ZERO);

            // When
            Object result = reportService.getCITBLevy(period);

            // Then
            List<CITBLevyReport> reports = (List<CITBLevyReport>) result;
            CITBLevyReport report = reports.get(0);
            
            assertEquals(new BigDecimal("0.00"), report.getCitbLevy());
        }
    }

    @Nested
    @DisplayName("CITB Levy Edge Cases")
    class CITBLevyEdgeCases {

        @Test
        @DisplayName("getCITBLevy handles contract without site")
        void getCITBLevy_handlesNoSite() {
            // Given
            String period = "2024-01";
            testContract.setSite(null);
            
            when(contractRepository.findAll()).thenReturn(List.of(testContract));
            // timesheetRepository should NOT be called

            // When
            Object result = reportService.getCITBLevy(period);

            // Then
            List<CITBLevyReport> reports = (List<CITBLevyReport>) result;
            CITBLevyReport report = reports.get(0);
            
            assertFalse(report.isUsedLiveWageData());
            assertEquals(new BigDecimal("500.00"), report.getCitbLevy());
            verify(timesheetRepository, never()).calculateTotalWagesBySiteAndPeriod(any(), any(), any());
        }

        @Test
        @DisplayName("getCITBLevy handles multiple contracts")
        void getCITBLevy_handlesMultipleContracts() {
            // Given
            String period = "2024-01";
            Contract contract2 = Contract.builder()
                    .id(2L)
                    .contractRef("CRMS-002")
                    .title("Contract 2")
                    .contractValue(new BigDecimal("200000.00"))
                    .labourValue(new BigDecimal("40000.00"))
                    .status(ContractStatus.IN_PROGRESS)
                    .build();
            
            when(contractRepository.findAll()).thenReturn(List.of(testContract, contract2));
            when(timesheetRepository.calculateTotalWagesBySiteAndPeriod(any(), any(), any()))
                    .thenReturn(BigDecimal.ZERO);

            // When
            Object result = reportService.getCITBLevy(period);

            // Then
            List<CITBLevyReport> reports = (List<CITBLevyReport>) result;
            assertEquals(2, reports.size());
            
            // Contract 1: 100000 * 0.5% = 500
            assertEquals(new BigDecimal("500.00"), reports.get(0).getCitbLevy());
            // Contract 2: 40000 * 0.5% = 200
            assertEquals(new BigDecimal("200.00"), reports.get(1).getCitbLevy());
        }

        @Test
        @DisplayName("getCITBLevy handles invalid period format gracefully")
        void getCITBLevy_handlesInvalidPeriodFormat() {
            // Given
            String period = "invalid-period";
            
            when(contractRepository.findAll()).thenReturn(List.of(testContract));

            // When
            Object result = reportService.getCITBLevy(period);

            // Then - should return empty list on error
            assertNotNull(result);
            assertTrue(result instanceof List);
            assertTrue(((List<?>) result).isEmpty());
        }

        @Test
        @DisplayName("getCITBLevy returns empty list when no contracts")
        void getCITBLevy_returnsEmptyList_whenNoContracts() {
            // Given
            String period = "2024-01";
            when(contractRepository.findAll()).thenReturn(Collections.emptyList());

            // When
            Object result = reportService.getCITBLevy(period);

            // Then
            List<CITBLevyReport> reports = (List<CITBLevyReport>) result;
            assertTrue(reports.isEmpty());
        }

        @Test
        @DisplayName("getCITBLevy handles null labour value")
        void getCITBLevy_handlesNullLabourValue() {
            // Given
            String period = "2024-01";
            testContract.setLabourValue(null);
            
            when(contractRepository.findAll()).thenReturn(List.of(testContract));
            when(timesheetRepository.calculateTotalWagesBySiteAndPeriod(eq(1L), any(), any()))
                    .thenReturn(BigDecimal.ZERO);

            // When
            Object result = reportService.getCITBLevy(period);

            // Then
            List<CITBLevyReport> reports = (List<CITBLevyReport>) result;
            CITBLevyReport report = reports.get(0);
            
            // Null labour value treated as zero
            assertEquals(new BigDecimal("0.00"), report.getCitbLevy());
        }
    }
}
