package com.crms.domain.financial.entity;

import com.crms.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "journal_entries", indexes = {
    @Index(name = "idx_je_journal_ref", columnList = "journal_reference"),
    @Index(name = "idx_je_transaction_date", columnList = "transaction_date"),
    @Index(name = "idx_je_wip_report", columnList = "wip_report_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wip_report_id")
    private WipReport wipReport;

    @Column(name = "journal_reference", nullable = false)
    private String journalReference;

    @Column(name = "journal_type", nullable = false)
    private String journalType;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "description")
    private String description;

    @Column(name = "debit_account_code", length = 20)
    private String debitAccountCode;

    @Column(name = "credit_account_code", length = 20)
    private String creditAccountCode;

    @Column(name = "debit_amount", precision = 14, scale = 2)
    private BigDecimal debitAmount;

    @Column(name = "credit_amount", precision = 14, scale = 2)
    private BigDecimal creditAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private JournalStatus status = JournalStatus.DRAFT;

    public enum JournalStatus {
        DRAFT,
        POSTED,
        REVERSED
    }
}
