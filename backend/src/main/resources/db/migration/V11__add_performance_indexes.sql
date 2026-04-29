-- V11__add_performance_indexes.sql
-- Construction Resource Management System for UK Groundworks
-- Additional performance indexes for common query patterns

-- ============================================
-- Additional composite indexes for queries not covered in V6
-- ============================================

-- Documents: indexes for document management queries
CREATE INDEX IF NOT EXISTS idx_documents_entity ON documents(entity_type, entity_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_documents_type_created ON documents(type, created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_documents_uploaded_by ON documents(uploaded_by, created_at DESC) WHERE deleted_at IS NULL;

-- Commuted Sums: indexes for adoption case financial tracking
CREATE INDEX IF NOT EXISTS idx_commuted_sums_case_status ON commuted_sums(adoption_case_id, status) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_commuted_sums_status_dates ON commuted_sums(status, commencement_date) WHERE deleted_at IS NULL;

-- Commuted Sum Movements: indexes for financial ledger queries
CREATE INDEX IF NOT EXISTS idx_commuted_sum_movements_case_date ON commuted_sum_movements(commuted_sum_id, movement_date DESC) WHERE deleted_at IS NULL;

-- Bonds: indexes for bond tracking
CREATE INDEX IF NOT EXISTS idx_bonds_case_status ON bonds(adoption_case_id, status) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_bonds_expiry ON bonds(expiry_date, status) WHERE deleted_at IS NULL;

-- Snagging Items: indexes for defect/snagging tracking
CREATE INDEX IF NOT EXISTS idx_snagging_items_case_status ON snagging_items(adoption_case_id, status) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_snagging_items_priority_status ON snagging_items(priority, status) WHERE deleted_at IS NULL;

-- Adoption Stages: additional indexes for stage sequencing
CREATE INDEX IF NOT EXISTS idx_adoption_stages_case_status ON adoption_stages(adoption_case_id, status) WHERE deleted_at IS NULL;

-- Audit Logs: indexes for audit trail queries
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity ON audit_logs(entity_type, entity_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_audit_logs_user_date ON audit_logs(user_id, created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_audit_logs_action ON audit_logs(action, created_at DESC) WHERE deleted_at IS NULL;

-- Daily Pre-Use Checks: indexes for plant maintenance tracking
CREATE INDEX IF NOT EXISTS idx_daily_pre_use_checks_plant_date ON daily_pre_use_checks(plant_item_id, check_date DESC) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_daily_pre_use_checks_operator ON daily_pre_use_checks(operative_id, check_date DESC) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_daily_pre_use_checks_site_date ON daily_pre_use_checks(site_id, check_date DESC) WHERE deleted_at IS NULL;

-- CAT Scan Records: indexes for underground service scanning
CREATE INDEX IF NOT EXISTS idx_cat_scan_records_site_date ON cat_scan_records(site_id, scan_date DESC) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_cat_scan_records_status ON cat_scan_records(status, scan_date DESC) WHERE deleted_at IS NULL;

-- F10 Notifications: indexes for CDM notification tracking
CREATE INDEX IF EXISTS idx_f10_notifications_project_status ON f10_notifications(project_id, status) WHERE deleted_at IS NULL;
CREATE INDEX IF EXISTS idx_f10_notifications_expiry ON f10_notifications(expiry_date, status) WHERE deleted_at IS NULL;

-- CDM Register: indexes for construction phase plan tracking
CREATE INDEX IF NOT EXISTS idx_cdm_register_contract_status ON cdm_register(contract_id, status) WHERE deleted_at IS NULL;

-- ITP Schedules: indexes for inspection and test plan tracking
CREATE INDEX IF NOT EXISTS idx_itp_schedules_contract_status ON itp_schedules(contract_id, status) WHERE deleted_at IS NULL;

-- Sign-offs: indexes for quality sign-off tracking
CREATE INDEX IF NOT EXISTS idx_sign_offs_schedule_status ON sign_offs(itp_schedule_id, status) WHERE deleted_at IS NULL;

-- Inspection Records: indexes for inspection history
CREATE INDEX IF NOT EXISTS idx_inspection_records_schedule_item ON inspection_records(itp_schedule_id, itp_item_id) WHERE deleted_at IS NULL;

-- Defects: indexes for defect tracking
CREATE INDEX IF NOT EXISTS idx_defects_contract_status ON defects(contract_id, status) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_defects_priority_status ON defects(priority, status) WHERE deleted_at IS NULL;

-- Retention Movements: indexes for retention ledger queries
CREATE INDEX IF NOT EXISTS idx_retention_movements_ledger_date ON retention_movements(retention_ledger_id, movement_date DESC) WHERE deleted_at IS NULL;

-- Payment Certificates: indexes for payment cycle queries
CREATE INDEX IF NOT EXISTS idx_payment_certificates_contract_period ON payment_certificates(contract_id, certificate_period_start) WHERE deleted_at IS NULL;

-- Pay Less Notices: indexes for notice tracking
CREATE INDEX IF NOT EXISTS idx_pay_less_notices_certificate ON pay_less_notices(payment_certificate_id) WHERE deleted_at IS NULL;

-- ============================================
-- Partial indexes for frequently filtered queries
-- ============================================

-- Active contracts with value over threshold
CREATE INDEX IF NOT EXISTS idx_contracts_active_value ON contracts(id, contract_value) WHERE status = 'ACTIVE' AND deleted_at IS NULL;

-- Pending tender submissions
CREATE INDEX IF NOT EXISTS idx_tenders_pending ON tenders(id, tender_return_date) WHERE status = 'SUBMITTED' AND deleted_at IS NULL;

-- Overdue LOLER/PUWER inspections
CREATE INDEX IF NOT EXISTS idx_puwer_overdue ON puwer_inspections(plant_item_id) WHERE next_due_date < NOW() AND result != 'PASS' AND deleted_at IS NULL;

-- ============================================
-- BRIN indexes for time-series data (efficient for append-only tables)
-- ============================================

-- Audit logs: BRIN index for time-ordered log entries
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at_brin ON audit_logs USING brin(created_at) WITH (pages_per_range = 32) WHERE deleted_at IS NULL;

-- Timesheets: BRIN index for week-ordered entries
CREATE INDEX IF NOT EXISTS idx_timesheets_week_ending_brin ON timesheets USING brin(week_ending) WITH (pages_per_range = 4) WHERE deleted_at IS NULL;

-- ============================================
-- Additional GIN indexes for full-text search
-- ============================================

-- Sites: GIN index for address/postcode search
CREATE INDEX IF NOT EXISTS idx_sites_postcode_gin ON sites USING gin(postcode gin_trgm_ops) WHERE deleted_at IS NULL;

-- Contracts: GIN index for title/contract reference search
CREATE INDEX IF NOT EXISTS idx_contracts_title_gin ON contracts USING gin(title gin_trgm_ops) WHERE deleted_at IS NULL;
