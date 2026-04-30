package com.crms.service.impl;

import com.crms.domain.healthsafety.entity.RAMSTemplate;
import com.crms.domain.healthsafety.repository.RAMSTemplateRepository;
import com.crms.dto.request.RAMSTemplateRequest;
import com.crms.dto.response.RAMSTemplateResponse;
import com.crms.dto.response.PageResponse;
import com.crms.exception.ResourceNotFoundException;
import com.crms.service.RAMSTemplateService;
import com.crms.util.PaginationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RAMSTemplateServiceImpl implements RAMSTemplateService {

    private final RAMSTemplateRepository ramsTemplateRepository;

    @Override
    public PageResponse<RAMSTemplateResponse> findAll(Map<String, Object> params) {
        int page = PaginationHelper.getPage(params);
        int size = PaginationHelper.getSize(params);
        String sort = PaginationHelper.getSort(params, "id");

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<RAMSTemplate> templatePage = ramsTemplateRepository.findAll(pageable);

        List<RAMSTemplateResponse> content = templatePage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<RAMSTemplateResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(templatePage.getTotalElements())
                .totalPages(templatePage.getTotalPages())
                .build();
    }

    @Override
    public RAMSTemplateResponse findById(Long id) {
        RAMSTemplate template = ramsTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RAMSTemplate", id));
        return mapToResponse(template);
    }

    @Override
    @Transactional
    public RAMSTemplateResponse create(RAMSTemplateRequest request) {
        RAMSTemplate template = RAMSTemplate.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .trade(request.getTrade())
                .riskAssessment(request.getRiskAssessment())
                .methodStatement(request.getMethodStatement())
                .ppeRequired(request.getPpeRequired())
                .frequencyDays(request.getFrequencyDays() != null ? request.getFrequencyDays() : 90)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        template = ramsTemplateRepository.save(template);
        return mapToResponse(template);
    }

    @Override
    @Transactional
    public RAMSTemplateResponse update(Long id, RAMSTemplateRequest request) {
        RAMSTemplate template = ramsTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RAMSTemplate", id));

        template.setTitle(request.getTitle());
        template.setDescription(request.getDescription());
        template.setTrade(request.getTrade());
        template.setRiskAssessment(request.getRiskAssessment());
        template.setMethodStatement(request.getMethodStatement());
        template.setPpeRequired(request.getPpeRequired());
        if (request.getFrequencyDays() != null) {
            template.setFrequencyDays(request.getFrequencyDays());
        }
        if (request.getIsActive() != null) {
            template.setIsActive(request.getIsActive());
        }

        template = ramsTemplateRepository.save(template);
        return mapToResponse(template);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        RAMSTemplate template = ramsTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RAMSTemplate", id));
        template.setIsActive(false);
        ramsTemplateRepository.save(template);
    }

    @Override
    public List<RAMSTemplateResponse> findActive() {
        return ramsTemplateRepository.findByIsActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RAMSTemplateResponse> findByTrade(String trade) {
        return ramsTemplateRepository.findActiveByTrade(trade).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> findDistinctTrades() {
        return ramsTemplateRepository.findDistinctTrades();
    }

    @Override
    @Transactional
    public RAMSTemplateResponse copyTemplate(Long id, String newTitle) {
        RAMSTemplate original = ramsTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RAMSTemplate", id));

        RAMSTemplate copy = RAMSTemplate.builder()
                .title(newTitle)
                .description(original.getDescription())
                .trade(original.getTrade())
                .riskAssessment(original.getRiskAssessment())
                .methodStatement(original.getMethodStatement())
                .ppeRequired(original.getPpeRequired())
                .frequencyDays(original.getFrequencyDays())
                .isActive(true)
                .build();

        copy = ramsTemplateRepository.save(copy);
        return mapToResponse(copy);
    }

    private RAMSTemplateResponse mapToResponse(RAMSTemplate template) {
        return RAMSTemplateResponse.builder()
                .id(template.getId())
                .title(template.getTitle())
                .description(template.getDescription())
                .trade(template.getTrade())
                .riskAssessment(template.getRiskAssessment())
                .methodStatement(template.getMethodStatement())
                .ppeRequired(template.getPpeRequired())
                .frequencyDays(template.getFrequencyDays())
                .isActive(template.getIsActive())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
