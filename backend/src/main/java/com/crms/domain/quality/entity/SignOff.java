package com.crms.domain.quality.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.contract.entity.Contract;
import com.crms.domain.quality.enums.BuildingControlType;
import com.crms.domain.quality.enums.SignOffResult;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "sign_offs", indexes = {
    @Index(name = "idx_signoff_contract", columnList = "contract_id"),
    @Index(name = "idx_signoff_type", columnList = "building_control_type"),
    @Index(name = "idx_signoff_result", columnList = "result"),
    @Index(name = "idx_signoff_date", columnList = "inspection_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignOff extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Enumerated(EnumType.STRING)
    @Column(name = "building_control_type", nullable = false)
    private BuildingControlType buildingControlType;

    @Column(name = "inspection_type", nullable = false)
    private String inspectionType;

    @Column(name = "reference_number")
    private String referenceNumber;

    @Column(name = "inspector_name")
    private String inspectorName;

    @Column(name = "inspector_email")
    private String inspectorEmail;

    @Column(name = "inspector_phone")
    private String inspectorPhone;

    @Column(name = "inspection_date", nullable = false)
    private LocalDate inspectionDate;

    @Column(name = "next_inspection_date")
    private LocalDate nextInspectionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SignOffResult result;

    @Column(name = "conditions_or_notes", columnDefinition = "TEXT")
    private String conditionsOrNotes;

    @Column(name = "report_path")
    private String reportPath;

    @Column(name = "report_number")
    private String reportNumber;

    @Column(name = "sign_off_signature", columnDefinition = "TEXT")
    private String signOffSignature;

    @Column(name = "sign_off_date")
    private LocalDate signOffDate;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
