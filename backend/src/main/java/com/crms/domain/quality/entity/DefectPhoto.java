package com.crms.domain.quality.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "defect_photos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DefectPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defect_id", nullable = false)
    private Defect defect;

    @Column(nullable = false)
    private String filename;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column
    private String description;

    @Column(name = "uploaded_by")
    private String uploadedBy;

    @Column(name = "taken_date")
    private String takenDate;
}
