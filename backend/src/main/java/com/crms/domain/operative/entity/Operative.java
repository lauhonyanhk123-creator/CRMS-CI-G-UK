package com.crms.domain.operative.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.company.entity.Company;
import com.crms.domain.operative.enums.EmploymentStatus;
import com.crms.domain.operative.enums.OperativeStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "operatives", indexes = {
    @Index(name = "idx_operative_ref", columnList = "employee_ref"),
    @Index(name = "idx_operative_ni", columnList = "ni_number"),
    @Index(name = "idx_operative_status", columnList = "status"),
    @Index(name = "idx_operative_employer", columnList = "employer_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Operative extends BaseEntity {

    @Transient
    private Long compatibilityId;

    @Column(name = "employee_ref", nullable = false, unique = true)
    private String employeeRef;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column
    private String gender;

    @Column
    private String nationality;

    @Column(name = "ni_number")
    private String niNumber;

    @Column
    private BigDecimal utr;

    @Column(name = "right_to_work_expiry")
    private LocalDate rightToWorkExpiry;

    @Column(name = "right_to_work_doc_type")
    private String rightToWorkDocType;

    @Column(name = "passport_number")
    private String passportNumber;

    @Column(name = "bank_sort_code")
    private String bankSortCode;

    @Column(name = "bank_account_number")
    private String bankAccountNumber;

    @Column(name = "hmrc_verified")
    @Builder.Default
    private Boolean hmrcVerified = false;

    @Column(name = "hourly_rate", precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal hourlyRate = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", nullable = false)
    @Builder.Default
    private EmploymentStatus employmentStatus = EmploymentStatus.PAYE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OperativeStatus status = OperativeStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id")
    private Company employer;

    @OneToMany(mappedBy = "operative", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Card> cards = new ArrayList<>();

    @OneToMany(mappedBy = "operative", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Qualification> qualifications = new ArrayList<>();

    @OneToMany(mappedBy = "operative", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Induction> inductions = new ArrayList<>();

    @OneToMany(mappedBy = "operative", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SiteSignOn> siteSignOns = new ArrayList<>();

    @OneToMany(mappedBy = "operative", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Timesheet> timesheets = new ArrayList<>();

    @OneToMany(mappedBy = "operative", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<com.crms.domain.plant.entity.DailyPreUseCheck> dailyPlantChecks = new ArrayList<>();

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isActive() {
        return status == OperativeStatus.ACTIVE;
    }

    public boolean hasRightToWork() {
        return rightToWorkExpiry == null || rightToWorkExpiry.isAfter(LocalDate.now());
    }
    public String getName() {
        String first = firstName == null ? "" : firstName;
        String last = lastName == null ? "" : lastName;
        return (first + " " + last).trim();
    }

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class OperativeBuilder {
        public OperativeBuilder id(Long id) {
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
