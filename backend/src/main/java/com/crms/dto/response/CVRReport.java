package com.crms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Cost-Value Reconciliation report for a single contract.
 * Provides a full breakdown of value, cost, and margin for each valuation period.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CVRReport {

    private Long contractId;
    private String contractRef;
    private String contractTitle;

    // Valuation metadata
    private LocalDate valuationDate;
    private String valuationPeriod;
    private Integer applicationNumber;
    private String measurementStandard;
    private String contractForm;

    // BCIS Indexation
    private BigDecimal bcisBaseIndex;
    private BigDecimal bcisCurrentIndex;
    private BigDecimal bcisAdjustmentFactor;

    // Value side
    private BigDecimal contractSum;
    private BigDecimal valueMeasuredWork;
    private BigDecimal valueDayworks;
    private BigDecimal valueVariations;
    private BigDecimal valueAdjustments;
    private BigDecimal grossValueToDate;
    private BigDecimal lessRetentionToDate;
    private BigDecimal netValueToDate;
    private BigDecimal valueForecastFinal;

    // Cost side (pre-indexation)
    private BigDecimal costPlant;
    private BigDecimal costLabour;
    private BigDecimal costMaterials;
    private BigDecimal costSubcontract;
    private BigDecimal costDayworks;
    private BigDecimal costDisallowed;  // NEC4 cl.11.2(25)
    private BigDecimal costTotalPreIndexation;

    // Cost side (post-indexation)
    private BigDecimal indexedCostPlant;
    private BigDecimal indexedCostMaterials;
    private BigDecimal indexedCostTotal;

    // Gross margin
    private BigDecimal grossMargin;
    private BigDecimal grossMarginPercent;

    // Retention
    private BigDecimal retentionPercent;
    private BigDecimal retentionHeld;
    private BigDecimal retentionReleasedPC;
    private BigDecimal retentionReleasedDefects;
    private BigDecimal retentionBalance;

    // Earthworks balance
    private BigDecimal earthworksImportedVolume;    // muckaway in (tonnes)
    private BigDecimal earthworksExportedVolume;    // spoil out (tonnes)
    private BigDecimal earthworksBalance;
    private BigDecimal earthworksImportedCost;
    private BigDecimal earthworksExportedValue;

    // Early warning / disallowed cost flags
    private BigDecimal earlyWarningAmount;
    private boolean hasDisallowedCosts;

    // Progress
    private BigDecimal progressPercent;
    private BigDecimal overOrUnderValuation;

    // Line items breakdown
    @Builder.Default
    private List<CVRPackageLine> packageLines = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CVRPackageLine {
        private String packageRef;
        private String description;
        private String cesmm4Class;

        private BigDecimal agreedQuantity;
        private BigDecimal currentQuantity;
        private BigDecimal previousQuantity;
        private BigDecimal thisPeriodQuantity;

        private BigDecimal rate;
        private BigDecimal uom;

        private BigDecimal measuredValue;
        private BigDecimal dayworksValue;
        private BigDecimal variationValue;
        private BigDecimal totalValue;

        private BigDecimal costToDate;
        private BigDecimal indexedCostToDate;
        private BigDecimal margin;
        private BigDecimal marginPercent;

        private boolean isDayworks;
        private boolean isDisallowedCost;
        private String notes;
    }
}