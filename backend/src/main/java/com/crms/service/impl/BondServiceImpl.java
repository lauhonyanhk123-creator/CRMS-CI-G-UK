package com.crms.service.impl;

import com.crms.domain.adoption.entity.Bond;
import com.crms.domain.adoption.entity.AdoptionCase;
import com.crms.domain.adoption.enums.BondStatus;
import com.crms.domain.adoption.repository.BondRepository;
import com.crms.domain.adoption.repository.AdoptionCaseRepository;
import com.crms.domain.company.entity.Company;
import com.crms.domain.company.repository.CompanyRepository;
import com.crms.dto.request.BondRequest;
import com.crms.dto.response.BondResponse;
import com.crms.dto.response.PageResponse;
import com.crms.exception.ResourceNotFoundException;
import com.crms.exception.ValidationException;
import com.crms.service.BondService;
import com.crms.util.PaginationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BondServiceImpl implements BondService {
    
    private final BondRepository bondRepository;
    private final AdoptionCaseRepository adoptionCaseRepository;
    private final CompanyRepository companyRepository;
    
    @Override
    public PageResponse<BondResponse> findAll(Map<String, Object> params) {
        int page = PaginationHelper.getPage(params);
        int size = PaginationHelper.getSize(params);
        String sort = PaginationHelper.getSort(params, "expiryDate");
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, sort));
        
        Page<Bond> bondPage;
        if (params.containsKey("status") && params.get("status") != null) {
            BondStatus status = BondStatus.valueOf(params.get("status").toString());
            bondPage = bondRepository.findByStatus(status, pageable);
        } else if (params.containsKey("contractId") && params.get("contractId") != null) {
            List<Bond> bonds = bondRepository.findByContractId(Long.parseLong(params.get("contractId").toString()));
            // Simple pagination for list result
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), bonds.size());
            List<Bond> pageContent = start < bonds.size() ? bonds.subList(start, end) : List.of();
            bondPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, bonds.size());
        } else {
            bondPage = bondRepository.findAll(pageable);
        }
        
        List<BondResponse> content = bondPage.getContent().stream()
                .map(BondResponse::fromEntity)
                .collect(Collectors.toList());
        
        return PageResponse.<BondResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(bondPage.getTotalElements())
                .totalPages(bondPage.getTotalPages())
                .build();
    }
    
    @Override
    public BondResponse findById(Long id) {
        Bond bond = bondRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bond", id));
        return BondResponse.fromEntity(bond);
    }
    
    @Override
    public BondResponse findByBondNumber(String bondNumber) {
        Bond bond = bondRepository.findByBondNumber(bondNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Bond with number: " + bondNumber));
        return BondResponse.fromEntity(bond);
    }
    
    @Override
    @Transactional
    public BondResponse create(Long adoptionCaseId, BondRequest request) {
        AdoptionCase adoptionCase = adoptionCaseRepository.findById(adoptionCaseId)
                .orElseThrow(() -> new ResourceNotFoundException("AdoptionCase", adoptionCaseId));
        
        if (bondRepository.existsByBondNumber(request.getBondNumber())) {
            throw new ValidationException("Bond number already exists: " + request.getBondNumber());
        }
        
        Company issuingSurety = companyRepository.findById(request.getIssuingSuretyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", request.getIssuingSuretyId()));
        
        Bond bond = Bond.builder()
                .bondNumber(request.getBondNumber())
                .bondType(request.getBondType())
                .issuingSurety(issuingSurety)
                .bondValue(request.getBondValue())
                .issueDate(request.getIssueDate())
                .expiryDate(request.getExpiryDate())
                .releaseConditions(request.getReleaseConditions())
                .releaseDate(request.getReleaseDate())
                .status(request.getStatus() != null ? request.getStatus() : BondStatus.ACTIVE)
                .adoptionCase(adoptionCase)
                .build();
        
        bond = bondRepository.save(bond);
        log.info("Created bond {} for adoption case {}", bond.getBondNumber(), adoptionCase.getCaseRef());
        
        return BondResponse.fromEntity(bond);
    }
    
    @Override
    @Transactional
    public BondResponse update(Long id, BondRequest request) {
        Bond bond = bondRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bond", id));
        
        if (request.getIssuingSuretyId() != null && 
            (bond.getIssuingSurety() == null || !request.getIssuingSuretyId().equals(bond.getIssuingSurety().getId()))) {
            Company issuingSurety = companyRepository.findById(request.getIssuingSuretyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company", request.getIssuingSuretyId()));
            bond.setIssuingSurety(issuingSurety);
        }
        
        if (request.getBondNumber() != null) {
            bond.setBondNumber(request.getBondNumber());
        }
        if (request.getBondType() != null) {
            bond.setBondType(request.getBondType());
        }
        if (request.getBondValue() != null) {
            bond.setBondValue(request.getBondValue());
        }
        if (request.getIssueDate() != null) {
            bond.setIssueDate(request.getIssueDate());
        }
        if (request.getExpiryDate() != null) {
            bond.setExpiryDate(request.getExpiryDate());
        }
        if (request.getReleaseConditions() != null) {
            bond.setReleaseConditions(request.getReleaseConditions());
        }
        if (request.getReleaseDate() != null) {
            bond.setReleaseDate(request.getReleaseDate());
        }
        if (request.getStatus() != null) {
            bond.setStatus(request.getStatus());
        }
        
        bond = bondRepository.save(bond);
        log.info("Updated bond {}", bond.getBondNumber());
        
        return BondResponse.fromEntity(bond);
    }
    
    @Override
    @Transactional
    public BondResponse releaseBond(Long id, String releaseConditions) {
        Bond bond = bondRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bond", id));
        
        if (bond.getStatus() == BondStatus.RELEASED) {
            throw new ValidationException("Bond is already released");
        }
        
        bond.setStatus(BondStatus.RELEASED);
        bond.setReleaseDate(LocalDate.now());
        if (releaseConditions != null) {
            bond.setReleaseConditions(releaseConditions);
        }
        
        bond = bondRepository.save(bond);
        log.info("Bond {} released for adoption case {}", bond.getBondNumber(), 
                bond.getAdoptionCase().getCaseRef());
        
        return BondResponse.fromEntity(bond);
    }
    
    @Override
    @Transactional
    public BondResponse partialRelease(Long id, BigDecimal releaseAmount) {
        Bond bond = bondRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bond", id));
        
        if (bond.getStatus() == BondStatus.RELEASED) {
            throw new ValidationException("Bond is already fully released");
        }
        
        if (releaseAmount.compareTo(bond.getBondValue()) > 0) {
            throw new ValidationException("Release amount cannot exceed bond value");
        }
        
        bond.setStatus(BondStatus.PARTIALLY_RELEASED);
        // Note: For full tracking, we'd need to track actual released amount separately
        // This is a simplified implementation
        
        bond = bondRepository.save(bond);
        log.info("Bond {} partially released by amount {}", bond.getBondNumber(), releaseAmount);
        
        return BondResponse.fromEntity(bond);
    }
    
    @Override
    @Transactional
    public BondResponse markAsCalled(Long id, String reason) {
        Bond bond = bondRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bond", id));
        
        if (bond.getStatus() == BondStatus.CALLED) {
            throw new ValidationException("Bond is already called");
        }
        
        bond.setStatus(BondStatus.CALLED);
        
        bond = bondRepository.save(bond);
        log.warn("Bond {} called. Reason: {}", bond.getBondNumber(), reason);
        
        return BondResponse.fromEntity(bond);
    }
    
    @Override
    public PageResponse<BondResponse> findExpiringBonds(int days) {
        LocalDate alertDate = LocalDate.now().plusDays(days);
        List<Bond> bonds = bondRepository.findBondsNeedingExpiryAlert(LocalDate.now(), alertDate);
        
        List<BondResponse> content = bonds.stream()
                .map(BondResponse::fromEntity)
                .collect(Collectors.toList());
        
        return PageResponse.<BondResponse>builder()
                .content(content)
                .page(0)
                .size(content.size())
                .totalElements((long) content.size())
                .totalPages(1)
                .build();
    }
    
    @Override
    public PageResponse<BondResponse> findExpiredBonds() {
        List<Bond> bonds = bondRepository.findExpiredActiveBonds(LocalDate.now());
        
        List<BondResponse> content = bonds.stream()
                .map(BondResponse::fromEntity)
                .collect(Collectors.toList());
        
        return PageResponse.<BondResponse>builder()
                .content(content)
                .page(0)
                .size(content.size())
                .totalElements((long) content.size())
                .totalPages(1)
                .build();
    }
}
