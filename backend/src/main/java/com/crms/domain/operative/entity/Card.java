package com.crms.domain.operative.entity;

import com.crms.domain.common.entity.BaseEntity;
import com.crms.domain.operative.enums.CardType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "operative_cards", indexes = {
    @Index(name = "idx_card_operative", columnList = "operative_id"),
    @Index(name = "idx_card_number", columnList = "card_number"),
    @Index(name = "idx_card_type", columnList = "card_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card extends BaseEntity {

    @Transient
    private Long compatibilityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operative_id", nullable = false)
    private Operative operative;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false)
    private CardType cardType;

    @Column
    private String scheme;

    @Column(name = "card_number", nullable = false)
    private String cardNumber;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "last_checked_at")
    private LocalDateTime lastCheckedAt;

    @Column(name = "competency_ref")
    private String competencyRef;

    public boolean isValid() {
        return expiryDate != null && expiryDate.isAfter(LocalDate.now());
    }

    public boolean isExpiringSoon(int days) {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now().plusDays(days)) && !isExpired();
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    /** Compatibility builder method for tests and legacy mapper code. */
    public static class CardBuilder {
        public CardBuilder id(Long id) {
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
