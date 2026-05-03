package com.crms.domain.company.entity;

import com.crms.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contacts", indexes = {
    @Index(name = "idx_contact_company", columnList = "company_id"),
    @Index(name = "idx_contact_email", columnList = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contact extends BaseEntity {

    @Transient
    private Long compatibilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column
    private String email;

    @Column
    private String phone;

    @Column
    private String mobile;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "is_primary")
    @Builder.Default
    private Boolean isPrimary = false;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public String getFullName() {
        return firstName + " " + lastName;
    }

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class ContactBuilder {
        public ContactBuilder id(Long id) {
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
