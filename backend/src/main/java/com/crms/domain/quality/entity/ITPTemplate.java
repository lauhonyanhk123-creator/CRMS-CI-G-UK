package com.crms.domain.quality.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.quality.enums.InspectionType;
import com.crms.domain.quality.enums.TemplateStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "itp_templates", indexes = {
    @Index(name = "idx_itp_template_name", columnList = "name"),
    @Index(name = "idx_itp_template_category", columnList = "category"),
    @Index(name = "idx_itp_template_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ITPTemplate extends BaseEntity {

    @Transient
    private Long compatibilityId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_category")
    private String tradeCategory;

    @Column(name = "version")
    @Builder.Default
    private Integer version = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TemplateStatus status = TemplateStatus.DRAFT;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ITPTemplateItem> items = new ArrayList<>();

    public void addItem(ITPTemplateItem item) {
        items.add(item);
        item.setTemplate(this);
    }

    public void removeItem(ITPTemplateItem item) {
        items.remove(item);
        item.setTemplate(null);
    }

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class ITPTemplateBuilder {
        public ITPTemplateBuilder id(Long id) {
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
