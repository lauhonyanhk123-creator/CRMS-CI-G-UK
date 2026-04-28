-- V6__add_performance_indexes.sql
-- Construction Resource Management System for UK Groundworks
-- Performance indexes for frequently queried columns

-- ============================================
-- Composite indexes for common query patterns
-- ============================================

-- Sites: composite indexes for site lookups by client and status/date ranges
CREATE INDEX idx_sites_client_status ON sites(client_id, status) WHERE deleted_at IS NULL;
CREATE INDEX idx_sites_client_dates ON sites(client_id, start_date, completion_date) WHERE deleted_at IS NULL;
CREATE INDEX idx_sites_status_dates ON sites(status, start_date, estimated_completion_date) WHERE deleted_at IS NULL;

-- Tenders: composite indexes for tender pipeline queries
CREATE INDEX idx_tenders_client_status ON tenders(client_id, status) WHERE deleted_at IS NULL;
CREATE INDEX idx_tenders_status_probability ON tenders(status, win_probability) WHERE deleted_at IS NULL;
CREATE INDEX idx_tenders_status_dates ON tenders(status, tender_issued_date, tender_return_date) WHERE deleted_at IS NULL;
CREATE INDEX idx_tenders_value_range ON tenders(value_range, status) WHERE deleted_at IS NULL;

-- Contracts: composite indexes for contract management
CREATE INDEX idx_contracts_client_status ON contracts(client_id, status) WHERE deleted_at IS NULL;
CREATE INDEX idx_contracts_site_status ON contracts(site_id, status) WHERE deleted_at IS NULL;
CREATE INDEX idx_contracts_status_dates ON contracts(status, start_date, end_date) WHERE deleted_at IS NULL;
CREATE INDEX idx_contracts_value_status ON contracts(contract_value, status) WHERE deleted_at IS NULL;

-- BoQ Items: composite indexes for tender costing queries
CREATE INDEX idx_boq_items_code_trade ON boq_items(item_code, trade) WHERE tender_id IS NOT NULL;
CREATE INDEX idx_boq_items_trade_tender ON boq_items(trade, tender_id) WHERE tender_id IS NOT NULL;

-- Applications for Payment: composite indexes for payment cycle queries
CREATE INDEX idx_applications_contract_period ON applications_for_payment(contract_id, application_period_start, application_period_end) WHERE deleted_at IS NULL;
CREATE INDEX idx_applications_status_due ON applications_for_payment(status, due_date) WHERE deleted_at IS NULL;
CREATE INDEX idx_applications_status_submitted ON applications_for_payment(status, submitted_date) WHERE deleted_at IS NULL;

-- Variations: composite indexes for variation tracking
CREATE INDEX idx_variations_contract_status ON variations(contract_id, status) WHERE deleted_at IS NULL;
CREATE INDEX idx_variations_type_status ON variations(type, status) WHERE deleted_at IS NULL;

-- Operatives: composite indexes for workforce queries
CREATE INDEX idx_operatives_company_status ON operatives(company_id, status) WHERE deleted_at IS NULL;
CREATE INDEX idx_operatives_status_employment ON operatives(status, employment_status) WHERE deleted_at IS NULL;

-- Operative Cards: composite indexes for card expiry tracking
CREATE INDEX idx_operative_cards_type_expiry ON operative_cards(card_type, expiry_date) WHERE deleted_at IS NULL;
CREATE INDEX idx_operative_cards_operative_type ON operative_cards(operative_id, card_type) WHERE deleted_at IS NULL;

-- Qualifications: composite indexes for qualification tracking
CREATE INDEX idx_qualifications_operative_type ON qualifications(operative_id, qualification_type) WHERE deleted_at IS NULL;
CREATE INDEX idx_qualifications_type_expiry ON qualifications(qualification_type, expiry_date) WHERE deleted_at IS NULL;

