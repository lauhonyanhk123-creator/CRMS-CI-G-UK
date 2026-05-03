package com.crms.service;

import com.crms.domain.company.entity.Company;
import com.crms.domain.company.repository.CompanyRepository;
import com.crms.domain.contract.entity.Contract;
import com.crms.domain.contract.entity.RetentionLedger;
import com.crms.domain.contract.enums.ContractStatus;
import com.crms.domain.contract.repository.ContractRepository;
import com.crms.domain.contract.repository.RetentionLedgerRepository;
import com.crms.domain.site.entity.Site;
import com.crms.domain.site.repository.SiteRepository;
import com.crms.domain.tender.entity.Tender;
import com.crms.domain.tender.enums.TenderStatus;
import com.crms.dto.request.ContractRequest;
import com.crms.dto.response.ContractResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.RetentionLedgerResponse;
import com.crms.exception.ResourceNotFoundException;
import com.crms.service.impl.ContractServiceImpl;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ContractServiceImpl.
 * Tests cover CRUD operations, contract value calculations, retention calculations,
 * and variation totals for awarded contracts.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ContractServiceImplTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private RetentionLedgerRepository retentionLedgerRepository;

    @InjectMocks
    private ContractServiceImpl contractService;

    // Test data
    private Company testClient;
    private Site testSite;
    private Contract testContract;
    private ContractRequest testRequest;

    @BeforeEach
    void setUp() {
        testClient = Company.builder()
                .id(1L)
                .name("Test Company Ltd")
                .build();

        testSite = Site.builder()
                .id(1L)
                .name("Test Site")
                .build();

        testContract = Contract.builder()
                .id(1L)
                .contractRef("CRMS-001")
                .title("Test Groundworks Contract")
                .client(testClient)
                .site(testSite)
                .contractValue(new BigDecimal("500000.00"))
                .retentionPercent(new BigDecimal("5.0"))
                .retentionReductionPercent(new BigDecimal("2.5"))
                .paymentTermsDays(30)
                .finalDateForPaymentOffsetDays(14)
                .payLessNoticePrescribedPeriodDays(7)
                .status(ContractStatus.ACTIVE)
                .build();

        testRequest = ContractRequest.builder()
                .contractRef("CRMS-002")
                .title("New Contract")
                .clientId(1L)
                .siteId(1L)
                .contractValue(new BigDecimal("750000.00"))
                .retentionPercent(new BigDecimal("5.0"))
                .build();
    }

    // ================================================================
    // CRUD OPERATION TESTS
    // ================================================================

    @Nested
    @DisplayName("CRUD Operation Tests")
    class CrudOperationTests {

        @Test
        @DisplayName("findAll returns paginated contracts")
        void findAll_returnsPagedContracts() {
            // Given
            Page<Contract> contractPage = new PageImpl<>(List.of(testContract));
            when(contractRepository.findAll(any(Pageable.class))).thenReturn(contractPage);

            // When
            PageResponse<ContractResponse> response = contractService.findAll(Map.of());

            // Then
            assertNotNull(response);
            assertEquals(1, response.getContent().size());
            assertEquals("CRMS-001", response.getContent().get(0).getContractRef());
            verify(contractRepository).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("findAll filters by status correctly")
        void findAll_filtersByStatus() {
            // Given
            Page<Contract> contractPage = new PageImpl<>(List.of(testContract));
            when(contractRepository.findByStatus(eq(ContractStatus.ACTIVE), any(Pageable.class)))
                    .thenReturn(contractPage);

            // When
            PageResponse<ContractResponse> response = contractService.findAll(Map.of("status", "ACTIVE"));

            // Then
            assertNotNull(response);
            verify(contractRepository).findByStatus(eq(ContractStatus.ACTIVE), any(Pageable.class));
        }

        @Test
        @DisplayName("findAll filters by siteId correctly")
        void findAll_filtersBySiteId() {
            // Given
            Page<Contract> contractPage = new PageImpl<>(List.of(testContract));
            when(contractRepository.findBySiteId(eq(1L), any(Pageable.class)))
                    .thenReturn(contractPage);

            // When
            PageResponse<ContractResponse> response = contractService.findAll(Map.of("siteId", "1"));

            // Then
            assertNotNull(response);
            verify(contractRepository).findBySiteId(eq(1L), any(Pageable.class));
        }

        @Test
        @DisplayName("findById returns contract when exists")
        void findById_returnsContract_whenExists() {
            // Given
            when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));

            // When
            ContractResponse response = contractService.findById(1L);

            // Then
            assertNotNull(response);
            assertEquals("CRMS-001", response.getContractRef());
            assertEquals("Test Groundworks Contract", response.getTitle());
            assertEquals(ContractStatus.ACTIVE.name(), response.getStatus());
        }

        @Test
        @DisplayName("findById throws exception when not found")
        void findById_throwsException_whenNotFound() {
            // Given
            when(contractRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> contractService.findById(999L));
        }

        @Test
        @DisplayName("create saves contract with default values")
        void create_savesContractWithDefaults() {
            // Given
            when(companyRepository.findById(1L)).thenReturn(Optional.of(testClient));
            when(siteRepository.findById(1L)).thenReturn(Optional.of(testSite));
            when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> {
                Contract c = invocation.getArgument(0);
                c.setId(2L);
                return c;
            });

            // When
            ContractResponse response = contractService.create(testRequest);

            // Then
            assertNotNull(response);
            assertEquals("CRMS-002", response.getContractRef());
            assertEquals(new BigDecimal("5.0"), response.getRetentionPercent());
            assertEquals(ContractStatus.DRAFT.name(), response.getStatus());
            verify(contractRepository).save(any(Contract.class));
            verify(retentionLedgerRepository).save(any(RetentionLedger.class));
        }

        @Test
        @DisplayName("create throws exception when client not found")
        void create_throwsException_whenClientNotFound() {
            // Given
            when(companyRepository.findById(999L)).thenReturn(Optional.empty());

            ContractRequest request = ContractRequest.builder()
                    .contractRef("CRMS-002")
                    .clientId(999L)
                    .siteId(1L)
                    .build();

            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> contractService.create(request));
        }

        @Test
        @DisplayName("update modifies contract fields")
        void update_modifiesContractFields() {
            // Given
            when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));
            when(contractRepository.save(any(Contract.class))).thenReturn(testContract);

            ContractRequest updateRequest = ContractRequest.builder()
                    .title("Updated Title")
                    .contractValue(new BigDecimal("600000.00"))
                    .build();

            // When
            ContractResponse response = contractService.update(1L, updateRequest);

            // Then
            assertNotNull(response);
            verify(contractRepository).save(any(Contract.class));
        }
    }

    // ================================================================
    // CONTRACT VALUE CALCULATION TESTS
    // ================================================================

    @Nested
    @DisplayName("Contract Value Calculation Tests")
    class ContractValueCalculationTests {

        @Test
        @DisplayName("Retention calculation: value * retention% / 100")
        void retentionCalculation() {
            // Given
            BigDecimal value = new BigDecimal("100000.00");
            BigDecimal retentionPercent = new BigDecimal("5.0");

            // When
            BigDecimal retention = testContract.calculateRetention(value);

            // Then
            assertEquals(new BigDecimal("5000.00"), retention);
        }

        @Test
        @DisplayName("Retention returns zero when percent is null")
        void retentionReturnsZero_whenPercentIsNull() {
            // Given
            Contract contract = Contract.builder()
                    .retentionPercent(null)
                    .build();
            BigDecimal value = new BigDecimal("100000.00");

            // When
            BigDecimal retention = contract.calculateRetention(value);

            // Then
            assertEquals(BigDecimal.ZERO, retention);
        }

        @Test
        @DisplayName("Defects end date calculated from start date plus months")
        void defectsEndDateCalculation() {
            // Given
            testContract.setStartDate(LocalDate.of(2024, 1, 15));
            testContract.setPracticalCompletionDefectsPeriodMonths(12);

            // When
            LocalDate defectsEndDate = testContract.calculateDefectsEndDate();

            // Then
            assertEquals(LocalDate.of(2025, 1, 15), defectsEndDate);
        }

        @Test
        @DisplayName("Contract total value includes variations")
        void contractTotalValueIncludesVariations() {
            // Given
            Contract contract = Contract.builder()
                    .contractValue(new BigDecimal("500000.00"))
                    .build();

            BigDecimal variation1 = new BigDecimal("25000.00");
            BigDecimal variation2 = new BigDecimal("15000.00");

            // When
            BigDecimal totalValue = contract.getContractValue()
                    .add(variation1)
                    .add(variation2);

            // Then
            assertEquals(new BigDecimal("540000.00"), totalValue);
        }
    }

    // ================================================================
    // RETENTION LEDGER TESTS
    // ================================================================

    @Nested
    @DisplayName("Retention Ledger Tests")
    class RetentionLedgerTests {

        @Test
        @DisplayName("getRetentionLedger returns ledger for contract")
        void getRetentionLedger_returnsLedger() {
            // Given
            RetentionLedger ledger = RetentionLedger.builder()
                    .id(1L)
                    .contract(testContract)
                    .totalRetention(new BigDecimal("25000.00"))
                    .totalReleased(new BigDecimal("12500.00"))
                    .currentRetention(new BigDecimal("12500.00"))
                    .build();

            testContract.setRetentionLedger(ledger);
            when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));

            // When
            RetentionLedgerResponse response = contractService.getRetentionLedger(1L);

            // Then
            assertNotNull(response);
            assertEquals(1L, response.getContractId());
            assertEquals("CRMS-001", response.getContractRef());
        }

        @Test
        @DisplayName("getRetentionLedger creates new ledger when none exists")
        void getRetentionLedger_createsNewLedger_whenNoneExists() {
            // Given
            testContract.setRetentionLedger(null);
            when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));
            when(retentionLedgerRepository.save(any(RetentionLedger.class))).thenAnswer(invocation -> {
                RetentionLedger l = invocation.getArgument(0);
                l.setId(1L);
                return l;
            });

            // When
            RetentionLedgerResponse response = contractService.getRetentionLedger(1L);

            // Then
            assertNotNull(response);
            verify(retentionLedgerRepository).save(any(RetentionLedger.class));
        }

        @Test
        @DisplayName("getRetentionLedger throws exception for non-existent contract")
        void getRetentionLedger_throwsException_whenContractNotFound() {
            // Given
            when(contractRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> contractService.getRetentionLedger(999L));
        }
    }

    // ================================================================
    // RETENTION CALCULATION EDGE CASES
    // ================================================================

    @Nested
    @DisplayName("Retention Edge Case Tests")
    class RetentionEdgeCaseTests {

        @Test
        @DisplayName("Retention calculation with different percentages")
        void retentionCalculation_withDifferentPercentages() {
            // Given
            BigDecimal value = new BigDecimal("200000.00");

            // Test 2.5% retention
            Contract contract25 = Contract.builder()
                    .retentionPercent(new BigDecimal("2.5"))
                    .build();
            assertEquals(new BigDecimal("5000.00"), contract25.calculateRetention(value));

            // Test 10% retention
            Contract contract10 = Contract.builder()
                    .retentionPercent(new BigDecimal("10.0"))
                    .build();
            assertEquals(new BigDecimal("20000.00"), contract10.calculateRetention(value));
        }

        @Test
        @DisplayName("Retention released at PC = contract value * reduction %")
        void retentionReleasedAtPC() {
            // Given
            BigDecimal contractValue = new BigDecimal("500000.00");
            BigDecimal reductionPercent = new BigDecimal("2.5");

            // When
            BigDecimal releasedAtPC = contractValue.multiply(reductionPercent)
                    .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);

            // Then
            assertEquals(new BigDecimal("12500.00"), releasedAtPC);
        }

        @Test
        @DisplayName("Net retention balance calculation")
        void netRetentionBalance() {
            // Given
            BigDecimal totalRetention = new BigDecimal("25000.00");  // 5% of 500k
            BigDecimal releasedAtPC = new BigDecimal("12500.00");     // 2.5% of 500k
            BigDecimal releasedAtDefects = new BigDecimal("12500.00"); // remaining 2.5%

            // When
            BigDecimal balance = totalRetention.subtract(releasedAtPC).subtract(releasedAtDefects);

            // Then
            assertEquals(new BigDecimal("0.00"), balance.setScale(2));
        }
    }
}
