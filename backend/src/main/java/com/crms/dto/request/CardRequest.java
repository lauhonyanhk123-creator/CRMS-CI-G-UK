package com.crms.dto.request;

import com.crms.domain.operative.enums.CardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardRequest {

    @NotNull(message = "Card type is required")
    private CardType cardType;

    private String scheme;

    @NotBlank(message = "Card number is required")
    private String cardNumber;

    private LocalDate expiryDate;

    private String photoUrl;

    private String competencyRef;
}