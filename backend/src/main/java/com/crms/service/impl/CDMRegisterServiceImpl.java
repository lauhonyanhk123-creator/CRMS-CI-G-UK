package com.crms.service.impl;

import com.crms.domain.company.entity.Company;
import com.crms.domain.company.repository.CompanyRepository;
import com.crms.domain.healthsafety.entity.CDMRegister;
import com.crms.domain.healthsafety.repository.CDMRegisterRepository;
import com.crms.domain.site.entity.Site;
import com.crms.domain.site.repository.SiteRepository;
import com.crms.dto.request.CDMRegisterRequest;
import com.crms.dto.response.CDMRegisterResponse;
import com.crms.dto.response.PageResponse;
import com.crms.exception.ResourceNotFoundException;
import com.crms.service.CDMRegisterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CDMRegisterServiceImpl implements CDMRegisterService {

    private final CDMRegisterRepository cdmRegisterRepository;
    private final CompanyRepository companyRepository;
    private final SiteRepository siteRepository;

    @Override
    public PageResponse<CDMRegisterResponse> findAll(Map<String, Object> params) {
        int page = params.containsKey("page") ? Integer.parseInt(params.get("page").toString()) : 0;
        int size = params.containsKey("size") ? Integer.parseInt(params.get("size").toString()) : 20;
        String sort = params.containsKey("sort") ? params.get("sort").toString() : "id";

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<CDMRegister> registerPage = cdmRegisterRepository.findAll(pageable);

        List<CDMRegisterResponse> content = registerPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<CDMRegisterResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(registerPage.getTotalElements())
                .totalPages(registerPage.getTotalPages())
                .build();
    }

    @Override
    public CDMRegisterResponse findById(Long id) {
        CDMRegister register = cdmRegisterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CDMRegister", id));
        return mapToResponse(register);
    }

    @Override
    @Transactional
    public CDMRegisterResponse create(CDMRegisterRequest request) {
        Company client = companyRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", request.getClientId()));

        String notificationNumber = generateNotificationNumber();

        CDMRegister register = CDMRegister.builder()
                .notificationNumber(notificationNumber)
                .projectName(request.getProjectName())
                .projectAddress(request.getProjectAddress())
                .projectDescription(request.getProjectDescription())
                .client(client)
                .principalDesignerName(request.getPrincipalDesignerName())
                .principalDesignerEmail(request.getPrincipalDesignerEmail())
                .principalDesignerPhone(request.getPrincipalDesignerPhone())
                .principalContractorName(request.getPrincipalContractorName())
                .principalContractorEmail(request.getPrincipalContractorEmail())
                .principalContractorPhone(request.getPrincipalContractorPhone())
                .notificationDate(request.getNotificationDate())
                .constructionStartDate(request.getConstructionStartDate())
                .constructionEndDate(request.getConstructionEndDate())
                .isNotifiable(request.getIsNotifiable())
                .moreThan30Days(request.getMoreThan30Days())
                .moreThan500PersonDays(request.getMoreThan500PersonDays())
                .isActive(true)
                .build();

        if (request.getSiteId() != null) {
            Site site = siteRepository.findById(request.getSiteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Site", request.getSiteId()));
            register.setSite(site);
        }

        register = cdmRegisterRepository.save(register);
        return mapToResponse(register);
    }

    @Override
    @Transactional
    public CDMRegisterResponse update(Long id, CDMRegisterRequest request) {
        CDMRegister register = cdmRegisterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CDMRegister", id));

        register.setProjectName(request.getProjectName());
        register.setProjectAddress(request.getProjectAddress());
        register.setProjectDescription(request.getProjectDescription());
        register.setPrincipalDesignerName(request.getPrincipalDesignerName());
        register.setPrincipalDesignerEmail(request.getPrincipalDesignerEmail());
        register.setPrincipalDesignerPhone(request.getPrincipalDesignerPhone());
        register.setPrincipalContractorName(request.getPrincipalContractorName());
        register.setPrincipalContractorEmail(request.getPrincipalContractorEmail());
        register.setPrincipalContractorPhone(request.getPrincipalContractorPhone());
        register.setNotificationDate(request.getNotificationDate());
        register.setConstructionStartDate(request.getConstructionStartDate());
        register.setConstructionEndDate(request.getConstructionEndDate());
        register.setIsNotifiable(request.getIsNotifiable());
        register.setMoreThan30Days(request.getMoreThan30Days());
        register.setMoreThan500PersonDays(request.getMoreThan500PersonDays());

        if (request.getClientId() != null && (register.getClient() == null || !request.getClientId().equals(register.getClient().getId()))) {
            Company client = companyRepository.findById(request.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client", request.getClientId()));
            register.setClient(client);
        }

        if (request.getSiteId() != null) {
            Site site = siteRepository.findById(request.getSiteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Site", request.getSiteId()));
            register.setSite(site);
        }

        register = cdmRegisterRepository.save(register);
        return mapToResponse(register);
    }

    @Override
    @Transactional
    public CDMRegisterResponse submitToHSE(Long id) {
        CDMRegister register = cdmRegisterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CDMRegister", id));

        register.setHseNotificationRef(generateHseReference());
        register = cdmRegisterRepository.save(register);
        return mapToResponse(register);
    }

    @Override
    @Transactional
    public CDMRegisterResponse createHealthSafetyFile(Long id) {
        CDMRegister register = cdmRegisterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CDMRegister", id));

        register.setHealthSafetyFileRef(generateHsfReference());
        register.setHealthSafetyFileCreatedDate(LocalDateTime.now());
        register = cdmRegisterRepository.save(register);
        return mapToResponse(register);
    }

    @Override
    @Transactional
    public CDMRegisterResponse completeHealthSafetyFile(Long id) {
        CDMRegister register = cdmRegisterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CDMRegister", id));

        register.setHealthSafetyFileCompletedDate(LocalDateTime.now());
        register = cdmRegisterRepository.save(register);
        return mapToResponse(register);
    }

    @Override
    public CDMRegisterResponse findByNotificationNumber(String notificationNumber) {
        return cdmRegisterRepository.findByNotificationNumber(notificationNumber)
                .map(this::mapToResponse)
                .orElse(null);
    }

    @Override
    public PageResponse<CDMRegisterResponse> findActiveByClientId(Long clientId) {
        List<CDMRegister> registers = cdmRegisterRepository.findActiveByClientId(clientId);
        List<CDMRegisterResponse> content = registers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<CDMRegisterResponse>builder()
                .content(content)
                .page(0)
                .size(content.size())
                .totalElements((long) content.size())
                .totalPages(1)
                .build();
    }

    @Override
    public PageResponse<CDMRegisterResponse> findExpiringProjects(LocalDate date) {
        List<CDMRegister> registers = cdmRegisterRepository.findExpiringProjects(date);
        List<CDMRegisterResponse> content = registers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<CDMRegisterResponse>builder()
                .content(content)
                .page(0)
                .size(content.size())
                .totalElements((long) content.size())
                .totalPages(1)
                .build();
    }

    @Override
    public PageResponse<CDMRegisterResponse> findPendingHseNotification() {
        List<CDMRegister> registers = cdmRegisterRepository.findPendingHseNotification();
        List<CDMRegisterResponse> content = registers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<CDMRegisterResponse>builder()
                .content(content)
                .page(0)
                .size(content.size())
                .totalElements((long) content.size())
                .totalPages(1)
                .build();
    }

    private String generateNotificationNumber() {
        return "CDM-" + System.currentTimeMillis();
    }

    private String generateHseReference() {
        return "HSE-" + System.currentTimeMillis();
    }

    private String generateHsfReference() {
        return "HSF-" + System.currentTimeMillis();
    }

    private CDMRegisterResponse mapToResponse(CDMRegister register) {
        return CDMRegisterResponse.builder()
                .id(register.getId())
                .notificationNumber(register.getNotificationNumber())
                .projectName(register.getProjectName())
                .projectAddress(register.getProjectAddress())
                .projectDescription(register.getProjectDescription())
                .clientId(register.getClient() != null ? register.getClient().getId() : null)
                .clientName(register.getClient() != null ? register.getClient().getName() : null)
                .siteId(register.getSite() != null ? register.getSite().getId() : null)
                .siteName(register.getSite() != null ? register.getSite().getName() : null)
                .principalDesignerName(register.getPrincipalDesignerName())
                .principalDesignerEmail(register.getPrincipalDesignerEmail())
                .principalDesignerPhone(register.getPrincipalDesignerPhone())
                .principalContractorName(register.getPrincipalContractorName())
                .principalContractorEmail(register.getPrincipalContractorEmail())
                .principalContractorPhone(register.getPrincipalContractorPhone())
                .notificationDate(register.getNotificationDate())
                .constructionStartDate(register.getConstructionStartDate())
                .constructionEndDate(register.getConstructionEndDate())
                .isNotifiable(register.getIsNotifiable())
                .moreThan30Days(register.getMoreThan30Days())
                .moreThan500PersonDays(register.getMoreThan500PersonDays())
                .hseNotificationRef(register.getHseNotificationRef())
                .datePreconstructionInfoShared(register.getDatePreconstructionInfoShared())
                .constructionPhasePlanDate(register.getConstructionPhasePlanDate())
                .healthSafetyFileRef(register.getHealthSafetyFileRef())
                .healthSafetyFileCreatedDate(register.getHealthSafetyFileCreatedDate())
                .healthSafetyFileCompletedDate(register.getHealthSafetyFileCompletedDate())
                .isActive(register.getIsActive())
                .createdAt(register.getCreatedAt())
                .updatedAt(register.getUpdatedAt())
                .build();
    }
}
