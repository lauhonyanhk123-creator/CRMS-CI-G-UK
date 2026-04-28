package com.crms.domain.tender.repository;

import com.crms.domain.tender.entity.TenderDocument;
import com.crms.domain.tender.enums.CdeStatus;
import com.crms.domain.tender.enums.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenderDocumentRepository extends JpaRepository<TenderDocument, Long> {

    List<TenderDocument> findByTenderId(Long tenderId);

    List<TenderDocument> findByDocumentType(DocumentType documentType);

    List<TenderDocument> findByCdeStatus(CdeStatus cdeStatus);
}
