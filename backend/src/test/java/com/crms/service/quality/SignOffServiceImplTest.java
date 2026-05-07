package com.crms.service.quality;

import com.crms.domain.contract.entity.Contract;
import com.crms.domain.contract.repository.ContractRepository;
import com.crms.domain.quality.entity.SignOff;
import com.crms.domain.quality.enums.BuildingControlType;
import com.crms.domain.quality.enums.SignOffResult;
import com.crms.domain.quality.repository.SignOffRepository;
import com.crms.dto.request.quality.SignOffRequest;
import com.crms.dto.response.quality.SignOffResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SignOffServiceImpl")
class SignOffServiceImplTest {

    @Mock
    private SignOffRepository repository;

    @Mock
    private ContractRepository contractRepository;

    @InjectMocks
    private SignOffServiceImpl service;

    private Contract contract;

    @BeforeEach
    void setUp() {
        contract = Contract.builder()
                .id(10L)
                .contractRef("CTR-001")
                .title("Test Contract")
                .build();
    }

    // ---------------------------------------------------------------------------
    // approve
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("approve()")
    class Approve {

        @Test
        @DisplayName("sets result to APPROVED with the supplied signature and today's date")
        void setsResultToApprovedWithSignatureAndTodayDate() {
            SignOff signOff = SignOff.builder()
                    .id(1L)
                    .contract(contract)
                    .buildingControlType(BuildingControlType.NHBC)
                    .inspectionType("Foundation")
                    .inspectionDate(LocalDate.now())
                    .result(SignOffResult.CONDITIONS)
                    .build();

            when(repository.findById(1L)).thenReturn(Optional.of(signOff));
            when(repository.save(any(SignOff.class))).thenAnswer(inv -> inv.getArgument(0));

            service.approve(1L, "data:image/png;base64,abc123");

            assertThat(signOff.getResult()).isEqualTo(SignOffResult.APPROVED);
            assertThat(signOff.getSignOffSignature()).isEqualTo("data:image/png;base64,abc123");
            assertThat(signOff.getSignOffDate()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("persists the sign-off after approval")
        void savesSignOffAfterApproval() {
            SignOff signOff = SignOff.builder()
                    .id(2L)
                    .contract(contract)
                    .buildingControlType(BuildingControlType.LABC)
                    .inspectionType("Roof")
                    .inspectionDate(LocalDate.now())
                    .result(SignOffResult.REFUSED)
                    .build();

            when(repository.findById(2L)).thenReturn(Optional.of(signOff));
            when(repository.save(any(SignOff.class))).thenAnswer(inv -> inv.getArgument(0));

            service.approve(2L, "signature-data");

            verify(repository).save(signOff);
        }

        @Test
        @DisplayName("response contains APPROVED result and the correct contract ID")
        void responseContainsApprovedResultAndCorrectContractId() {
            SignOff signOff = SignOff.builder()
                    .id(3L)
                    .contract(contract)
                    .buildingControlType(BuildingControlType.LOCAL_AUTHORITY)
                    .inspectionType("Drainage")
                    .inspectionDate(LocalDate.now())
                    .result(SignOffResult.CONDITIONS)
                    .build();

            when(repository.findById(3L)).thenReturn(Optional.of(signOff));
            when(repository.save(any(SignOff.class))).thenAnswer(inv -> inv.getArgument(0));

            SignOffResponse response = service.approve(3L, "sig");

            assertThat(response.getContractId()).isEqualTo(10L);
            assertThat(response.getResult()).isEqualTo(SignOffResult.APPROVED);
        }

        @Test
        @DisplayName("throws RuntimeException when sign-off is not found")
        void throwsWhenSignOffNotFoundOnApprove() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.approve(99L, "signature"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Sign-off not found: 99");

            verify(repository, never()).save(any());
        }
    }

    // ---------------------------------------------------------------------------
    // refuse
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("refuse()")
    class Refuse {

        @Test
        @DisplayName("sets result to REFUSED and stores the conditions text")
        void setsResultToRefusedAndStoresConditions() {
            SignOff signOff = SignOff.builder()
                    .id(4L)
                    .contract(contract)
                    .buildingControlType(BuildingControlType.NHBC)
                    .inspectionType("Structural")
                    .inspectionDate(LocalDate.now())
                    .result(SignOffResult.CONDITIONS)
                    .build();

            when(repository.findById(4L)).thenReturn(Optional.of(signOff));
            when(repository.save(any(SignOff.class))).thenAnswer(inv -> inv.getArgument(0));

            service.refuse(4L, "Brickwork not to specification");

            assertThat(signOff.getResult()).isEqualTo(SignOffResult.REFUSED);
            assertThat(signOff.getConditionsOrNotes()).isEqualTo("Brickwork not to specification");
        }

        @Test
        @DisplayName("persists the sign-off after refusal")
        void savesSignOffAfterRefusal() {
            SignOff signOff = SignOff.builder()
                    .id(5L)
                    .contract(contract)
                    .buildingControlType(BuildingControlType.LABC)
                    .inspectionType("Electrical")
                    .inspectionDate(LocalDate.now())
                    .result(SignOffResult.APPROVED)
                    .build();

            when(repository.findById(5L)).thenReturn(Optional.of(signOff));
            when(repository.save(any(SignOff.class))).thenAnswer(inv -> inv.getArgument(0));

            service.refuse(5L, "Wiring does not meet Part P");

            verify(repository).save(signOff);
        }

        @Test
        @DisplayName("response has REFUSED result and the conditions text")
        void refusalResponseHasRefusedResultAndConditions() {
            SignOff signOff = SignOff.builder()
                    .id(6L)
                    .contract(contract)
                    .buildingControlType(BuildingControlType.OTHER)
                    .inspectionType("Insulation")
                    .inspectionDate(LocalDate.now())
                    .result(SignOffResult.APPROVED)
                    .build();

            when(repository.findById(6L)).thenReturn(Optional.of(signOff));
            when(repository.save(any(SignOff.class))).thenAnswer(inv -> inv.getArgument(0));

            SignOffResponse response = service.refuse(6L, "Depth insufficient");

            assertThat(response.getResult()).isEqualTo(SignOffResult.REFUSED);
            assertThat(response.getConditionsOrNotes()).isEqualTo("Depth insufficient");
        }

        @Test
        @DisplayName("throws RuntimeException when sign-off is not found")
        void throwsWhenSignOffNotFoundOnRefuse() {
            when(repository.findById(88L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.refuse(88L, "Some conditions"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Sign-off not found: 88");

            verify(repository, never()).save(any());
        }
    }

    // ---------------------------------------------------------------------------
    // create
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("creates sign-off with correct contract and buildingControlType")
        void createsSignOffWithCorrectContractAndBuildingControlType() {
            SignOffRequest request = new SignOffRequest();
            request.setContractId(10L);
            request.setBuildingControlType(BuildingControlType.NHBC);
            request.setInspectionType("Foundation");
            request.setInspectionDate(LocalDate.of(2026, 1, 15));
            request.setResult(SignOffResult.APPROVED);

            SignOff saved = SignOff.builder()
                    .id(7L)
                    .contract(contract)
                    .buildingControlType(BuildingControlType.NHBC)
                    .inspectionType("Foundation")
                    .inspectionDate(LocalDate.of(2026, 1, 15))
                    .result(SignOffResult.APPROVED)
                    .build();

            when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));
            when(repository.save(any(SignOff.class))).thenReturn(saved);

            SignOffResponse response = service.create(request);

            assertThat(response.getContractId()).isEqualTo(10L);
            assertThat(response.getBuildingControlType()).isEqualTo(BuildingControlType.NHBC);
            assertThat(response.getInspectionType()).isEqualTo("Foundation");
            assertThat(response.getResult()).isEqualTo(SignOffResult.APPROVED);
        }

        @Test
        @DisplayName("persists all optional fields from the request")
        void savedSignOffHasAllFieldsFromRequest() {
            SignOffRequest request = new SignOffRequest();
            request.setContractId(10L);
            request.setBuildingControlType(BuildingControlType.LOCAL_AUTHORITY);
            request.setInspectionType("Drainage");
            request.setReferenceNumber("REF-2026-001");
            request.setInspectorName("Alan Smith");
            request.setInspectorEmail("alan@council.gov.uk");
            request.setInspectionDate(LocalDate.of(2026, 3, 10));
            request.setResult(SignOffResult.APPROVED);
            request.setNotes("All clear");

            when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));
            when(repository.save(any(SignOff.class))).thenAnswer(inv -> inv.getArgument(0));

            service.create(request);

            ArgumentCaptor<SignOff> captor = ArgumentCaptor.forClass(SignOff.class);
            verify(repository).save(captor.capture());
            SignOff captured = captor.getValue();

            assertThat(captured.getContract()).isEqualTo(contract);
            assertThat(captured.getBuildingControlType()).isEqualTo(BuildingControlType.LOCAL_AUTHORITY);
            assertThat(captured.getInspectionType()).isEqualTo("Drainage");
            assertThat(captured.getReferenceNumber()).isEqualTo("REF-2026-001");
            assertThat(captured.getInspectorName()).isEqualTo("Alan Smith");
            assertThat(captured.getNotes()).isEqualTo("All clear");
        }

