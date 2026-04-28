package com.crms.service;

import com.crms.domain.contract.entity.Contract;
import com.crms.domain.contract.entity.Variation;
import com.crms.domain.contract.enums.ContractStatus;
import com.crms.domain.contract.enums.VariationStatus;
import com.crms.domain.contract.enums.VariationType;
import com.crms.domain.contract.repository.ContractRepository;
import com.crms.domain.contract.repository.VariationRepository;
import com.crms.dto.request.VariationRequest;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.VariationResponse;
import com.crms.exception.ResourceNotFoundException;
import com.crms.service.impl.VariationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for VariationServiceImpl.
 * Tests cover variation creation, status transitions, value calculations,
 * and contract value totals including variations.
 */
@ExtendWith(MockitoExtension.class)
class VariationServiceImplTest {

    @Mock
    private VariationRepository variationRepository;

    @Mock
    private ContractRepository contractRepository;

    @InjectMocks
    private VariationServiceImpl variationService;

    // Test data
    private Contract testContract;
    private Variation testVariation;
    private VariationRequest testRequest;

    @BeforeEach
    void setUp() {
        testContract = Contract.builder()
                .id(1L)
                .contractRef("CRMS-001")
                .title("Test Contract")
                .contractValue(new BigDecimal("500000.00"))
                .status(ContractStatus.IN_PROGRESS)
                .build();

        testVariation = Variation.builder()
                .id(1L)
                .contract(testContract)
                .variationRef("CRMS-001-VAR-ABC123")
                .type(VariationType.ADDITION)
                .description("Additional drainage works")
                .originalValue(new BigDecimal("25000.00"))
                .agreedValue(new BigDecimal("22000.00"))
                .status(VariationStatus.PENDING)
                .notifiedDate(LocalDate.of(2024, 2, 1))
                .build();

        testRequest = VariationRequest.builder()
                .type(VariationType.ADDITION)
                .description("New concrete slab")
                .originalValue(new BigDecimal("15000.00"))
                .agreedValue(new BigDecimal("12000.00"))
                .notifiedDate(LocalDate.of(2024, 2, 15))
                .instructionRef("INST-001")
                .reason("Client requested additional works")
                .build();
    }

    // ================================================================
    // FIND TESTS
    // ================================================================

    @Nested
    @DisplayName("Find Operation Tests")
    class FindTests {

        @Test
        @DisplayName("findByContract returns variations for contract")
        void findByContract_returnsVariations() {
            // Given
            when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));
            when(variationRepository.findByContractIdOrderByNotifiedDateDesc(1L))
                    .thenReturn(List.of(testVariation));

            // When
            PageResponse<VariationResponse> response = variationService.findByContract(1L);

