package com.crms.service.quality;

import com.crms.domain.contract.entity.Contract;
import com.crms.domain.contract.repository.ContractRepository;
import com.crms.domain.quality.entity.Defect;
import com.crms.domain.quality.enums.DefectPriority;
import com.crms.domain.quality.enums.DefectStatus;
import com.crms.domain.quality.repository.DefectRepository;
import com.crms.dto.request.quality.DefectRequest;
import com.crms.dto.response.quality.DefectResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DefectServiceImpl}.
 *
 * Covers: create, updateStatus, assignOperative, assignContractor, and delete.
 * No Spring context is loaded — all dependencies are Mockito mocks.
 */
@ExtendWith(MockitoExtension.class)
class DefectServiceImplTest {

    // -------------------------------------------------------------------------
    //  Mocks
    // -------------------------------------------------------------------------

    @Mock private DefectRepository repository;
    @Mock private ContractRepository contractRepository;

    @InjectMocks
    private DefectServiceImpl service;

    // -------------------------------------------------------------------------
    //  Shared fixtures
    // -------------------------------------------------------------------------

    private Contract contract;

    @BeforeEach
    void setUpSharedFixtures() {
        contract = new Contract();
        contract.setId(1L);
        contract.setContractRef("CTR-TEST-001");
    }

    // =========================================================================
    //  create()
    // =========================================================================

    @Nested
    @DisplayName("create()")
    class CreateTests {

        private DefectRequest baseRequest() {
            DefectRequest req = new DefectRequest();
            req.setTitle("Cracked kerb on Plot 12");
            req.setContractId(1L);
            req.setDescription("150 mm longitudinal crack on concrete kerb unit.");
            req.setLocation("Plot 12 — eastern boundary");
            return req;
        }

        @Test
        @DisplayName("creates defect linked to the correct contract")
        void create_setsContractFromRepository() {
            when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
            when(repository.save(any())).thenAnswer(inv -> {
                Defect d = inv.getArgument(0);
                d.setId(100L);
                return d;
            });

            DefectResponse response = service.create(baseRequest());

            assertThat(response.getContractId()).isEqualTo(1L);
            assertThat(response.getContractRef()).isEqualTo("CTR-TEST-001");
        }

        @Test
        @DisplayName("sets title from the request")
        void create_setsTitleFromRequest() {
            when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            DefectResponse response = service.create(baseRequest());

            assertThat(response.getTitle()).isEqualTo("Cracked kerb on Plot 12");
        }

        @Test
        @DisplayName("defaults status to OPEN when request does not specify a status")
        void create_noStatusInRequest_defaultsToOPEN() {
            when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            DefectResponse response = service.create(baseRequest());

            assertThat(response.getStatus()).isEqualTo(DefectStatus.OPEN);
        }

        @Test
        @DisplayName("defaults priority to MEDIUM when request does not specify a priority")
        void create_noPriorityInRequest_defaultsToMEDIUM() {
            when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            DefectResponse response = service.create(baseRequest());

            assertThat(response.getPriority()).isEqualTo(DefectPriority.MEDIUM);
        }

        @Test
        @DisplayName("honours explicit HIGH priority when supplied in request")
        void create_explicitPriority_isPreserved() {
            DefectRequest req = baseRequest();
            req.setPriority(DefectPriority.HIGH);

            when(contractRepository.findById(1L)).thenReturn(Optional.of(contract));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            DefectResponse response = service.create(req);

            assertThat(response.getPriority()).isEqualTo(DefectPriority.HIGH);
        }

        @Test
        @DisplayName("throws RuntimeException when contract not found")
        void create_contractNotFound_throwsRuntimeException() {
            when(contractRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create(baseRequest()))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Contract not found");

            verify(repository, never()).save(any());
        }
    }

    // =========================================================================
    //  updateStatus()
    // =========================================================================

    @Nested
    @DisplayName("updateStatus()")
    class UpdateStatusTests {

        private Defect openDefect() {
            return Defect.builder()
                    .id(200L)
                    .title("Broken paving slab")
                    .contract(contract)
                    .description("Raised slab — trip hazard.")
                    .location("Car park — row C")
                    .status(DefectStatus.OPEN)
                    .build();
        }

