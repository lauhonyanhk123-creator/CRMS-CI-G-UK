package com.crms.service.impl;

import com.crms.domain.contract.entity.Contract;
import com.crms.domain.contract.entity.Variation;
import com.crms.domain.contract.enums.VariationStatus;
import com.crms.domain.contract.repository.ContractRepository;
import com.crms.domain.contract.repository.VariationRepository;
import com.crms.dto.request.VariationRequest;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.VariationResponse;
import com.crms.exception.ResourceNotFoundException;
import com.crms.service.VariationService;
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
public class VariationServiceImpl implements VariationService {
    
    private final VariationRepository variationRepository;
    private final ContractRepository contractRepository;
    
    @Override
    public PageResponse<VariationResponse> findAll() {
        List<VariationResponse> content = variationRepository.findAll(Pageable.ofSize(1000))
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return PageResponse.<VariationResponse>builder()
                .content(content)
                .page(0)
                .size(content.size())
                .totalElements((long) content.size())
                .totalPages(1)
                .build();
    }

    @Override
    public PageResponse<VariationResponse> findByContract(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", contractId));
        
        List<VariationResponse> content = variationRepository.findByContractIdOrderByNotifiedDateDesc(contractId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return PageResponse.<VariationResponse>builder()
                .content(content)
                .page(0)
                .size(content.size())
                .totalElements((long) content.size())
                .totalPages(1)
                .build();
    }
    
    @Override
    public VariationResponse findById(Long id) {
        Variation variation = variationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variation", id));
        return mapToResponse(variation);
    }
    
    @Override
    @Transactional
    public VariationResponse create(Long contractId, VariationRequest request) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", contractId));
        
        String variationRef = contract.getContractRef() + "-VAR-" + 
                UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        
        Variation variation = Variation.builder()
                .contract(contract)
                .variationRef(variationRef)
                .type(request.getType())
                .description(request.getDescription())
                .originalValue(request.getOriginalValue())
                .agreedValue(request.getAgreedValue())
                .notifiedDate(request.getNotifiedDate())
                .instructionRef(request.getInstructionRef())
                .reason(request.getReason())
                .status(VariationStatus.PENDING)
                .build();
        
        variation = variationRepository.save(variation);
        log.info("Variation {} created for contract {}", variationRef, contract.getContractRef());
        
        return mapToResponse(variation);
    }
    
    @Override
    @Transactional
    public VariationResponse update(Long id, VariationRequest request) {
        Variation variation = variationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variation", id));
        
        variation.setType(request.getType());
        variation.setDescription(request.getDescription());
        variation.setOriginalValue(request.getOriginalValue());
        variation.setAgreedValue(request.getAgreedValue());
        variation.setNotifiedDate(request.getNotifiedDate());
        variation.setInstructionRef(request.getInstructionRef());
        variation.setReason(request.getReason());
        
        variation = variationRepository.save(variation);
        log.info("Variation {} updated", variation.getVariationRef());
        
        return mapToResponse(variation);
    }
    
    private VariationResponse mapToResponse(Variation variation) {
        return VariationResponse.builder()
                .id(variation.getId())
                .variationRef(variation.getVariationRef())
                .contractId(variation.getContract().getId())
                .contractRef(variation.getContract().getContractRef())
                .type(variation.getType() != null ? variation.getType().name() : null)
                .description(variation.getDescription())
                .originalValue(variation.getOriginalValue())
                .agreedValue(variation.getAgreedValue())
                .notifiedDate(variation.getNotifiedDate())
                .status(variation.getStatus() != null ? variation.getStatus().name() : null)
                .instructionRef(variation.getInstructionRef())
                .reason(variation.getReason())
                .build();
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        Variation variation = variationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variation", id));
        variationRepository.delete(variation);
    }
}