package com.crms.service;

import com.crms.domain.company.entity.Company;
import com.crms.domain.contract.entity.Contract;
import com.crms.domain.contract.enums.ContractStatus;
import com.crms.domain.contract.repository.ContractRepository;
import com.crms.domain.site.entity.Site;
import com.crms.domain.site.repository.SiteRepository;
import com.crms.domain.tender.entity.Tender;
import com.crms.domain.tender.enums.TenderStatus;
import com.crms.domain.tender.repository.TenderRepository;
import com.crms.dto.request.TenderRequest;
import com.crms.dto.response.ContractResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.TenderResponse;
import com.crms.exception.ResourceNotFoundException;
import com.crms.exception.ValidationException;
import com.crms.service.impl.TenderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
 * Unit tests for TenderServiceImpl.
 * Tests cover stage transitions: LEAD -> QUALIFIED -> PRICING -> SUBMITTED -> AWARDED/LOST
 * and tender lifecycle management.
 */
@ExtendWith(MockitoExtension.class)
class TenderServiceImplTest {

    @Mock
    private TenderRepository tenderRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private SiteRepository siteRepository;

    @Mock
    private ContractRepository contractRepository;

    @InjectMocks
    private TenderServiceImpl tenderService;

    // Test data
    private Company testClient;
    private Site testSite;
    private Tender testTender;
    private TenderRequest testRequest;

    @BeforeEach
    void setUp() {
        testClient = Company.builder()
                .id(1L)
                .name("Test Client Ltd")
                .build();

        testSite = Site.builder()
                .id(1L)
                .name("Test Site")
                .build();

        testTender = Tender.builder()
                .id(1L)
                .tenderRef("TND-2024-001")
                .title("Highway Works Tender")
                .description("Major highway improvement project")
                .client(testClient)
                .site(testSite)
                .status(TenderStatus.LEAD)
                .winProbability(new BigDecimal("50"))
                .tenderOwner("John Smith")
                .tenderIssuedDate(LocalDate.of(2024, 1, 1))
                .tenderReturnDate(LocalDate.of(2024, 3, 15))
                .build();

        testRequest = TenderRequest.builder()
                .tenderRef("TND-2024-002")
                .title("New Tender")
                .clientId(1L)
                .siteId(1L)
                .status(TenderStatus.LEAD)
                .winProbability(new BigDecimal("40"))
                .build();
    }

    // ================================================================
    // CRUD OPERATION TESTS
    // ================================================================

    @Nested
    @DisplayName("CRUD Operation Tests")
    class CrudOperationTests {

        @Test
        @DisplayName("findAll returns paginated tenders")
        void findAll_returnsPagedTenders() {
            // Given
            Page<Tender> tenderPage = new PageImpl<>(List.of(testTender));
            when(tenderRepository.findAll(any(Pageable.class))).thenReturn(tenderPage);

            // When
            PageResponse<TenderResponse> response = tenderService.findAll(Map.of());

            // Then
            assertNotNull(response);
            assertEquals(1, response.getContent().size());
            assertEquals("TND-2024-001", response.getContent().get(0).getTenderRef());
            verify(tenderRepository).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("findAll filters by status correctly")
        void findAll_filtersByStatus() {
            // Given
            Page<Tender> tenderPage = new PageImpl<>(List.of(testTender));
            when(tenderRepository.findByStatus(eq(TenderStatus.LEAD), any(Pageable.class)))
                    .thenReturn(tenderPage);

            // When
            PageResponse<TenderResponse> response = tenderService.findAll(Map.of("status", "LEAD"));

            // Then
            assertNotNull(response);
            verify(tenderRepository).findByStatus(eq(TenderStatus.LEAD), any(Pageable.class));
        }

        @Test
        @DisplayName("findById returns tender when exists")
        void findById_returnsTender_whenExists() {
            // Given
            when(tenderRepository.findById(1L)).thenReturn(Optional.of(testTender));

            // When
            TenderResponse response = tenderService.findById(1L);

            // Then
            assertNotNull(response);
            assertEquals("TND-2024-001", response.getTenderRef());
            assertEquals(TenderStatus.LEAD.name(), response.getStatus());
        }

        @Test
        @DisplayName("findById throws exception when not found")
        void findById_throwsException_whenNotFound() {
            // Given
            when(tenderRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> tenderService.findById(999L));
        }

        @Test
        @DisplayName("create saves tender with default LEAD status")
        void create_savesTenderWithDefaultStatus() {
            // Given
            when(companyRepository.findById(1L)).thenReturn(Optional.of(testClient));
            when(siteRepository.findById(1L)).thenReturn(Optional.of(testSite));
            when(tenderRepository.save(any(Tender.class))).thenAnswer(invocation -> {
                Tender t = invocation.getArgument(0);
                t.setId(2L);
                return t;
            });

            // When
            TenderResponse response = tenderService.create(testRequest);

            // Then
            assertNotNull(response);
            assertEquals("TND-2024-002", response.getTenderRef());
            assertEquals(TenderStatus.LEAD.name(), response.getStatus());
            verify(tenderRepository).save(any(Tender.class));
        }

        @Test
        @DisplayName("update modifies tender fields")
        void update_modifiesTenderFields() {
            // Given
            when(tenderRepository.findById(1L)).thenReturn(Optional.of(testTender));
            when(tenderRepository.save(any(Tender.class))).thenReturn(testTender);

            TenderRequest updateRequest = TenderRequest.builder()
                    .title("Updated Tender Title")
                    .winProbability(new BigDecimal("75"))
                    .build();

            // When
            TenderResponse response = tenderService.update(1L, updateRequest);

            // Then
            assertNotNull(response);
            verify(tenderRepository).save(any(Tender.class));
        }
    }

