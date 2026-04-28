package com.crms.service.quality;

import com.crms.dto.request.quality.SignOffRequest;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.quality.SignOffResponse;

import java.util.Map;

public interface SignOffService {

    PageResponse<SignOffResponse> findAll(Map<String, Object> params);

    SignOffResponse findById(Long id);

    SignOffResponse create(SignOffRequest request);

    SignOffResponse update(Long id, SignOffRequest request);

    void delete(Long id);

    SignOffResponse approve(Long id, String signature);

    SignOffResponse refuse(Long id, String conditions);
}
