package com.crms.domain.site.entity;

import com.crms.domain.common.entity.Address;
import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.company.entity.Company;
import com.crms.domain.site.enums.SiteStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sites", indexes = {
    @Index(name = "idx_site_name", columnList = "name"),
    @Index(name = "idx_site_code", columnList = "site_code"),
    @Index(name = "idx_site_client", columnList = "client_id"),
    @Index(name = "idx_site_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Site extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(name = "site_code", nullable = false, unique = true)
    private String siteCode;

    @Embedded
    private Address address;

    @Column(name = "grid_reference")
    private String gridReference;

    @Column
    private String postcode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Company client;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SiteStatus status = SiteStatus.TENDER;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "completion_date")
    private LocalDate completionDate;

    @Column(name = "estimated_completion_date")
    private LocalDate estimatedCompletionDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<com.crms.domain.tender.entity.Tender> tenders = new ArrayList<>();

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<com.crms.domain.contract.entity.Contract> contracts = new ArrayList<>();

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<com.crms.domain.healthsafety.entity.IncidentReport> incidents = new ArrayList<>();

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<com.crms.domain.healthsafety.entity.PermitToDig> permits = new ArrayList<>();

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<com.crms.domain.operative.entity.SiteSignOn> siteSignOns = new ArrayList<>();

    public boolean isActive() {
        return status == SiteStatus.ACTIVE;
    }

    public boolean isCompleted() {
        return status == SiteStatus.COMPLETED || status == SiteStatus.CLOSED;
    }
}