    // ================================================================
    // STAGE TRANSITION TESTS: LEAD -> QUALIFIED -> PRICING -> SUBMITTED -> AWARDED/LOST
    // ================================================================

    @Nested
    @DisplayName("Tender Stage Transition Tests")
    class StageTransitionTests {

        @Test
        @DisplayName("Tender starts in LEAD status")
        void tenderStartsInLeadStatus() {
            assertEquals(TenderStatus.LEAD, testTender.getStatus());
        }

        @Test
        @DisplayName("Tender can transition from LEAD to QUALIFIED")
        void tenderCanTransitionFromLeadToQualified() {
            // Given
            when(tenderRepository.findById(1L)).thenReturn(Optional.of(testTender));
            when(tenderRepository.save(any(Tender.class))).thenAnswer(invocation -> {
                Tender t = invocation.getArgument(0);
                t.setStatus(TenderStatus.QUALIFIED);
                return t;
            });

            // When
            TenderRequest updateRequest = TenderRequest.builder()
                    .status(TenderStatus.QUALIFIED)
                    .build();
            TenderResponse response = tenderService.update(1L, updateRequest);

            // Then
            assertNotNull(response);
            verify(tenderRepository).save(argThat(tender -> 
                tender.getStatus() == TenderStatus.QUALIFIED));
        }

        @Test
        @DisplayName("Tender can transition from QUALIFIED to PRICING")
        void tenderCanTransitionFromQualifiedToPricing() {
            // Given
            testTender.setStatus(TenderStatus.QUALIFIED);
            when(tenderRepository.findById(1L)).thenReturn(Optional.of(testTender));
            when(tenderRepository.save(any(Tender.class))).thenAnswer(invocation -> {
                Tender t = invocation.getArgument(0);
                return t;
            });

            // When
            TenderRequest updateRequest = TenderRequest.builder()
                    .status(TenderStatus.PRICING)
                    .tenderValueSubmitted(new BigDecimal("450000.00"))
                    .build();
            TenderResponse response = tenderService.update(1L, updateRequest);

            // Then
            assertNotNull(response);
            verify(tenderRepository).save(argThat(tender -> 
                tender.getStatus() == TenderStatus.PRICING));
        }

        @Test
        @DisplayName("Tender can transition from PRICING to SUBMITTED")
        void tenderCanTransitionFromPricingToSubmitted() {
            // Given
            testTender.setStatus(TenderStatus.PRICING);
            testTender.setTenderValueSubmitted(new BigDecimal("450000.00"));
            when(tenderRepository.findById(1L)).thenReturn(Optional.of(testTender));
            when(tenderRepository.save(any(Tender.class))).thenAnswer(invocation -> {
                Tender t = invocation.getArgument(0);
                return t;
            });

            // When
            TenderRequest updateRequest = TenderRequest.builder()
                    .status(TenderStatus.SUBMITTED)
                    .build();
            TenderResponse response = tenderService.update(1L, updateRequest);

            // Then
            assertNotNull(response);
            verify(tenderRepository).save(argThat(tender -> 
                tender.getStatus() == TenderStatus.SUBMITTED));
        }
    }

    // ================================================================
    // WIN/LOSE TRANSITION TESTS
    // ================================================================

    @Nested
    @DisplayName("Win/Lose Transition Tests")
    class WinLoseTransitionTests {

        @Test
        @DisplayName("win() transitions tender to AWARDED and creates contract")
        void win_transitionsToAwardedAndCreatesContract() {
            // Given
            testTender.setStatus(TenderStatus.SUBMITTED);
            testTender.setTenderValueSubmitted(new BigDecimal("450000.00"));

            when(tenderRepository.findById(1L)).thenReturn(Optional.of(testTender));
            when(tenderRepository.save(any(Tender.class))).thenReturn(testTender);
            when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> {
                Contract c = invocation.getArgument(0);
                c.setId(1L);
                return c;
            });

            // When
            ContractResponse response = tenderService.win(1L);

            // Then
            assertNotNull(response);
            verify(tenderRepository).save(argThat(tender -> 
                tender.getStatus() == TenderStatus.AWARDED));
            verify(contractRepository).save(argThat(contract -> 
                contract.getContractValue().equals(new BigDecimal("450000.00")) &&
                contract.getStatus() == ContractStatus.ACTIVE));
        }

