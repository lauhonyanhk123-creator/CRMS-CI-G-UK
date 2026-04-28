package com.crms.domain.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@SQLDelete(sql = "UPDATE companies SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class SoftDeletable {

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
