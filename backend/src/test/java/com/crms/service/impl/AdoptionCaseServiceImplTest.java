package com.crms.service.impl;

import com.crms.domain.adoption.entity.AdoptionCase;
import com.crms.domain.adoption.entity.AdoptionStage;
import com.crms.domain.adoption.enums.AdoptionStatus;
import com.crms.domain.adoption.enums.AdoptionType;
import com.crms.domain.adoption.enums.StageStatus;
import com.crms.domain.adoption.repository.AdoptionCaseRepository;
import com.crms.domain.adoption.repository.AdoptionStageRepository;
import com.crms.domain.adoption.repository.BondRepository;
import com.crms.domain.adoption.repository.CommutedSumMovementRepository;
import com.crms.domain.adoption.repository.SnaggingItemRepository;
import com.crms.domain.company.entity.Company;
import com.crms.domain.contract.entity.Contract;
import com.crms.domain.company.repository.CompanyRepository;
import com.crms.domain.contract.repository.ContractRepository;
import com.crms.dto.request.AdoptionCaseRequest;
import com.crms.dto.request.AdoptionStageRequest;
import com.crms.dto.response.AdoptionCaseResponse;
import com.crms.dto.response.AdoptionStageResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AdoptionCaseServiceImpl}.
 *
 * Covers the core business-logic paths: create, updateStatus, delete, and
 * addStage. All repository interactions are Mockito mocks; no Spring context
 * is loaded.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdoptionCaseServiceImplTest {

    // -------------------------------------------------------------------------
    //  Mocks
    // -------------------------------------------------------------------------

    @Mock private AdoptionCaseRepository adoptionCaseRepository;
    @Mock private AdoptionStageRepository adoptionStageRepository;
    @Mock private SnaggingItemRepository snaggingItemRepository;
    @Mock private BondRepository bondRepository;
    @Mock private CommutedSumMovementRepository commutedSumMovementRepository;
    @Mock private ContractRepository contractRepository;
    @Mock private CompanyRepository companyRepository;

    @InjectMocks
    private AdoptionCaseServiceImpl service;

    // -------------------------------------------------------------------------
    //  Shared fixtures
    // -------------------------------------------------------------------------

    private Contract contract;
    private Company client;
    private Company localAuthority;

    @BeforeEach
    void setUpSharedFixtures() {
        contract = new Contract();
        contract.setId(10L);
        contract.setContractRef("CTR-001");

        client = new Company();
        client.setId(20L);
        client.setName("Client Ltd");

        localAuthority = new Company();
        localAuthority.setId(30L);
        localAuthority.setName("City Council");
    }

    // =========================================================================
    //  create()
    // =========================================================================

    @Nested
    @DisplayName("create()")
    class CreateTests {

        private AdoptionCaseRequest baseRequest() {
            return AdoptionCaseRequest.builder()
                    .caseRef("AC-2024-001")
                    .adoptionType(AdoptionType.SECTION_38)
                    .contractId(10L)
                    .clientId(20L)
                    .localAuthorityOrWaterAuthorityId(30L)
                    .build();
        }

        private void stubLookups() {
            when(adoptionCaseRepository.existsByCaseRef(any())).thenReturn(false);
            when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));
            when(companyRepository.findById(20L)).thenReturn(Optional.of(client));
            when(companyRepository.findById(30L)).thenReturn(Optional.of(localAuthority));
            when(adoptionCaseRepository.save(any())).thenAnswer(inv -> {
                AdoptionCase c = inv.getArgument(0);
                c.setId(1L);
                return c;
            });
        }

        @Test
        @DisplayName("throws ValidationException when caseRef already exists")
        void create_duplicateCaseRef_throwsValidationException() {
            AdoptionCaseRequest request = baseRequest();
            when(adoptionCaseRepository.existsByCaseRef("AC-2024-001")).thenReturn(true);

            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("AC-2024-001");

            verify(adoptionCaseRepository, never()).save(any());
        }

        @Test
        @DisplayName("defaults status to PRE_APP when none supplied in request")
        void create_noStatusInRequest_defaultsToPREAPP() {
            stubLookups();

            AdoptionCaseResponse response = service.create(baseRequest());

            assertThat(response.getStatus()).isEqualTo(AdoptionStatus.PRE_APP);
            assertThat(response.getCaseRef()).isEqualTo("AC-2024-001");
        }

        @Test
        @DisplayName("calculates maintenanceEndDate from commencementDate + maintenancePeriodMonths when endDate omitted")
        void create_commencementDateAndPeriodProvided_calculatesMaintenanceEndDate() {
            LocalDate commencementDate = LocalDate.of(2024, 1, 1);

            AdoptionCaseRequest request = AdoptionCaseRequest.builder()
                    .caseRef("AC-2024-002")
                    .adoptionType(AdoptionType.SECTION_38)
                    .contractId(10L)
                    .clientId(20L)
                    .localAuthorityOrWaterAuthorityId(30L)
                    .commencementDate(commencementDate)
                    .maintenancePeriodMonths(12)
                    .build();

            when(adoptionCaseRepository.existsByCaseRef(any())).thenReturn(false);
            when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));
            when(companyRepository.findById(20L)).thenReturn(Optional.of(client));
            when(companyRepository.findById(30L)).thenReturn(Optional.of(localAuthority));
            when(adoptionCaseRepository.save(any())).thenAnswer(inv -> {
                AdoptionCase c = inv.getArgument(0); c.setId(1L); return c;
            });

            AdoptionCaseResponse response = service.create(request);

            assertThat(response.getMaintenanceEndDate())
                    .isEqualTo(commencementDate.plusMonths(12));
        }

        @Test
        @DisplayName("explicit maintenanceEndDate is not overwritten by calculated value")
        void create_explicitMaintenanceEndDate_isNotOverwritten() {
            LocalDate commencementDate = LocalDate.of(2024, 1, 1);
            LocalDate explicitEndDate  = LocalDate.of(2026, 6, 30);

            AdoptionCaseRequest request = AdoptionCaseRequest.builder()
                    .caseRef("AC-2024-003")
                    .adoptionType(AdoptionType.SECTION_38)
                    .contractId(10L)
                    .clientId(20L)
                    .localAuthorityOrWaterAuthorityId(30L)
                    .commencementDate(commencementDate)
                    .maintenancePeriodMonths(12)
                    .maintenanceEndDate(explicitEndDate)
                    .build();

            when(adoptionCaseRepository.existsByCaseRef(any())).thenReturn(false);
            when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));
            when(companyRepository.findById(20L)).thenReturn(Optional.of(client));
            when(companyRepository.findById(30L)).thenReturn(Optional.of(localAuthority));
            when(adoptionCaseRepository.save(any())).thenAnswer(inv -> {
                AdoptionCase c = inv.getArgument(0); c.setId(1L); return c;
            });

            AdoptionCaseResponse response = service.create(request);

            assertThat(response.getMaintenanceEndDate()).isEqualTo(explicitEndDate);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when contract not found")
        void create_contractNotFound_throwsResourceNotFoundException() {
            when(adoptionCaseRepository.existsByCaseRef(any())).thenReturn(false);
            when(contractRepository.findById(10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create(baseRequest()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when client company not found")
        void create_clientNotFound_throwsResourceNotFoundException() {
            when(adoptionCaseRepository.existsByCaseRef(any())).thenReturn(false);
            when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));
            when(companyRepository.findById(20L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create(baseRequest()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // =========================================================================
    //  updateStatus()
    // =========================================================================

    @Nested
    @DisplayName("updateStatus()")
    class UpdateStatusTests {

        /** Builds a minimal AdoptionCase with the supplied status. */
        private AdoptionCase caseWithStatus(AdoptionStatus status) {
            AdoptionCase ac = AdoptionCase.builder()
                    .id(99L)
                    .caseRef("AC-TEST")
                    .adoptionType(AdoptionType.SECTION_38)
                    .contract(contract)
                    .client(client)
                    .localAuthorityOrWaterAuthority(localAuthority)
                    .commutedSumPaid(BigDecimal.ZERO)
                    .build();
            ac.setStatus(status);
            return ac;
        }

        @Test
        @DisplayName("valid forward transition PRE_APP -> APPLICATION succeeds")
        void updateStatus_validForwardTransition_succeeds() {
            AdoptionCase ac = caseWithStatus(AdoptionStatus.PRE_APP);
            when(adoptionCaseRepository.findById(99L)).thenReturn(Optional.of(ac));
            when(adoptionCaseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            AdoptionCaseResponse response = service.updateStatus(99L, AdoptionStatus.APPLICATION);

            assertThat(response.getStatus()).isEqualTo(AdoptionStatus.APPLICATION);
        }

        @Test
        @DisplayName("invalid transition PRE_APP -> DESIGN throws ValidationException")
        void updateStatus_illegalTransition_PRE_APP_to_DESIGN_throws() {
            AdoptionCase ac = caseWithStatus(AdoptionStatus.PRE_APP);
            when(adoptionCaseRepository.findById(99L)).thenReturn(Optional.of(ac));

            assertThatThrownBy(() -> service.updateStatus(99L, AdoptionStatus.DESIGN))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Invalid status transition");

            verify(adoptionCaseRepository, never()).save(any());
        }

        @Test
        @DisplayName("invalid transition CONSTRUCTION -> ADOPTION throws ValidationException")
        void updateStatus_illegalTransition_CONSTRUCTION_to_ADOPTION_throws() {
            AdoptionCase ac = caseWithStatus(AdoptionStatus.CONSTRUCTION);
            when(adoptionCaseRepository.findById(99L)).thenReturn(Optional.of(ac));

            assertThatThrownBy(() -> service.updateStatus(99L, AdoptionStatus.ADOPTION))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Invalid status transition");
        }

        @Test
        @DisplayName("COMPLETED is a terminal state — no transition is allowed")
        void updateStatus_fromCompleted_alwaysThrows() {
            AdoptionCase ac = caseWithStatus(AdoptionStatus.COMPLETED);
            when(adoptionCaseRepository.findById(99L)).thenReturn(Optional.of(ac));

            assertThatThrownBy(() -> service.updateStatus(99L, AdoptionStatus.ADOPTION))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Invalid status transition");
        }

        @Test
        @DisplayName("permitted backward transition APPLICATION -> PRE_APP succeeds")
        void updateStatus_backwardTransition_APPLICATION_to_PREAPP_succeeds() {
            AdoptionCase ac = caseWithStatus(AdoptionStatus.APPLICATION);
            when(adoptionCaseRepository.findById(99L)).thenReturn(Optional.of(ac));
            when(adoptionCaseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            AdoptionCaseResponse response = service.updateStatus(99L, AdoptionStatus.PRE_APP);

            assertThat(response.getStatus()).isEqualTo(AdoptionStatus.PRE_APP);
        }

        @Test
        @DisplayName("transition to MAINTENANCE auto-calculates maintenanceEndDate when commencement and period are set")
        void updateStatus_toMaintenance_calculatesMaintenanceEndDate() {
            LocalDate commencementDate = LocalDate.of(2024, 3, 1);
            AdoptionCase ac = caseWithStatus(AdoptionStatus.CONSTRUCTION);
            ac.setCommencementDate(commencementDate);
            ac.setMaintenancePeriodMonths(6);

            when(adoptionCaseRepository.findById(99L)).thenReturn(Optional.of(ac));
            when(adoptionCaseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            AdoptionCaseResponse response = service.updateStatus(99L, AdoptionStatus.MAINTENANCE);

            assertThat(response.getMaintenanceEndDate())
                    .isEqualTo(commencementDate.plusMonths(6));
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when adoption case id does not exist")
        void updateStatus_caseNotFound_throwsResourceNotFoundException() {
            when(adoptionCaseRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateStatus(999L, AdoptionStatus.APPLICATION))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // =========================================================================
    //  delete()
    // =========================================================================

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        private AdoptionCase caseWithStatus(AdoptionStatus status) {
            AdoptionCase ac = new AdoptionCase();
            ac.setId(50L);
            ac.setStatus(status);
            return ac;
        }

        @Test
        @DisplayName("deletes successfully when status is PRE_APP")
        void delete_statusPreApp_deletesSuccessfully() {
            AdoptionCase ac = caseWithStatus(AdoptionStatus.PRE_APP);
            when(adoptionCaseRepository.findById(50L)).thenReturn(Optional.of(ac));

            service.delete(50L);

            verify(adoptionCaseRepository).delete(ac);
        }

        @Test
        @DisplayName("deletes successfully when status is APPLICATION")
        void delete_statusApplication_deletesSuccessfully() {
            AdoptionCase ac = caseWithStatus(AdoptionStatus.APPLICATION);
            when(adoptionCaseRepository.findById(50L)).thenReturn(Optional.of(ac));

            service.delete(50L);

            verify(adoptionCaseRepository).delete(ac);
        }

        @Test
        @DisplayName("throws ValidationException when status is DESIGN")
        void delete_statusDesign_throwsValidationException() {
            AdoptionCase ac = caseWithStatus(AdoptionStatus.DESIGN);
            when(adoptionCaseRepository.findById(50L)).thenReturn(Optional.of(ac));

            assertThatThrownBy(() -> service.delete(50L))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Cannot delete adoption case");

            verify(adoptionCaseRepository, never()).delete(any());
        }

        @Test
        @DisplayName("throws ValidationException when status is MAINTENANCE")
        void delete_statusMaintenance_throwsValidationException() {
            AdoptionCase ac = caseWithStatus(AdoptionStatus.MAINTENANCE);
            when(adoptionCaseRepository.findById(50L)).thenReturn(Optional.of(ac));

            assertThatThrownBy(() -> service.delete(50L))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Cannot delete adoption case");
        }

        @Test
        @DisplayName("throws ValidationException when status is COMPLETED")
        void delete_statusCompleted_throwsValidationException() {
            AdoptionCase ac = caseWithStatus(AdoptionStatus.COMPLETED);
            when(adoptionCaseRepository.findById(50L)).thenReturn(Optional.of(ac));

            assertThatThrownBy(() -> service.delete(50L))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Cannot delete adoption case");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when case does not exist")
        void delete_caseNotFound_throwsResourceNotFoundException() {
            when(adoptionCaseRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.delete(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // =========================================================================
    //  addStage()
    // =========================================================================

    @Nested
    @DisplayName("addStage()")
    class AddStageTests {

        private AdoptionCase existingCase() {
            AdoptionCase ac = AdoptionCase.builder()
                    .id(70L)
                    .caseRef("AC-STAGE-TEST")
                    .adoptionType(AdoptionType.SECTION_38)
                    .contract(contract)
                    .client(client)
                    .localAuthorityOrWaterAuthority(localAuthority)
                    .commutedSumPaid(BigDecimal.ZERO)
                    .build();
            ac.setStatus(AdoptionStatus.DESIGN);
            return ac;
        }

        private AdoptionStageRequest stageRequest(int order) {
            return AdoptionStageRequest.builder()
                    .stageName("Stage " + order)
                    .stageOrder(order)
                    .build();
        }

        @Test
        @DisplayName("throws ValidationException when stage order already exists for the same adoption case")
        void addStage_duplicateOrder_throwsValidationException() {
            AdoptionCase ac = existingCase();

            when(adoptionCaseRepository.findById(70L)).thenReturn(Optional.of(ac));

            AdoptionStage existingStage = AdoptionStage.builder()
                    .id(5L)
                    .adoptionCase(ac)
                    .stageName("Existing Stage")
                    .stageOrder(1)
                    .build();
            when(adoptionStageRepository.findByAdoptionCaseIdAndStageOrder(70L, 1))
                    .thenReturn(Optional.of(existingStage));

            assertThatThrownBy(() -> service.addStage(70L, stageRequest(1)))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Stage order already exists");

            verify(adoptionStageRepository, never()).save(any());
        }

        @Test
        @DisplayName("saves and returns stage response when stage order is unique")
        void addStage_uniqueOrder_stageIsSaved() {
            AdoptionCase ac = existingCase();

            when(adoptionCaseRepository.findById(70L)).thenReturn(Optional.of(ac));
            when(adoptionStageRepository.findByAdoptionCaseIdAndStageOrder(70L, 2))
                    .thenReturn(Optional.empty());
            when(adoptionStageRepository.save(any())).thenAnswer(inv -> {
                AdoptionStage s = inv.getArgument(0);
                s.setId(10L);
                return s;
            });

            AdoptionStageResponse response = service.addStage(70L, stageRequest(2));

            assertThat(response.getStageName()).isEqualTo("Stage 2");
            assertThat(response.getStageOrder()).isEqualTo(2);
            verify(adoptionStageRepository).save(any());
        }

        @Test
        @DisplayName("defaults stage status to PENDING when no status provided in request")
        void addStage_noStatusProvided_defaultsToPending() {
            AdoptionCase ac = existingCase();
            AdoptionStageRequest request = AdoptionStageRequest.builder()
                    .stageName("Initial Survey")
                    .stageOrder(1)
                    .build();

            when(adoptionCaseRepository.findById(70L)).thenReturn(Optional.of(ac));
            when(adoptionStageRepository.findByAdoptionCaseIdAndStageOrder(70L, 1))
                    .thenReturn(Optional.empty());
            when(adoptionStageRepository.save(any())).thenAnswer(inv -> {
                AdoptionStage s = inv.getArgument(0);
                s.setId(20L);
                return s;
            });

            AdoptionStageResponse response = service.addStage(70L, request);

            assertThat(response.getStatus()).isEqualTo(StageStatus.PENDING);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when adoption case does not exist")
        void addStage_caseNotFound_throwsResourceNotFoundException() {
            when(adoptionCaseRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.addStage(999L, stageRequest(1)))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
