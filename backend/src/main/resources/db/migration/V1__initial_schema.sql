-- V1__initial_schema.sql
-- Construction Resource Management System for UK Groundworks
-- Initial database schema

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Base audit/log tables (before main tables so FKs work)
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID,
    user_name VARCHAR(255),
    action VARCHAR(20) NOT NULL,
    entity_type VARCHAR(255),
    entity_id VARCHAR(255),
    before_state TEXT,
    after_state TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    sha256 VARCHAR(64),
    timestamp TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(50) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP,
    failed_login_attempts INTEGER DEFAULT 0,
    lockout_end TIMESTAMP,
    must_change_password BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP
);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);

-- Address embeddable table
CREATE TABLE addresses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    address_line3 VARCHAR(255),
    city VARCHAR(100),
    county VARCHAR(100),
    postcode VARCHAR(20),
    country VARCHAR(50) DEFAULT 'UK',
    grid_reference VARCHAR(50),
    latitude DECIMAL(10,7),
    longitude DECIMAL(10,7)
);

-- Companies
CREATE TABLE companies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    company_type VARCHAR(50) NOT NULL,
    registration_number VARCHAR(20),
    vat_number VARCHAR(20),
    sic_code VARCHAR(10),
    address_id UUID REFERENCES addresses(id),
    phone VARCHAR(30),
    email VARCHAR(255),
    website VARCHAR(255),
    companies_house_id VARCHAR(20),
    companies_house_data JSONB,
    hmrc_verification_ref VARCHAR(50),
    hmrc_verification_date DATE,
    hmrc_deduction_rate DECIMAL(5,2),
    cis_status VARCHAR(30),
    cop_verified BOOLEAN DEFAULT FALSE,
    bank_name VARCHAR(255),
    bank_sort_code VARCHAR(6),
    bank_account_number VARCHAR(8),
    bank_account_name VARCHAR(255),
    tax_address TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_companies_type ON companies(company_type);
CREATE INDEX idx_companies_cis_status ON companies(cis_status);
CREATE INDEX idx_companies_name ON companies(name);
CREATE INDEX idx_companies_reg_number ON companies(registration_number);

-- Contacts
CREATE TABLE contacts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES companies(id),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(30),
    mobile VARCHAR(30),
    job_title VARCHAR(100),
    is_primary BOOLEAN DEFAULT FALSE,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_contacts_company ON contacts(company_id);

-- Sites
CREATE TABLE sites (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    site_code VARCHAR(30),
    address_id UUID REFERENCES addresses(id),
    grid_reference VARCHAR(50),
    client_id UUID REFERENCES companies(id),
    status VARCHAR(30) DEFAULT 'TENDER',
    start_date DATE,
    completion_date DATE,
    estimated_completion_date DATE,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_sites_status ON sites(status);
CREATE INDEX idx_sites_client ON sites(client_id);

-- Tenders
CREATE TABLE tenders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tender_ref VARCHAR(30),
    title VARCHAR(500) NOT NULL,
    description TEXT,
    client_id UUID REFERENCES companies(id),
    site_id UUID REFERENCES sites(id),
    status VARCHAR(30) DEFAULT 'LEAD',
    contract_form VARCHAR(30),
    measurement_standard VARCHAR(30),
    value_range DECIMAL(15,2),
    win_probability INTEGER DEFAULT 0,
    tender_owner VARCHAR(255),
    tender_issued_date DATE,
    tender_return_date DATE,
    tender_value_submitted DECIMAL(15,2),
    loss_reason VARCHAR(30),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_tenders_status ON tenders(status);
CREATE INDEX idx_tenders_client ON tenders(client_id);
CREATE INDEX idx_tenders_return_date ON tenders(tender_return_date);

-- BoQ Items
CREATE TABLE boq_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tender_id UUID REFERENCES tenders(id),
    item_code VARCHAR(50),
    description TEXT NOT NULL,
    trade VARCHAR(100),
    unit VARCHAR(30),
    quantity DECIMAL(15,3),
    measured_quantity DECIMAL(15,3),
    unit_rate DECIMAL(15,4),
    total_value DECIMAL(15,2),
    composite_id UUID,
    is_locked BOOLEAN DEFAULT FALSE,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0
);
CREATE INDEX idx_boq_tender ON boq_items(tender_id);
CREATE INDEX idx_boq_trade ON boq_items(trade);

