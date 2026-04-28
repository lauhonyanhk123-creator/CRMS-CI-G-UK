package com.crms.service.quality;

import com.crms.domain.contract.entity.Contract;
import com.crms.domain.contract.repository.ContractRepository;
import com.crms.domain.quality.entity.SignOff;
import com.crms.domain.quality.enums.BuildingControlType;
import com.crms.domain.quality.enums.SignOffResult;
import com.crms.domain.quality.repository.SignOffRepository;
import com.crms.dto.request.quality.SignOffRequest;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.quality.SignOffResponse;
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
public class SignOffServiceImpl implements SignOffService {

    private final SignOffRepository repository;
    private final ContractRepository contractRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SignOffResponse> findAll(Map<String, Object> params) {
        int page = params.get("page") != null ? (int) params.get("page") : 0;
        int size = params.get("size") != null ? (int) params.get("size") : 20;
        String sort = params.get("sort") != null ? (String) params.get("sort") : "id";
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
        
        Long contractId = params.get("contractId") != null ? ((Number) params.get("contractId")).longValue() : null;
        BuildingControlType type = params.get("buildingControlType") != null 
            ? BuildingControlType.valueOf((String) params.get("buildingControlType")) : null;
        SignOffResult result = params.get("result") != null 
            ? SignOffResult.valueOf((String) params.get("result")) : null;
        
        Page<SignOff> signOffs = repository.findByFilters(contractId, type, result, pageable);
        
        List<SignOffResponse> content = signOffs.getContent().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
        
        return PageResponse.<SignOffResponse>builder()
            .content(content)
            .page(signOffs.getNumber())
            .size(signOffs.getSize())
            .totalElements(signOffs.getTotalElements())
            .totalPages(signOffs.getTotalPages())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SignOffResponse findById(Long id) {
        SignOff signOff = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Sign-off not found: " + id));
        return toResponse(signOff);
    }

    @Override
    public SignOffResponse create(SignOffRequest request) {
        Contract contract = contractRepository.findById(request.getContractId())
            .orElseThrow(() -> new RuntimeException("Contract not found: " + request.getContractId()));
        
        SignOff signOff = SignOff.builder()
            .contract(contract)
            .buildingControlType(request.getBuildingControlType())
            .inspectionType(request.getInspectionType())
            .referenceNumber(request.getReferenceNumber())
            .inspectorName(request.getInspectorName())
            .inspectorEmail(request.getInspectorEmail())
            .inspectorPhone(request.getInspectorPhone())
            .inspectionDate(request.getInspectionDate())
            .nextInspectionDate(request.getNextInspectionDate())
            .result(request.getResult())
            .conditionsOrNotes(request.getConditionsOrNotes())
            .reportPath(request.getReportPath())
            .reportNumber(request.getReportNumber())
            .signOffSignature(request.getSignOffSignature())
            .signOffDate(request.getSignOffDate())
            .notes(request.getNotes())
            .build();
        
        SignOff saved = repository.save(signOff);
        return toResponse(saved);
    }

    @Override
    public SignOffResponse update(Long id, SignOffRequest request) {
        SignOff signOff = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Sign-off not found: " + id));
        
        signOff.setBuildingControlType(request.getBuildingControlType());
        signOff.setInspectionType(request.getInspectionType());
        signOff.setReferenceNumber(request.getReferenceNumber());
        signOff.setInspectorName(request.getInspectorName());
        signOff.setInspectorEmail(request.getInspectorEmail());
        signOff.setInspectorPhone(request.getInspectorPhone());
        signOff.setInspectionDate(request.getInspectionDate());
        signOff.setNextInspectionDate(request.getNextInspectionDate());
        signOff.setResult(request.getResult());
        signOff.setConditionsOrNotes(request.getConditionsOrNotes());
        signOff.setReportPath(request.getReportPath());
        signOff.setReportNumber(request.getReportNumber());
        signOff.setSignOffSignature(request.getSignOffSignature());
        signOff.setSignOffDate(request.getSignOffDate());
        signOff.setNotes(request.getNotes());
        
        SignOff saved = repository.save(signOff);
        return toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Sign-off not found: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    public SignOffResponse approve(Long id, String signature) {
        SignOff signOff = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Sign-off not found: " + id));
        
        signOff.setResult(SignOffResult.APPROVED);
        signOff.setSignOffSignature(signature);
        signOff.setSignOffDate(LocalDate.now());
        
        SignOff saved = repository.save(signOff);
        return toResponse(saved);
    }

    @Override
    public SignOffResponse refuse(Long id, String conditions) {
        SignOff signOff = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Sign-off not found: " + id));
        
        signOff.setResult(SignOffResult.REFUSED);
        signOff.setConditionsOrNotes(conditions);
        
        SignOff saved = repository.save(signOff);
        return toResponse(saved);
    }

    private SignOffResponse toResponse(SignOff signOff) {
        return SignOffResponse.builder()
            .id(signOff.getId())
            .contractId(signOff.getContract().getId())
            .contractRef(signOff.getContract().getContractRef())
            .buildingControlType(signOff.getBuildingControlType())
            .inspectionType(signOff.getInspectionType())
            .referenceNumber(signOff.getReferenceNumber())
            .inspectorName(signOff.getInspectorName())
            .inspectorEmail(signOff.getInspectorEmail())
            .inspectorPhone(signOff.getInspectorPhone())
            .inspectionDate(signOff.getInspectionDate())
            .nextInspectionDate(signOff.getNextInspectionDate())
            .result(signOff.getResult())
            .conditionsOrNotes(signOff.getConditionsOrNotes())
            .reportPath(signOff.getReportPath())
            .reportNumber(signOff.getReportNumber())
            .signOffSignature(signOff.getSignOffSignature())
            .signOffDate(signOff.getSignOffDate())
            .notes(signOff.getNotes())
            .createdAt(signOff.getCreatedAt())
            .updatedAt(signOff.getUpdatedAt())
            .build();
    }
}
