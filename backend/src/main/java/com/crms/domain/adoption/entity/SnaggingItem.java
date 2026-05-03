package com.crms.domain.adoption.entity;

import com.crms.domain.adoption.enums.SnaggingItemPriority;
import com.crms.domain.adoption.enums.SnaggingItemStatus;
import com.crms.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "snagging_items", indexes = {
    @Index(name = "idx_snagging_case", columnList = "adoption_case_id"),
    @Index(name = "idx_snagging_status", columnList = "status"),
    @Index(name = "idx_snagging_priority", columnList = "priority")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SnaggingItem extends BaseEntity {

    @Transient
    private Long compatibilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adoption_case_id", nullable = false)
    private AdoptionCase adoptionCase;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SnaggingItemPriority priority = SnaggingItemPriority.MEDIUM;

    @Column(name = "identified_date", nullable = false)
    private LocalDate identifiedDate;

    @Column(name = "target_completion_date")
    private LocalDate targetCompletionDate;

    @Column(name = "actual_completion_date")
    private LocalDate actualCompletionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SnaggingItemStatus status = SnaggingItemStatus.OPEN;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "assigned_to")
    private String assignedTo;

    @Column(name = "verified_date")
    private LocalDate verifiedDate;

    @Column(name = "verified_by")
    private String verifiedBy;

    public boolean isOverdue() {
        return status != SnaggingItemStatus.COMPLETED 
            && status != SnaggingItemStatus.VERIFIED 
            && status != SnaggingItemStatus.CLOSED
            && targetCompletionDate != null 
            && targetCompletionDate.isBefore(LocalDate.now());
    }

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class SnaggingItemBuilder {
        public SnaggingItemBuilder id(Long id) {
            this.compatibilityId = id;
            return this;
        }
    }


    @Override
    public Long getId() {
        Long id = super.getId();
        return id != null ? id : compatibilityId;
    }

    @Override
    public void setId(Long id) {
        super.setId(id);
        this.compatibilityId = id;
    }

}
