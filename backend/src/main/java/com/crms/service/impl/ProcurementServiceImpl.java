package com.crms.service.impl;

import com.crms.service.ProcurementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcurementServiceImpl implements ProcurementService {
    
    @Override
    public Object findRequisitions(Map<String, Object> params) {
        log.info("Finding purchase requisitions");
        return null;
    }
    
    @Override
    public Object createRequisition(Object request) {
        log.info("Creating purchase requisition");
        return null;
    }
    
    @Override
    public Object approveRequisition(Long id) {
        log.info("Approving requisition {}", id);
        return null;
    }
    
    @Override
    public Object createPO(Long requisitionId) {
        log.info("Creating purchase order from requisition {}", requisitionId);
        return null;
    }
    
    @Override
    public Object getDeliveryNotes(Map<String, Object> params) {
        log.info("Finding delivery notes");
        return null;
    }
}