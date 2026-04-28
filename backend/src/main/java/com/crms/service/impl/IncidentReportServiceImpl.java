package com.crms.service.impl;

import com.crms.domain.healthsafety.entity.IncidentReport;
import com.crms.domain.healthsafety.enums.IncidentStatus;
import com.crms.domain.healthsafety.repository.IncidentReportRepository;
import com.crms.domain.operative.entity.Operative;
import com.crms.domain.operative.repository.OperativeRepository;
import com.crms.domain.site.entity.Site;
import com.crms.domain.site.repository.SiteRepository;
import com.crms.dto.request.IncidentReportRequest;
import com.crms.dto.response.IncidentReportResponse;
import com.crms.dto.response.PageResponse;
import com.crms.exception.ResourceNotFoundException;
import com.crms.service.IncidentReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncidentReportServiceImpl implements IncidentReportService {

    private final IncidentReportRepository incidentReportRepository;
    private final SiteRepository siteRepository;
    private final OperativeRepository operativeRepository;

    @Override
    public PageResponse<IncidentReportResponse> findAll(Map<String, Object> params) {
        int page = params.containsKey("page") ? Integer.parseInt(params.get("page").toString()) : 0;
        int size = params.containsKey("size") ? Integer.parseInt(params.get("size").toString()) : 20;
        String sort = params.containsKey("sort") ? params.get("sort").toString() : "id";

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
        Page<IncidentReport> reportPage = incidentReportRepository.findAll(pageable);

        List<IncidentReportResponse> content = reportPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<IncidentReportResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(reportPage.getTotalElements())
                .totalPages(reportPage.getTotalPages())
                .build();
    }

    @Override
    public IncidentReportResponse findById(Long id) {
        IncidentReport report = incidentReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IncidentReport", id));
        return mapToResponse(report);
    }

    @Override
    @Transactional
    public IncidentReportResponse create(IncidentReportRequest request) {
        Site site = siteRepository.findById(request.getSiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Site", request.getSiteId()));

        String reportNumber = generateReportNumber();

        IncidentReport report = IncidentReport.builder()
                .site(site)
                .reportNumber(reportNumber)
                .incidentDate(request.getIncidentDate())
                .locationDescription(request.getLocationDescription())
                .type(request.getType())
                .severity(request.getSeverity())
                .description(request.getDescription())
                .immediateActions(request.getImmediateActions())
                .ridDORNotifiable(request.getRidDORNotifiable())
                .reportedToHse(request.getReportedToHse())
                .hseRef(request.getHseRef())
                .documentRefs(request.getDocumentRefs())
                .status(IncidentStatus.DRAFT)
                .morReference(request.getMorReference())
                .morConditions(request.getMorConditions())
                .morRestrictions(request.getMorRestrictions())
                .morPermitRef(request.getMorPermitRef())
                .morPermitExpiry(request.getMorPermitExpiry())
                .build();

        if (request.getOperativeId() != null) {
            Operative operative = operativeRepository.findById(request.getOperativeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Operative", request.getOperativeId()));
            report.setOperative(operative);
        }

        report = incidentReportRepository.save(report);
        return mapToResponse(report);
    }

    @Override
    @Transactional
    public IncidentReportResponse update(Long id, IncidentReportRequest request) {
        IncidentReport report = incidentReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IncidentReport", id));

        report.setLocationDescription(request.getLocationDescription());
        report.setType(request.getType());
        report.setSeverity(request.getSeverity());
        report.setDescription(request.getDescription());
        report.setImmediateActions(request.getImmediateActions());
        report.setRidDORNotifiable(request.getRidDORNotifiable());
        report.setReportedToHse(request.getReportedToHse());
        report.setHseRef(request.getHseRef());
        report.setInvestigationOutcome(request.getInvestigationOutcome());
        report.setDocumentRefs(request.getDocumentRefs());

        if (request.getMorReference() != null) report.setMorReference(request.getMorReference());
        if (request.getMorConditions() != null) report.setMorConditions(request.getMorConditions());
        if (request.getMorRestrictions() != null) report.setMorRestrictions(request.getMorRestrictions());
        if (request.getMorPermitRef() != null) report.setMorPermitRef(request.getMorPermitRef());
        if (request.getMorPermitExpiry() != null) report.setMorPermitExpiry(request.getMorPermitExpiry());

        report = incidentReportRepository.save(report);
        return mapToResponse(report);
    }

    @Override
    @Transactional
    public IncidentReportResponse submit(Long id) {
        IncidentReport report = incidentReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IncidentReport", id));

        report.setStatus(IncidentStatus.SUBMITTED);
        report = incidentReportRepository.save(report);
        return mapToResponse(report);
    }

    @Override
    @Transactional
    public IncidentReportResponse investigate(Long id, String investigationOutcome) {
        IncidentReport report = incidentReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IncidentReport", id));

        report.setStatus(IncidentStatus.INVESTIGATION);
        report.setInvestigationOutcome(investigationOutcome);
        report = incidentReportRepository.save(report);
        return mapToResponse(report);
    }

    @Override
    @Transactional
    public IncidentReportResponse close(Long id) {
        IncidentReport report = incidentReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IncidentReport", id));

        report.setStatus(IncidentStatus.CLOSED);
        report = incidentReportRepository.save(report);
        return mapToResponse(report);
    }

    @Override
    @Transactional
    public IncidentReportResponse submitRIDDOR(Long id, String hseRef) {
        IncidentReport report = incidentReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IncidentReport", id));

        report.setReportedToHse(true);
        report.setHseRef(hseRef);
        report = incidentReportRepository.save(report);
        return mapToResponse(report);
    }

    @Override
    @Transactional
    public IncidentReportResponse submitMOR(Long id, String conditions, String restrictions) {
        IncidentReport report = incidentReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IncidentReport", id));

        report.setMorReference(generateMorReference());
        report.setMorSubmittedDate(LocalDateTime.now());
        report.setMorConditions(conditions);
        report.setMorRestrictions(restrictions);
        report = incidentReportRepository.save(report);
        return mapToResponse(report);
    }

    @Override
    @Transactional
    public IncidentReportResponse signMOR(Long id, String signedBy) {
        IncidentReport report = incidentReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IncidentReport", id));

        report.setMorSignedBy(signedBy);
        report.setMorSignedDate(LocalDateTime.now());
        report = incidentReportRepository.save(report);
        return mapToResponse(report);
    }

    @Override
    @Transactional
    public IncidentReportResponse verifyMOR(Long id, String verifiedBy) {
        IncidentReport report = incidentReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IncidentReport", id));

        report.setMorVerificationStatus("VERIFIED");
        report.setMorVerifiedBy(verifiedBy);
        report.setMorVerifiedDate(LocalDateTime.now());
        report = incidentReportRepository.save(report);
        return mapToResponse(report);
    }

    @Override
    public PageResponse<IncidentReportResponse> findBySiteId(Long siteId) {
        List<IncidentReport> reports = incidentReportRepository.findBySiteId(siteId);
        List<IncidentReportResponse> content = reports.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<IncidentReportResponse>builder()
                .content(content)
                .page(0)
                .size(content.size())
                .totalElements((long) content.size())
                .totalPages(1)
                .build();
    }

    @Override
    public PageResponse<IncidentReportResponse> findRIDDORReportable() {
        List<IncidentReport> reports = incidentReportRepository.findRIDDORNotifiableOpen();
        List<IncidentReportResponse> content = reports.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<IncidentReportResponse>builder()
                .content(content)
                .page(0)
                .size(content.size())
                .totalElements((long) content.size())
                .totalPages(1)
                .build();
    }

    private String generateReportNumber() {
        String prefix = "INC";
        String timestamp = String.valueOf(System.currentTimeMillis());
        return prefix + "-" + timestamp;
    }

    private String generateMorReference() {
        String prefix = "MOR";
        String timestamp = String.valueOf(System.currentTimeMillis());
        return prefix + "-" + timestamp;
    }

    private IncidentReportResponse mapToResponse(IncidentReport report) {
        return IncidentReportResponse.builder()
                .id(report.getId())
                .siteId(report.getSite() != null ? report.getSite().getId() : null)
                .siteName(report.getSite() != null ? report.getSite().getName() : null)
                .operativeId(report.getOperative() != null ? report.getOperative().getId() : null)
                .operativeName(report.getOperative() != null ? report.getOperative().getFullName() : null)
                .reportNumber(report.getReportNumber())
                .incidentDate(report.getIncidentDate())
                .locationDescription(report.getLocationDescription())
                .type(report.getType())
                .severity(report.getSeverity())
                .description(report.getDescription())
                .immediateActions(report.getImmediateActions())
                .ridDORNotifiable(report.getRidDORNotifiable())
                .reportedToHse(report.getReportedToHse())
                .hseRef(report.getHseRef())
                .investigationOutcome(report.getInvestigationOutcome())
                .documentRefs(report.getDocumentRefs())
                .status(report.getStatus())
                .morReference(report.getMorReference())
                .morSubmittedDate(report.getMorSubmittedDate())
                .morSignedBy(report.getMorSignedBy())
                .morSignedDate(report.getMorSignedDate())
                .morVerificationStatus(report.getMorVerificationStatus())
                .morVerifiedBy(report.getMorVerifiedBy())
                .morVerifiedDate(report.getMorVerifiedDate())
                .morConditions(report.getMorConditions())
                .morRestrictions(report.getMorRestrictions())
                .morPermitRef(report.getMorPermitRef())
                .morPermitExpiry(report.getMorPermitExpiry())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}
