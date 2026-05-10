# CRMS CI G UK — UAT Checklist for Pilot Customers

## Version
CRMS CI G UK v1.0

## Purpose
This checklist validates that all 12 modules function correctly in a real-world UK groundwork contractor environment.

---

## Module 1: Sales & Tender Pipeline
- [ ] Create new tender with enquiry details
- [ ] Move tender through Kanban stages (Enquiry → Quote → Submitted → Awarded/Lost)
- [ ] Add BoQ items to tender from library (276+ NRM2/CESMM4 items)
- [ ] Import CITE-XML BoQ file
- [ ] Upload CDE documents to tender
- [ ] Mark tender as Won → Contract auto-created
- [ ] Mark tender as Lost with reason

---

## Module 2: Estimating & BoQ Library
- [ ] Search BoQ library by code, description, category
- [ ] Filter by NRM2 or CESMM4 standard
- [ ] Add custom BoQ item to library
- [ ] Verify rates (£/m³, £/m², £/linear m, etc.)
- [ ] Create tender BoQ from library items
- [ ] Calculate total estimate

---

## Module 3: Contract & Commercial Control
- [ ] View contract created from won tender
- [ ] Capture JCT/NEC4 contract form and details
- [ ] Create application for payment (AFPs)
- [ ] Submit AFP to client
- [ ] Receive payment certificate from client
- [ ] Issue pay-less notice if applicable
- [ ] Create variation orders (VOs)
- [ ] Generate CVR (Cost Value Reconciliation) report
- [ ] Post WIP journal entries

---

## Module 4: Subcontractors, CIS & Supply Chain
- [ ] Add subcontractor company
- [ ] Verify company via Companies House API
- [ ] Check CIS verification status
- [ ] Generate CIS300 return
- [ ] Submit CIS300 to HMRC API (sandbox mode)
- [ ] Issue Payment & Deduction Statement to subbie
- [ ] Run subbie gate check (CIS + CSCS + RAMS + induction + plant ticket)
- [ ] Block/allow subbie based on gate status

---

## Module 5: Operatives, Payroll Bridge & Competence
- [ ] Add operative with CSCS/CPCS/NPORS card
- [ ] Verify card via CSCS Smart Check API
- [ ] Add qualifications (NVQ, SMSTS, SSSTS, EUSR, IPAF, PASMA)
- [ ] Receive expiry alerts (60/30/14/7 days)
- [ ] Submit timesheet with hours worked
- [ ] Export FPS-ready timesheet data
- [ ] View operative competence dashboard

---

## Module 6: Plant Register & LOLER/PUWER Calendar
- [ ] Add plant item (owned/hired/cross-hired)
- [ ] Schedule LOLER examination (6 or 12-monthly)
- [ ] Schedule PUWER inspection
- [ ] Record daily pre-use check
- [ ] View plant Gantt chart
- [ ] Allocate plant to site/contract
- [ ] Check CPCS operator allocation
- [ ] Record hire record under CPA conditions

---

## Module 7: Materials, Procurement & Delivery
- [ ] Create purchase requisition
- [ ] Convert PR to purchase order
- [ ] Receive goods (GRN)
- [ ] Record concrete delivery ticket
- [ ] Record slump test result
- [ ] Record water added to mix
- [ ] Record cube sample (7/28-day strength)
- [ ] View CUSUM chart for cube results
- [ ] Record muckaway ticket with waste type
- [ ] Verify landfill tax calculation (£126.15/t standard, £4.05/t inert)

---

## Module 8: Health, Safety & CDM 2015
- [ ] Submit F10 notification to HSE
- [ ] Create Construction Phase Plan (CPP)
- [ ] Use RAMS template from library (30 templates)
- [ ] Digital RAMS sign-on on PWA
- [ ] Create permit to dig
- [ ] Create permit to load/strike
- [ ] Submit mandatory occurrence report
- [ ] View H&S statistics (AFR)

---

## Module 9: Quality, ITPs & Inspections
- [ ] Create ITP schedule from template
- [ ] Track ITP items (CBR, plate-bearing, TR34, drainage test, CCTV)
- [ ] Record inspection result
- [ ] Add external inspection (NHBC, LABC, Local Authority, Water Authority)
- [ ] Raise defect
- [ ] Assign defect to responsible party
- [ ] Close out defect
- [ ] View defect summary

---

## Module 10: Section 38 / 278 / 104 Adoption Workflow
- [ ] Create adoption case
- [ ] Add bond details (10-25% works value)
- [ ] Track 4-stage Road Safety Audit
- [ ] Record CCTV inspection
- [ ] Record air/water testing
- [ ] Issue Provisional Certificate
- [ ] Track 12-month maintenance period
- [ ] Calculate commuted sums
- [ ] Receive bond-release alerts (90/60/30/14/7/0 days)

---

## Module 11: Site PWA (Offline-First)
- [ ] Install PWA on mobile device
- [ ] Work offline with IndexedDB queue
- [ ] Capture GPS-tagged photos
- [ ] Site sign-on with face capture
- [ ] Complete toolbox talk
- [ ] Record dayworks sheet
- [ ] Plant pre-use check on mobile
- [ ] Capture muckaway/concrete ticket
- [ ] Raise defect from site
- [ ] Submit near-miss/incident report
- [ ] Sync data when back online

---

## Module 12: Reporting Suite
- [ ] Generate CVR pack
- [ ] View cash-flow forecast
- [ ] Generate retention schedule
- [ ] Generate CIS300 pack
- [ ] Calculate CITB levy estimate
- [ ] View plant utilisation report
- [ ] View H&S statistics (AFR)
- [ ] Generate subcontractor performance report
- [ ] View adoption status report
- [ ] Generate Payment Practices (PPPR 2017) report

---

## Security & Admin
- [ ] Login with admin credentials
- [ ] Login with role-based user (Director, Manager, Site Agent, etc.)
- [ ] Verify RBAC permissions enforced
- [ ] Enable TOTP 2FA
- [ ] View audit log
- [ ] Verify SHA-256 hash chain
- [ ] Manage users (create, edit, deactivate)
- [ ] Configure integrations (HMRC, Companies House, CSCS)

---

## Performance & Reliability
- [ ] Login response time < 2 seconds
- [ ] Dashboard loads < 3 seconds
- [ ] Large dataset pagination works (1000+ records)
- [ ] Offline mode survives 4G drop
- [ ] Data syncs correctly after offline period
- [ ] PWA installs correctly on iOS/Android

---

## Integration Points
- [ ] HMRC CIS API (sandbox) connection works
- [ ] Companies House auto-refresh works
- [ ] CSCS Smart Check validates card
- [ ] Email notifications send correctly
- [ ] Document upload/download works

---

## Sign-Off

| Role | Name | Signature | Date |
|------|------|-----------|------|
| Pilot Customer | | | |
| CRMS Engineer | | | |
| Project Manager | | | |

---

## Known Issues (Pilot Phase)
_Record any issues found during UAT for resolution before general availability._

| # | Issue Description | Severity | Status |
|---|-------------------|----------|--------|
| 1 | | | |
| 2 | | | |
| 3 | | | |
