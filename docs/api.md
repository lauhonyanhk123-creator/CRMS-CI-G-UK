# CRMS CI G UK REST API Reference

## Base URL

```
Production: https://crms.example.com/api/v1
Development: http://localhost:8080/api/v1
```

## Authentication

All API requests require Bearer token authentication:

```http
Authorization: Bearer <jwt_token>
```

### Endpoints

#### Authentication
```
POST   /auth/login          - Login and receive JWT
POST   /auth/logout         - Invalidate session
POST   /auth/refresh        - Refresh JWT token
POST   /auth/password-reset - Request password reset
```

## Companies & Contacts

#### Companies
```
GET    /companies           - List companies (with filters)
POST   /companies           - Create company
GET    /companies/{id}      - Get company details
PATCH  /companies/{id}      - Update company
DELETE /companies/{id}      - Soft delete company
POST   /companies/{id}/companies-house-refresh - Refresh from Companies House
POST   /companies/{id}/cis-verify - Verify CIS status
```

#### Contacts
```
GET    /companies/{companyId}/contacts
POST   /companies/{companyId}/contacts
GET    /contacts/{id}
PATCH  /contacts/{id}
DELETE /contacts/{id}
```

## Sites & Projects

#### Sites
```
GET    /sites               - List sites
POST   /sites               - Create site
GET    /sites/{id}          - Get site details
PATCH  /sites/{id}          - Update site
GET    /sites/{id}/dashboard - Site dashboard data
```

#### Tenders
```
GET    /tenders             - List tenders
POST   /tenders             - Create tender
GET    /tenders/{id}        - Get tender details
PATCH  /tenders/{id}        - Update tender
POST   /tenders/{id}/win    - Mark tender as won (creates contract)
POST   /tenders/{id}/lose   - Mark tender as lost
```

## BoQ & Estimating

#### BoQ Library
```
GET    /boq-library         - Search BoQ library
POST   /boq-library         - Add custom BoQ item
GET    /boq-library/{id}    - Get BoQ item
PATCH  /boq-library/{id}    - Update BoQ item
```

#### Tender BoQ
```
GET    /tenders/{tenderId}/boq-items
POST   /tenders/{tenderId}/boq-items
PATCH  /boq-items/{id}
DELETE /boq-items/{id}
POST   /tenders/{id}/import-cite-xml - Import CITE-XML
```

## Contracts & Commercial

#### Contracts
```
GET    /contracts           - List contracts
POST   /contracts           - Create contract
GET    /contracts/{id}      - Get contract details
PATCH  /contracts/{id}      - Update contract
GET    /contracts/{id}/cvr  - Get CVR report
```

#### Applications for Payment
```
GET    /contracts/{contractId}/applications
POST   /contracts/{contractId}/applications
GET    /applications/{id}
PATCH  /applications/{id}
POST   /applications/{id}/submit
POST   /applications/{id}/payment-notice
POST   /applications/{id}/pay-less-notice
POST   /applications/{id}/default-payment-notice
```

#### Variations
```
GET    /contracts/{contractId}/variations
POST   /contracts/{contractId}/variations
GET    /variations/{id}
PATCH  /variations/{id}
POST   /contracts/{contractId}/early-warnings - NEC4 EWN
```

#### Retention
```
GET    /contracts/{contractId}/retention-ledger
POST   /retention-movements
```

## Subcontractors & CIS

#### Subcontractors
```
GET    /subcontractors
POST   /subcontractors
GET    /subcontractors/{id}
PATCH  /subcontractors/{id}
POST   /subcontractors/{id}/verify
GET    /subcontractors/{id}/performance
```

#### CIS Returns
```
GET    /cis-returns
POST   /cis-returns
GET    /cis-returns/{id}
PATCH  /cis-returns/{id}
POST   /cis-returns/{id}/submit
GET    /cis-returns/{id}/payment-deduction-statements
```

## Operatives & Payroll

#### Operatives
```
GET    /operatives
POST   /operatives
GET    /operatives/{id}
PATCH  /operatives/{id}
GET    /operatives/{id}/cards
POST   /operatives/{id}/cards
POST   /operatives/{id}/cards/cscs-smart-check
GET    /operatives/{id}/qualifications
POST   /operatives/{id}/qualifications
```

