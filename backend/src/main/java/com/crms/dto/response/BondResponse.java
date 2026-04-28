package com.crms.dto.response;

import com.crms.domain.adoption.entity.Bond;
import com.crms.domain.adoption.enums.BondStatus;
import com.crms.domain.adoption.enums.BondType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BondResponse {
    
    private String id;
    private String bondNumber;
    private BondType bondType;
    private Long issuingSuretyId;
    private String issuingSuretyName;
    private BigDecimal bondValue;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String releaseConditions;
    private LocalDate releaseDate;
    private BondStatus status;
    private Long adoptionCaseId;
    private String adoptionCaseRef;
    private boolean active;
    private boolean expiringSoon;
    private boolean expired;
    
    public static BondResponse fromEntity(Bond entity) {
        return BondResponse.builder()
                .id(entity.getId().toString())
                .bondNumber(entity.getBondNumber())
                .bondType(entity.getBondType())
                .issuingSuretyId(entity.getIssuingSurety() != null ? entity.getIssuingSurety().getId() : null)
                .issuingSuretyName(entity.getIssuingSurety() != null ? entity.getIssuingSurety().getName() : null)
                .bondValue(entity.getBondValue())
                .issueDate(entity.getIssueDate())
                .expiryDate(entity.getExpiryDate())
                .releaseConditions(entity.getReleaseConditions())
                .releaseDate(entity.getReleaseDate())
                .status(entity.getStatus())
                .adoptionCaseId(entity.getAdoptionCase() != null ? entity.getAdoptionCase().getId() : null)
                .adoptionCaseRef(entity.getAdoptionCase() != null ? entity.getAdoptionCase().getCaseRef() : null)
                .active(entity.isActive())
                .expiringSoon(entity.isExpiringSoon(30))
                .expired(entity.isExpired())
                .build();
    }
}
