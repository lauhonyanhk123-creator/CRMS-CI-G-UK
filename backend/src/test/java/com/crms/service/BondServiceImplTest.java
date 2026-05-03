package com.crms.service;

import com.crms.domain.adoption.entity.AdoptionCase;
import com.crms.domain.adoption.entity.Bond;
import com.crms.domain.adoption.enums.AdoptionType;
import com.crms.domain.adoption.enums.BondStatus;
import com.crms.domain.adoption.enums.BondType;
import com.crms.domain.adoption.repository.AdoptionCaseRepository;
import com.crms.domain.adoption.repository.BondRepository;
import com.crms.domain.company.entity.Company;
import com.crms.domain.company.repository.CompanyRepository;
import com.crms.domain.contract.entity.Contract;
import com.crms.domain.contract.enums.ContractStatus;
import com.crms.dto.request.BondRequest;
import com.crms.dto.response.BondResponse;
import com.crms.dto.response.PageResponse;
import com.crms.exception.ResourceNotFoundException;
import com.crms.exception.ValidationException;
import com.crms.service.impl.BondServiceImpl;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BondServiceImpl.
 * Tests cover bond CRUD operations, release workflows, expiry tracking,
 * and partial release functionality.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BondServiceImplTest {

    @Mock
    private BondRepository bondRepository;

    @Mock
    private AdoptionCaseRepository adoptionCaseRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private BondServiceImpl bondService;

    // Test data
    private Company testSurety;
    private Company testClient;
    private Contract testContract;
    private AdoptionCase testAdoptionCase;
    private Bond testBond;
    private BondRequest testRequest;

    @BeforeEach
    void setUp() {
        testSurety = Company.builder()
                .id(1L)
                .name("Surety Insurance Co")
                .build();

        testClient = Company.builder()
                .id(2L)
                .name("Test Client")
                .build();

        testContract = Contract.builder()
                .id(1L)
                .contractRef("CRMS-001")
                .status(ContractStatus.ACTIVE)
                .build();

        testAdoptionCase = AdoptionCase.builder()
                .id(1L)
                .caseRef("ADOPT-001")
                .adoptionType(AdoptionType.ADOPTION_AGREEMENT)
                .contract(testContract)
                .client(testClient)
                .localAuthorityOrWaterAuthority(testSurety)
                .build();

        testBond = Bond.builder()
                .id(1L)
                .bondNumber("BOND-001")
                .bondType(BondType.PERFORMANCE_BOND)
                .issuingSurety(testSurety)
                .bondValue(new BigDecimal("50000.00"))
                .issueDate(LocalDate.of(2024, 1, 1))
                .expiryDate(LocalDate.of(2025, 1, 1))
                .releaseConditions("Upon satisfactory completion of works")
                .status(BondStatus.ACTIVE)
                .adoptionCase(testAdoptionCase)
                .build();

        testRequest = BondRequest.builder()
                .bondNumber("BOND-002")
                .bondType(BondType.PERFORMANCE_BOND)
                .issuingSuretyId(1L)
                .bondValue(new BigDecimal("75000.00"))
                .issueDate(LocalDate.of(2024, 2, 1))
                .expiryDate(LocalDate.of(2025, 2, 1))
                .releaseConditions("Standard release conditions")
                .build();
    }

    // ================================================================
    // FIND TESTS
    // ================================================================

    @Nested
    @DisplayName("Find Operation Tests")
    class FindTests {

        @Test
        @DisplayName("findAll returns paginated bonds")
        void findAll_returnsPagedBonds() {
            // Given
            Page<Bond> bondPage = new PageImpl<>(List.of(testBond));
            when(bondRepository.findAll(any(Pageable.class))).thenReturn(bondPage);

            // When
            PageResponse<BondResponse> response = bondService.findAll(Map.of());

            // Then
            assertNotNull(response);
            assertEquals(1, response.getContent().size());
            assertEquals("BOND-001", response.getContent().get(0).getBondNumber());
            verify(bondRepository).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("findAll filters by status correctly")
        void findAll_filtersByStatus() {
            // Given
            Page<Bond> bondPage = new PageImpl<>(List.of(testBond));
            when(bondRepository.findByStatus(eq(BondStatus.ACTIVE), any(Pageable.class)))
                    .thenReturn(bondPage);

            // When
            PageResponse<BondResponse> response = bondService.findAll(Map.of("status", "ACTIVE"));

            // Then
            assertNotNull(response);
            verify(bondRepository).findByStatus(eq(BondStatus.ACTIVE), any(Pageable.class));
        }

        @Test
        @DisplayName("findAll filters by contractId correctly")
        void findAll_filtersByContractId() {
            // Given
            when(bondRepository.findByContractId(eq(1L)))
                    .thenReturn(List.of(testBond));

            // When
            PageResponse<BondResponse> response = bondService.findAll(Map.of("contractId", "1"));

            // Then
            assertNotNull(response);
            verify(bondRepository).findByContractId(eq(1L));
        }

        @Test
        @DisplayName("findById returns bond when exists")
        void findById_returnsBond_whenExists() {
            // Given
            when(bondRepository.findById(1L)).thenReturn(Optional.of(testBond));

            // When
            BondResponse response = bondService.findById(1L);

            // Then
            assertNotNull(response);
            assertEquals("BOND-001", response.getBondNumber());
            assertEquals(BondType.PERFORMANCE_BOND, response.getBondType());
        }

        @Test
        @DisplayName("findById throws exception when not found")
        void findById_throwsException_whenNotFound() {
            // Given
            when(bondRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> bondService.findById(999L));
        }

        @Test
        @DisplayName("findByBondNumber returns bond when exists")
        void findByBondNumber_returnsBond_whenExists() {
            // Given
            when(bondRepository.findByBondNumber("BOND-001")).thenReturn(Optional.of(testBond));

            // When
            BondResponse response = bondService.findByBondNumber("BOND-001");

            // Then
            assertNotNull(response);
            assertEquals("BOND-001", response.getBondNumber());
        }

        @Test
        @DisplayName("findByBondNumber throws exception when not found")
        void findByBondNumber_throwsException_whenNotFound() {
            // Given
            when(bondRepository.findByBondNumber("INVALID")).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> 
                bondService.findByBondNumber("INVALID"));
        }
    }

    // ================================================================
    // CREATE TESTS
    // ================================================================

    @Nested
    @DisplayName("Create Operation Tests")
    class CreateTests {

        @Test
        @DisplayName("create saves bond with valid data")
        void create_savesBond() {
            // Given
            when(adoptionCaseRepository.findById(1L)).thenReturn(Optional.of(testAdoptionCase));
            when(bondRepository.existsByBondNumber("BOND-002")).thenReturn(false);
            when(companyRepository.findById(1L)).thenReturn(Optional.of(testSurety));
            when(bondRepository.save(any(Bond.class))).thenAnswer(invocation -> {
                Bond b = invocation.getArgument(0);
                b.setId(2L);
                return b;
            });

            // When
            BondResponse response = bondService.create(1L, testRequest);

            // Then
            assertNotNull(response);
            assertEquals("BOND-002", response.getBondNumber());
            verify(bondRepository).save(any(Bond.class));
        }

        @Test
        @DisplayName("create sets ACTIVE status by default")
        void create_setsActiveStatusByDefault() {
            // Given
            BondRequest requestNoStatus = BondRequest.builder()
                    .bondNumber("BOND-003")
                    .bondType(BondType.PERFORMANCE_BOND)
                    .issuingSuretyId(1L)
                    .bondValue(new BigDecimal("50000.00"))
                    .build();

            when(adoptionCaseRepository.findById(1L)).thenReturn(Optional.of(testAdoptionCase));
            when(bondRepository.existsByBondNumber("BOND-003")).thenReturn(false);
            when(companyRepository.findById(1L)).thenReturn(Optional.of(testSurety));
            when(bondRepository.save(any(Bond.class))).thenAnswer(invocation -> {
                Bond b = invocation.getArgument(0);
                b.setId(3L);
                return b;
            });

            // When
            BondResponse response = bondService.create(1L, requestNoStatus);

            // Then
            assertEquals(BondStatus.ACTIVE, response.getStatus());
        }

        @Test
        @DisplayName("create throws exception when adoption case not found")
        void create_throwsException_whenAdoptionCaseNotFound() {
            // Given
            when(adoptionCaseRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> 
                bondService.create(999L, testRequest));
        }

        @Test
        @DisplayName("create throws exception when bond number already exists")
        void create_throwsException_whenBondNumberExists() {
            // Given
            when(adoptionCaseRepository.findById(1L)).thenReturn(Optional.of(testAdoptionCase));
            when(bondRepository.existsByBondNumber("BOND-002")).thenReturn(true);

            // When/Then
            assertThrows(ValidationException.class, () -> 
                bondService.create(1L, testRequest));
        }

        @Test
        @DisplayName("create throws exception when surety not found")
        void create_throwsException_whenSuretyNotFound() {
            // Given
            when(adoptionCaseRepository.findById(1L)).thenReturn(Optional.of(testAdoptionCase));
            when(bondRepository.existsByBondNumber("BOND-002")).thenReturn(false);
            when(companyRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> 
                bondService.create(1L, testRequest));
        }
    }

    // ================================================================
    // UPDATE TESTS
    // ================================================================

    @Nested
    @DisplayName("Update Operation Tests")
    class UpdateTests {

        @Test
        @DisplayName("update modifies bond fields")
        void update_modifiesFields() {
            // Given
            when(bondRepository.findById(1L)).thenReturn(Optional.of(testBond));
            when(bondRepository.save(any(Bond.class))).thenReturn(testBond);

            BondRequest updateRequest = BondRequest.builder()
                    .bondValue(new BigDecimal("60000.00"))
                    .expiryDate(LocalDate.of(2025, 6, 1))
                    .build();

            // When
            BondResponse response = bondService.update(1L, updateRequest);

            // Then
            assertNotNull(response);
            verify(bondRepository).save(any(Bond.class));
        }

        @Test
        @DisplayName("update throws exception when bond not found")
        void update_throwsException_whenNotFound() {
            // Given
            when(bondRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> 
                bondService.update(999L, testRequest));
        }

        @Test
        @DisplayName("update changes issuing surety when provided")
        void update_changesIssuingSurety() {
            // Given
            Company newSurety = Company.builder()
                    .id(2L)
                    .name("New Surety Co")
                    .build();

            when(bondRepository.findById(1L)).thenReturn(Optional.of(testBond));
            when(bondRepository.save(any(Bond.class))).thenReturn(testBond);
            when(companyRepository.findById(2L)).thenReturn(Optional.of(newSurety));

            BondRequest updateRequest = BondRequest.builder()
                    .issuingSuretyId(2L)
                    .build();

            // When
            bondService.update(1L, updateRequest);

            // Then
            verify(companyRepository).findById(2L);
            verify(bondRepository).save(any(Bond.class));
        }
    }

    // ================================================================
    // RELEASE TESTS
    // ================================================================

    @Nested
    @DisplayName("Release Operation Tests")
    class ReleaseTests {

        @Test
        @DisplayName("releaseBond releases active bond")
        void releaseBond_releasesActiveBond() {
            // Given
            when(bondRepository.findById(1L)).thenReturn(Optional.of(testBond));
            when(bondRepository.save(any(Bond.class))).thenAnswer(invocation -> {
                Bond b = invocation.getArgument(0);
                return b;
            });

            // When
            BondResponse response = bondService.releaseBond(1L, "Works completed satisfactorily");

            // Then
            assertNotNull(response);
            verify(bondRepository).save(any(Bond.class));
        }

        @Test
        @DisplayName("releaseBond sets RELEASE status and date")
        void releaseBond_setsReleaseStatusAndDate() {
            // Given
            when(bondRepository.findById(1L)).thenReturn(Optional.of(testBond));
            when(bondRepository.save(any(Bond.class))).thenReturn(testBond);

            // When
            bondService.releaseBond(1L, "Release conditions met");

            // Then
            verify(bondRepository).save(argThat(b -> 
                b.getStatus() == BondStatus.RELEASED && b.getReleaseDate() != null));
        }

        @Test
        @DisplayName("releaseBond throws exception when bond already released")
        void releaseBond_throwsException_whenAlreadyReleased() {
            // Given
            testBond.setStatus(BondStatus.RELEASED);
            when(bondRepository.findById(1L)).thenReturn(Optional.of(testBond));

            // When/Then
            assertThrows(ValidationException.class, () -> 
                bondService.releaseBond(1L, null));
        }

        @Test
        @DisplayName("releaseBond throws exception when bond not found")
        void releaseBond_throwsException_whenNotFound() {
            // Given
            when(bondRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> 
                bondService.releaseBond(999L, null));
        }

        @Test
        @DisplayName("partialRelease sets PARTIALLY_RELEASED status")
        void partialRelease_setsPartiallyReleasedStatus() {
            // Given
            when(bondRepository.findById(1L)).thenReturn(Optional.of(testBond));
            when(bondRepository.save(any(Bond.class))).thenAnswer(invocation -> {
                Bond b = invocation.getArgument(0);
                return b;
            });

            // When
            BondResponse response = bondService.partialRelease(1L, new BigDecimal("25000.00"));

            // Then
            assertNotNull(response);
            verify(bondRepository).save(argThat(b -> 
                b.getStatus() == BondStatus.PARTIALLY_RELEASED));
        }

        @Test
        @DisplayName("partialRelease throws exception when bond already released")
        void partialRelease_throwsException_whenAlreadyReleased() {
            // Given
            testBond.setStatus(BondStatus.RELEASED);
            when(bondRepository.findById(1L)).thenReturn(Optional.of(testBond));

            // When/Then
            assertThrows(ValidationException.class, () -> 
                bondService.partialRelease(1L, new BigDecimal("25000.00")));
        }

        @Test
        @DisplayName("partialRelease throws exception when release amount exceeds bond value")
        void partialRelease_throwsException_whenAmountExceedsValue() {
            // Given
            when(bondRepository.findById(1L)).thenReturn(Optional.of(testBond));

            // When/Then
            assertThrows(ValidationException.class, () -> 
                bondService.partialRelease(1L, new BigDecimal("100000.00")));
        }
    }

    // ================================================================
    // CALLED STATUS TESTS
    // ================================================================

    @Nested
    @DisplayName("Called Status Tests")
    class CalledStatusTests {

        @Test
        @DisplayName("markAsCalled sets CALLED status")
        void markAsCalled_setsCalledStatus() {
            // Given
            when(bondRepository.findById(1L)).thenReturn(Optional.of(testBond));
            when(bondRepository.save(any(Bond.class))).thenReturn(testBond);

            // When
            BondResponse response = bondService.markAsCalled(1L, "Default reason");

            // Then
            assertNotNull(response);
            verify(bondRepository).save(argThat(b -> b.getStatus() == BondStatus.CALLED));
        }

        @Test
        @DisplayName("markAsCalled throws exception when bond already called")
        void markAsCalled_throwsException_whenAlreadyCalled() {
            // Given
            testBond.setStatus(BondStatus.CALLED);
            when(bondRepository.findById(1L)).thenReturn(Optional.of(testBond));

            // When/Then
            assertThrows(ValidationException.class, () -> 
                bondService.markAsCalled(1L, "Reason"));
        }
    }

    // ================================================================
    // EXPIRY TESTS
    // ================================================================

    @Nested
    @DisplayName("Expiry Tracking Tests")
    class ExpiryTrackingTests {

        @Test
        @DisplayName("findExpiringBonds returns bonds expiring within days")
        void findExpiringBonds_returnsExpiringBonds() {
            // Given
            when(bondRepository.findBondsNeedingExpiryAlert(any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(List.of(testBond));

            // When
            PageResponse<BondResponse> response = bondService.findExpiringBonds(30);

            // Then
            assertNotNull(response);
            assertEquals(1, response.getContent().size());
            verify(bondRepository).findBondsNeedingExpiryAlert(any(LocalDate.class), any(LocalDate.class));
        }

        @Test
        @DisplayName("findExpiredBonds returns expired active bonds")
        void findExpiredBonds_returnsExpiredBonds() {
            // Given
            when(bondRepository.findExpiredActiveBonds(any(LocalDate.class)))
                    .thenReturn(List.of(testBond));

            // When
            PageResponse<BondResponse> response = bondService.findExpiredBonds();

            // Then
            assertNotNull(response);
            assertEquals(1, response.getContent().size());
            verify(bondRepository).findExpiredActiveBonds(any(LocalDate.class));
        }

        @Test
        @DisplayName("expiring bonds calculation includes today")
        void expiringBonds_calculationIncludesToday() {
            // Given
            LocalDate today = LocalDate.now();
            LocalDate alertDate = today.plusDays(30);

            when(bondRepository.findBondsNeedingExpiryAlert(eq(today), eq(alertDate)))
                    .thenReturn(List.of());

            // When
            bondService.findExpiringBonds(30);

            // Then
            verify(bondRepository).findBondsNeedingExpiryAlert(eq(today), eq(alertDate));
        }
    }

    // ================================================================
    // EDGE CASE TESTS
    // ================================================================

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Bond value calculation for partial release percentage")
        void bondValueCalculation_partialReleasePercentage() {
            // Given
            BigDecimal bondValue = new BigDecimal("50000.00");
            BigDecimal releasePercent = new BigDecimal("50");
            BigDecimal releaseAmount = bondValue.multiply(releasePercent)
                    .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);

            // Then
            assertEquals(new BigDecimal("25000.00"), releaseAmount);
        }

        @Test
        @DisplayName("Bond remaining value calculation")
        void bondRemainingValueCalculation() {
            // Given
            BigDecimal bondValue = new BigDecimal("50000.00");
            BigDecimal releasedAmount = new BigDecimal("25000.00");
            BigDecimal remaining = bondValue.subtract(releasedAmount);

            // Then
            assertEquals(new BigDecimal("25000.00"), remaining);
        }

        @Test
        @DisplayName("Null release conditions allowed in releaseBond")
        void nullReleaseConditionsAllowed() {
            // Given
            when(bondRepository.findById(1L)).thenReturn(Optional.of(testBond));
            when(bondRepository.save(any(Bond.class))).thenReturn(testBond);

            // When
            BondResponse response = bondService.releaseBond(1L, null);

            // Then
            assertNotNull(response);
        }
    }
}
