package com.crms.domain.operative.repository;

import com.crms.domain.operative.entity.Card;
import com.crms.domain.operative.enums.CardType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByOperativeId(Long operativeId);

    List<Card> findByCardType(CardType cardType);

    Optional<Card> findByCardNumber(String cardNumber);

    @Query("SELECT c FROM Card c WHERE c.operative.id = :operativeId AND c.cardType = :cardType AND c.expiryDate > :date")
    List<Card> findValidCardsByType(@Param("operativeId") Long operativeId, @Param("cardType") CardType cardType, @Param("date") LocalDate date);

    @Query("SELECT c FROM Card c WHERE c.expiryDate <= :date AND c.expiryDate > :today")
    List<Card> findExpiringCards(@Param("date") LocalDate date, @Param("today") LocalDate today);
}
