package com.crms.domain.healthsafety.entity;

import com.crms.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rams_templates", indexes = {
    @Index(name = "idx_rams_template_title", columnList = "title"),
    @Index(name = "idx_rams_template_trade", columnList = "trade")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RAMSTemplate extends BaseEntity {

    @Transient
    private Long compatibilityId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private String trade;

    @Column(name = "risk_assessment", columnDefinition = "TEXT")
    private String riskAssessment;

    @Column(name = "method_statement", columnDefinition = "TEXT")
    private String methodStatement;

    @Column(name = "ppe_required")
    private String ppeRequired;

    @Column(name = "frequency_days")
    @Builder.Default
    private Integer frequencyDays = 90;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class RAMSTemplateBuilder {
        public RAMSTemplateBuilder id(Long id) {
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
