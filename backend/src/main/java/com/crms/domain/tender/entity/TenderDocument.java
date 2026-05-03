package com.crms.domain.tender.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.tender.enums.CdeStatus;
import com.crms.domain.tender.enums.DocumentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tender_documents", indexes = {
    @Index(name = "idx_td_tender", columnList = "tender_id"),
    @Index(name = "idx_td_document_type", columnList = "document_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenderDocument extends BaseEntity {

    @Transient
    private Long compatibilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tender_id", nullable = false)
    private Tender tender;

    @Column(name = "document_name", nullable = false)
    private String documentName;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "cde_status")
    @Builder.Default
    private CdeStatus cdeStatus = CdeStatus.WIP;

    @Column
    private String version;

    @Column(name = "uploaded_at")
    @Builder.Default
    private LocalDateTime uploadedAt = LocalDateTime.now();

    @Column(name = "file_ref")
    private String fileRef;

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class TenderDocumentBuilder {
        public TenderDocumentBuilder id(Long id) {
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
