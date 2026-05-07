package com.crms.service.impl;

import com.crms.domain.contract.entity.Contract;
import com.crms.domain.contract.repository.ContractRepository;
import com.crms.domain.financial.entity.WipReport;
import com.crms.domain.financial.entity.WipReport.WipReportStatus;
import com.crms.domain.financial.repository.WipReportRepository;
import com.crms.dto.response.WipReportResponse;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WipJournalServiceImpl unit tests")
class WipJournalServiceImplTest {

    @Mock
    private WipReportRepository wipReportRepository;

    @Mock
    private ContractRepository contractRepository;

    @InjectMocks
    private WipJournalServiceImpl wipJournalService;

    private Contract contract;
    private final Long CONTRACT_ID = 1L;
    private final Long REPORT_ID = 10L;
    private final LocalDate REPORT_DATE = LocalDate.of(2025, 6, 15);

    @BeforeEach
    void setUp() {
        contract = Contract.builder()
                .id(CONTRACT_ID)
                .contractRef("CT-2025-001")
                .build();
    }

    // -------------------------------------------------------------------------
    // generateWipReport
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("generateWipReport")
    class GenerateWipReport {

        @Test
        @DisplayName("throws ResourceNotFoundException when contract does not exist")
        void throwsWhenContractNotFound() {
            when(contractRepository.findById(CONTRACT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> wipJournalService.generateWipReport(CONTRACT_ID, REPORT_DATE))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Contract");

            verify(contractRepository).findById(CONTRACT_ID);
            verifyNoInteractions(wipReportRepository);
        }

        @Test
        @DisplayName("returns existing report when one already exists for the given date")
        void returnsExistingReportWhenAlreadyExists() {
            WipReport existing = WipReport.builder()
                    .id(REPORT_ID)
                    .contract(contract)
                    .reportDate(REPORT_DATE)
                    .periodStart(REPORT_DATE.withDayOfMonth(1))
                    .periodEnd(REPORT_DATE.withDayOfMonth(REPORT_DATE.lengthOfMonth()))
                    .certifiedValue(new BigDecimal("50000.00"))
                    .costIncurred(new BigDecimal("40000.00"))
                    .wipValue(new BigDecimal("10000.00"))
                    .underRecovery(BigDecimal.ZERO)
                    .overRecovery(BigDecimal.ZERO)
                    .status(WipReportStatus.DRAFT)
                    .build();

            when(contractRepository.findById(CONTRACT_ID)).thenReturn(Optional.of(contract));
            when(wipReportRepository.existsByContractIdAndReportDate(CONTRACT_ID, REPORT_DATE)).thenReturn(true);
            when(wipReportRepository.findByContractIdAndReportDate(CONTRACT_ID, REPORT_DATE))
                    .thenReturn(Optional.of(existing));

            WipReportResponse response = wipJournalService.generateWipReport(CONTRACT_ID, REPORT_DATE);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(REPORT_ID);
            assertThat(response.getReportDate()).isEqualTo(REPORT_DATE);

            verify(wipReportRepository, never()).save(any());
        }

        @Test
        @DisplayName("creates a new DRAFT report when no report exists for that date")
        void createsNewDraftReportWhenNoneExists() {
            when(contractRepository.findById(CONTRACT_ID)).thenReturn(Optional.of(contract));
            when(wipReportRepository.existsByContractIdAndReportDate(CONTRACT_ID, REPORT_DATE)).thenReturn(false);
            when(wipReportRepository.save(any(WipReport.class))).thenAnswer(inv -> inv.getArgument(0));

            WipReportResponse response = wipJournalService.generateWipReport(CONTRACT_ID, REPORT_DATE);

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(WipReportStatus.DRAFT.name());
            assertThat(response.getContractId()).isEqualTo(CONTRACT_ID);
            assertThat(response.getReportDate()).isEqualTo(REPORT_DATE);

            verify(wipReportRepository).save(any(WipReport.class));
        }

        @Test
        @DisplayName("sets period start to first day of month and period end to last day of month")
        void setsPeriodBoundsCorrectly() {
            when(contractRepository.findById(CONTRACT_ID)).thenReturn(Optional.of(contract));
            when(wipReportRepository.existsByContractIdAndReportDate(CONTRACT_ID, REPORT_DATE)).thenReturn(false);
            when(wipReportRepository.save(any(WipReport.class))).thenAnswer(inv -> {
                WipReport saved = inv.getArgument(0);
                assertThat(saved.getPeriodStart()).isEqualTo(LocalDate.of(2025, 6, 1));
                assertThat(saved.getPeriodEnd()).isEqualTo(LocalDate.of(2025, 6, 30));
                return saved;
            });

            wipJournalService.generateWipReport(CONTRACT_ID, REPORT_DATE);

            verify(wipReportRepository).save(any(WipReport.class));
        }
    }