-- Inductions: composite indexes for site induction tracking
CREATE INDEX idx_inductions_operative_site ON inductions(operative_id, site_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_inductions_validity_site ON inductions(valid_until, site_id) WHERE deleted_at IS NULL;

-- Site Sign-ons: composite indexes for time and attendance
CREATE INDEX idx_site_sign_ons_site_date ON site_sign_ons(site_id, sign_on_time) WHERE sign_off_time IS NULL;
CREATE INDEX idx_site_sign_ons_operative_date ON site_sign_ons(operative_id, sign_on_time) WHERE deleted_at IS NULL;

-- Timesheets: composite indexes for payroll queries
CREATE INDEX idx_timesheets_operative_week ON timesheets(operative_id, week_ending) WHERE deleted_at IS NULL;
CREATE INDEX idx_timesheets_week_site ON timesheets(week_ending, site_id) WHERE deleted_at IS NULL;

-- Plant: composite indexes for plant management
CREATE INDEX idx_plant_items_status_category ON plant_items(status, category) WHERE deleted_at IS NULL;
CREATE INDEX idx_plant_items_status_hire ON plant_items(status, hire_status) WHERE deleted_at IS NULL;

-- Plant Allocations: composite indexes for allocation queries
CREATE INDEX idx_plant_allocations_site_status ON plant_allocations(site_id, status) WHERE deleted_at IS NULL;
CREATE INDEX idx_plant_allocations_status_dates ON plant_allocations(status, start_date, end_date) WHERE deleted_at IS NULL;

-- LOLER/PUWER: composite indexes for compliance tracking
CREATE INDEX idx_loler_next_due_plant ON loler_examinations(next_due_date, plant_item_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_puwer_next_due_plant ON puwer_inspections(next_due_date, plant_item_id) WHERE deleted_at IS NULL;

-- Purchase Orders: composite indexes for procurement queries
CREATE INDEX idx_purchase_orders_supplier_status ON purchase_orders(supplier_id, status) WHERE deleted_at IS NULL;
CREATE INDEX idx_purchase_orders_status_dates ON purchase_orders(status, order_date, delivery_date) WHERE deleted_at IS NULL;

-- Purchase Requisitions: composite indexes for requisition tracking
CREATE INDEX idx_requisitions_status_site ON purchase_requisitions(status, site_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_requisitions_requested_dates ON purchase_requisitions(requested_by, required_date) WHERE deleted_at IS NULL;

-- CIS Verifications: composite indexes for subcontractor verification
CREATE INDEX idx_cis_verifications_company_status ON cis_verifications(company_id, status) WHERE deleted_at IS NULL;
CREATE INDEX idx_cis_verifications_expiry ON cis_verifications(expires_at, status) WHERE deleted_at IS NULL;

-- CIS Returns: composite indexes for HMRC submissions
CREATE INDEX idx_cis_returns_status_month ON cis_returns(status, tax_month) WHERE deleted_at IS NULL;

-- RAMS Documents: composite indexes for safety document tracking
CREATE INDEX idx_rams_documents_contract_status ON rams_documents(contract_id, status) WHERE deleted_at IS NULL;
CREATE INDEX idx_rams_documents_validity ON rams_documents(valid_from, valid_until) WHERE deleted_at IS NULL;

-- RAMS Sign-ons: composite indexes for compliance tracking
CREATE INDEX idx_rams_sign_ons_document_operative ON rams_sign_ons(rams_document_id, operative_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_rams_sign_ons_validity_operative ON rams_sign_ons(valid_until, operative_id) WHERE deleted_at IS NULL;

-- Permits to Dig: composite indexes for permit management
CREATE INDEX idx_permits_site_status ON permits_to_dig(site_id, status) WHERE deleted_at IS NULL;
CREATE INDEX idx_permits_status_dates ON permits_to_dig(status, start_date, end_date) WHERE deleted_at IS NULL;

-- Incident Reports: composite indexes for safety reporting
CREATE INDEX idx_incident_reports_site_date ON incident_reports(site_id, incident_date) WHERE deleted_at IS NULL;
CREATE INDEX idx_incident_reports_type_severity ON incident_reports(incident_type, severity) WHERE deleted_at IS NULL;
CREATE INDEX idx_incident_reports_status_severity ON incident_reports(status, severity) WHERE deleted_at IS NULL;

-- Adoption Cases: composite indexes for adoption management
CREATE INDEX idx_adoption_cases_status_type ON adoption_cases(status, adoption_type) WHERE deleted_at IS NULL;
CREATE INDEX idx_adoption_cases_maintenance_status ON adoption_cases(maintenance_end_date, status) WHERE deleted_at IS NULL;

-- Adoption Stages: composite indexes for stage tracking
CREATE INDEX idx_adoption_stages_case_order ON adoption_stages(adoption_case_id, stage_order) WHERE deleted_at IS NULL;
CREATE INDEX idx_adoption_stages_status_dates ON adoption_stages(status, planned_date) WHERE deleted_at IS NULL;

-- ============================================
-- Partial indexes for common filtered queries
-- ============================================

-- Active sites only
CREATE INDEX idx_sites_active ON sites(id, name) WHERE status = 'ACTIVE' AND deleted_at IS NULL;

-- Pending applications for payment
CREATE INDEX idx_applications_pending ON applications_for_payment(id, due_date) WHERE status = 'SUBMITTED' AND deleted_at IS NULL;

-- Expiring qualifications (within 90 days)
CREATE INDEX idx_qualifications_expiring ON qualifications(operative_id, expiry_date) WHERE expiry_date IS NOT NULL AND expiry_date > NOW() AND expiry_date <= NOW() + INTERVAL '90 days' AND deleted_at IS NULL;

-- Cards expiring soon
CREATE INDEX idx_operative_cards_expiring ON operative_cards(operative_id, expiry_date) WHERE expiry_date IS NOT NULL AND expiry_date > NOW() AND expiry_date <= NOW() + INTERVAL '30 days' AND deleted_at IS NULL;

-- Overdue inspections
CREATE INDEX idx_loler_overdue ON loler_examinations(plant_item_id) WHERE next_due_date < NOW() AND result != 'PASS' AND deleted_at IS NULL;

-- ============================================
-- GIN indexes for full-text and JSON search
-- ============================================

-- Companies: GIN index for name search (trigram)
CREATE INDEX idx_companies_name_gin ON companies USING gin(name gin_trgm_ops) WHERE deleted_at IS NULL;

-- Users: GIN index for name search
CREATE INDEX idx_users_name_gin ON users USING gin(first_name, last_name) WHERE deleted_at IS NULL;

-- Tender Documents: GIN index for document type search
CREATE INDEX idx_tender_documents_type_gin ON tender_documents USING gin(document_type) WHERE deleted_at IS NULL;

-- Material: GIN index for description search
CREATE INDEX idx_materials_description_gin ON materials USING gin(description gin_trgm_ops) WHERE deleted_at IS NULL;
