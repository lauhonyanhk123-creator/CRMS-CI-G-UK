package com.crms.domain.quality.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inspection_attachments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InspectionAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspection_record_id", nullable = false)
    private InspectionRecord inspectionRecord;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private String fileType;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column
    private String description;

    @Column(name = "uploaded_by")
    private String uploadedBy;
}
