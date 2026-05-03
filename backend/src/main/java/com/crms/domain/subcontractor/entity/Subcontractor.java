package com.crms.domain.subcontractor.entity;

import com.crms.domain.company.entity.Company;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.*;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("SUB")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Where(clause = "deleted_at IS NULL")
public class Subcontractor extends Company {

    @OneToMany(mappedBy = "company", cascade = jakarta.persistence.CascadeType.ALL)
    private List<CISVerification> cisVerifications = new ArrayList<>();

    public boolean isGateApproved() {
        return cisVerifications.stream()
                .anyMatch(CISVerification::isValid);
    }

    public String getSubbieGateStatus() {
        if (cisVerifications.isEmpty()) {
            return "UNVERIFIED";
        }
        return cisVerifications.stream()
                .filter(CISVerification::isValid)
                .findFirst()
                .map(v -> v.getStatus().name())
                .orElse("EXPIRED");
    }
}
