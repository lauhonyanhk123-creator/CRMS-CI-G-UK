package com.crms.dto.response;

import com.crms.domain.healthsafety.entity.RAMSDocument;
import com.crms.domain.healthsafety.enums.RamsStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RAMSDocumentResponse {

    private Long id;
    private Long contractId;
    private String contractRef;
    private Long templateId;
    private String ramsRef;
    private String title;
    private String version;
    private String description;
    private LocalDate validFrom;
    private LocalDate validUntil;
    private RamsStatus status;
    private String documentRef;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RAMSDocumentResponse fromEntity(RAMSDocument entity) {
        return RAMSDocumentResponse.builder()
                .id(entity.getId())
                .contractId(entity.getContract() != null ? entity.getContract().getId() : null)
                .contractRef(entity.getContract() != null ? entity.getContract().getContractRef() : null)
                .templateId(entity.getTemplate() != null ? entity.getTemplate().getId() : null)
                .ramsRef(entity.getRamsRef())
                .title(entity.getTitle())
                .version(entity.getVersion())
                .description(entity.getDescription())
                .validFrom(entity.getValidFrom())
                .validUntil(entity.getValidUntil())
                .status(entity.getStatus())
                .documentRef(entity.getDocumentRef())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