-- Tender Documents
CREATE TABLE tender_documents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tender_id UUID REFERENCES tenders(id),
    document_name VARCHAR(255) NOT NULL,
    document_type VARCHAR(30),
    cde_status VARCHAR(20) DEFAULT 'WIP',
    version VARCHAR(20) DEFAULT '1.0',
    uploaded_at TIMESTAMP NOT NULL DEFAULT NOW(),
    file_ref VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    lock_version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_tender_documents_tender ON tender_documents(tender_id);

-- Contracts
CREATE TABLE contracts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    contract_ref VARCHAR(30) UNIQUE NOT NULL,
    title VARCHAR(500) NOT NULL,
    client_id UUID REFERENCES companies(id),
    site_id UUID REFERENCES sites(id),
    tender_id UUID REFERENCES tenders(id),
    contract_form VARCHAR(30),
    measurement_standard VARCHAR(30),
    contract_value DECIMAL(15,2),
    retention_percent DECIMAL(5,2) DEFAULT 5.0,
    retention_reduction_percent DECIMAL(5,2) DEFAULT 2.5,
    practical_completion_defects_period_months INTEGER DEFAULT 12,
    payment_terms_days INTEGER DEFAULT 30,
    final_date_for_payment_offset_days INTEGER DEFAULT 14,
    pay_less_notice_prescribed_period_days INTEGER DEFAULT 7,
    bond_percent DECIMAL(5,2),
    bond_value DECIMAL(15,2),
    bond_ref VARCHAR(50),
    nec4_options TEXT,
    nec4_pricing_mechanism VARCHAR(30),
    status VARCHAR(30) DEFAULT 'DRAFT',
    start_date DATE,
    end_date DATE,
    defects_end_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_contracts_status ON contracts(status);
CREATE INDEX idx_contracts_client ON contracts(client_id);
CREATE INDEX idx_contracts_site ON contracts(site_id);

-- Variations
CREATE TABLE variations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    contract_id UUID NOT NULL REFERENCES contracts(id),
    variation_ref VARCHAR(30),
    type VARCHAR(30) NOT NULL,
    description TEXT NOT NULL,
    original_value DECIMAL(15,2),
    agreed_value DECIMAL(15,2),
    status VARCHAR(30) DEFAULT 'PROPOSED',
    notified_date DATE,
    agreed_date DATE,
    instructions_ref VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_variations_contract ON variations(contract_id);
CREATE INDEX idx_variations_status ON variations(status);

-- Applications for Payment
CREATE TABLE applications_for_payment (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    application_ref VARCHAR(30) NOT NULL,
    contract_id UUID NOT NULL REFERENCES contracts(id),
    application_number INTEGER NOT NULL,
    application_period_start DATE NOT NULL,
    application_period_end DATE NOT NULL,
    due_date DATE,
    value_of_works DECIMAL(15,2),
    retention DECIMAL(15,2),
    gross_value DECIMAL(15,2),
    status VARCHAR(30) DEFAULT 'DRAFT',
    submitted_date DATE,
    payer_ref VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_applications_contract ON applications_for_payment(contract_id);
CREATE INDEX idx_applications_status ON applications_for_payment(status);
CREATE INDEX idx_applications_due_date ON applications_for_payment(due_date);

-- Payment Notices
CREATE TABLE payment_notices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    application_id UUID NOT NULL REFERENCES applications_for_payment(id),
    notice_type VARCHAR(30) NOT NULL,
    issued_on TIMESTAMP NOT NULL,
    sum_considered_due DECIMAL(15,2),
    currency VARCHAR(3) DEFAULT 'GBP',
    basis_of_calculation TEXT,
    document_ref VARCHAR(500),
    sha256 VARCHAR(64),
    final_date_for_payment TIMESTAMP,
    deadline_for_pay_less_notice TIMESTAMP,
    audit_log_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_payment_notices_application ON payment_notices(application_id);

-- Pay Less Notices
CREATE TABLE pay_less_notices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    application_id UUID NOT NULL REFERENCES applications_for_payment(id),
    issued_on TIMESTAMP NOT NULL,
    sum_considered_due DECIMAL(15,2),
    currency VARCHAR(3) DEFAULT 'GBP',
    basis_of_calculation TEXT,
    document_ref VARCHAR(500),
    sha256 VARCHAR(64),
    audit_log_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);

