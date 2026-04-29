package com.crms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentRequest {

    @NotBlank(message = "Document type is required")
    private String type;

    private Long entityId;

    private String entityType;

    private String metadata;
}
