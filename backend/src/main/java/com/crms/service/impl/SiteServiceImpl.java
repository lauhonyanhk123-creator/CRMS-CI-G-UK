package com.crms.service.impl;

import com.crms.domain.company.entity.Company;
import com.crms.domain.company.repository.CompanyRepository;
import com.crms.domain.site.entity.Site;
import com.crms.domain.site.repository.SiteRepository;
import com.crms.dto.request.SiteRequest;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.SiteResponse;
import com.crms.exception.ResourceNotFoundException;
import com.crms.service.SiteService;
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
public class SiteServiceImpl implements SiteService {
    
    private final SiteRepository siteRepository;
    private final CompanyRepository companyRepository;
    
    @Override
    @Transactional(readOnly = true)
    public PageResponse<SiteResponse> findAll(Map<String, Object> params) {
        int page = PaginationHelper.getPage(params);
        int size = PaginationHelper.getSize(params);
        String sort = PaginationHelper.getSort(params, "id");
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        
        Page<Site> sitePage;
        if (params.containsKey("clientId") && params.get("clientId") != null) {
            Long clientId = Long.parseLong(params.get("clientId").toString());
            sitePage = siteRepository.findByClientId(clientId, pageable);
        } else if (params.containsKey("status") && params.get("status") != null) {
            sitePage = siteRepository.findByStatus(params.get("status").toString(), pageable);
        } else {
            sitePage = siteRepository.findAll(pageable);
        }
        
        List<SiteResponse> content = sitePage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return PageResponse.<SiteResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(sitePage.getTotalElements())
                .totalPages(sitePage.getTotalPages())
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public SiteResponse findById(Long id) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Site", id));
        return mapToResponse(site);
    }
    
    @Override
    @Transactional
    public SiteResponse create(SiteRequest request) {
        Company client = companyRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", request.getClientId()));
        
        Site site = Site.builder()
                .name(request.getName())
                .siteCode(request.getSiteCode())
                .address(request.getAddress())
                .gridReference(request.getGridReference())
                .postcode(request.getPostcode())
                .client(client)
                .status(request.getStatus())
                .startDate(request.getStartDate())
                .completionDate(request.getCompletionDate())
                .estimatedCompletionDate(request.getEstimatedCompletionDate())
                .notes(request.getNotes())
                .build();
        
        site = siteRepository.save(site);
        return mapToResponse(site);
    }
    
    @Override
    @Transactional
    public SiteResponse update(Long id, SiteRequest request) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Site", id));
        
        if (request.getClientId() != null && (site.getClient() == null || !request.getClientId().equals(site.getClient().getId()))) {
            Company client = companyRepository.findById(request.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client", request.getClientId()));
            site.setClient(client);
        }
        
        site.setName(request.getName());
        site.setSiteCode(request.getSiteCode());
        site.setAddress(request.getAddress());
        site.setGridReference(request.getGridReference());
        site.setPostcode(request.getPostcode());
        site.setStatus(request.getStatus());
        site.setStartDate(request.getStartDate());
        site.setCompletionDate(request.getCompletionDate());
        site.setEstimatedCompletionDate(request.getEstimatedCompletionDate());
        site.setNotes(request.getNotes());
        
        site = siteRepository.save(site);
        return mapToResponse(site);
    }
    
    private SiteResponse mapToResponse(Site site) {
        return SiteResponse.builder()
                .id(site.getId())
                .name(site.getName())
                .siteCode(site.getSiteCode())
                .gridReference(site.getGridReference())
                .postcode(site.getPostcode())
                .clientId(site.getClient() != null ? site.getClient().getId() : null)
                .clientName(site.getClient() != null ? site.getClient().getName() : null)
                .status(site.getStatus() != null ? site.getStatus().name() : null)
                .startDate(site.getStartDate())
                .completionDate(site.getCompletionDate())
                .estimatedCompletionDate(site.getEstimatedCompletionDate())
                .notes(site.getNotes())
                .build();
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Site", id));
        siteRepository.delete(site);
    }
}