#### Site Sign-ons
```
GET    /sites/{siteId}/sign-ons
POST   /sites/{siteId}/sign-ons
GET    /operatives/{id}/sign-ons
```

## Plant Management

#### Plant Items
```
GET    /plant-items
POST   /plant-items
GET    /plant-items/{id}
PATCH  /plant-items/{id}
DELETE /plant-items/{id}
POST   /plant-items/{id}/loler-examinations
POST   /plant-items/{id}/puwer-inspections
POST   /plant-items/{id}/allocations
```

#### Plant Gantt
```
GET    /plant-gantt?from=2026-04-01&to=2026-05-31
GET    /plant-utilisation?from=&to=&groupBy=item|site|week
```

## Procurement & Materials

#### Purchase Orders
```
GET    /purchase-orders
POST   /purchase-orders
GET    /purchase-orders/{id}
PATCH  /purchase-orders/{id}
POST   /purchase-orders/{id}/approve
```

#### Delivery Notes
```
POST   /purchase-orders/{id}/delivery-notes
GET    /delivery-notes/{id}
POST   /delivery-notes/{id}/concrete-ticket
POST   /delivery-notes/{id}/muckaway-ticket
```

#### Concrete
```
GET    /concrete-tickets/{id}
POST   /concrete-tickets/{id}/cube-samples
PATCH  /cube-samples/{id}/results
```

## Health & Safety

#### CDM
```
POST   /contracts/{contractId}/cpp       - Construction Phase Plan
POST   /contracts/{contractId}/f10       - F10 notification
```

#### RAMS
```
GET    /rams-library                     - RAMS templates
POST   /rams-library                     - Create template
GET    /contracts/{contractId}/rams      - Contract RAMS
POST   /contracts/{contractId}/rams
GET    /rams/{id}/sign-ons
POST   /rams/{id}/sign-ons               - Sign RAMS
```

#### Permits
```
POST   /permits/dig
GET    /permits/dig/{id}
POST   /permits/dig/{id}/approve
POST   /permits/dig/{id}/complete
```

## Adoption Workflows

#### Adoption Cases
```
GET    /adoption-cases
POST   /adoption-cases
GET    /adoption-cases/{id}
PATCH  /adoption-cases/{id}
POST   /adoption-cases/{id}/stage
POST   /adoption-cases/{id}/bond/release-request
```

#### Bonds
```
GET    /adoption-cases/{caseId}/bonds
POST   /adoption-cases/{caseId}/bonds
PATCH  /bonds/{id}
POST   /bonds/{id}/drawdown
```

## Quality & Inspections

#### Inspections
```
GET    /inspections
POST   /inspections
GET    /inspections/{id}
PATCH  /inspections/{id}
POST   /inspections/{id}/complete
```

#### Defects
```
GET    /defects
POST   /defects
GET    /defects/{id}
PATCH  /defects/{id}
POST   /defects/{id}/close
```

## Reports

```
GET    /reports/cvr?contract={id}&period=2026-04
GET    /reports/cashflow?from=2026-04&to=2027-03
GET    /reports/retention-schedule
GET    /reports/cis-summary?tax-month=2026-04
GET    /reports/plant-utilisation
GET    /reports/hs-statistics
GET    /reports/subcontractor-performance
GET    /reports/adoption-status
```

## Documents

```
POST   /documents               - Upload document
GET    /documents/{id}          - Get document metadata
GET    /documents/{id}/content  - Download document
GET    /documents/{id}/versions - Version history
POST   /documents/{id}/versions - Upload new version
DELETE /documents/{id}          - Delete document
```

## Common Query Parameters

| Parameter | Description |
|-----------|-------------|
| `page` | Page number (default: 0) |
| `size` | Page size (default: 20, max: 100) |
| `sort` | Sort field (e.g., `-createdAt,name`) |
| `filter` | Filter expression (e.g., `status:eq:active`) |
| `fields` | Sparse fields (e.g., `id,name,status`) |

## Response Format

### Success Response
```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8
}
```

### Error Response
```json
{
  "timestamp": "2026-04-27T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/companies",
  "details": [
    {"field": "name", "message": "must not be blank"}
  ]
}
```

## Rate Limiting

- Default: 100 requests per minute
- Burst: 20 requests
- Rate limit headers returned:
  - `X-RateLimit-Limit`
  - `X-RateLimit-Remaining`
  - `X-RateLimit-Reset`
