package com.crms.domain.company.entity;

import com.crms.domain.common.entity.Address;
import com.crms.domain.common.entity.Auditable;
import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.common.entity.SoftDeletable;
import com.crms.domain.company.enums.CompanyStatus;
import com.crms.domain.company.enums.CompanyType;
import com.crms.domain.company.enums.CisStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "companies", indexes = {
    @Index(name = "idx_company_name", columnList = "name"),
    @Index(name = "idx_company_type", columnList = "company_type"),
    @Index(name = "idx_company_reg_number", columnList = "registration_number"),
    @Index(name = "idx_company_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SoftDeletable
@Auditable
public class Company extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "company_type", nullable = false)
    private CompanyType companyType;

    @Column(name = "registration_number")
    private String registrationNumber;

    @Column(name = "vat_number")
    private String vatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "cis_status")
    @Builder.Default
    private CisStatus cisStatus = CisStatus.NON_CIS;

    @Column(name = "sic_code")
    private String sicCode;

    @Embedded
    private Address address;

    @Column
    private String phone;

    @Column
    private String email;

    @Column
    private String website;

    @Column(name = "companies_house_id")
    private String companiesHouseId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "companies_house_data", columnDefinition = "jsonb")
    private Map<String, Object> companiesHouseData;

    @Column(name = "hmrc_verification_ref")
    private String hmrcVerificationRef;

    @Column(name = "hmrc_verification_date")
    private LocalDate hmrcVerificationDate;

    @Column(name = "hmrc_deduction_rate", precision = 5, scale = 2)
    private BigDecimal hmrcDeductionRate;

    @Column(name = "cop_verified")
    private Boolean copVerified;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_sort_code")
    private String bankSortCode;

    @Column(name = "bank_account_number")
    private String bankAccountNumber;

    @Column(name = "bank_account_name")
    private String bankAccountName;

    @Column(name = "tax_address", columnDefinition = "TEXT")
    private String taxAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CompanyStatus status = CompanyStatus.ACTIVE;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Contact> contacts = new ArrayList<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<com.crms.domain.site.entity.Site> sites = new ArrayList<>();

    @OneToMany(mappedBy = "employer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<com.crms.domain.operative.entity.Operative> operatives = new ArrayList<>();

    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<com.crms.domain.material.entity.PurchaseOrder> purchaseOrders = new ArrayList<>();

    public boolean isActive() {
        return status == CompanyStatus.ACTIVE;
    }
}
