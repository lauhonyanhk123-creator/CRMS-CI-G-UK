package com.crms.service;

import com.crms.dto.request.CardRequest;
import com.crms.dto.request.OperativeRequest;
import com.crms.dto.request.QualificationRequest;
import com.crms.dto.response.CardResponse;
import com.crms.dto.response.OperativeResponse;
import com.crms.dto.response.PageResponse;
import com.crms.dto.response.QualificationResponse;
import com.crms.dto.response.SubbieGateStatus;

import java.util.List;
import java.util.Map;

public interface OperativeService {

    PageResponse<OperativeResponse> findAll(Map<String, Object> params);

    OperativeResponse findById(Long id);

    OperativeResponse create(OperativeRequest request);

    OperativeResponse update(Long id, OperativeRequest request);

    // Cards
    CardResponse addCard(Long operativeId, CardRequest request);

    List<CardResponse> getCards(Long operativeId);

    CardResponse deleteCard(Long operativeId, Long cardId);

    // Qualifications
    QualificationResponse addQualification(Long operativeId, QualificationRequest request);

    List<QualificationResponse> getQualifications(Long operativeId);

    QualificationResponse deleteQualification(Long operativeId, Long qualificationId);

    // CSCS Smart Check
    SubbieGateStatus smartCheckCard(Long operativeId, Long cardId);

    SubbieGateStatus getSubbieGateStatus(Long operativeId);

    void delete(Long id);
}