-- Payment Certificates
CREATE TABLE payment_certificates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    application_id UUID NOT NULL REFERENCES applications_for_payment(id),
    certificate_number VARCHAR(30) NOT NULL,
    gross_certified DECIMAL(15,2),
    retention_held DECIMAL(15,2),
    net_certified DECIMAL(15,2),
    certified_date DATE,
    certificate_ref VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);

-- Retention Ledger
CREATE TABLE retention_ledger (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    contract_id UUID NOT NULL REFERENCES contracts(id),
    total_retention DECIMAL(15,2) DEFAULT 0,
    released_at_pc DECIMAL(15,2) DEFAULT 0,
    released_at_defects DECIMAL(15,2) DEFAULT 0,
    balance DECIMAL(15,2) DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);

-- Retention Movements
CREATE TABLE retention_movements (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    retention_ledger_id UUID NOT NULL REFERENCES retention_ledger(id),
    movement_date DATE NOT NULL,
    type VARCHAR(30) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    application_id UUID REFERENCES applications_for_payment(id),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0
);

-- Subcontractors and CIS
CREATE TABLE cis_verifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID NOT NULL REFERENCES companies(id),
    verification_ref VARCHAR(50),
    rate DECIMAL(5,2),
    status VARCHAR(30) DEFAULT 'PENDING',
    verified_at DATE,
    expires_at DATE,
    verification_data JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_cis_verifications_company ON cis_verifications(company_id);
CREATE INDEX idx_cis_verifications_status ON cis_verifications(status);

