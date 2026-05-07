package com.crms.service.quality;

import com.crms.domain.contract.entity.Contract;
import com.crms.domain.contract.repository.ContractRepository;
import com.crms.domain.quality.entity.ITPSchedule;
import com.crms.domain.quality.entity.ITPScheduleItem;
import com.crms.domain.quality.entity.ITPTemplate;
import com.crms.domain.quality.entity.ITPTemplateItem;
import com.crms.domain.quality.enums.InspectionType;
import com.crms.domain.quality.enums.ScheduleStatus;
import com.crms.domain.quality.repository.ITPScheduleRepository;
import com.crms.domain.quality.repository.ITPTemplateRepository;
import com.crms.dto.response.quality.ITPScheduleResponse;
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
@DisplayName("ITPScheduleServiceImpl")
class ITPScheduleServiceImplTest {

    @Mock
    private ITPScheduleRepository repository;

    @Mock
    private ITPTemplateRepository templateRepository;

    @Mock
    private ContractRepository contractRepository;

    @InjectMocks
    private ITPScheduleServiceImpl service;

    private Contract contract;
    private ITPTemplate template;

    @BeforeEach
    void setUp() {
        contract = Contract.builder()
                .id(10L)
                .contractRef("CTR-001")
                .title("Test Contract")
                .build();

        template = ITPTemplate.builder()
                .id(20L)
                .name("Foundation Inspection")
                .category("Structural")
                .build();
    }

    // ---------------------------------------------------------------------------
    // createFromTemplate
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("createFromTemplate()")
    class CreateFromTemplate {

        @Test
        @DisplayName("copies all items from the template onto the new schedule")
        void copiesItemsFromTemplateAndSavesSchedule() {
            ITPTemplateItem templateItem = ITPTemplateItem.builder()
                    .id(1L)
                    .sequence(1)
                    .description("Check foundations")
                    .inspectionType(InspectionType.WITNESS)
                    .responsibleParty("Site Manager")
                    .frequency("Once")
                    .requiredEvidence("Photo")
                    .build();
            template.getItems().add(templateItem);

            ITPSchedule saved = ITPSchedule.builder()
                    .id(100L)
                    .title("Foundation Inspection - Schedule")
                    .contract(contract)
                    .template(template)
                    .status(ScheduleStatus.PENDING)
                    .build();

            when(templateRepository.findById(20L)).thenReturn(Optional.of(template));
            when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));
            when(repository.save(any(ITPSchedule.class))).thenReturn(saved);

            ITPScheduleResponse response = service.createFromTemplate(20L, 10L);

            assertThat(response).isNotNull();
            assertThat(response.getTitle()).isEqualTo("Foundation Inspection - Schedule");
            assertThat(response.getContractId()).isEqualTo(10L);
            assertThat(response.getTemplateId()).isEqualTo(20L);
            assertThat(response.getStatus()).isEqualTo(ScheduleStatus.PENDING);

