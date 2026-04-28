package com.crms.service;

import com.crms.dto.request.ContactRequest;
import com.crms.dto.response.ContactResponse;
import com.crms.dto.response.PageResponse;

import java.util.Map;

public interface ContactService {
    
    PageResponse<ContactResponse> findAll(Map<String, Object> params);
    
    ContactResponse findById(Long id);
    
    ContactResponse create(ContactRequest request);
    
    ContactResponse update(Long id, ContactRequest request);
    
    void delete(Long id);
}