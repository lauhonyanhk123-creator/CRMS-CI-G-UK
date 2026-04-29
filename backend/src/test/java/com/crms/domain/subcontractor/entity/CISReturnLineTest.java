package com.crms.domain.subcontractor.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CISReturnLine entity.
 * Tests CIS deduction rate enforcement per HMRC rules.
 * Valid CIS rates: 0%, 20%, 30% only.
 */
class CISReturnLineTest {

    @Nested
    @DisplayName("CIS Rate Enforcement Tests")
    class CisRateEnforcementTests {

        @Test
        @DisplayName("calculateDeduction with 20% rate computes correct deduction")
        void calculateDeduction_with20PercentRate() {
            // Given
            CISReturnLine line = new CISReturnLine();
            line.setGrossPaid(new BigDecimal("10000.00"));
            line.setCisRate(new BigDecimal("20.00"));

            // When - trigger @PrePersist
            line.calculateDeduction();

            // Then - 10000 * 20% = 2000
            assertEquals(new BigDecimal("2000.00"), line.getDeduction());
            assertEquals(new BigDecimal("8000.00"), line.getNetPaid());
        }

        @Test
        @DisplayName("calculateDeduction with 30% rate computes correct deduction")
        void calculateDeduction_with30PercentRate() {
            // Given
            CISReturnLine line = new CISReturnLine();
            line.setGrossPaid(new BigDecimal("10000.00"));
            line.setCisRate(new BigDecimal("30.00"));

            // When
            line.calculateDeduction();

            // Then - 10000 * 30% = 3000
            assertEquals(new BigDecimal("3000.00"), line.getDeduction());
            assertEquals(new BigDecimal("7000.00"), line.getNetPaid());
        }

        @Test
        @DisplayName("calculateDeduction with 0% rate results in zero deduction")
        void calculateDeduction_with0PercentRate() {
            // Given
            CISReturnLine line = new CISReturnLine();
            line.setGrossPaid(new BigDecimal("10000.00"));
            line.setCisRate(new BigDecimal("0.00"));

            // When
            line.calculateDeduction();

            // Then - no deduction for 0% rate
            assertEquals(new BigDecimal("0.00"), line.getDeduction());
            assertEquals(new BigDecimal("10000.00"), line.getNetPaid());
        }

        @Test
        @DisplayName("calculateDeduction with null rate defaults to zero deduction")
        void calculateDeduction_withNullRate() {
            // Given
            CISReturnLine line = new CISReturnLine();
            line.setGrossPaid(new BigDecimal("10000.00"));
            line.setCisRate(null);

            // When
            line.calculateDeduction();

            // Then - no deduction when rate is null
            assertEquals(new BigDecimal("0.00"), line.getDeduction());
            assertEquals(new BigDecimal("10000.00"), line.getNetPaid());
        }

        @Test
        @DisplayName("calculateDeduction with null gross paid handles gracefully")
        void calculateDeduction_withNullGrossPaid() {
            // Given
            CISReturnLine line = new CISReturnLine();
            line.setGrossPaid(null);
            line.setCisRate(new BigDecimal("20.00"));

            // When
            line.calculateDeduction();

            // Then
            assertEquals(new BigDecimal("0.00"), line.getDeduction());
            assertNull(line.getNetPaid());
        }

        @Test
        @DisplayName("calculateDeduction with negative rate handles gracefully")
        void calculateDeduction_withNegativeRate() {
            // Given
            CISReturnLine line = new CISReturnLine();
            line.setGrossPaid(new BigDecimal("10000.00"));
            line.setCisRate(new BigDecimal("-5.00"));

            // When - negative rate should not trigger deduction
            line.calculateDeduction();

            // Then - negative rate treated as zero
            assertEquals(new BigDecimal("0.00"), line.getDeduction());
            assertEquals(new BigDecimal("10000.00"), line.getNetPaid());
        }

        @Test
        @DisplayName("calculateDeduction rounds to 2 decimal places")
        void calculateDeduction_roundsToTwoDecimalPlaces() {
            // Given
            CISReturnLine line = new CISReturnLine();
            line.setGrossPaid(new BigDecimal("999.99"));
            line.setCisRate(new BigDecimal("20.00"));

            // When - 999.99 * 20% = 199.998 -> rounds to 200.00
            line.calculateDeduction();

            // Then
            assertEquals(new BigDecimal("200.00"), line.getDeduction());
            assertEquals(new BigDecimal("799.99"), line.getNetPaid());
        }

        @Test
        @DisplayName("calculateDeduction with large amounts handles precision")
        void calculateDeduction_withLargeAmounts() {
            // Given
            CISReturnLine line = new CISReturnLine();
            line.setGrossPaid(new BigDecimal("999999.99"));
            line.setCisRate(new BigDecimal("20.00"));

            // When - 999999.99 * 20% = 199999.998 -> 200000.00
            line.calculateDeduction();

            // Then
            assertEquals(new BigDecimal("200000.00"), line.getDeduction());
            assertEquals(new BigDecimal("799999.99"), line.getNetPaid());
        }

        @Test
        @DisplayName("calculateDeduction with decimal rate values")
        void calculateDeduction_withDecimalRate() {
            // Given - rates should be 0, 20, or 30, but we test handling
            CISReturnLine line = new CISReturnLine();
            line.setGrossPaid(new BigDecimal("10000.00"));
            line.setCisRate(new BigDecimal("17.50"));

            // When - 10000 * 17.5% = 1750.00
            line.calculateDeduction();

            // Then
            assertEquals(new BigDecimal("1750.00"), line.getDeduction());
            assertEquals(new BigDecimal("8250.00"), line.getNetPaid());
        }
    }
}
