package com.crms.service.quality;

import com.crms.domain.contract.entity.Contract;
import com.crms.domain.contract.repository.ContractRepository;
import com.crms.domain.quality.entity.Defect;
import com.crms.domain.quality.entity.DefectPhoto;
import com.crms.domain.quality.enums.DefectPriority;
import com.crms.domain.quality.enums.DefectStatus;
import com.crms.domain.quality.repository.DefectRepository;
import com.crms.dto.request.quality.DefectRequest;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.quality.DefectResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DefectServiceImpl implements DefectService {

    private final DefectRepository repository;
    private final ContractRepository contractRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DefectResponse> findAll(Map<String, Object> params) {
        int page = params.get("page") != null ? (int) params.get("page") : 0;
        int size = params.get("size") != null ? (int) params.get("size") : 20;
        String sort = params.get("sort") != null ? (String) params.get("sort") : "id";
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
        
        Long contractId = params.get("contractId") != null ? ((Number) params.get("contractId")).longValue() : null;
        DefectStatus status = params.get("status") != null 
            ? DefectStatus.valueOf((String) params.get("status")) : null;
        DefectPriority priority = params.get("priority") != null 
            ? DefectPriority.valueOf((String) params.get("priority")) : null;
        
        Page<Defect> defects = repository.findByFilters(contractId, status, priority, pageable);
        
        List<DefectResponse> content = defects.getContent().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
        
        return PageResponse.<DefectResponse>builder()
            .content(content)
            .page(defects.getNumber())
            .size(defects.getSize())
            .totalElements(defects.getTotalElements())
            .totalPages(defects.getTotalPages())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DefectResponse findById(Long id) {
        Defect defect = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Defect not found: " + id));
        return toResponse(defect);
    }

    @Override
    public DefectResponse create(DefectRequest request) {
        Contract contract = contractRepository.findById(request.getContractId())
            .orElseThrow(() -> new RuntimeException("Contract not found: " + request.getContractId()));
        
        Defect defect = Defect.builder()
            .title(request.getTitle())
            .contract(contract)
            .description(request.getDescription())
            .location(request.getLocation())
            .priority(request.getPriority() != null ? request.getPriority() : DefectPriority.MEDIUM)
            .status(request.getStatus() != null ? request.getStatus() : DefectStatus.OPEN)
            .identifiedDate(request.getIdentifiedDate() != null ? request.getIdentifiedDate() : LocalDate.now())
            .dueDate(request.getDueDate())
            .assignedOperative(request.getAssignedOperative())
            .assignedContractor(request.getAssignedContractor())
            .notes(request.getNotes())
            .rootCause(request.getRootCause())
            .resolutionDetails(request.getResolutionDetails())
            .reinspectionRequired(request.getReinspectionRequired() != null ? request.getReinspectionRequired() : false)
            .reinspectionDate(request.getReinspectionDate())
            .ncReference(request.getNcReference())
            .build();
        
        Defect saved = repository.save(defect);
        return toResponse(saved);
    }

    @Override
    public DefectResponse update(Long id, DefectRequest request) {
        Defect defect = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Defect not found: " + id));
        
        defect.setTitle(request.getTitle());
        defect.setDescription(request.getDescription());
        defect.setLocation(request.getLocation());
        if (request.getPriority() != null) defect.setPriority(request.getPriority());
        if (request.getStatus() != null) defect.setStatus(request.getStatus());
        defect.setDueDate(request.getDueDate());
        defect.setAssignedOperative(request.getAssignedOperative());
        defect.setAssignedContractor(request.getAssignedContractor());
        defect.setNotes(request.getNotes());
        defect.setRootCause(request.getRootCause());
        defect.setResolutionDetails(request.getResolutionDetails());
        if (request.getReinspectionRequired() != null) defect.setReinspectionRequired(request.getReinspectionRequired());
        defect.setReinspectionDate(request.getReinspectionDate());
        defect.setNcReference(request.getNcReference());
        
        Defect saved = repository.save(defect);
        return toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Defect not found: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    public DefectResponse updateStatus(Long id, String status) {
        Defect defect = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Defect not found: " + id));
        
        DefectStatus newStatus = DefectStatus.valueOf(status);
        defect.setStatus(newStatus);
        
        if (newStatus == DefectStatus.RESOLVED) {
            defect.setResolvedDate(LocalDate.now());
        }
        
        Defect saved = repository.save(defect);
        return toResponse(saved);
    }

    @Override
    public DefectResponse assignOperative(Long id, String operative) {
        Defect defect = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Defect not found: " + id));
        
        defect.setAssignedOperative(operative);
        if (defect.getStatus() == DefectStatus.OPEN) {
            defect.setStatus(DefectStatus.IN_PROGRESS);
        }
        
        Defect saved = repository.save(defect);
        return toResponse(saved);
    }

    @Override
    public DefectResponse assignContractor(Long id, String contractor) {
        Defect defect = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Defect not found: " + id));
        
        defect.setAssignedContractor(contractor);
        if (defect.getStatus() == DefectStatus.OPEN) {
            defect.setStatus(DefectStatus.IN_PROGRESS);
        }
        
        Defect saved = repository.save(defect);
        return toResponse(saved);
    }

    private DefectResponse toResponse(Defect defect) {
        List<DefectResponse.PhotoResponse> photos = defect.getPhotos().stream()
            .map(photo -> DefectResponse.PhotoResponse.builder()
                .id(photo.getId())
                .filename(photo.getFilename())
                .filePath(photo.getFilePath())
                .fileSize(photo.getFileSize())
                .description(photo.getDescription())
                .uploadedBy(photo.getUploadedBy())
                .takenDate(photo.getTakenDate())
                .build())
            .collect(Collectors.toList());
        
        return DefectResponse.builder()
            .id(defect.getId())
            .title(defect.getTitle())
            .contractId(defect.getContract().getId())
            .contractRef(defect.getContract().getContractRef())
            .description(defect.getDescription())
            .location(defect.getLocation())
            .priority(defect.getPriority())
            .status(defect.getStatus())
            .identifiedDate(defect.getIdentifiedDate())
            .dueDate(defect.getDueDate())
            .resolvedDate(defect.getResolvedDate())
            .assignedOperative(defect.getAssignedOperative())
            .assignedContractor(defect.getAssignedContractor())
            .notes(defect.getNotes())
            .rootCause(defect.getRootCause())
            .resolutionDetails(defect.getResolutionDetails())
            .reinspectionRequired(defect.getReinspectionRequired())
            .reinspectionDate(defect.getReinspectionDate())
            .ncReference(defect.getNcReference())
            .photos(photos)
            .createdAt(defect.getCreatedAt())
            .updatedAt(defect.getUpdatedAt())
            .build();
    }
}
