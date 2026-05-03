package com.crms.domain.material.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.material.enums.WasteType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "muckaway_tickets", indexes = {
    @Index(name = "idx_mt_delivery_note", columnList = "delivery_note_id"),
    @Index(name = "idx_mt_ticket_number", columnList = "ticket_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MuckawayTicket extends BaseEntity {

    @Transient
    private Long compatibilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_note_id", nullable = false)
    private DeliveryNote deliveryNote;

    @Column(name = "ticket_number", nullable = false)
    private String ticketNumber;

    @Column(name = "vehicle_reg")
    private String vehicleReg;

    @Column(name = "waste_carrier_licence_ref")
    private String wasteCarrierLicenceRef;

    @Column(name = "permitted_facility")
    private String permittedFacility;

    @Column(name = "facility_permit_ref")
    private String facilityPermitRef;

    @Enumerated(EnumType.STRING)
    @Column(name = "waste_type", nullable = false)
    private WasteType wasteType;

    @Column(name = "load_out_weight", precision = 10, scale = 2)
    private BigDecimal loadOutWeight;

    @Column(name = "load_in_weight", precision = 10, scale = 2)
    private BigDecimal loadInWeight;

    @Column(name = "net_weight", precision = 10, scale = 2)
    private BigDecimal netWeight;

    @Column(name = "landfill_tax_rate", precision = 10, scale = 2)
    private BigDecimal landfillTaxRate;

    @Column(name = "tax_due", precision = 10, scale = 2)
    private BigDecimal taxDue;

    @Column(name = "transfer_note_ref")
    private String transferNoteRef;

    @Column(name = "disposal_cost", precision = 10, scale = 2)
    private BigDecimal disposalCost;

    @PrePersist
    @PreUpdate
    public void calculateNetWeight() {
        if (loadOutWeight != null && loadInWeight != null) {
            this.netWeight = loadOutWeight.subtract(loadInWeight);
        }
        if (netWeight != null && landfillTaxRate != null) {
            this.taxDue = netWeight.multiply(landfillTaxRate);
        }
    }

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class MuckawayTicketBuilder {
        public MuckawayTicketBuilder id(Long id) {
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
