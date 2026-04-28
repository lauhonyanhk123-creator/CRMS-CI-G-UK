package com.crms.service;

import com.crms.dto.request.RAMSTemplateRequest;
import com.crms.dto.response.RAMSTemplateResponse;
import com.crms.dto.response.PageResponse;

import java.util.List;
import java.util.Map;

public interface RAMSTemplateService {

    PageResponse<RAMSTemplateResponse> findAll(Map<String, Object> params);

    RAMSTemplateResponse findById(Long id);

    RAMSTemplateResponse create(RAMSTemplateRequest request);

    RAMSTemplateResponse update(Long id, RAMSTemplateRequest request);

    void delete(Long id);

    List<RAMSTemplateResponse> findActive();

    List<RAMSTemplateResponse> findByTrade(String trade);

    List<String> findDistinctTrades();

    RAMSTemplateResponse copyTemplate(Long id, String newTitle);
}
