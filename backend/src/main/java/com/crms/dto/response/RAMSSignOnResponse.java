package com.crms.dto.response;

import com.crms.domain.healthsafety.entity.RAMSSignOn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RAMSSignOnResponse {

    private Long id;
    private Long ramsId;
    private String ramsRef;
    private Long operativeId;
    private String operativeName;
    private Long siteId;
    private String siteName;
    private LocalDateTime signedAt;
    private LocalDateTime validUntil;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RAMSSignOnResponse fromEntity(RAMSSignOn entity) {
        return RAMSSignOnResponse.builder()
                .id(entity.getId())
                .ramsId(entity.getRams() != null ? entity.getRams().getId() : null)
                .ramsRef(entity.getRams() != null ? entity.getRams().getRamsRef() : null)
                .operativeId(entity.getOperative() != null ? entity.getOperative().getId() : null)
                .operativeName(entity.getOperative() != null ? entity.getOperative().getName() : null)
                .siteId(entity.getSite() != null ? entity.getSite().getId() : null)
                .siteName(entity.getSite() != null ? entity.getSite().getName() : null)
                .signedAt(entity.getSignedAt())
                .validUntil(entity.getValidUntil())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
