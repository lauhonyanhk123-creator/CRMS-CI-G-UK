package com.crms.service.quality;

import com.crms.domain.quality.entity.InspectionRecord;
import com.crms.domain.quality.entity.InspectionAttachment;
import com.crms.domain.quality.entity.ITPScheduleItem;
import com.crms.domain.quality.repository.InspectionRecordRepository;
import com.crms.domain.quality.repository.ITPScheduleItemRepository;
import com.crms.dto.request.quality.InspectionRecordRequest;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.quality.InspectionRecordResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InspectionRecordServiceImpl implements InspectionRecordService {

    private final InspectionRecordRepository repository;
    private final ITPScheduleItemRepository scheduleItemRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InspectionRecordResponse> findAll(Map<String, Object> params) {
        int page = params.get("page") != null ? (int) params.get("page") : 0;
        int size = params.get("size") != null ? (int) params.get("size") : 20;
        String sort = params.get("sort") != null ? (String) params.get("sort") : "id";
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
        
        Long scheduleItemId = params.get("scheduleItemId") != null ? ((Number) params.get("scheduleItemId")).longValue() : null;
        String result = (String) params.get("result");
        String inspectorName = (String) params.get("inspectorName");
        
        Page<InspectionRecord> records = repository.findByFilters(
            scheduleItemId,
            result != null ? com.crms.domain.quality.enums.InspectionResult.valueOf(result) : null,
            inspectorName,
            pageable
        );
        
        List<InspectionRecordResponse> content = records.getContent().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
        
        return PageResponse.<InspectionRecordResponse>builder()
            .content(content)
            .page(records.getNumber())
            .size(records.getSize())
            .totalElements(records.getTotalElements())
            .totalPages(records.getTotalPages())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public InspectionRecordResponse findById(Long id) {
        InspectionRecord record = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Inspection record not found: " + id));
        return toResponse(record);
    }

    @Override
    public InspectionRecordResponse create(InspectionRecordRequest request) {
        ITPScheduleItem scheduleItem = scheduleItemRepository.findById(request.getScheduleItemId())
            .orElseThrow(() -> new RuntimeException("Schedule item not found: " + request.getScheduleItemId()));
        
        InspectionRecord record = InspectionRecord.builder()
            .scheduleItem(scheduleItem)
            .title(request.getTitle())
            .inspectorName(request.getInspectorName())
            .inspectorSignature(request.getInspectorSignature())
            .inspectionDate(request.getInspectionDate())
            .inspectionTime(request.getInspectionTime())
            .result(request.getResult())
            .notes(request.getNotes())
            .findings(request.getFindings())
            .nonConformanceRef(request.getNonConformanceRef())
            .build();
        
        InspectionRecord saved = repository.save(record);
        return toResponse(saved);
    }

    @Override
    public InspectionRecordResponse update(Long id, InspectionRecordRequest request) {
        InspectionRecord record = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Inspection record not found: " + id));
        
        record.setTitle(request.getTitle());
        record.setInspectorName(request.getInspectorName());
        record.setInspectorSignature(request.getInspectorSignature());
        record.setInspectionDate(request.getInspectionDate());
        record.setInspectionTime(request.getInspectionTime());
        record.setResult(request.getResult());
        record.setNotes(request.getNotes());
        record.setFindings(request.getFindings());
        record.setNonConformanceRef(request.getNonConformanceRef());
        
        InspectionRecord saved = repository.save(record);
        return toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Inspection record not found: " + id);
        }
        repository.deleteById(id);
    }

    private InspectionRecordResponse toResponse(InspectionRecord record) {
        List<InspectionRecordResponse.AttachmentResponse> attachments = record.getAttachments().stream()
            .map(att -> InspectionRecordResponse.AttachmentResponse.builder()
                .id(att.getId())
                .filename(att.getFilename())
                .fileType(att.getFileType())
                .filePath(att.getFilePath())
                .fileSize(att.getFileSize())
                .description(att.getDescription())
                .uploadedBy(att.getUploadedBy())
                .build())
            .collect(Collectors.toList());
        
        return InspectionRecordResponse.builder()
            .id(record.getId())
            .scheduleItemId(record.getScheduleItem().getId())
            .scheduleItemDescription(record.getScheduleItem().getDescription())
            .scheduleId(record.getScheduleItem().getSchedule().getId())
            .scheduleTitle(record.getScheduleItem().getSchedule().getTitle())
            .contractId(record.getScheduleItem().getSchedule().getContract().getId())
            .title(record.getTitle())
            .inspectorName(record.getInspectorName())
            .inspectorSignature(record.getInspectorSignature())
            .inspectionDate(record.getInspectionDate())
            .inspectionTime(record.getInspectionTime())
            .result(record.getResult())
            .notes(record.getNotes())
            .findings(record.getFindings())
            .nonConformanceRef(record.getNonConformanceRef())
            .attachments(attachments)
            .createdAt(record.getCreatedAt())
            .updatedAt(record.getUpdatedAt())
            .build();
    }
}
