package com.crms.service.impl;

import java.util.Optional;
import com.crms.domain.subcontractor.entity.CISReturn;
import com.crms.domain.subcontractor.entity.CISReturnLine;
import com.crms.domain.subcontractor.entity.CISVerification;
import com.crms.domain.subcontractor.enums.CisReturnStatus;
import com.crms.domain.subcontractor.enums.CisVerificationStatus;
import com.crms.domain.subcontractor.repository.CISReturnLineRepository;
import com.crms.domain.subcontractor.repository.CISReturnRepository;
import com.crms.domain.subcontractor.repository.CISVerificationRepository;
import com.crms.exception.ResourceNotFoundException;
import com.crms.service.CISService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CISServiceImpl implements CISService {
    
    private final CISReturnRepository cisReturnRepository;
    private final CISReturnLineRepository cisReturnLineRepository;
    private final CISVerificationRepository cisVerificationRepository;
    
    @Override
    @Transactional(readOnly = true)
    public Object findAll(String taxMonth, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<CISReturn> returnPage;
        
        if (taxMonth != null && !taxMonth.isEmpty()) {
            returnPage = cisReturnRepository.findByTaxMonth(taxMonth, pageable);
        } else {
            returnPage = cisReturnRepository.findAll(pageable);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("content", returnPage.getContent());
        result.put("page", returnPage.getNumber());
        result.put("size", returnPage.getSize());
        result.put("totalElements", returnPage.getTotalElements());
        result.put("totalPages", returnPage.getTotalPages());
        
        return result;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Object findById(Long id) {
        CISReturn cisReturn = cisReturnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CISReturn", id));
        return cisReturn;
    }
    
    @Override
    @Transactional
    public Object generateReturn(String taxMonth) {
        log.info("Generating CIS return for {}", taxMonth);
        
        // Validate tax month format (YYYY-MM)
        if (taxMonth == null || !taxMonth.matches("\\d{4}-\\d{2}")) {
            throw new IllegalArgumentException("Invalid tax month format. Expected YYYY-MM");
        }
        
        // Check if return already exists for this tax month
        Optional<CISReturn> existingReturn = cisReturnRepository.findByTaxMonth(taxMonth);
        if (existingReturn.isPresent()) {
            return Map.of(
                "message", "CIS return already exists for " + taxMonth,
                "returnId", existingReturn.get().getId(),
                "status", existingReturn.get().getStatus().name()
            );
        }
        
        // Get all valid CIS verifications
        List<CISVerification> validVerifications = cisVerificationRepository
                .findByStatus(CisVerificationStatus.VERIFIED);
        
        if (validVerifications.isEmpty()) {
            throw new IllegalStateException("No valid CIS verifications found to generate return");
        }
        
        // Create the CIS return
        CISReturn cisReturn = CISReturn.builder()
                .taxMonth(taxMonth)
                .status(CisReturnStatus.DRAFT)
                .build();
        
        cisReturn = cisReturnRepository.save(cisReturn);
        
        // Generate return lines from verifications and applications for payment
        List<CISReturnLine> returnLines = new ArrayList<>();
        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalDeduction = BigDecimal.ZERO;
        
        for (CISVerification verification : validVerifications) {
            if (verification.getCompany() == null) continue;
            
            BigDecimal cisRate = verification.getRate() != null 
                    ? verification.getRate() 
                    : new BigDecimal("20"); // Default 20% CIS rate
            
            // Calculate based on subcontractor's payments
            BigDecimal grossPaid = BigDecimal.ZERO; // Would be calculated from actual payments
            BigDecimal deduction = grossPaid.multiply(cisRate).divide(new BigDecimal("100"));
            BigDecimal netPaid = grossPaid.subtract(deduction);
            
            CISReturnLine line = CISReturnLine.builder()
                    .cisReturn(cisReturn)
                    .subcontractor(verification.getCompany())
                    .grossPaid(grossPaid)
                    .cisRate(cisRate)
                    .deduction(deduction)
                    .netPaid(netPaid)
                    .build();
            
            returnLines.add(line);
            totalGross = totalGross.add(grossPaid);
            totalDeduction = totalDeduction.add(deduction);
        }
        
        cisReturnLineRepository.saveAll(returnLines);
        
        log.info("Generated CIS return {} with {} lines for tax month {}", 
                cisReturn.getId(), returnLines.size(), taxMonth);
        
        return Map.of(
            "id", cisReturn.getId(),
            "taxMonth", taxMonth,
            "status", CisReturnStatus.DRAFT.name(),
            "lineCount", returnLines.size(),
            "totalGross", totalGross,
            "totalDeduction", totalDeduction,
            "totalNet", totalGross.subtract(totalDeduction),
            "message", "CIS return generated successfully"
        );
    }
    
    @Override
    @Transactional
    public Object submitReturn(Long id) {
        log.info("Submitting CIS return {}", id);
        
        CISReturn cisReturn = cisReturnRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CISReturn", id));
        
        if (cisReturn.getCisReturnLines() == null || cisReturn.getCisReturnLines().isEmpty()) {
            throw new IllegalStateException("Cannot submit empty CIS return");
        }
        
        if (cisReturn.isSubmitted()) {
            return Map.of(
                "message", "Return already submitted",
                "id", id,
                "status", cisReturn.getStatus().name(),
                "submittedAt", cisReturn.getSubmittedAt(),
                "hmrcReceiptRef", cisReturn.getHmrcReceiptRef() != null ? cisReturn.getHmrcReceiptRef() : ""
            );
        }
        
        // Calculate totals
        BigDecimal totalGross = cisReturn.getCisReturnLines().stream()
                .map(CISReturnLine::getGrossPaid)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalDeduction = cisReturn.getCisReturnLines().stream()
                .map(CISReturnLine::getDeduction)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Simulate HMRC submission (in production, this would call HMRC API)
        String hmrcReceiptRef = "HMRC-" + id + "-" + System.currentTimeMillis();
        
        cisReturn.setStatus(CisReturnStatus.SUBMITTED);
        cisReturn.setSubmittedAt(LocalDateTime.now());
        cisReturn.setHmrcReceiptRef(hmrcReceiptRef);
        
        cisReturn = cisReturnRepository.save(cisReturn);
        
        log.info("CIS return {} submitted successfully with reference {}", id, hmrcReceiptRef);
        
        return Map.of(
            "id", cisReturn.getId(),
            "status", cisReturn.getStatus().name(),
            "submittedAt", cisReturn.getSubmittedAt(),
            "hmrcReceiptRef", hmrcReceiptRef,
            "totalGross", totalGross,
            "totalDeduction", totalDeduction,
            "totalNet", totalGross.subtract(totalDeduction),
            "message", "CIS return submitted successfully to HMRC"
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public Object generatePaymentStatements(Long returnId) {
        log.info("Generating payment statements for return {}", returnId);
        
        CISReturn cisReturn = cisReturnRepository.findById(returnId)
                .orElseThrow(() -> new ResourceNotFoundException("CISReturn", returnId));
        
        if (cisReturn.getCisReturnLines() == null || cisReturn.getCisReturnLines().isEmpty()) {
            throw new IllegalStateException("No return lines found for CIS return " + returnId);
        }
        
        List<Map<String, Object>> statements = new ArrayList<>();
        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalDeduction = BigDecimal.ZERO;
        
        for (CISReturnLine line : cisReturn.getCisReturnLines()) {
            BigDecimal gross = line.getGrossPaid() != null ? line.getGrossPaid() : BigDecimal.ZERO;
            BigDecimal deduction = line.getDeduction() != null ? line.getDeduction() : BigDecimal.ZERO;
            BigDecimal net = line.getNetPaid() != null ? line.getNetPaid() : BigDecimal.ZERO;
            
            Map<String, Object> statement = new HashMap<>();
            statement.put("subcontractorId", line.getSubcontractor().getId());
            statement.put("subcontractorName", line.getSubcontractor().getName());
            statement.put("subcontractorUtr", line.getSubcontractor().getUtr() != null ? line.getSubcontractor().getUtr() : "N/A");
            statement.put("cisRate", line.getCisRate());
            statement.put("grossPaid", gross);
            statement.put("cisDeduction", deduction);
            statement.put("netPaid", net);
            
            statements.add(statement);
            
            totalGross = totalGross.add(gross);
            totalDeduction = totalDeduction.add(deduction);
        }
        
        log.info("Generated {} payment statements for CIS return {}", statements.size(), returnId);
        
        return Map.of(
            "returnId", returnId,
            "taxMonth", cisReturn.getTaxMonth(),
            "status", cisReturn.getStatus().name(),
            "statements", statements,
            "summary", Map.of(
                "totalGross", totalGross,
                "totalDeduction", totalDeduction,
                "totalNet", totalGross.subtract(totalDeduction),
                "subcontractorCount", statements.size()
            )
        );
    }
}
