package com.crms.domain.quality.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.quality.enums.InspectionResult;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inspection_records", indexes = {
    @Index(name = "idx_inspection_schedule_item", columnList = "schedule_item_id"),
    @Index(name = "idx_inspection_result", columnList = "result"),
    @Index(name = "idx_inspection_date", columnList = "inspection_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InspectionRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_item_id", nullable = false)
    private ITPScheduleItem scheduleItem;

    @Column(nullable = false)
    private String title;

    @Column(name = "inspector_name", nullable = false)
    private String inspectorName;

    @Column(name = "inspector_signature", columnDefinition = "TEXT")
    private String inspectorSignature;

    @Column(name = "inspection_date", nullable = false)
    private LocalDate inspectionDate;

    @Column(name = "inspection_time")
    private LocalDateTime inspectionTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InspectionResult result;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String findings;

    @Column(name = "non_conformance_ref")
    private String nonConformanceRef;

    @OneToMany(mappedBy = "inspectionRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InspectionAttachment> attachments = new ArrayList<>();

    public void addAttachment(InspectionAttachment attachment) {
        attachments.add(attachment);
        attachment.setInspectionRecord(this);
    }

    public void removeAttachment(InspectionAttachment attachment) {
        attachments.remove(attachment);
        attachment.setInspectionRecord(null);
    }
}