        @Test
        @DisplayName("win() throws exception when tender already has contract")
        void win_throwsException_whenAlreadyHasContract() {
            // Given
            testTender.setContract(Contract.builder().id(1L).build());
            when(tenderRepository.findById(1L)).thenReturn(Optional.of(testTender));

            // When/Then
            assertThrows(ValidationException.class, () -> tenderService.win(1L));
        }

        @Test
        @DisplayName("win() throws exception when tender not found")
        void win_throwsException_whenTenderNotFound() {
            // Given
            when(tenderRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> tenderService.win(999L));
        }

        @Test
        @DisplayName("lose() transitions tender to LOST and stores reason")
        void lose_transitionsToLostAndStoresReason() {
            // Given
            when(tenderRepository.findById(1L)).thenReturn(Optional.of(testTender));
            when(tenderRepository.save(any(Tender.class))).thenReturn(testTender);

            String reason = "Client chose lower bid from competitor";

            // When
            tenderService.lose(1L, reason);

            // Then
            verify(tenderRepository).save(argThat(tender -> 
                tender.getStatus() == TenderStatus.LOST &&
                tender.getNotes().contains(reason)));
        }

        @Test
        @DisplayName("lose() appends reason to existing notes")
        void lose_appendsReasonToExistingNotes() {
            // Given
            testTender.setNotes("Initial notes about tender");
            when(tenderRepository.findById(1L)).thenReturn(Optional.of(testTender));
            when(tenderRepository.save(any(Tender.class))).thenReturn(testTender);

            // When
            tenderService.lose(1L, "Lost to competitor");

            // Then
            verify(tenderRepository).save(argThat(tender -> 
                tender.getNotes().contains("Initial notes about tender") &&
                tender.getNotes().contains("Lost: Lost to competitor")));
        }
    }

    // ================================================================
    // DELETE TESTS
    // ================================================================

    @Nested
    @DisplayName("Delete Tests")
    class DeleteTests {

        @Test
        @DisplayName("delete() removes tender when no contract exists")
        void delete_removesTender_whenNoContractExists() {
            // Given
            when(tenderRepository.findById(1L)).thenReturn(Optional.of(testTender));
            doNothing().when(tenderRepository).delete(testTender);

            // When
            tenderService.delete(1L);

            // Then
            verify(tenderRepository).delete(testTender);
        }

        @Test
        @DisplayName("delete() throws exception when tender has contract")
        void delete_throwsException_whenTenderHasContract() {
            // Given
            testTender.setContract(Contract.builder().id(1L).build());
            when(tenderRepository.findById(1L)).thenReturn(Optional.of(testTender));

            // When/Then
            assertThrows(ValidationException.class, () -> tenderService.delete(1L));
            verify(tenderRepository, never()).delete(any(Tender.class));
        }

        @Test
        @DisplayName("delete() throws exception when tender not found")
        void delete_throwsException_whenTenderNotFound() {
            // Given
            when(tenderRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThrows(ResourceNotFoundException.class, () -> tenderService.delete(999L));
        }
    }

    // ================================================================
    // CONTRACT CREATION FROM WINNING TENDER
    // ================================================================

    @Nested
    @DisplayName("Contract Creation from Winning Tender")
    class ContractCreationTests {

        @Test
        @DisplayName("win() creates contract with correct reference pattern")
        void win_createsContractWithCorrectReference() {
            // Given
            testTender.setStatus(TenderStatus.SUBMITTED);
            when(tenderRepository.findById(1L)).thenReturn(Optional.of(testTender));
            when(tenderRepository.save(any(Tender.class))).thenReturn(testTender);
            when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> {
                Contract c = invocation.getArgument(0);
                c.setId(1L);
                return c;
            });

            // When
            ContractResponse response = tenderService.win(1L);

            // Then
            assertNotNull(response);
            assertTrue(response.getContractRef().startsWith("C-"));
            assertEquals(testTender.getTitle(), response.getTitle());
            assertEquals(testTender.getClient().getId(), response.getClientId());
            assertEquals(testTender.getSite().getId(), response.getSiteId());
        }

        @Test
        @DisplayName("win() copies tender form and measurement standard")
        void win_copiesTenderDetailsToContract() {
            // Given
            testTender.setStatus(TenderStatus.SUBMITTED);
            testTender.setContractForm(com.crms.domain.tender.enums.ContractForm.NEC4);
            testTender.setMeasurementStandard(com.crms.domain.tender.enums.MeasurementStandard.CESMM4);

            when(tenderRepository.findById(1L)).thenReturn(Optional.of(testTender));
            when(tenderRepository.save(any(Tender.class))).thenReturn(testTender);
            when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> {
                Contract c = invocation.getArgument(0);
                c.setId(1L);
                return c;
            });

            // When
            ContractResponse response = tenderService.win(1L);

            // Then
            assertEquals("NEC4", response.getContractForm());
            assertEquals("CESMM4", response.getMeasurementStandard());
        }
    }
}
