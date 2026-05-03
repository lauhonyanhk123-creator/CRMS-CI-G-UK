package com.crms.domain.quality.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.contract.entity.Contract;
import com.crms.domain.quality.enums.ScheduleStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "itp_schedules", indexes = {
    @Index(name = "idx_itp_schedule_contract", columnList = "contract_id"),
    @Index(name = "idx_itp_schedule_status", columnList = "status"),
    @Index(name = "idx_itp_schedule_due_date", columnList = "due_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ITPSchedule extends BaseEntity {

    @Transient
    private Long compatibilityId;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private ITPTemplate template;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ScheduleStatus status = ScheduleStatus.PENDING;

    @Column(name = "assigned_inspector")
    private String assignedInspector;

    @Column(name = "sign_off_by")
    private String signOffBy;

    @Column(name = "sign_off_date")
    private LocalDate signOffDate;

    @Column(name = "sign_off_signature", columnDefinition = "TEXT")
    private String signOffSignature;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ITPScheduleItem> items = new ArrayList<>();

    public void addItem(ITPScheduleItem item) {
        items.add(item);
        item.setSchedule(this);
    }

    public void removeItem(ITPScheduleItem item) {
        items.remove(item);
        item.setSchedule(null);
    }

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class ITPScheduleBuilder {
        public ITPScheduleBuilder id(Long id) {
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