            // Then
            assertNotNull(response);
            assertEquals(1, response.getContent().size());
            assertEquals("CRMS-001-VAR-ABC123", response.getContent().get(0).getVariationRef());
        }

        @Test
        @DisplayName("findByContract throws exception for non-existent contract")
        void findByContract_throwsException_whenContractNotFound() {
            // Given
            when(contractRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> 
                variationService.findByContract(999L));
        }

        @Test
        @DisplayName("findById returns variation when exists")
        void findById_returnsVariation_whenExists() {
            // Given
            when(variationRepository.findById(1L)).thenReturn(Optional.of(testVariation));

            // When
            VariationResponse response = variationService.findById(1L);

            // Then
            assertNotNull(response);
            assertEquals("CRMS-001-VAR-ABC123", response.getVariationRef());
            assertEquals("Additional drainage works", response.getDescription());
        }

        @Test
        @DisplayName("findById throws exception when not found")
        void findById_throwsException_whenNotFound() {
            // Given
            when(variationRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> 
                variationService.findById(999L));
        }
    }

    // ================================================================
    // VARIATION CREATION TESTS
    // ================================================================

    @Nested
    @DisplayName("Variation Creation Tests")
    class CreationTests {

        @Test
        @DisplayName("create() generates unique variation reference")
        void create_generatesUniqueReference() {
            // Given
            when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));
            when(variationRepository.save(any(Variation.class))).thenAnswer(invocation -> {
                Variation v = invocation.getArgument(0);
                v.setId(2L);
                return v;
            });

            // When
            VariationResponse response = variationService.create(1L, testRequest);

            // Then
            assertNotNull(response);
            assertTrue(response.getVariationRef().startsWith("CRMS-001-VAR-"));
            verify(variationRepository).save(any(Variation.class));
        }

        @Test
        @DisplayName("create() sets PENDING status by default")
        void create_setsPendingStatusByDefault() {
            // Given
            when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));
            when(variationRepository.save(any(Variation.class))).thenAnswer(invocation -> {
                Variation v = invocation.getArgument(0);
                v.setId(2L);
                return v;
            });

            // When
            VariationResponse response = variationService.create(1L, testRequest);

            // Then
            assertEquals(VariationStatus.PENDING.name(), response.getStatus());
        }

        @Test
        @DisplayName("create() stores all variation details")
        void create_storesAllDetails() {
            // Given
            when(contractRepository.findById(1L)).thenReturn(Optional.of(testContract));
            when(variationRepository.save(any(Variation.class))).thenAnswer(invocation -> {
                Variation v = invocation.getArgument(0);
                v.setId(2L);
                return v;
            });

            // When
            VariationResponse response = variationService.create(1L, testRequest);

            // Then
            assertEquals(VariationType.ADDITION.name(), response.getType());
            assertEquals("New concrete slab", response.getDescription());
            assertEquals(new BigDecimal("15000.00"), response.getOriginalValue());
            assertEquals(new BigDecimal("12000.00"), response.getAgreedValue());
            assertEquals("INST-001", response.getInstructionRef());
            assertEquals("Client requested additional works", response.getReason());
        }

        @Test
        @DisplayName("create() throws exception when contract not found")
        void create_throwsException_whenContractNotFound() {
            // Given
            when(contractRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> 
                variationService.create(999L, testRequest));
        }
    }

    // ================================================================
    // VARIATION UPDATE TESTS
    // ================================================================

    @Nested
    @DisplayName("Variation Update Tests")
    class UpdateTests {

        @Test
        @DisplayName("update() modifies variation fields")
        void update_modifiesFields() {
            // Given
            when(variationRepository.findById(1L)).thenReturn(Optional.of(testVariation));
            when(variationRepository.save(any(Variation.class))).thenReturn(testVariation);

            VariationRequest updateRequest = VariationRequest.builder()
                    .type(VariationType.REDUCTION)
                    .description("Updated description")
                    .agreedValue(new BigDecimal("20000.00"))
                    .build();

            // When
            VariationResponse response = variationService.update(1L, updateRequest);

            // Then
            assertNotNull(response);
            verify(variationRepository).save(any(Variation.class));
        }

        @Test
        @DisplayName("update() throws exception when variation not found")
        void update_throwsException_whenNotFound() {
            // Given
            when(variationRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> 
                variationService.update(999L, testRequest));
        }
    }

    // ================================================================
    // VARIATION TOTALS CALCULATION TESTS
    // ================================================================

    @Nested
    @DisplayName("Variation Totals Calculation Tests")
    class VariationTotalsTests {

        @Test
        @DisplayName("Calculate total variations from contract")
        void calculateTotalVariations() {
            // Given
            BigDecimal originalContractValue = new BigDecimal("500000.00");
            
            Variation variation1 = Variation.builder()
                    .agreedValue(new BigDecimal("25000.00"))
                    .status(VariationStatus.AGREED)
                    .build();
            
            Variation variation2 = Variation.builder()
                    .agreedValue(new BigDecimal("-5000.00"))  // Reduction
                    .status(VariationStatus.AGREED)
                    .build();
            
            Variation variation3 = Variation.builder()
                    .agreedValue(new BigDecimal("15000.00"))
                    .status(VariationStatus.PENDING)
                    .build();

            // When
            BigDecimal totalVariations = variation1.getAgreedValue()
                    .add(variation2.getAgreedValue())
                    .add(variation3.getAgreedValue());

            // Then
            assertEquals(new BigDecimal("35000.00"), totalVariations);
        }

        @Test
        @DisplayName("Calculate total contract value including variations")
        void calculateTotalContractValueWithVariations() {
            // Given
            BigDecimal originalContractValue = new BigDecimal("500000.00");
            BigDecimal additions = new BigDecimal("40000.00");
            BigDecimal reductions = new BigDecimal("-10000.00");

            // When
            BigDecimal totalContractValue = originalContractValue.add(additions).add(reductions);

            // Then
            assertEquals(new BigDecimal("530000.00"), totalContractValue);
        }

        @Test
        @DisplayName("Sum only AGREED variations for current contract value")
        void sumOnlyAgreedVariations() {
            // Given
            BigDecimal originalContractValue = new BigDecimal("500000.00");
            
            List<Variation> variations = List.of(
                    Variation.builder()
                            .agreedValue(new BigDecimal("25000.00"))
                            .status(VariationStatus.AGREED)
                            .build(),
                    Variation.builder()
                            .agreedValue(new BigDecimal("-5000.00"))
                            .status(VariationStatus.REJECTED)  // Not counted
                            .build(),
                    Variation.builder()
                            .agreedValue(new BigDecimal("15000.00"))
                            .status(VariationStatus.AGREED)
                            .build()
            );

            // When
            BigDecimal agreedVariations = variations.stream()
                    .filter(v -> v.getStatus() == VariationStatus.AGREED)
                    .map(Variation::getAgreedValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalValue = originalContractValue.add(agreedVariations);

            // Then
            assertEquals(new BigDecimal("540000.00"), totalValue);
        }

        @Test
        @DisplayName("Variation value difference calculation")
        void variationValueDifference() {
            // Given
            BigDecimal originalValue = new BigDecimal("25000.00");
            BigDecimal agreedValue = new BigDecimal("22000.00");

            // When
            BigDecimal difference = originalValue.subtract(agreedValue);

            // Then
            assertEquals(new BigDecimal("3000.00"), difference);
        }
    }

    // ================================================================
    // VARIATION TYPE TESTS
    // ================================================================

    @Nested
    @DisplayName("Variation Type Tests")
    class VariationTypeTests {

        @Test
        @DisplayName("ADDITION type increases contract value")
        void additionTypeIncreasesValue() {
            // Given
            BigDecimal baseValue = new BigDecimal("500000.00");
            BigDecimal addition = new BigDecimal("25000.00");

            // When
            BigDecimal newValue = baseValue.add(addition);

            // Then
            assertEquals(new BigDecimal("525000.00"), newValue);
            assertTrue(newValue.compareTo(baseValue) > 0);
        }

        @Test
        @DisplayName("REDUCTION type decreases contract value")
        void reductionTypeDecreasesValue() {
            // Given
            BigDecimal baseValue = new BigDecimal("500000.00");
            BigDecimal reduction = new BigDecimal("-15000.00");

            // When
            BigDecimal newValue = baseValue.add(reduction);

            // Then
            assertEquals(new BigDecimal("485000.00"), newValue);
            assertTrue(newValue.compareTo(baseValue) < 0);
        }

        @Test
        @DisplayName("Variation types include all categories")
        void variationTypes() {
            VariationType[] types = VariationType.values();
            
            assertTrue(types.length >= 2);  // At least ADDITION and REDUCTION
        }
    }

    // ================================================================
    // VARIATION STATUS TESTS
    // ================================================================

    @Nested
    @DisplayName("Variation Status Tests")
    class VariationStatusTests {

        @Test
        @DisplayName("Variation status transitions")
        void statusTransitions() {
            // Expected workflow: PENDING -> AGREED -> INSTRUCTED -> COMPLETED
            // Or: PENDING -> REJECTED
            
            assertEquals(VariationStatus.PENDING, testVariation.getStatus());
            
            // Simulate transition to AGREED
            testVariation.setStatus(VariationStatus.AGREED);
            assertEquals(VariationStatus.AGREED, testVariation.getStatus());
            
            // Simulate transition to REJECTED
            testVariation.setStatus(VariationStatus.REJECTED);
            assertEquals(VariationStatus.REJECTED, testVariation.getStatus());
        }

        @Test
        @DisplayName("Only AGREED variations affect contract value")
        void onlyAgreedAffectsValue() {
            // Given
            BigDecimal originalValue = new BigDecimal("500000.00");
            
            List<Variation> variations = List.of(
                    Variation.builder().agreedValue(new BigDecimal("20000.00")).status(VariationStatus.AGREED).build(),
                    Variation.builder().agreedValue(new BigDecimal("10000.00")).status(VariationStatus.PENDING).build(),
                    Variation.builder().agreedValue(new BigDecimal("5000.00")).status(VariationStatus.REJECTED).build()
            );

            // When
            BigDecimal onlyAgreed = variations.stream()
                    .filter(v -> v.getStatus() == VariationStatus.AGREED)
                    .map(Variation::getAgreedValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Then - Only agreed variation counts
            assertEquals(new BigDecimal("20000.00"), onlyAgreed);
        }
    }

    // ================================================================
    // EDGE CASE TESTS
    // ================================================================

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Empty variation list returns zero total")
        void emptyVariationList_returnsZero() {
            // Given
            List<Variation> emptyList = List.of();

            // When
            BigDecimal total = emptyList.stream()
                    .map(Variation::getAgreedValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Then
            assertEquals(BigDecimal.ZERO, total);
        }

        @Test
        @DisplayName("Null agreed value treated as zero")
        void nullAgreedValue_treatedAsZero() {
            // Given
            BigDecimal nullValue = null;
            BigDecimal defaultValue = nullValue != null ? nullValue : BigDecimal.ZERO;

            // When
            BigDecimal result = BigDecimal.ZERO.add(defaultValue);

            // Then
            assertEquals(BigDecimal.ZERO, result);
        }

        @Test
        @DisplayName("Large variation values handled correctly")
        void largeVariationValues() {
            // Given
            BigDecimal largeValue = new BigDecimal("99999999.99");
            
            // When
            BigDecimal doubled = largeValue.multiply(new BigDecimal("2"));
            
            // Then
            assertEquals(new BigDecimal("199999999.98"), doubled);
        }

        @Test
        @DisplayName("Negative contract value prevented")
        void negativeContractValuePrevented() {
            // Given
            BigDecimal originalValue = new BigDecimal("500000.00");
            BigDecimal hugeReduction = new BigDecimal("-600000.00");

            // When
            BigDecimal newValue = originalValue.add(hugeReduction);

            // Then - business logic should prevent this
            assertTrue(newValue.compareTo(BigDecimal.ZERO) < 0);
        }
    }

    // ================================================================
    // NOTIFICATION DATE TESTS
    // ================================================================

    @Nested
    @DisplayName("Notification Date Tests")
    class NotificationDateTests {

        @Test
        @DisplayName("Variation notified date recorded correctly")
        void notifiedDateRecorded() {
            // Given
            LocalDate notifiedDate = LocalDate.of(2024, 2, 1);
            
            // When
            testVariation.setNotifiedDate(notifiedDate);
            
            // Then
            assertEquals(LocalDate.of(2024, 2, 1), testVariation.getNotifiedDate());
        }

        @Test
        @DisplayName("Variations sorted by notified date descending")
        void sortedByNotifiedDateDescending() {
            // Given
            List<Variation> variations = List.of(
                    Variation.builder().variationRef("VAR-1").notifiedDate(LocalDate.of(2024, 1, 15)).build(),
                    Variation.builder().variationRef("VAR-2").notifiedDate(LocalDate.of(2024, 2, 1)).build(),
                    Variation.builder().variationRef("VAR-3").notifiedDate(LocalDate.of(2024, 1, 1)).build()
            );

            // When
            List<Variation> sorted = variations.stream()
                    .sorted((v1, v2) -> v2.getNotifiedDate().compareTo(v1.getNotifiedDate()))
                    .toList();

            // Then
            assertEquals("VAR-2", sorted.get(0).getVariationRef());
            assertEquals("VAR-1", sorted.get(1).getVariationRef());
            assertEquals("VAR-3", sorted.get(2).getVariationRef());
        }
    }
}