        @Test
        @DisplayName("transition to RESOLVED sets resolvedDate to today")
        void updateStatus_toRESOLVED_setsResolvedDate() {
            Defect defect = openDefect();
            when(repository.findById(200L)).thenReturn(Optional.of(defect));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            DefectResponse response = service.updateStatus(200L, "RESOLVED");

            assertThat(response.getStatus()).isEqualTo(DefectStatus.RESOLVED);
            assertThat(response.getResolvedDate()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("transition to IN_PROGRESS is allowed and does not set resolvedDate")
        void updateStatus_toIN_PROGRESS_isAllowed_noResolvedDate() {
            Defect defect = openDefect();
            when(repository.findById(200L)).thenReturn(Optional.of(defect));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            DefectResponse response = service.updateStatus(200L, "IN_PROGRESS");

            assertThat(response.getStatus()).isEqualTo(DefectStatus.IN_PROGRESS);
            assertThat(response.getResolvedDate()).isNull();
        }

        @Test
        @DisplayName("throws IllegalArgumentException for unknown status string")
        void updateStatus_unknownStatusString_throwsIllegalArgumentException() {
            Defect defect = openDefect();
            when(repository.findById(200L)).thenReturn(Optional.of(defect));

            assertThatThrownBy(() -> service.updateStatus(200L, "BOGUS_STATUS"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("throws RuntimeException when defect not found")
        void updateStatus_defectNotFound_throwsRuntimeException() {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateStatus(999L, "RESOLVED"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Defect not found");
        }
    }

    // =========================================================================
    //  assignOperative()
    // =========================================================================

    @Nested
    @DisplayName("assignOperative()")
    class AssignOperativeTests {

        private Defect openDefect() {
            return Defect.builder()
                    .id(300L)
                    .title("Pothole on access road")
                    .contract(contract)
                    .description("Pothole 200 mm x 150 mm.")
                    .location("Access road junction")
                    .status(DefectStatus.OPEN)
                    .build();
        }

        @Test
        @DisplayName("sets assignedOperative field to supplied operative name")
        void assignOperative_setsOperativeName() {
            Defect defect = openDefect();
            when(repository.findById(300L)).thenReturn(Optional.of(defect));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            DefectResponse response = service.assignOperative(300L, "John Smith");

            assertThat(response.getAssignedOperative()).isEqualTo("John Smith");
        }

        @Test
        @DisplayName("transitions status from OPEN to IN_PROGRESS when assigning operative")
        void assignOperative_whenStatusIsOPEN_transitionsToIN_PROGRESS() {
            Defect defect = openDefect();
            when(repository.findById(300L)).thenReturn(Optional.of(defect));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            DefectResponse response = service.assignOperative(300L, "John Smith");

            assertThat(response.getStatus()).isEqualTo(DefectStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("does not change status to IN_PROGRESS when status is already RESOLVED")
        void assignOperative_whenAlreadyRESOLVED_doesNotChangeStatus() {
            Defect defect = Defect.builder()
                    .id(301L)
                    .title("Minor crack")
                    .contract(contract)
                    .description("Small surface crack.")
                    .location("Plot 5")
                    .status(DefectStatus.RESOLVED)
                    .build();

            when(repository.findById(301L)).thenReturn(Optional.of(defect));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            DefectResponse response = service.assignOperative(301L, "Jane Doe");

            assertThat(response.getStatus()).isEqualTo(DefectStatus.RESOLVED);
        }
    }

    // =========================================================================
    //  assignContractor()
    // =========================================================================

    @Nested
    @DisplayName("assignContractor()")
    class AssignContractorTests {

        private Defect openDefect() {
            return Defect.builder()
                    .id(400L)
                    .title("Damaged fence panel")
                    .contract(contract)
                    .description("Fence panel missing.")
                    .location("Northern boundary")
                    .status(DefectStatus.OPEN)
                    .build();
        }

        @Test
        @DisplayName("sets assignedContractor field to supplied contractor name")
        void assignContractor_setsContractorName() {
            Defect defect = openDefect();
            when(repository.findById(400L)).thenReturn(Optional.of(defect));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            DefectResponse response = service.assignContractor(400L, "BuildFix Ltd");

            assertThat(response.getAssignedContractor()).isEqualTo("BuildFix Ltd");
        }

        @Test
        @DisplayName("transitions status from OPEN to IN_PROGRESS when assigning contractor")
        void assignContractor_whenStatusIsOPEN_transitionsToIN_PROGRESS() {
            Defect defect = openDefect();
            when(repository.findById(400L)).thenReturn(Optional.of(defect));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            DefectResponse response = service.assignContractor(400L, "BuildFix Ltd");

            assertThat(response.getStatus()).isEqualTo(DefectStatus.IN_PROGRESS);
        }
    }

    // =========================================================================
    //  delete()
    // =========================================================================

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("calls repository.deleteById when defect exists")
        void delete_defectExists_callsDeleteById() {
            when(repository.existsById(500L)).thenReturn(true);

            service.delete(500L);

            verify(repository).deleteById(500L);
        }

        @Test
        @DisplayName("throws RuntimeException without calling deleteById when defect not found")
        void delete_defectNotFound_throwsRuntimeException() {
            when(repository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> service.delete(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Defect not found");

            verify(repository, never()).deleteById(any());
        }
    }
}
