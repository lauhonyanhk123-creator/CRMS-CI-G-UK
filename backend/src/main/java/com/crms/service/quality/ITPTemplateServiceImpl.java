package com.crms.service.quality;

import com.crms.domain.quality.entity.ITPTemplate;
import com.crms.domain.quality.entity.ITPTemplateItem;
import com.crms.domain.quality.enums.TemplateStatus;
import com.crms.domain.quality.repository.ITPTemplateRepository;
import com.crms.dto.request.quality.ITPTemplateRequest;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.quality.ITPTemplateResponse;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
@Transactional
public class ITPTemplateServiceImpl implements ITPTemplateService {

    private final ITPTemplateRepository repository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ITPTemplateResponse> findAll(Map<String, Object> params) {
        int page = params.get("page") != null ? (int) params.get("page") : 0;
        int size = params.get("size") != null ? (int) params.get("size") : 20;
        String sort = params.get("sort") != null ? (String) params.get("sort") : "id";
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
        
        TemplateStatus status = params.get("status") != null 
            ? TemplateStatus.valueOf((String) params.get("status")) : null;
        String category = (String) params.get("category");
        String tradeCategory = (String) params.get("tradeCategory");
        
        Page<ITPTemplate> templates = repository.findByFilters(status, category, tradeCategory, pageable);
        
        List<ITPTemplateResponse> content = templates.getContent().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
        
        return PageResponse.<ITPTemplateResponse>builder()
            .content(content)
            .page(templates.getNumber())
            .size(templates.getSize())
            .totalElements(templates.getTotalElements())
            .totalPages(templates.getTotalPages())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ITPTemplateResponse findById(Long id) {
        ITPTemplate template = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("ITP Template not found: " + id));
        return toResponse(template);
    }

    @Override
    public ITPTemplateResponse create(ITPTemplateRequest request) {
        ITPTemplate template = ITPTemplate.builder()
            .name(request.getName())
            .description(request.getDescription())
            .category(request.getCategory())
            .tradeCategory(request.getTradeCategory())
            .status(request.getStatus() != null ? request.getStatus() : TemplateStatus.DRAFT)
            .version(1)
            .build();
        
        if (request.getItems() != null) {
            for (ITPTemplateRequest.ITPItemRequest itemReq : request.getItems()) {
                ITPTemplateItem item = ITPTemplateItem.builder()
                    .sequence(itemReq.getSequence())
                    .description(itemReq.getDescription())
                    .inspectionType(itemReq.getInspectionType())
                    .responsibleParty(itemReq.getResponsibleParty())
                    .notes(itemReq.getNotes())
                    .frequency(itemReq.getFrequency())
                    .requiredEvidence(itemReq.getRequiredEvidence())
                    .build();
                template.addItem(item);
            }
        }
        
        ITPTemplate saved = repository.save(template);
        return toResponse(saved);
    }

    @Override
    public ITPTemplateResponse update(Long id, ITPTemplateRequest request) {
        ITPTemplate template = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("ITP Template not found: " + id));
        
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setCategory(request.getCategory());
        template.setTradeCategory(request.getTradeCategory());
        if (request.getStatus() != null) {
            template.setStatus(request.getStatus());
        }
        
        // Update items - clear and re-add
        template.getItems().clear();
        if (request.getItems() != null) {
            for (ITPTemplateRequest.ITPItemRequest itemReq : request.getItems()) {
                ITPTemplateItem item = ITPTemplateItem.builder()
                    .sequence(itemReq.getSequence())
                    .description(itemReq.getDescription())
                    .inspectionType(itemReq.getInspectionType())
                    .responsibleParty(itemReq.getResponsibleParty())
                    .notes(itemReq.getNotes())
                    .frequency(itemReq.getFrequency())
                    .requiredEvidence(itemReq.getRequiredEvidence())
                    .build();
                template.addItem(item);
            }
        }
        
        ITPTemplate saved = repository.save(template);
        return toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("ITP Template not found: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    public ITPTemplateResponse copyTemplate(Long id) {
        ITPTemplate original = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("ITP Template not found: " + id));
        
        ITPTemplate copy = ITPTemplate.builder()
            .name(original.getName() + " (Copy)")
            .description(original.getDescription())
            .category(original.getCategory())
            .tradeCategory(original.getTradeCategory())
            .status(TemplateStatus.DRAFT)
            .version(1)
            .build();
        
        for (ITPTemplateItem originalItem : original.getItems()) {
            ITPTemplateItem item = ITPTemplateItem.builder()
                .sequence(originalItem.getSequence())
                .description(originalItem.getDescription())
                .inspectionType(originalItem.getInspectionType())
                .responsibleParty(originalItem.getResponsibleParty())
                .notes(originalItem.getNotes())
                .frequency(originalItem.getFrequency())
                .requiredEvidence(originalItem.getRequiredEvidence())
                .build();
            copy.addItem(item);
        }
        
        ITPTemplate saved = repository.save(copy);
        return toResponse(saved);
    }

    private ITPTemplateResponse toResponse(ITPTemplate template) {
        List<ITPTemplateResponse.ITPItemResponse> items = template.getItems().stream()
            .map(item -> ITPTemplateResponse.ITPItemResponse.builder()
                .id(item.getId())
                .sequence(item.getSequence())
                .description(item.getDescription())
                .inspectionType(item.getInspectionType())
                .responsibleParty(item.getResponsibleParty())
                .notes(item.getNotes())
                .frequency(item.getFrequency())
                .requiredEvidence(item.getRequiredEvidence())
                .build())
            .collect(Collectors.toList());
        
        return ITPTemplateResponse.builder()
            .id(template.getId())
            .name(template.getName())
            .description(template.getDescription())
            .category(template.getCategory())
            .tradeCategory(template.getTradeCategory())
            .version(template.getVersion())
            .status(template.getStatus())
            .items(items)
            .createdAt(template.getCreatedAt())
            .updatedAt(template.getUpdatedAt())
            .createdBy(template.getCreatedBy())
            .updatedBy(template.getUpdatedBy())
            .build();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public java.util.List<String> getDistinctCategories() {
        return repository.findDistinctCategories();
    }
}
