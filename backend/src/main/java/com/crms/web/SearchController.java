package com.crms.web;

import com.crms.domain.company.repository.CompanyRepository;
import com.crms.domain.contract.repository.ContractRepository;
import com.crms.domain.operative.repository.OperativeRepository;
import com.crms.domain.plant.repository.PlantItemRepository;
import com.crms.domain.site.repository.SiteRepository;
import com.crms.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "Global search endpoint")
@SecurityRequirement(name = "bearerAuth")
public class SearchController {

    private final ContractRepository contractRepository;
    private final OperativeRepository operativeRepository;
    private final SiteRepository siteRepository;
    private final CompanyRepository companyRepository;
    private final PlantItemRepository plantItemRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Global search", description = "Search across all entity types")
    public ResponseEntity<ApiResponse<Map<String, List<Map<String, Object>>>>> search(
            @RequestParam String q) {

        PageRequest limit = PageRequest.of(0, 5);
        Map<String, List<Map<String, Object>>> results = new HashMap<>();

        // Contracts: search by title or contractRef
        List<Map<String, Object>> contracts = contractRepository.findAll(PageRequest.of(0, 100))
                .stream()
                .filter(c -> c.getTitle() != null && c.getTitle().toLowerCase().contains(q.toLowerCase())
                        || c.getContractRef() != null && c.getContractRef().toLowerCase().contains(q.toLowerCase()))
                .limit(5)
                .map(c -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", c.getId());
                    m.put("label", c.getContractRef() + " – " + c.getTitle());
                    m.put("ref", c.getContractRef());
                    return m;
                })
                .collect(Collectors.toList());
        results.put("contracts", contracts);

        // Operatives: search by name or employeeRef
        List<Map<String, Object>> operatives = operativeRepository.searchByNameOrRef(q, limit)
                .stream()
                .map(o -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", o.getId());
                    m.put("label", o.getFirstName() + " " + o.getLastName()
                            + (o.getEmployeeRef() != null ? " (" + o.getEmployeeRef() + ")" : ""));
                    return m;
                })
                .collect(Collectors.toList());
        results.put("operatives", operatives);

        // Sites: search by name
        List<Map<String, Object>> sites = siteRepository.findByNameContainingIgnoreCase(q, limit)
                .stream()
                .map(s -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", s.getId());
                    m.put("label", s.getName() + (s.getSiteCode() != null ? " (" + s.getSiteCode() + ")" : ""));
                    return m;
                })
                .collect(Collectors.toList());
        results.put("sites", sites);

        // Companies: search by name
        List<Map<String, Object>> companies = companyRepository.findByNameContainingIgnoreCase(q, limit)
                .stream()
                .map(c -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", c.getId());
                    m.put("label", c.getName());
                    m.put("type", c.getCompanyType() != null ? c.getCompanyType().name() : "");
                    return m;
                })
                .collect(Collectors.toList());
        results.put("companies", companies);

        // Plant: search by ref or description
        List<Map<String, Object>> plant = plantItemRepository.searchByRefOrDescription(q, limit)
                .stream()
                .map(p -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", p.getId());
                    m.put("label", p.getPlantRef() + " – " + p.getDescription());
                    return m;
                })
                .collect(Collectors.toList());
        results.put("plant", plant);

        return ResponseEntity.ok(ApiResponse.success(results));
    }
}