        @Test
        @DisplayName("throws RuntimeException when contract is not found")
        void throwsWhenContractNotFoundOnCreate() {
            SignOffRequest request = new SignOffRequest();
            request.setContractId(77L);
            request.setBuildingControlType(BuildingControlType.LABC);
            request.setInspectionType("Roof");
            request.setInspectionDate(LocalDate.now());
            request.setResult(SignOffResult.CONDITIONS);

            when(contractRepository.findById(77L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Contract not found: 77");

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("correctly maps LABC buildingControlType onto the saved entity")
        void mapsLabcBuildingControlTypeCorrectly() {
            SignOffRequest request = new SignOffRequest();
            request.setContractId(10L);
            request.setBuildingControlType(BuildingControlType.LABC);
            request.setInspectionType("Superstructure");
            request.setInspectionDate(LocalDate.of(2026, 5, 1));
            request.setResult(SignOffResult.CONDITIONS);

            when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));
            when(repository.save(any(SignOff.class))).thenAnswer(inv -> inv.getArgument(0));

            service.create(request);

            ArgumentCaptor<SignOff> captor = ArgumentCaptor.forClass(SignOff.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getBuildingControlType()).isEqualTo(BuildingControlType.LABC);
        }
    }

    // ---------------------------------------------------------------------------
    // delete
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("calls deleteById when the sign-off exists")
        void deletesSignOffWhenItExists() {
            when(repository.existsById(1L)).thenReturn(true);

            service.delete(1L);

            verify(repository).deleteById(1L);
        }

        @Test
        @DisplayName("throws RuntimeException when sign-off does not exist")
        void throwsWhenSignOffNotFound() {
            when(repository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> service.delete(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Sign-off not found: 999");

            verify(repository, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("does not call deleteById when sign-off is not found")
        void doesNotCallDeleteByIdWhenNotFound() {
            when(repository.existsById(42L)).thenReturn(false);

            try {
                service.delete(42L);
            } catch (RuntimeException ignored) {
            }

            verify(repository, never()).deleteById(any());
        }
    }
}