CREATE TABLE cis_returns (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tax_month VARCHAR(7) NOT NULL,
    submitted_at TIMESTAMP,
    submitted_by UUID REFERENCES users(id),
    hmrc_receipt_ref VARCHAR(100),
    status VARCHAR(30) DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_cis_returns_tax_month ON cis_returns(tax_month);

CREATE TABLE cis_return_lines (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cis_return_id UUID NOT NULL REFERENCES cis_returns(id),
    subcontractor_id UUID NOT NULL REFERENCES companies(id),
    gross_paid DECIMAL(15,2),
    deduction DECIMAL(15,2),
    net_paid DECIMAL(15,2),
    cis_rate DECIMAL(5,2),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_cis_return_lines_return ON cis_return_lines(cis_return_id);

-- Operatives
CREATE TABLE operatives (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_ref VARCHAR(30),
    company_id UUID NOT NULL REFERENCES companies(id),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE,
    gender VARCHAR(10),
    nationality VARCHAR(50),
    ni_number VARCHAR(9),
    utr VARCHAR(20),
    right_to_work_expiry DATE,
    right_to_work_doc_type VARCHAR(50),
    passport_number VARCHAR(30),
    bank_sort_code VARCHAR(6),
    bank_account_number VARCHAR(8),
    employment_status VARCHAR(20) DEFAULT 'PAYE',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_operatives_company ON operatives(company_id);
CREATE INDEX idx_operatives_status ON operatives(status);

CREATE TABLE operative_cards (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    operative_id UUID NOT NULL REFERENCES operatives(id),
    card_type VARCHAR(20) NOT NULL,
    scheme VARCHAR(100),
    card_number VARCHAR(50) NOT NULL,
    expiry_date DATE,
    photo_url VARCHAR(500),
    is_verified BOOLEAN DEFAULT FALSE,
    last_checked_at TIMESTAMP,
    competency_ref VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_operative_cards_operative ON operative_cards(operative_id);
CREATE INDEX idx_operative_cards_expiry ON operative_cards(expiry_date);

CREATE TABLE qualifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    operative_id UUID NOT NULL REFERENCES operatives(id),
    qualification_type VARCHAR(30) NOT NULL,
    level VARCHAR(20),
    awarding_body VARCHAR(100),
    certificate_number VARCHAR(100),
    achieved_date DATE,
    expiry_date DATE,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_qualifications_operative ON qualifications(operative_id);
CREATE INDEX idx_qualifications_type ON qualifications(qualification_type);

CREATE TABLE inductions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    operative_id UUID NOT NULL REFERENCES operatives(id),
    site_id UUID REFERENCES sites(id),
    inducted_at TIMESTAMP NOT NULL DEFAULT NOW(),
    valid_until TIMESTAMP,
    trainer VARCHAR(255),
    method VARCHAR(20),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_inductions_operative ON inductions(operative_id);
CREATE INDEX idx_inductions_validity ON inductions(valid_until);

CREATE TABLE site_sign_ons (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    operative_id UUID NOT NULL REFERENCES operatives(id),
    site_id UUID NOT NULL REFERENCES sites(id),
    sign_on_time TIMESTAMP NOT NULL,
    sign_off_time TIMESTAMP,
    plant_used VARCHAR(255),
    plant_hours DECIMAL(5,2),
    daywork_hours DECIMAL(5,2),
    notes TEXT,
    latitude DECIMAL(10,7),
    longitude DECIMAL(10,7),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0
);
CREATE INDEX idx_site_sign_ons_operative ON site_sign_ons(operative_id);
CREATE INDEX idx_site_sign_ons_site ON site_sign_ons(site_id);

CREATE TABLE timesheets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    operative_id UUID NOT NULL REFERENCES operatives(id),
    site_id UUID REFERENCES sites(id),
    week_ending DATE NOT NULL,
    regular_hours DECIMAL(6,2),
    overtime_hours DECIMAL(6,2),
    holiday_hours DECIMAL(6,2),
    sick_hours DECIMAL(6,2),
    notes TEXT,
    exported BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_timesheets_operative ON timesheets(operative_id);
CREATE INDEX idx_timesheets_week ON timesheets(week_ending);

-- Plant
CREATE TABLE plant_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    plant_ref VARCHAR(30) UNIQUE NOT NULL,
    serial_number VARCHAR(50),
    description VARCHAR(255),
    make VARCHAR(100),
    model VARCHAR(100),
    plant_year INTEGER,
    category VARCHAR(30),
    weight_kg DECIMAL(8,2),
    hire_status VARCHAR(20) DEFAULT 'OWNED',
    supplier_id UUID REFERENCES companies(id),
    telematics_id VARCHAR(100),
    quick_hitch_type VARCHAR(50),
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    daily_hire_rate DECIMAL(8,2),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_plant_items_status ON plant_items(status);
CREATE INDEX idx_plant_items_category ON plant_items(category);
CREATE INDEX idx_plant_items_ref ON plant_items(plant_ref);

CREATE TABLE loler_examinations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    plant_item_id UUID NOT NULL REFERENCES plant_items(id),
    examination_date DATE NOT NULL,
    next_due_date DATE NOT NULL,
    examiner VARCHAR(255),
    examiner_company VARCHAR(255),
    result VARCHAR(30) NOT NULL,
    report_ref VARCHAR(100),
    notes TEXT,
    document_ref VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_loler_plant ON loler_examinations(plant_item_id);
CREATE INDEX idx_loler_next_due ON loler_examinations(next_due_date);

CREATE TABLE puwer_inspections (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    plant_item_id UUID NOT NULL REFERENCES plant_items(id),
    inspection_date DATE NOT NULL,
    next_due_date DATE,
    inspector VARCHAR(255),
    result VARCHAR(30),
    notes TEXT,
    document_ref VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_puwer_plant ON puwer_inspections(plant_item_id);

CREATE TABLE daily_pre_use_checks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    plant_item_id UUID NOT NULL REFERENCES plant_items(id),
    operative_id UUID REFERENCES operatives(id),
    site_id UUID REFERENCES sites(id),
    check_date DATE NOT NULL,
    result VARCHAR(30),
    defects_noted TEXT,
    repaired_before_use BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP,
    CONSTRAINT daily_pre_use_checks_pkey PRIMARY KEY (plant_item_id, operative_id, site_id, check_date)
);

CREATE TABLE hire_records (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    plant_item_id UUID NOT NULL REFERENCES plant_items(id),
    supplier_id UUID REFERENCES companies(id),
    on_hire_date DATE,
    off_hire_date DATE,
    daily_rate DECIMAL(8,2),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_hire_records_plant ON hire_records(plant_item_id);

CREATE TABLE plant_allocations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    plant_item_id UUID NOT NULL REFERENCES plant_items(id),
    operative_id UUID REFERENCES operatives(id),
    site_id UUID REFERENCES sites(id),
    start_date DATE NOT NULL,
    end_date DATE,
    status VARCHAR(20) DEFAULT 'PLANNED',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_plant_allocations_plant ON plant_allocations(plant_item_id);
CREATE INDEX idx_plant_allocations_site ON plant_allocations(site_id);
CREATE INDEX idx_plant_allocations_dates ON plant_allocations(start_date, end_date);

-- Materials
CREATE TABLE materials (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    material_code VARCHAR(30) UNIQUE NOT NULL,
    description VARCHAR(255) NOT NULL,
    trade VARCHAR(100),
    unit VARCHAR(20),
    category VARCHAR(100),
    supplier_id UUID REFERENCES companies(id),
    standard_rate DECIMAL(10,4),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_materials_code ON materials(material_code);
CREATE INDEX idx_materials_trade ON materials(trade);

CREATE TABLE supplier_price_list_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    material_id UUID NOT NULL REFERENCES materials(id),
    supplier_id UUID NOT NULL REFERENCES companies(id),
    unit_price DECIMAL(10,4),
    unit VARCHAR(20),
    valid_from DATE,
    valid_to DATE,
    lead_time_days INTEGER,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);

CREATE TABLE purchase_requisitions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    requisition_ref VARCHAR(30) UNIQUE NOT NULL,
    requested_by UUID NOT NULL REFERENCES users(id),
    site_id UUID REFERENCES sites(id),
    contract_id UUID REFERENCES contracts(id),
    status VARCHAR(30) DEFAULT 'DRAFT',
    required_date DATE,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_requisitions_status ON purchase_requisitions(status);

CREATE TABLE purchase_requisition_lines (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    requisition_id UUID NOT NULL REFERENCES purchase_requisitions(id),
    material_id UUID REFERENCES materials(id),
    description VARCHAR(255),
    quantity DECIMAL(12,3),
    unit VARCHAR(20),
    estimated_unit_price DECIMAL(10,4),
    estimated_total DECIMAL(15,2),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);

CREATE TABLE purchase_orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    purchase_order_ref VARCHAR(30) UNIQUE NOT NULL,
    order_number VARCHAR(30),
    supplier_id UUID NOT NULL REFERENCES companies(id),
    site_id UUID REFERENCES sites(id),
    requisition_id UUID REFERENCES purchase_requisitions(id),
    status VARCHAR(30) DEFAULT 'DRAFT',
    order_date DATE,
    delivery_date DATE,
    net_value DECIMAL(15,2),
    vat_value DECIMAL(15,2),
    total_value DECIMAL(15,2),
    terms TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_purchase_orders_status ON purchase_orders(status);
CREATE INDEX idx_purchase_orders_supplier ON purchase_orders(supplier_id);

CREATE TABLE purchase_order_lines (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL REFERENCES purchase_orders(id),
    material_id UUID REFERENCES materials(id),
    description VARCHAR(255),
    quantity DECIMAL(12,3),
    unit VARCHAR(20),
    unit_price DECIMAL(10,4),
    net_value DECIMAL(15,2),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);

CREATE TABLE delivery_notes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    delivery_note_ref VARCHAR(30) NOT NULL,
    order_id UUID REFERENCES purchase_orders(id),
    supplier_id UUID REFERENCES companies(id),
    site_id UUID REFERENCES sites(id),
    delivery_date DATE,
    delivery_time TIME,
    delivered_by VARCHAR(255),
    vehicle_reg VARCHAR(20),
    status VARCHAR(30) DEFAULT 'EXPECTED',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_delivery_notes_status ON delivery_notes(status);

-- Concrete Tickets
CREATE TABLE concrete_tickets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    delivery_note_id UUID REFERENCES delivery_notes(id),
    ticket_number VARCHAR(50),
    batch_number VARCHAR(50),
    truck_ref VARCHAR(50),
    batch_time TIME,
    ordered_volume DECIMAL(8,3),
    delivered_volume DECIMAL(8,3),
    bs8500_designation VARCHAR(30),
    exposure_class VARCHAR(20),
    slump_target VARCHAR(10),
    slump_on_site VARCHAR(10),
    water_added_on_site BOOLEAN DEFAULT FALSE,
    time_arrival TIME,
    time_discharge TIME,
    temperature DECIMAL(4,1),
    discharge_rate VARCHAR(20),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);

CREATE TABLE cube_samples (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    concrete_ticket_id UUID REFERENCES concrete_tickets(id),
    sample_ref VARCHAR(30) NOT NULL,
    cast_date DATE,
    cast_time TIME,
    cube_set INTEGER DEFAULT 1,
    batch_time TIME,
    truck_ref VARCHAR(50),
    lab_destination VARCHAR(255),
    mpa_7day DECIMAL(5,2),
    mpa_28day DECIMAL(5,2),
    result VARCHAR(20) DEFAULT 'PENDING',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_cube_samples_ticket ON cube_samples(concrete_ticket_id);

CREATE TABLE muckaway_tickets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    delivery_note_id UUID REFERENCES delivery_notes(id),
    ticket_number VARCHAR(50),
    vehicle_reg VARCHAR(20),
    waste_carrier_licence_ref VARCHAR(50),
    permitted_facility VARCHAR(255),
    facility_permit_ref VARCHAR(50),
    waste_type VARCHAR(30),
    load_out_weight DECIMAL(8,2),
    load_in_weight DECIMAL(8,2),
    net_weight DECIMAL(8,2),
    landfill_tax_rate DECIMAL(8,2),
    tax_due DECIMAL(10,2),
    transfer_note_ref VARCHAR(50),
    disposal_cost DECIMAL(10,2),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_muckaway_tickets_date ON muckaway_tickets(created_at);

-- Health & Safety
CREATE TABLE f10_notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    contract_id UUID NOT NULL REFERENCES contracts(id),
    notification_number VARCHAR(50),
    submitted_date DATE,
    confirmation_number VARCHAR(100),
    more_than_30_days BOOLEAN DEFAULT FALSE,
    more_than_500_person_days BOOLEAN DEFAULT FALSE,
    construction_start_date DATE,
    construction_end_date DATE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);

CREATE TABLE construction_phase_plans (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    contract_id UUID NOT NULL REFERENCES contracts(id),
    version VARCHAR(10) DEFAULT '1.0',
    status VARCHAR(20) DEFAULT 'DRAFT',
    approved_by VARCHAR(255),
    approved_date DATE,
    document_ref VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    lock_version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);

CREATE TABLE rams_templates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    trade VARCHAR(100),
    risk_assessment TEXT,
    method_statement TEXT,
    ppe_required VARCHAR(255),
    frequency_days INTEGER DEFAULT 90,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_rams_templates_trade ON rams_templates(trade);

CREATE TABLE rams_documents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    contract_id UUID NOT NULL REFERENCES contracts(id),
    template_id UUID REFERENCES rams_templates(id),
    title VARCHAR(255),
    version VARCHAR(10) DEFAULT '1.0',
    valid_from DATE,
    valid_until DATE,
    status VARCHAR(20) DEFAULT 'DRAFT',
    document_ref VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    lock_version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_rams_documents_contract ON rams_documents(contract_id);

CREATE TABLE rams_sign_ons (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    rams_document_id UUID NOT NULL REFERENCES rams_documents(id),
    operative_id UUID NOT NULL REFERENCES operatives(id),
    site_id UUID REFERENCES sites(id),
    signed_at TIMESTAMP NOT NULL DEFAULT NOW(),
    valid_until TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_rams_sign_ons_document ON rams_sign_ons(rams_document_id);
CREATE INDEX idx_rams_sign_ons_validity ON rams_sign_ons(valid_until);

CREATE TABLE permits_to_dig (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    site_id UUID NOT NULL REFERENCES sites(id),
    permit_number VARCHAR(30),
    works_description TEXT,
    start_date DATE,
    end_date DATE,
    status VARCHAR(20) DEFAULT 'DRAFT',
    lsbud_reference VARCHAR(100),
    trial_hole_count INTEGER,
    trial_hole_photo_ref VARCHAR(500),
    cat_scan_ref VARCHAR(500),
    cat_scan_date DATE,
    cat_scan_device_serial VARCHAR(100),
    cat_scan_last_calibration_date DATE,
    supervisor_approval_ref VARCHAR(100),
    supervisor_approval_date DATE,
    document_ref VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_permits_site ON permits_to_dig(site_id);
CREATE INDEX idx_permits_status ON permits_to_dig(status);

CREATE TABLE cat_scan_records (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    permit_id UUID NOT NULL REFERENCES permits_to_dig(id),
    scan_date TIMESTAMP NOT NULL,
    utility VARCHAR(30) NOT NULL,
    depth DECIMAL(6,2),
    marked_by VARCHAR(255),
    photo_ref VARCHAR(500),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);

CREATE TABLE incident_reports (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    site_id UUID NOT NULL REFERENCES sites(id),
    operative_id UUID REFERENCES operatives(id),
    report_number VARCHAR(30),
    incident_date TIMESTAMP NOT NULL,
    location_description VARCHAR(255),
    incident_type VARCHAR(30),
    severity VARCHAR(20),
    description TEXT,
    immediate_actions TEXT,
    rid_dor_notifiable BOOLEAN DEFAULT FALSE,
    reported_to_hse BOOLEAN DEFAULT FALSE,
    hse_ref VARCHAR(50),
    investigation_outcome TEXT,
    document_refs TEXT,
    status VARCHAR(20) DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_incident_reports_site ON incident_reports(site_id);
CREATE INDEX idx_incident_reports_type ON incident_reports(incident_type);
CREATE INDEX idx_incident_reports_severity ON incident_reports(severity);

-- Adoption Cases
CREATE TABLE adoption_cases (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    case_ref VARCHAR(30) UNIQUE NOT NULL,
    adoption_type VARCHAR(30) NOT NULL,
    contract_id UUID REFERENCES contracts(id),
    client_id UUID REFERENCES companies(id),
    local_authority_or_water_authority_id UUID REFERENCES companies(id),
    technical_approval_ref VARCHAR(100),
    design_check_fees DECIMAL(15,2),
    supervision_fees DECIMAL(15,2),
    commuted_sum_total DECIMAL(15,2),
    commuted_sum_paid DECIMAL(15,2),
    maintenance_period_months INTEGER,
    commencement_date DATE,
    maintenance_end_date DATE,
    status VARCHAR(30) DEFAULT 'PRE_APP',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_adoption_cases_type ON adoption_cases(adoption_type);
CREATE INDEX idx_adoption_cases_status ON adoption_cases(status);
CREATE INDEX idx_adoption_cases_maintenance ON adoption_cases(maintenance_end_date);

CREATE TABLE bonds (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    adoption_case_id UUID NOT NULL REFERENCES adoption_cases(id),
    bond_number VARCHAR(50),
    bond_type VARCHAR(30),
    issuing_surety_id UUID REFERENCES companies(id),
    bond_value DECIMAL(15,2),
    issue_date DATE,
    expiry_date DATE,
    release_conditions TEXT,
    release_date DATE,
    status VARCHAR(30) DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);

CREATE TABLE adoption_stages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    adoption_case_id UUID NOT NULL REFERENCES adoption_cases(id),
    stage_name VARCHAR(100) NOT NULL,
    stage_order INTEGER NOT NULL,
    planned_date DATE,
    actual_date DATE,
    status VARCHAR(20) DEFAULT 'PENDING',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_adoption_stages_case ON adoption_stages(adoption_case_id);

CREATE TABLE commuted_sum_movements (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    adoption_case_id UUID NOT NULL REFERENCES adoption_cases(id),
    movement_date DATE NOT NULL,
    type VARCHAR(30) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    reason VARCHAR(255),
    document_ref VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    version BIGINT DEFAULT 0,
    deleted_at TIMESTAMP
);

-- Create default users
INSERT INTO users (id, username, email, password, first_name, last_name, role, enabled, must_change_password, created_at, updated_at)
VALUES 
('00000000-0000-0000-0000-000000000001', 'admin', 'admin@crms.local', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5f7.MQD7VVZ2K', 'System', 'Administrator', 'ROLE_ADMIN', true, false, NOW(), NOW()),
('00000000-0000-0000-0000-000000000002', 'ops_director', 'ops@crms.local', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5f7.MQD7VVZ2K', 'Operations', 'Director', 'ROLE_OPS_DIRECTOR', true, false, NOW(), NOW()),
('00000000-0000-0000-0000-000000000003', 'contracts_mgr', 'contracts@crms.local', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5f7.MQD7VVZ2K', 'Contracts', 'Manager', 'ROLE_CONTRACTS_MANAGER', true, false, NOW(), NOW()),
('00000000-0000-0000-0000-000000000004', 'qs', 'qs@crms.local', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5f7.MQD7VVZ2K', 'Senior', 'Quantity Surveyor', 'ROLE_QS', true, false, NOW(), NOW()),
('00000000-0000-0000-0000-000000000005', 'site_agent', 'site@crms.local', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5f7.MQD7VVZ2K', 'Site', 'Agent', 'ROLE_SITE_AGENT', true, false, NOW(), NOW()),
('00000000-0000-0000-0000-000000000006', 'engineer', 'engineer@crms.local', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5f7.MQD7VVZ2K', 'Site', 'Engineer', 'ROLE_ENGINEER', true, false, NOW(), NOW()),
('00000000-0000-0000-0000-000000000007', 'plant_mgr', 'plant@crms.local', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5f7.MQD7VVZ2K', 'Plant', 'Manager', 'ROLE_PLANT_MANAGER', true, false, NOW(), NOW()),
('00000000-0000-0000-0000-000000000008', 'buyer', 'buyer@crms.local', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5f7.MQD7VVZ2K', 'Procurement', 'Buyer', 'ROLE_BUYER', true, false, NOW(), NOW()),
('00000000-0000-0000-0000-000000000009', 'finance', 'finance@crms.local', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5f7.MQD7VVZ2K', 'Finance', 'Manager', 'ROLE_FINANCE', true, false, NOW(), NOW()),
('00000000-0000-0000-0000-000000000010', 'estimator', 'estimator@crms.local', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5f7.MQD7VVZ2K', 'Estimator', 'Lead', 'ROLE_ESTIMATOR', true, false, NOW(), NOW());
