package com.crms.service.quality;

import com.crms.domain.contract.entity.Contract;
import com.crms.domain.contract.repository.ContractRepository;
import com.crms.domain.quality.entity.ITPSchedule;
import com.crms.domain.quality.entity.ITPScheduleItem;
import com.crms.domain.quality.entity.ITPTemplate;
import com.crms.domain.quality.enums.ScheduleStatus;
import com.crms.domain.quality.repository.ITPScheduleRepository;
import com.crms.domain.quality.repository.ITPTemplateRepository;
import com.crms.dto.request.quality.ITPScheduleRequest;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.quality.ITPScheduleResponse;
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
public class ITPScheduleServiceImpl implements ITPScheduleService {

    private final ITPScheduleRepository repository;
    private final ITPTemplateRepository templateRepository;
    private final ContractRepository contractRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ITPScheduleResponse> findAll(Map<String, Object> params) {
        int page = params.get("page") != null ? (int) params.get("page") : 0;
        int size = params.get("size") != null ? (int) params.get("size") : 20;
        String sort = params.get("sort") != null ? (String) params.get("sort") : "id";
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
        
        Long contractId = params.get("contractId") != null ? ((Number) params.get("contractId")).longValue() : null;
        ScheduleStatus status = params.get("status") != null 
            ? ScheduleStatus.valueOf((String) params.get("status")) : null;
        
        Page<ITPSchedule> schedules = repository.findByFilters(contractId, status, pageable);
        
        List<ITPScheduleResponse> content = schedules.getContent().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
        
        return PageResponse.<ITPScheduleResponse>builder()
            .content(content)
            .page(schedules.getNumber())
            .size(schedules.getSize())
            .totalElements(schedules.getTotalElements())
            .totalPages(schedules.getTotalPages())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ITPScheduleResponse findById(Long id) {
        ITPSchedule schedule = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("ITP Schedule not found: " + id));
        return toResponse(schedule);
    }

    @Override
    public ITPScheduleResponse create(ITPScheduleRequest request) {
        Contract contract = contractRepository.findById(request.getContractId())
            .orElseThrow(() -> new RuntimeException("Contract not found: " + request.getContractId()));
        
        ITPSchedule schedule = ITPSchedule.builder()
            .title(request.getTitle())
            .contract(contract)
            .startDate(request.getStartDate())
            .dueDate(request.getDueDate())
            .status(request.getStatus() != null ? request.getStatus() : ScheduleStatus.PENDING)
            .assignedInspector(request.getAssignedInspector())
            .notes(request.getNotes())
            .build();
        
        if (request.getTemplateId() != null) {
            ITPTemplate template = templateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new RuntimeException("ITP Template not found: " + request.getTemplateId()));
            schedule.setTemplate(template);
        }
        
        if (request.getItems() != null) {
            for (ITPScheduleRequest.ScheduleItemRequest itemReq : request.getItems()) {
                ITPScheduleItem item = ITPScheduleItem.builder()
                    .sequence(itemReq.getSequence())
                    .description(itemReq.getDescription())
                    .inspectionType(itemReq.getInspectionType())
                    .responsibleParty(itemReq.getResponsibleParty())
                    .dueDate(itemReq.getDueDate())
                    .frequency(itemReq.getFrequency())
                    .requiredEvidence(itemReq.getRequiredEvidence())
                    .status(ScheduleStatus.PENDING)
                    .build();
                schedule.addItem(item);
            }
        }
        
