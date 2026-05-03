package com.crms.domain.material.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.material.enums.CubeTestResult;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "cube_samples", indexes = {
    @Index(name = "idx_cs_concrete_ticket", columnList = "concrete_ticket_id"),
    @Index(name = "idx_cs_ref", columnList = "sample_ref"),
    @Index(name = "idx_cs_result", columnList = "result")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CubeSample extends BaseEntity {

    @Transient
    private Long compatibilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concrete_ticket_id", nullable = false)
    private ConcreteTicket concreteTicket;

    @Column(name = "sample_ref", nullable = false)
    private String sampleRef;

    @Column(name = "cast_date", nullable = false)
    private LocalDate castDate;

    @Column(name = "cast_time")
    private LocalTime castTime;

    @Column(name = "cube_set")
    private Integer cubeSet;

    @Column(name = "batch_time")
    private LocalTime batchTime;

    @Column(name = "truck_ref")
    private String truckRef;

    @Column(name = "lab_destination")
    private String labDestination;

    @Column(precision = 5, scale = 2)
    private BigDecimal mpa7Day;

    @Column(precision = 5, scale = 2)
    private BigDecimal mpa28Day;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CubeTestResult result = CubeTestResult.PENDING;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public boolean hasPassed() {
        return result == CubeTestResult.PASS;
    }

    public boolean hasFailed() {
        return result == CubeTestResult.FAIL;
    }

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class CubeSampleBuilder {
        public CubeSampleBuilder id(Long id) {
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