            ArgumentCaptor<ITPSchedule> captor = ArgumentCaptor.forClass(ITPSchedule.class);
            verify(repository).save(captor.capture());
            ITPSchedule scheduleArg = captor.getValue();
            assertThat(scheduleArg.getItems()).hasSize(1);
            assertThat(scheduleArg.getItems().get(0).getDescription()).isEqualTo("Check foundations");
            assertThat(scheduleArg.getItems().get(0).getStatus()).isEqualTo(ScheduleStatus.PENDING);
        }

        @Test
        @DisplayName("sets schedule title to '<templateName> - Schedule'")
        void setsScheduleTitleAsTemplateNamePlusSuffix() {
            template = ITPTemplate.builder()
                    .id(20L)
                    .name("Roof Works")
                    .category("Structural")
                    .build();

            ITPSchedule saved = ITPSchedule.builder()
                    .id(101L)
                    .title("Roof Works - Schedule")
                    .contract(contract)
                    .template(template)
                    .status(ScheduleStatus.PENDING)
                    .build();

            when(templateRepository.findById(20L)).thenReturn(Optional.of(template));
            when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));
            when(repository.save(any(ITPSchedule.class))).thenReturn(saved);

            ITPScheduleResponse response = service.createFromTemplate(20L, 10L);

            assertThat(response.getTitle()).isEqualTo("Roof Works - Schedule");
        }

        @Test
        @DisplayName("sets initial status to PENDING")
        void setsInitialStatusToPending() {
            ITPSchedule saved = ITPSchedule.builder()
                    .id(102L)
                    .title("Foundation Inspection - Schedule")
                    .contract(contract)
                    .template(template)
                    .status(ScheduleStatus.PENDING)
                    .build();

            when(templateRepository.findById(20L)).thenReturn(Optional.of(template));
            when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));
            when(repository.save(any(ITPSchedule.class))).thenReturn(saved);

            ITPScheduleResponse response = service.createFromTemplate(20L, 10L);

            assertThat(response.getStatus()).isEqualTo(ScheduleStatus.PENDING);
        }

        @Test
        @DisplayName("sets startDate to today and dueDate to 30 days ahead")
        void setsStartDateToTodayAndDueDateThirtyDaysAhead() {
            when(templateRepository.findById(20L)).thenReturn(Optional.of(template));
            when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));

            ITPSchedule captured = ITPSchedule.builder()
                    .id(102L)
                    .title("Foundation Inspection - Schedule")
                    .contract(contract)
                    .template(template)
                    .startDate(LocalDate.now())
                    .dueDate(LocalDate.now().plusDays(30))
                    .status(ScheduleStatus.PENDING)
                    .build();

            when(repository.save(any(ITPSchedule.class))).thenReturn(captured);

            service.createFromTemplate(20L, 10L);

            ArgumentCaptor<ITPSchedule> captor = ArgumentCaptor.forClass(ITPSchedule.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getStartDate()).isEqualTo(LocalDate.now());
            assertThat(captor.getValue().getDueDate()).isEqualTo(LocalDate.now().plusDays(30));
        }

        @Test
        @DisplayName("creates schedule with no items when template has no items")
        void createsScheduleWithNoItemsWhenTemplateHasNoItems() {
            // template.getItems() is empty by default
            ITPSchedule saved = ITPSchedule.builder()
                    .id(103L)
                    .title("Foundation Inspection - Schedule")
                    .contract(contract)
                    .template(template)
                    .status(ScheduleStatus.PENDING)
                    .build();

            when(templateRepository.findById(20L)).thenReturn(Optional.of(template));
            when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));
            when(repository.save(any(ITPSchedule.class))).thenReturn(saved);

            service.createFromTemplate(20L, 10L);

            ArgumentCaptor<ITPSchedule> captor = ArgumentCaptor.forClass(ITPSchedule.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getItems()).isEmpty();
        }

        @Test
        @DisplayName("copies multiple template items preserving sequence and description")
        void copiesMultipleTemplateItemsPreservingOrder() {
            ITPTemplateItem item1 = ITPTemplateItem.builder()
                    .id(1L).sequence(1).description("Step One")
                    .inspectionType(InspectionType.WITNESS).responsibleParty("PM")
                    .frequency("Daily").requiredEvidence("Log")
                    .build();
            ITPTemplateItem item2 = ITPTemplateItem.builder()
                    .id(2L).sequence(2).description("Step Two")
                    .inspectionType(InspectionType.HOLD).responsibleParty("Engineer")
                    .frequency("Weekly").requiredEvidence("Report")
                    .build();
            template.getItems().add(item1);
            template.getItems().add(item2);

            ITPSchedule saved = ITPSchedule.builder()
                    .id(104L)
                    .title("Foundation Inspection - Schedule")
                    .contract(contract)
                    .template(template)
                    .status(ScheduleStatus.PENDING)
                    .build();

            when(templateRepository.findById(20L)).thenReturn(Optional.of(template));
            when(contractRepository.findById(10L)).thenReturn(Optional.of(contract));
            when(repository.save(any(ITPSchedule.class))).thenReturn(saved);

            service.createFromTemplate(20L, 10L);

            ArgumentCaptor<ITPSchedule> captor = ArgumentCaptor.forClass(ITPSchedule.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getItems()).hasSize(2);
            assertThat(captor.getValue().getItems().get(0).getSequence()).isEqualTo(1);
            assertThat(captor.getValue().getItems().get(1).getSequence()).isEqualTo(2);
        }

        @Test
        @DisplayName("throws RuntimeException when template not found")
        void throwsWhenTemplateNotFound() {
            when(templateRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createFromTemplate(99L, 10L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("ITP Template not found: 99");

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("throws RuntimeException when contract not found")
        void throwsWhenContractNotFound() {
            when(templateRepository.findById(20L)).thenReturn(Optional.of(template));
            when(contractRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createFromTemplate(20L, 99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Contract not found: 99");

            verify(repository, never()).save(any());
        }
    }

    // ---------------------------------------------------------------------------
    // completeItem
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("completeItem()")
    class CompleteItem {

        @Test
        @DisplayName("marks the targeted item as COMPLETED with completedBy and result")
        void setsItemStatusToCompleted() {
            ITPScheduleItem item = ITPScheduleItem.builder()
                    .id(200L)
                    .sequence(1)
                    .description("Check footings")
                    .inspectionType(InspectionType.HOLD)
                    .responsibleParty("Inspector")
                    .status(ScheduleStatus.PENDING)
                    .build();

            ITPSchedule schedule = ITPSchedule.builder()
                    .id(50L)
                    .title("Schedule A")
                    .contract(contract)
                    .status(ScheduleStatus.PENDING)
                    .build();
            schedule.addItem(item);

            when(repository.findById(50L)).thenReturn(Optional.of(schedule));
            when(repository.save(any(ITPSchedule.class))).thenAnswer(inv -> inv.getArgument(0));

            service.completeItem(50L, 200L, "John", "Pass");

            assertThat(item.getStatus()).isEqualTo(ScheduleStatus.COMPLETED);
            assertThat(item.getCompletedBy()).isEqualTo("John");
            assertThat(item.getResult()).isEqualTo("Pass");
            assertThat(item.getCompletedDate()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("sets schedule status to COMPLETED when all items are complete")
        void setsScheduleStatusToCompletedWhenAllItemsComplete() {
            ITPScheduleItem item1 = ITPScheduleItem.builder()
                    .id(201L).sequence(1).description("Step 1")
                    .inspectionType(InspectionType.WITNESS).responsibleParty("Inspector")
                    .status(ScheduleStatus.COMPLETED)
                    .build();

            ITPScheduleItem item2 = ITPScheduleItem.builder()
                    .id(202L).sequence(2).description("Step 2")
                    .inspectionType(InspectionType.WITNESS).responsibleParty("Inspector")
                    .status(ScheduleStatus.PENDING)
                    .build();

            ITPSchedule schedule = ITPSchedule.builder()
                    .id(51L).title("Schedule B").contract(contract)
                    .status(ScheduleStatus.IN_PROGRESS)
                    .build();
            schedule.addItem(item1);
            schedule.addItem(item2);

            when(repository.findById(51L)).thenReturn(Optional.of(schedule));
            when(repository.save(any(ITPSchedule.class))).thenAnswer(inv -> inv.getArgument(0));

            service.completeItem(51L, 202L, "Jane", "Pass");

            assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.COMPLETED);
            assertThat(schedule.getCompletedDate()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("sets schedule status to IN_PROGRESS when some items remain pending")
        void setsScheduleStatusToInProgressWhenSomeItemsRemain() {
            ITPScheduleItem item1 = ITPScheduleItem.builder()
                    .id(203L).sequence(1).description("Step 1")
                    .inspectionType(InspectionType.WITNESS).responsibleParty("Inspector")
                    .status(ScheduleStatus.PENDING)
                    .build();

            ITPScheduleItem item2 = ITPScheduleItem.builder()
                    .id(204L).sequence(2).description("Step 2")
                    .inspectionType(InspectionType.WITNESS).responsibleParty("Inspector")
                    .status(ScheduleStatus.PENDING)
                    .build();

            ITPSchedule schedule = ITPSchedule.builder()
                    .id(52L).title("Schedule C").contract(contract)
                    .status(ScheduleStatus.PENDING)
                    .build();
            schedule.addItem(item1);
            schedule.addItem(item2);

            when(repository.findById(52L)).thenReturn(Optional.of(schedule));
            when(repository.save(any(ITPSchedule.class))).thenAnswer(inv -> inv.getArgument(0));

            // Complete only item1; item2 still pending
            service.completeItem(52L, 203L, "Bob", "OK");

            assertThat(schedule.getStatus()).isEqualTo(ScheduleStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("response contains updated schedule status")
        void responseContainsUpdatedStatus() {
            ITPScheduleItem item = ITPScheduleItem.builder()
                    .id(205L).sequence(1).description("Only step")
                    .inspectionType(InspectionType.MONITOR).responsibleParty("QA")
                    .status(ScheduleStatus.PENDING)
                    .build();

            ITPSchedule schedule = ITPSchedule.builder()
                    .id(53L).title("Schedule D").contract(contract)
                    .status(ScheduleStatus.PENDING)
                    .build();
            schedule.addItem(item);

            when(repository.findById(53L)).thenReturn(Optional.of(schedule));
            when(repository.save(any(ITPSchedule.class))).thenAnswer(inv -> inv.getArgument(0));

            ITPScheduleResponse response = service.completeItem(53L, 205L, "QA Lead", "Compliant");

            assertThat(response.getStatus()).isEqualTo(ScheduleStatus.COMPLETED);
        }

        @Test
        @DisplayName("throws RuntimeException when schedule not found")
        void throwsWhenScheduleNotFound() {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.completeItem(999L, 200L, "John", "Pass"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("ITP Schedule not found: 999");
        }

        @Test
        @DisplayName("throws RuntimeException when item ID is not in the schedule")
        void throwsWhenItemNotFoundInSchedule() {
            ITPSchedule schedule = ITPSchedule.builder()
                    .id(54L).title("Schedule E").contract(contract)
                    .status(ScheduleStatus.PENDING)
                    .build();
            // no items added

            when(repository.findById(54L)).thenReturn(Optional.of(schedule));

            assertThatThrownBy(() -> service.completeItem(54L, 999L, "John", "Pass"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Schedule item not found: 999");

            verify(repository, never()).save(any());
        }
    }

    // ---------------------------------------------------------------------------
    // delete
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("calls deleteById when the schedule exists")
        void deletesScheduleWhenItExists() {
            when(repository.existsById(1L)).thenReturn(true);

            service.delete(1L);

            verify(repository).deleteById(1L);
        }

        @Test
        @DisplayName("throws RuntimeException when schedule does not exist")
        void throwsWhenScheduleDoesNotExist() {
            when(repository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> service.delete(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("ITP Schedule not found: 999");

            verify(repository, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("does not call deleteById when schedule is not found")
        void doesNotCallDeleteByIdWhenNotFound() {
            when(repository.existsById(5L)).thenReturn(false);

            try {
                service.delete(5L);
            } catch (RuntimeException ignored) {
            }

            verify(repository, never()).deleteById(any());
        }
    }
}
