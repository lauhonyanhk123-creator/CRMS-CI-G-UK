package com.crms.domain.quality.entity;

import com.crms.domain.quality.enums.InspectionType;
import com.crms.domain.quality.enums.ScheduleStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "itp_schedule_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ITPScheduleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private ITPSchedule schedule;

    @Column(nullable = false)
    private Integer sequence;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "inspection_type", nullable = false)
    private InspectionType inspectionType;

    @Column(name = "responsible_party", nullable = false)
    private String responsibleParty;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "frequency")
    private String frequency;

    @Column(name = "required_evidence")
    private String requiredEvidence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ScheduleStatus status = ScheduleStatus.PENDING;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @Column(name = "completed_by")
    private String completedBy;

    @Column(name = "result", columnDefinition = "TEXT")
    private String result;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