        ITPSchedule saved = repository.save(schedule);
        return toResponse(saved);
    }

    @Override
    public ITPScheduleResponse update(Long id, ITPScheduleRequest request) {
        ITPSchedule schedule = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("ITP Schedule not found: " + id));
        
        schedule.setTitle(request.getTitle());
        schedule.setStartDate(request.getStartDate());
        schedule.setDueDate(request.getDueDate());
        schedule.setAssignedInspector(request.getAssignedInspector());
        schedule.setNotes(request.getNotes());
        
        if (request.getStatus() != null) {
            schedule.setStatus(request.getStatus());
        }
        
        ITPSchedule saved = repository.save(schedule);
        return toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("ITP Schedule not found: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    public ITPScheduleResponse createFromTemplate(Long templateId, Long contractId) {
        ITPTemplate template = templateRepository.findById(templateId)
            .orElseThrow(() -> new RuntimeException("ITP Template not found: " + templateId));
        
        Contract contract = contractRepository.findById(contractId)
            .orElseThrow(() -> new RuntimeException("Contract not found: " + contractId));
        
        ITPSchedule schedule = ITPSchedule.builder()
            .title(template.getName() + " - Schedule")
            .contract(contract)
            .template(template)
            .startDate(LocalDate.now())
            .dueDate(LocalDate.now().plusDays(30))
            .status(ScheduleStatus.PENDING)
            .build();
        
        for (ITPTemplateItem templateItem : template.getItems()) {
            ITPScheduleItem item = ITPScheduleItem.builder()
                .sequence(templateItem.getSequence())
                .description(templateItem.getDescription())
                .inspectionType(templateItem.getInspectionType())
                .responsibleParty(templateItem.getResponsibleParty())
                .frequency(templateItem.getFrequency())
                .requiredEvidence(templateItem.getRequiredEvidence())
                .status(ScheduleStatus.PENDING)
                .build();
            schedule.addItem(item);
        }
        
        ITPSchedule saved = repository.save(schedule);
        return toResponse(saved);
    }

    @Override
    public ITPScheduleResponse completeItem(Long scheduleId, Long itemId, String completedBy, String result) {
        ITPSchedule schedule = repository.findById(scheduleId)
            .orElseThrow(() -> new RuntimeException("ITP Schedule not found: " + scheduleId));
        
        ITPScheduleItem item = schedule.getItems().stream()
            .filter(i -> i.getId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Schedule item not found: " + itemId));
        
        item.setStatus(ScheduleStatus.COMPLETED);
        item.setCompletedDate(LocalDate.now());
        item.setCompletedBy(completedBy);
        item.setResult(result);
        
        // Check if all items are complete
        boolean allComplete = schedule.getItems().stream()
            .allMatch(i -> i.getStatus() == ScheduleStatus.COMPLETED);
        
        if (allComplete) {
            schedule.setStatus(ScheduleStatus.COMPLETED);
            schedule.setCompletedDate(LocalDate.now());
        } else {
            schedule.setStatus(ScheduleStatus.IN_PROGRESS);
        }
        
        ITPSchedule saved = repository.save(schedule);
        return toResponse(saved);
    }

    private ITPScheduleResponse toResponse(ITPSchedule schedule) {
        List<ITPScheduleResponse.ScheduleItemResponse> items = schedule.getItems().stream()
            .map(item -> ITPScheduleResponse.ScheduleItemResponse.builder()
                .id(item.getId())
                .sequence(item.getSequence())
                .description(item.getDescription())
                .inspectionType(item.getInspectionType())
                .responsibleParty(item.getResponsibleParty())
                .dueDate(item.getDueDate())
                .frequency(item.getFrequency())
                .requiredEvidence(item.getRequiredEvidence())
                .status(item.getStatus())
                .completedDate(item.getCompletedDate())
                .completedBy(item.getCompletedBy())
                .result(item.getResult())
                .notes(item.getNotes())
                .build())
            .collect(Collectors.toList());
        
        return ITPScheduleResponse.builder()
            .id(schedule.getId())
            .title(schedule.getTitle())
            .contractId(schedule.getContract().getId())
            .contractRef(schedule.getContract().getContractRef())
            .templateId(schedule.getTemplate() != null ? schedule.getTemplate().getId() : null)
            .templateName(schedule.getTemplate() != null ? schedule.getTemplate().getName() : null)
            .startDate(schedule.getStartDate())
            .dueDate(schedule.getDueDate())
            .completedDate(schedule.getCompletedDate())
            .status(schedule.getStatus())
            .assignedInspector(schedule.getAssignedInspector())
            .signOffBy(schedule.getSignOffBy())
            .signOffDate(schedule.getSignOffDate())
            .signOffSignature(schedule.getSignOffSignature())
            .notes(schedule.getNotes())
            .items(items)
            .createdAt(schedule.getCreatedAt())
            .updatedAt(schedule.getUpdatedAt())
            .build();
    }
}
