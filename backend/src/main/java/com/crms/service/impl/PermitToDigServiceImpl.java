package com.crms.service.impl;

import com.crms.domain.healthsafety.entity.PermitToDig;
import com.crms.domain.healthsafety.enums.PermitStatus;
import com.crms.domain.healthsafety.repository.PermitToDigRepository;
import com.crms.domain.site.entity.Site;
import com.crms.domain.site.repository.SiteRepository;
import com.crms.dto.request.PermitToDigRequest;
import com.crms.dto.response.PermitToDigResponse;
import com.crms.dto.response.PageResponse;
import com.crms.exception.ResourceNotFoundException;
import com.crms.service.PermitToDigService;
import com.crms.util.PaginationHelper;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermitToDigServiceImpl implements PermitToDigService {

    private final PermitToDigRepository permitToDigRepository;
    private final SiteRepository siteRepository;

    @Override
    public PageResponse<PermitToDigResponse> findAll(Map<String, Object> params) {
        int page = PaginationHelper.getPage(params);
        int size = PaginationHelper.getSize(params);
        String sort = PaginationHelper.getSort(params, "id");

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<PermitToDig> permitPage = permitToDigRepository.findAll(pageable);

        List<PermitToDigResponse> content = permitPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<PermitToDigResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(permitPage.getTotalElements())
                .totalPages(permitPage.getTotalPages())
                .build();
    }

    @Override
    public PermitToDigResponse findById(Long id) {
        PermitToDig permit = permitToDigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PermitToDig", id));
        return mapToResponse(permit);
    }

    @Override
    @Transactional
    public PermitToDigResponse create(PermitToDigRequest request) {
        Site site = siteRepository.findById(request.getSiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Site", request.getSiteId()));

        String permitNumber = generatePermitNumber();

        PermitToDig permit = PermitToDig.builder()
                .site(site)
                .permitNumber(permitNumber)
                .worksDescription(request.getWorksDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(PermitStatus.DRAFT)
                .lsbudReference(request.getLsbudReference())
                .trialHoleCount(request.getTrialHoleCount())
                .trialHolePhotoRef(request.getTrialHolePhotoRef())
                .catScanRef(request.getCatScanRef())
                .catScanDate(request.getCatScanDate())
                .catScanDeviceSerial(request.getCatScanDeviceSerial())
                .catScanLastCalibrationDate(request.getCatScanLastCalibrationDate())
                .supervisorApprovalRef(request.getSupervisorApprovalRef())
                .supervisorApprovalDate(request.getSupervisorApprovalDate())
                .documentRef(request.getDocumentRef())
                .requestedDate(LocalDate.now())
                .build();

        permit = permitToDigRepository.save(permit);
        return mapToResponse(permit);
    }

    @Override
    @Transactional
    public PermitToDigResponse update(Long id, PermitToDigRequest request) {
        PermitToDig permit = permitToDigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PermitToDig", id));

        if (request.getSiteId() != null && (permit.getSite() == null || !request.getSiteId().equals(permit.getSite().getId()))) {
            Site site = siteRepository.findById(request.getSiteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Site", request.getSiteId()));
            permit.setSite(site);
        }

        permit.setWorksDescription(request.getWorksDescription());
        permit.setStartDate(request.getStartDate());
        permit.setEndDate(request.getEndDate());
        permit.setLsbudReference(request.getLsbudReference());
        permit.setTrialHoleCount(request.getTrialHoleCount());
        permit.setTrialHolePhotoRef(request.getTrialHolePhotoRef());
        permit.setCatScanRef(request.getCatScanRef());
        permit.setCatScanDate(request.getCatScanDate());
        permit.setCatScanDeviceSerial(request.getCatScanDeviceSerial());
        permit.setCatScanLastCalibrationDate(request.getCatScanLastCalibrationDate());
        permit.setSupervisorApprovalRef(request.getSupervisorApprovalRef());
        permit.setSupervisorApprovalDate(request.getSupervisorApprovalDate());
        permit.setDocumentRef(request.getDocumentRef());

        permit = permitToDigRepository.save(permit);
        return mapToResponse(permit);
    }

    @Override
    @Transactional
    public PermitToDigResponse submitForPrecheck(Long id) {
        PermitToDig permit = permitToDigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PermitToDig", id));

        permit.setStatus(PermitStatus.PRECHECKED);
        permit.setRequestedDate(LocalDate.now());
        permit = permitToDigRepository.save(permit);
        return mapToResponse(permit);
    }

    @Override
    @Transactional
    public PermitToDigResponse precheck(Long id) {
        PermitToDig permit = permitToDigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PermitToDig", id));

        permit.setStatus(PermitStatus.PRECHECKED);
        permit.setPrecheckedDate(LocalDate.now());
        permit = permitToDigRepository.save(permit);
        return mapToResponse(permit);
    }

    @Override
    @Transactional
    public PermitToDigResponse rejectPrecheck(Long id, String reason) {
        PermitToDig permit = permitToDigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PermitToDig", id));

        permit.setStatus(PermitStatus.DRAFT);
        permit.setCancellationReason(reason);
        permit = permitToDigRepository.save(permit);
        return mapToResponse(permit);
    }

    @Override
    @Transactional
    public PermitToDigResponse issue(Long id) {
        PermitToDig permit = permitToDigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PermitToDig", id));

        permit.setStatus(PermitStatus.ISSUED);
        permit.setIssuedDate(LocalDate.now());
        permit = permitToDigRepository.save(permit);
        return mapToResponse(permit);
    }

    @Override
    @Transactional
    public PermitToDigResponse startWork(Long id) {
        PermitToDig permit = permitToDigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PermitToDig", id));

        permit.setStatus(PermitStatus.IN_PROGRESS);
        permit = permitToDigRepository.save(permit);
        return mapToResponse(permit);
    }

    @Override
    @Transactional
    public PermitToDigResponse complete(Long id) {
        PermitToDig permit = permitToDigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PermitToDig", id));

        permit.setStatus(PermitStatus.COMPLETED);
        permit.setCompletedDate(LocalDate.now());
        permit = permitToDigRepository.save(permit);
        return mapToResponse(permit);
    }

    @Override
    @Transactional
    public PermitToDigResponse cancel(Long id, String reason) {
        PermitToDig permit = permitToDigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PermitToDig", id));

        permit.setStatus(PermitStatus.CANCELLED);
        permit.setCancellationReason(reason);
        permit.setCancelledDate(LocalDate.now());
        permit = permitToDigRepository.save(permit);
        return mapToResponse(permit);
    }

    @Override
    @Transactional
    public PermitToDigResponse extend(Long id, LocalDate newEndDate) {
        PermitToDig permit = permitToDigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PermitToDig", id));

        permit.setEndDate(newEndDate);
        permit.setExtensionCount(permit.getExtensionCount() + 1);
        permit.setLastExtensionDate(LocalDate.now());
        permit = permitToDigRepository.save(permit);
        return mapToResponse(permit);
    }

    @Override
    public PermitToDigResponse findActiveBySiteId(Long siteId) {
        return permitToDigRepository.findActivePermitForSite(siteId)
                .map(this::mapToResponse)
                .orElse(null);
    }

    private String generatePermitNumber() {
        String prefix = "PTD";
        String timestamp = String.valueOf(System.currentTimeMillis());
        return prefix + "-" + timestamp;
    }

    private PermitToDigResponse mapToResponse(PermitToDig permit) {
        return PermitToDigResponse.builder()
                .id(permit.getId())
                .siteId(permit.getSite() != null ? permit.getSite().getId() : null)
                .siteName(permit.getSite() != null ? permit.getSite().getName() : null)
                .permitNumber(permit.getPermitNumber())
                .worksDescription(permit.getWorksDescription())
                .startDate(permit.getStartDate())
                .endDate(permit.getEndDate())
                .status(permit.getStatus())
                .lsbudReference(permit.getLsbudReference())
                .trialHoleCount(permit.getTrialHoleCount())
                .trialHolePhotoRef(permit.getTrialHolePhotoRef())
                .catScanRef(permit.getCatScanRef())
                .catScanDate(permit.getCatScanDate())
                .catScanDeviceSerial(permit.getCatScanDeviceSerial())
                .catScanLastCalibrationDate(permit.getCatScanLastCalibrationDate())
                .supervisorApprovalRef(permit.getSupervisorApprovalRef())
                .supervisorApprovalDate(permit.getSupervisorApprovalDate())
                .documentRef(permit.getDocumentRef())
                .requestedBy(permit.getRequestedBy())
                .requestedDate(permit.getRequestedDate())
                .precheckedBy(permit.getPrecheckedBy())
                .precheckedDate(permit.getPrecheckedDate())
                .issuedBy(permit.getIssuedBy())
                .issuedDate(permit.getIssuedDate())
                .completedBy(permit.getCompletedBy())
                .completedDate(permit.getCompletedDate())
                .cancellationReason(permit.getCancellationReason())
                .cancelledBy(permit.getCancelledBy())
                .cancelledDate(permit.getCancelledDate())
                .extensionCount(permit.getExtensionCount())
                .lastExtensionDate(permit.getLastExtensionDate())
                .createdAt(permit.getCreatedAt())
                .updatedAt(permit.getUpdatedAt())
                .build();
    }
}