    // -------------------------------------------------------------------------
    // postToJournal
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("postToJournal")
    class PostToJournal {

        @Test
        @DisplayName("throws ResourceNotFoundException when report does not exist")
        void throwsWhenReportNotFound() {
            when(wipReportRepository.findById(REPORT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> wipJournalService.postToJournal(REPORT_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("WipReport");
        }

        @Test
        @DisplayName("throws ValidationException when report status is POSTED")
        void throwsWhenAlreadyPosted() {
            WipReport report = WipReport.builder()
                    .id(REPORT_ID)
                    .contract(contract)
                    .reportDate(REPORT_DATE)
                    .status(WipReportStatus.POSTED)
                    .journalReference("WIP-CT-2025-001-20250615-ABCD1234")
                    .build();

            when(wipReportRepository.findById(REPORT_ID)).thenReturn(Optional.of(report));

            assertThatThrownBy(() -> wipJournalService.postToJournal(REPORT_ID))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("Only DRAFT reports can be posted to journal");
        }

        @Test
        @DisplayName("throws ValidationException when report status is REVERSED")
        void throwsWhenReversed() {
            WipReport report = WipReport.builder()
                    .id(REPORT_ID)
                    .contract(contract)
                    .reportDate(REPORT_DATE)
                    .status(WipReportStatus.REVERSED)
                    .journalReference("WIP-CT-2025-001-20250615-ABCD1234-REV")
                    .build();

            when(wipReportRepository.findById(REPORT_ID)).thenReturn(Optional.of(report));

            assertThatThrownBy(() -> wipJournalService.postToJournal(REPORT_ID))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("Only DRAFT reports can be posted to journal");
        }

        @Test
        @DisplayName("sets status to POSTED and sets journalReference when report is DRAFT")
        void postsSuccessfullyWhenDraft() {
            WipReport report = WipReport.builder()
                    .id(REPORT_ID)
                    .contract(contract)
                    .reportDate(REPORT_DATE)
                    .status(WipReportStatus.DRAFT)
                    .certifiedValue(BigDecimal.ZERO)
                    .costIncurred(BigDecimal.ZERO)
                    .wipValue(BigDecimal.ZERO)
                    .underRecovery(BigDecimal.ZERO)
                    .overRecovery(BigDecimal.ZERO)
                    .build();

            when(wipReportRepository.findById(REPORT_ID)).thenReturn(Optional.of(report));
            when(wipReportRepository.save(any(WipReport.class))).thenAnswer(inv -> inv.getArgument(0));

            WipReportResponse response = wipJournalService.postToJournal(REPORT_ID);

            assertThat(response.getStatus()).isEqualTo(WipReportStatus.POSTED.name());
            assertThat(response.getJournalReference()).isNotBlank();

            verify(wipReportRepository).save(any(WipReport.class));
        }

        @Test
        @DisplayName("generated journalReference contains the contract ref")
        void journalReferenceContainsContractRef() {
            WipReport report = WipReport.builder()
                    .id(REPORT_ID)
                    .contract(contract)
                    .reportDate(REPORT_DATE)
                    .status(WipReportStatus.DRAFT)
                    .certifiedValue(BigDecimal.ZERO)
                    .costIncurred(BigDecimal.ZERO)
                    .wipValue(BigDecimal.ZERO)
                    .underRecovery(BigDecimal.ZERO)
                    .overRecovery(BigDecimal.ZERO)
                    .build();

            when(wipReportRepository.findById(REPORT_ID)).thenReturn(Optional.of(report));
            when(wipReportRepository.save(any(WipReport.class))).thenAnswer(inv -> inv.getArgument(0));

            WipReportResponse response = wipJournalService.postToJournal(REPORT_ID);

            assertThat(response.getJournalReference()).contains(contract.getContractRef());
        }
    }

    // -------------------------------------------------------------------------
    // reverseJournal
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("reverseJournal")
    class ReverseJournal {

        @Test
        @DisplayName("throws ValidationException when report status is DRAFT")
        void throwsWhenDraft() {
            WipReport report = WipReport.builder()
                    .id(REPORT_ID)
                    .contract(contract)
                    .reportDate(REPORT_DATE)
                    .status(WipReportStatus.DRAFT)
                    .build();

            when(wipReportRepository.findById(REPORT_ID)).thenReturn(Optional.of(report));

            assertThatThrownBy(() -> wipJournalService.reverseJournal(REPORT_ID))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("Only POSTED reports can be reversed");
        }

        @Test
        @DisplayName("throws ValidationException when report status is already REVERSED")
        void throwsWhenAlreadyReversed() {
            WipReport report = WipReport.builder()
                    .id(REPORT_ID)
                    .contract(contract)
                    .reportDate(REPORT_DATE)
                    .status(WipReportStatus.REVERSED)
                    .journalReference("WIP-CT-2025-001-20250615-ABCD1234-REV")
                    .build();

            when(wipReportRepository.findById(REPORT_ID)).thenReturn(Optional.of(report));

            assertThatThrownBy(() -> wipJournalService.reverseJournal(REPORT_ID))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("Only POSTED reports can be reversed");
        }

        @Test
        @DisplayName("sets status to REVERSED and appends '-REV' to journalReference when POSTED")
        void reversesSuccessfullyWhenPosted() {
            String originalRef = "WIP-CT-2025-001-20250615-ABCD1234";

            WipReport report = WipReport.builder()
                    .id(REPORT_ID)
                    .contract(contract)
                    .reportDate(REPORT_DATE)
                    .status(WipReportStatus.POSTED)
                    .journalReference(originalRef)
                    .certifiedValue(BigDecimal.ZERO)
                    .costIncurred(BigDecimal.ZERO)
                    .wipValue(BigDecimal.ZERO)
                    .underRecovery(BigDecimal.ZERO)
                    .overRecovery(BigDecimal.ZERO)
                    .build();

            when(wipReportRepository.findById(REPORT_ID)).thenReturn(Optional.of(report));
            when(wipReportRepository.save(any(WipReport.class))).thenAnswer(inv -> inv.getArgument(0));

            WipReportResponse response = wipJournalService.reverseJournal(REPORT_ID);

            assertThat(response.getStatus()).isEqualTo(WipReportStatus.REVERSED.name());
            assertThat(response.getJournalReference()).isEqualTo(originalRef + "-REV");

            verify(wipReportRepository).save(any(WipReport.class));
        }
    }

    // -------------------------------------------------------------------------
    // deleteDraftReport
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("deleteDraftReport")
    class DeleteDraftReport {

        @Test
        @DisplayName("throws ValidationException when report status is POSTED")
        void throwsWhenPosted() {
            WipReport report = WipReport.builder()
                    .id(REPORT_ID)
                    .contract(contract)
                    .reportDate(REPORT_DATE)
                    .status(WipReportStatus.POSTED)
                    .journalReference("WIP-CT-2025-001-20250615-ABCD1234")
                    .build();

            when(wipReportRepository.findById(REPORT_ID)).thenReturn(Optional.of(report));

            assertThatThrownBy(() -> wipJournalService.deleteDraftReport(REPORT_ID))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("Only DRAFT reports can be deleted");

            verify(wipReportRepository, never()).delete(any());
        }

        @Test
        @DisplayName("throws ValidationException when report status is REVERSED")
        void throwsWhenReversed() {
            WipReport report = WipReport.builder()
                    .id(REPORT_ID)
                    .contract(contract)
                    .reportDate(REPORT_DATE)
                    .status(WipReportStatus.REVERSED)
                    .journalReference("WIP-CT-2025-001-20250615-ABCD1234-REV")
                    .build();

            when(wipReportRepository.findById(REPORT_ID)).thenReturn(Optional.of(report));

            assertThatThrownBy(() -> wipJournalService.deleteDraftReport(REPORT_ID))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage("Only DRAFT reports can be deleted");

            verify(wipReportRepository, never()).delete(any());
        }

        @Test
        @DisplayName("calls wipReportRepository.delete() when report is DRAFT")
        void deletesDraftSuccessfully() {
            WipReport report = WipReport.builder()
                    .id(REPORT_ID)
                    .contract(contract)
                    .reportDate(REPORT_DATE)
                    .status(WipReportStatus.DRAFT)
                    .certifiedValue(BigDecimal.ZERO)
                    .costIncurred(BigDecimal.ZERO)
                    .wipValue(BigDecimal.ZERO)
                    .underRecovery(BigDecimal.ZERO)
                    .overRecovery(BigDecimal.ZERO)
                    .build();

            when(wipReportRepository.findById(REPORT_ID)).thenReturn(Optional.of(report));
            doNothing().when(wipReportRepository).delete(report);

            wipJournalService.deleteDraftReport(REPORT_ID);

            verify(wipReportRepository).delete(report);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when report does not exist")
        void throwsWhenReportNotFound() {
            when(wipReportRepository.findById(REPORT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> wipJournalService.deleteDraftReport(REPORT_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("WipReport");

            verify(wipReportRepository, never()).delete(any());
        }
    }
}
