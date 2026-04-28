package com.crms.service;

import com.crms.dto.request.ApplicationForPaymentRequest;
import com.crms.dto.request.PayLessNoticeRequest;
import com.crms.dto.request.PaymentNoticeRequest;
import com.crms.dto.response.ApplicationResponse;
import com.crms.dto.response.PageResponse;

import java.util.Map;

public interface ApplicationForPaymentService {
    
    PageResponse<ApplicationResponse> findByContract(Long contractId);
    
    ApplicationResponse findById(Long id);
    
    ApplicationResponse create(Long contractId, ApplicationForPaymentRequest request);
    
    ApplicationResponse submit(Long id);
    
    ApplicationResponse measure(Long id);
    
    ApplicationResponse agree(Long id);
    
    ApplicationResponse approve(Long id);
    
    ApplicationResponse reject(Long id);
    
    ApplicationResponse markPaid(Long id);
    
    ApplicationResponse addPaymentNotice(Long id, PaymentNoticeRequest request);
    
    ApplicationResponse addPayLessNotice(Long id, PayLessNoticeRequest request);
    
    ApplicationResponse addDefaultNotice(Long id);
}