package com.crms.domain.material.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.material.enums.CubeTestResult;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "concrete_tickets", indexes = {
    @Index(name = "idx_ct_delivery_note", columnList = "delivery_note_id"),
    @Index(name = "idx_ct_ticket_number", columnList = "ticket_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConcreteTicket extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_note_id", nullable = false)
    private DeliveryNote deliveryNote;

    @Column(name = "ticket_number", nullable = false)
    private String ticketNumber;

    @Column(name = "batch_number")
    private String batchNumber;

    @Column(name = "truck_ref")
    private String truckRef;

    @Column(name = "batch_time")
    private LocalTime batchTime;

    @Column(name = "ordered_volume", precision = 10, scale = 3)
    private BigDecimal orderedVolume;

    @Column(name = "delivered_volume", precision = 10, scale = 3)
    private BigDecimal deliveredVolume;

    @Column(name = "bs8500_designation")
    private String bs8500Designation;

    @Column(name = "exposure_class")
    private String exposureClass;

    @Column(name = "slump_target")
    private String slumpTarget;

    @Column(name = "slump_on_site")
    private String slumpOnSite;

    @Column(name = "water_added_on_site")
    private Boolean waterAddedOnSite;

    @Column(name = "time_arrival")
    private LocalTime timeArrival;

    @Column(name = "time_discharge")
    private LocalTime timeDischarge;

    @Column(precision = 5, scale = 2)
    private BigDecimal temperature;

    @Column(name = "discharge_rate")
    private String dischargeRate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "concreteTicket", cascade = CascadeType.ALL)
    @Builder.Default
    private List<CubeSample> cubeSamples = new ArrayList<>();

    public BigDecimal getVolumeDifference() {
        if (orderedVolume != null && deliveredVolume != null) {
            return deliveredVolume.subtract(orderedVolume);
        }
        return BigDecimal.ZERO;
    }
}
