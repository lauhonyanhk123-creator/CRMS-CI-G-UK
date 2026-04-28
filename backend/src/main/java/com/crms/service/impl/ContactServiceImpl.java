package com.crms.service.impl;

import com.crms.domain.company.entity.Company;
import com.crms.domain.company.entity.Contact;
import com.crms.domain.company.repository.CompanyRepository;
import com.crms.domain.company.repository.ContactRepository;
import com.crms.dto.request.ContactRequest;
import com.crms.dto.response.ContactResponse;
import com.crms.dto.response.PageResponse;
import com.crms.exception.ResourceNotFoundException;
import com.crms.service.ContactService;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {
    
    private final ContactRepository contactRepository;
    private final CompanyRepository companyRepository;
    
    @Override
    public PageResponse<ContactResponse> findAll(Map<String, Object> params) {
        int page = params.containsKey("page") ? Integer.parseInt(params.get("page").toString()) : 0;
        int size = params.containsKey("size") ? Integer.parseInt(params.get("size").toString()) : 20;
        String sort = params.containsKey("sort") ? params.get("sort").toString() : "id";
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        
        Page<Contact> contactPage;
        if (params.containsKey("companyId") && params.get("companyId") != null) {
            Long companyId = Long.parseLong(params.get("companyId").toString());
            contactPage = contactRepository.findByCompanyId(companyId, pageable);
        } else {
            contactPage = contactRepository.findAll(pageable);
        }
        
        List<ContactResponse> content = contactPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return PageResponse.<ContactResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(contactPage.getTotalElements())
                .totalPages(contactPage.getTotalPages())
                .build();
    }
    
    @Override
    public ContactResponse findById(Long id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact", id));
        return mapToResponse(contact);
    }
    
    @Override
    @Transactional
    public ContactResponse create(ContactRequest request) {
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", request.getCompanyId()));
        
        Contact contact = Contact.builder()
                .company(company)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .mobile(request.getMobile())
                .jobTitle(request.getJobTitle())
                .isPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false)
                .notes(request.getNotes())
                .build();
        
        contact = contactRepository.save(contact);
        return mapToResponse(contact);
    }
    
    @Override
    @Transactional
    public ContactResponse update(Long id, ContactRequest request) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact", id));
        
        if (request.getCompanyId() != null && !request.getCompanyId().equals(contact.getCompany().getId())) {
            Company company = companyRepository.findById(request.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company", request.getCompanyId()));
            contact.setCompany(company);
        }
        
        contact.setFirstName(request.getFirstName());
        contact.setLastName(request.getLastName());
        contact.setEmail(request.getEmail());
        contact.setPhone(request.getPhone());
        contact.setMobile(request.getMobile());
        contact.setJobTitle(request.getJobTitle());
        contact.setIsPrimary(request.getIsPrimary());
        contact.setNotes(request.getNotes());
        
        contact = contactRepository.save(contact);
        return mapToResponse(contact);
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact", id));
        contactRepository.delete(contact);
    }
    
    private ContactResponse mapToResponse(Contact contact) {
        return ContactResponse.builder()
                .id(contact.getId())
                .companyId(contact.getCompany().getId())
                .companyName(contact.getCompany().getName())
                .firstName(contact.getFirstName())
                .lastName(contact.getLastName())
                .fullName(contact.getFullName())
                .email(contact.getEmail())
                .phone(contact.getPhone())
                .mobile(contact.getMobile())
                .jobTitle(contact.getJobTitle())
                .isPrimary(contact.getIsPrimary())
                .notes(contact.getNotes())
                .build();
    }
}