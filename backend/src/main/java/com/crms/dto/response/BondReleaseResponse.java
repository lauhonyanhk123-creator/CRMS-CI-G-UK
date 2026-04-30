package com.crms.dto.response;

import com.crms.domain.adoption.entity.Bond;
import com.crms.domain.adoption.enums.BondStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BondReleaseResponse {

    private Long id;
    private Long caseId;
    private String caseRef;
    private String bondRef;
    private Boolean releaseRequested;
    private LocalDate releaseRequestedDate;
    private LocalDate releaseDate;
    private BondStatus status;

    public static BondReleaseResponse fromEntity(Bond entity) {
        return BondReleaseResponse.builder()
                .id(entity.getId())
                .caseId(entity.getAdoptionCase() != null ? entity.getAdoptionCase().getId() : null)
                .caseRef(entity.getAdoptionCase() != null ? entity.getAdoptionCase().getCaseRef() : null)
                .bondRef(entity.getBondRef())
                .releaseRequested(entity.getReleaseRequested())
                .releaseRequestedDate(entity.getReleaseRequestedDate())
                .releaseDate(entity.getReleaseDate())
                .status(entity.getStatus())
                .build();
    }
}
