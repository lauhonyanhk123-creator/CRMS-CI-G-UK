package com.crms.domain.quality.entity;

import com.crms.domain.quality.enums.InspectionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "itp_template_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ITPTemplateItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private ITPTemplate template;

    @Column(nullable = false)
    private Integer sequence;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "inspection_type", nullable = false)
    private InspectionType inspectionType;

    @Column(name = "responsible_party", nullable = false)
    private String responsibleParty;

    @Column
    private String notes;

    @Column(name = "frequency")
    private String frequency;

    @Column(name = "required_evidence")
    private String requiredEvidence;
}
