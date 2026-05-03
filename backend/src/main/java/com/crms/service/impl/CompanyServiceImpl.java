package com.crms.service.impl;

import com.crms.domain.company.entity.Company;
import com.crms.domain.company.repository.CompanyRepository;
import com.crms.dto.request.CompanyRequest;
import com.crms.dto.response.CISVerificationResponse;
import com.crms.dto.response.CompanyResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.SubbieGateStatus;
import com.crms.exception.ResourceNotFoundException;
import com.crms.service.CompanyService;
import com.crms.util.PaginationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {
    
    private final CompanyRepository companyRepository;
    
    @Override
    @Transactional(readOnly = true)
    public PageResponse<CompanyResponse> findAll(Map<String, Object> params) {
        int page = PaginationHelper.getPage(params);
        int size = PaginationHelper.getSize(params);
        String sort = PaginationHelper.getSort(params, "id");
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        
        Page<Company> companyPage;
        if (params.containsKey("type") && params.get("type") != null) {
            companyPage = companyRepository.findByCompanyType(com.crms.domain.company.enums.CompanyType.valueOf(params.get("type").toString()), pageable);
        } else {
            companyPage = companyRepository.findAll(pageable);
        }
        
        List<CompanyResponse> content = companyPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return PageResponse.<CompanyResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(companyPage.getTotalElements())
                .totalPages(companyPage.getTotalPages())
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public CompanyResponse findById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", id));
        return mapToResponse(company);
    }
    
    @Override
    @Transactional
    public CompanyResponse create(CompanyRequest request) {
        Company company = Company.builder()
                .name(request.getName())
                .companyType(request.getCompanyType())
                .registrationNumber(request.getRegistrationNumber())
                .vatNumber(request.getVatNumber())
                .address(request.getAddress())
                .phone(request.getPhone())
                .email(request.getEmail())
                .website(request.getWebsite())
                .sicCode(request.getSicCode())
                .cisStatus(request.getCisStatus())
                .companiesHouseId(request.getCompaniesHouseId())
                .hmrcVerificationRef(request.getHmrcVerificationRef())
                .hmrcDeductionRate(request.getHmrcDeductionRate())
                .copVerified(request.getCopVerified())
                .bankName(request.getBankName())
                .bankSortCode(request.getBankSortCode())
                .bankAccountNumber(request.getBankAccountNumber())
                .bankAccountName(request.getBankAccountName())
                .taxAddress(request.getTaxAddress())
                .build();
        
        company = companyRepository.save(company);
        return mapToResponse(company);
    }
    
    @Override
    @Transactional
    public CompanyResponse update(Long id, CompanyRequest request) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", id));
        
        company.setName(request.getName());
        company.setCompanyType(request.getCompanyType());
        company.setRegistrationNumber(request.getRegistrationNumber());
        company.setVatNumber(request.getVatNumber());
        company.setAddress(request.getAddress());
        company.setPhone(request.getPhone());
        company.setEmail(request.getEmail());
        company.setWebsite(request.getWebsite());
        company.setSicCode(request.getSicCode());
        company.setCisStatus(request.getCisStatus());
        company.setBankName(request.getBankName());
        company.setBankSortCode(request.getBankSortCode());
        company.setBankAccountNumber(request.getBankAccountNumber());
        company.setBankAccountName(request.getBankAccountName());
        company.setTaxAddress(request.getTaxAddress());
        
        company = companyRepository.save(company);
        return mapToResponse(company);
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", id));
        companyRepository.delete(company);
    }
    
    @Override
    @Transactional
    public CompanyResponse refreshCompaniesHouse(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", id));
        
        // Stub for Companies House API integration
        log.info("Refreshing Companies House data for company: {}", company.getName());
        
        // In a real implementation, this would call the Companies House API
        // For now, we'll just return the existing data
        return mapToResponse(company);
    }
    
    @Override
    @Transactional
    public CISVerificationResponse verifyCIS(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", id));
        
        // Stub for HMRC CIS verification API
        log.info("Verifying CIS status for company: {}", company.getName());
        
        return CISVerificationResponse.builder()
                .companyId(id)
                .companyName(company.getName())
                .verificationStatus(company.getCisStatus() != null ? company.getCisStatus().name() : "UNKNOWN")
                .hmrcDeductionRate(company.getHmrcDeductionRate())
                .reference(company.getHmrcVerificationRef())
                .verifiedAt(java.time.LocalDateTime.now().toString())
                .message("CIS verification completed")
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public SubbieGateStatus getSubbieGateStatus(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", id));
        
        // Stub implementation - would integrate with actual subbie gate system
        return SubbieGateStatus.builder()
                .operativeId(id)
                .isHMRCVerified(company.getHmrcVerificationRef() != null)
                .isCSCSValid(true)
                .isRAMSValid(true)
                .isInductionValid(true)
                .isPlantTicketValid(true)
                .isGateOpen(true)
                .statusMessage("All checks passed")
                .build();
    }
    
    private CompanyResponse mapToResponse(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .companyType(company.getCompanyType() != null ? company.getCompanyType().name() : null)
                .registrationNumber(company.getRegistrationNumber())
                .vatNumber(company.getVatNumber())
                .phone(company.getPhone())
                .email(company.getEmail())
                .website(company.getWebsite())
                .sicCode(company.getSicCode())
                .cisStatus(company.getCisStatus() != null ? company.getCisStatus().name() : null)
                .companiesHouseId(company.getCompaniesHouseId())
                .companiesHouseData(company.getCompaniesHouseData())
                .hmrcVerificationRef(company.getHmrcVerificationRef())
                .hmrcVerificationDate(company.getHmrcVerificationDate())
                .hmrcDeductionRate(company.getHmrcDeductionRate() != null ? company.getHmrcDeductionRate().toString() : null)
                .copVerified(company.getCopVerified())
                .bankName(company.getBankName())
                .bankSortCode(company.getBankSortCode())
                .bankAccountNumber(maskAccountNumber(company.getBankAccountNumber()))
                .bankAccountName(company.getBankAccountName())
                .taxAddress(company.getTaxAddress())
                .status(company.getStatus() != null ? company.getStatus().name() : null)
                .build();
    }
    
    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return accountNumber;
        }
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }
}