package com.crms.domain.material.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.company.entity.Company;
import com.crms.domain.material.enums.DeliveryStatus;
import com.crms.domain.site.entity.Site;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "delivery_notes", indexes = {
    @Index(name = "idx_dn_ref", columnList = "delivery_note_ref"),
    @Index(name = "idx_dn_order", columnList = "order_id"),
    @Index(name = "idx_dn_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryNote extends BaseEntity {

    @Transient
    private Long compatibilityId;

    @Column(name = "delivery_note_ref", nullable = false, unique = true)
    private String deliveryNoteRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private PurchaseOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Company supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "delivery_time")
    private LocalTime deliveryTime;

    @Column(name = "delivered_by")
    private String deliveredBy;

    @Column(name = "vehicle_reg")
    private String vehicleReg;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DeliveryStatus status = DeliveryStatus.EXPECTED;

    @Column(columnDefinition = "TEXT")
    private String notes;

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class DeliveryNoteBuilder {
        public DeliveryNoteBuilder id(Long id) {
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
