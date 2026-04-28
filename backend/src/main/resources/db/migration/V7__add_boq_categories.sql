-- V7__add_boq_categories.sql
-- Construction Resource Management System for UK Groundworks
-- BoQ Categories and Standard Items Library for tendering
-- NRM2 and CESMM4 coded items for UK groundwork contractors

-- ============================================
-- BOQ CATEGORIES
-- ============================================

CREATE TABLE boq_categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    parent_id UUID REFERENCES boq_categories(id),
    measurement_standard VARCHAR(20) DEFAULT 'CESMM4', -- CESMM4, NRM2, SMM7
    sort_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_boq_categories_code ON boq_categories(code);
CREATE INDEX idx_boq_categories_parent ON boq_categories(parent_id);
CREATE INDEX idx_boq_categories_active ON boq_categories(is_active) WHERE is_active = TRUE;

-- ============================================
-- BOQ STANDARD ITEMS (Library)
-- ============================================

CREATE TABLE boq_standard_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    category_id UUID NOT NULL REFERENCES boq_categories(id),
    item_code VARCHAR(30) NOT NULL,
    description TEXT NOT NULL,
    unit VARCHAR(20) NOT NULL,
    work_category VARCHAR(100), -- e.g., 'Excavation', 'Drainage', 'Concrete'
    material_cost DECIMAL(10,4), -- default material cost
    labour_cost DECIMAL(10,4), -- default labour cost
    plant_cost DECIMAL(10,4), -- default plant cost
    total_cost DECIMAL(10,4), -- calculated total
    composite_rate DECIMAL(10,4), -- all-in composite rate if applicable
    notes TEXT,
    nrm2_code VARCHAR(20), -- NRM2 measurement code
    cesmm4_class VARCHAR(10), -- CESMM4 class
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id),
    version BIGINT DEFAULT 0,
    UNIQUE(category_id, item_code)
);

CREATE INDEX idx_boq_standard_items_code ON boq_standard_items(item_code);
CREATE INDEX idx_boq_standard_items_category ON boq_standard_items(category_id);
CREATE INDEX idx_boq_standard_items_work_category ON boq_standard_items(work_category);

-- ============================================
-- SEED DATA: BOQ CATEGORIES
-- ============================================

-- Level 1 Categories
INSERT INTO boq_categories (id, code, name, description, measurement_standard, sort_order) VALUES
-- Main groundwork categories
('c0000000-0000-0000-0000-000000000001', 'DIV01', 'Site Clearance', 'Site clearance, demolition and site preparation', 'CESMM4', 10),
('c0000000-0000-0000-0000-000000000002', 'DIV02', 'Earthworks', 'Excavation, filling and earthworks', 'CESMM4', 20),
('c0000000-0000-0000-0000-000000000003', 'DIV03', 'Drainage', 'Drainage and sewerage works', 'CESMM4', 30),
('c0000000-0000-0000-0000-000000000004', 'DIV04', 'Concrete', 'In situ concrete works', 'CESMM4', 40),
('c0000000-0000-0000-0000-000000000005', 'DIV05', 'Foundations', 'Foundation works including trench fill', 'NRM2', 50),
('c0000000-0000-0000-0000-000000000006', 'DIV06', 'Roads and Pavings', 'Road construction, pavings and hard standings', 'CESMM4', 60),
('c0000000-0000-0000-0000-000000000007', 'DIV07', 'Masonry', 'Brickwork, blockwork and masonry', 'NRM2', 70),
('c0000000-0000-0000-0000-000000000008', 'DIV08', 'Structural Steelwork', 'Structural steel and metalwork', 'NRM2', 80),
('c0000000-0000-0000-0000-000000000009', 'DIV09', 'Ducting and Utilities', 'Service ducts and utility installations', 'CESMM4', 90),
('c0000000-0000-0000-0000-000000000010', 'DIV10', 'Attenuation', 'Storm water attenuation systems', 'CESMM4', 100),
('c0000000-0000-0000-0000-000000000011', 'DIV11', 'Fencing and Barriers', 'Fencing, gates and barriers', 'NRM2', 110),
('c0000000-0000-0000-0000-000000000012', 'DIV12', 'Landscaping', 'Soft landscaping and planting', 'NRM2', 120),
('c0000000-0000-0000-0000-000000000013', 'DIV13', ' Ancillary', 'Ancillary items and sundries', 'CESMM4', 130);

-- Level 2 Categories (Site Clearance)
INSERT INTO boq_categories (id, code, name, description, parent_id, sort_order) VALUES
('c1000000-0000-0000-0000-000000000001', 'DIV01-01', 'Demolition', 'Building and structure demolition', 'c0000000-0000-0000-0000-000000000001', 11),
('c1000000-0000-0000-0000-000000000002', 'DIV01-02', 'Soft Strip', 'Internal soft stripping works', 'c0000000-0000-0000-0000-000000000001', 12);

-- Level 2 Categories (Earthworks)
INSERT INTO boq_categories (id, code, name, description, parent_id, sort_order) VALUES
('c2000000-0000-0000-0000-000000000001', 'DIV02-01', 'Muckaway', 'Excavation and disposal off site', 'c0000000-0000-0000-0000-000000000002', 21),
('c2000000-0000-0000-0000-000000000002', 'DIV02-02', 'Cut and Fill', 'Excavation and reuse on site', 'c0000000-0000-0000-0000-000000000002', 22),
('c2000000-0000-0000-0000-000000000003', 'DIV02-03', 'Sub-base', 'Granular sub-base materials', 'c0000000-0000-0000-0000-000000000002', 23);

-- Level 2 Categories (Drainage)
INSERT INTO boq_categories (id, code, name, description, parent_id, sort_order) VALUES
('c3000000-0000-0000-0000-000000000001', 'DIV03-01', 'Pipes', 'Drainage pipes by diameter', 'c0000000-0000-0000-0000-000000000003', 31),
('c3000000-0000-0000-0000-000000000002', 'DIV03-02', 'Chambers', 'Manholes and chambers', 'c0000000-0000-0000-0000-000000000003', 32),
('c3000000-0000-0000-0000-000000000003', 'DIV03-03', 'Gullies', 'Gullies and rodding eyes', 'c0000000-0000-0000-0000-000000000003', 33),
('c3000000-0000-0000-0000-000000000004', 'DIV03-04', 'Bedding', 'Pipe bedding and surround', 'c0000000-0000-0000-0000-000000000003', 34);

-- Level 2 Categories (Concrete)
INSERT INTO boq_categories (id, code, name, description, parent_id, sort_order) VALUES
('c4000000-0000-0000-0000-000000000001', 'DIV04-01', 'RC Concrete', 'Reinforced concrete by designation', 'c0000000-0000-0000-0000-000000000004', 41),
('c4000000-0000-0000-0000-000000000002', 'DIV04-02', 'FND Concrete', 'Foundation concrete by designation', 'c0000000-0000-0000-0000-000000000004', 42),
('c4000000-0000-0000-0000-000000000003', 'DIV04-03', 'Reinforcement', 'Steel reinforcement', 'c0000000-0000-0000-0000-000000000004', 43),
('c4000000-0000-0000-0000-000000000004', 'DIV04-04', 'Formwork', 'Formwork and temporary works', 'c0000000-0000-0000-0000-000000000004', 44),
('c4000000-0000-0000-0000-000000000005', 'DIV04-05', 'Slabs', 'Floor slabs by thickness', 'c0000000-0000-0000-0000-000000000004', 45);

-- Level 2 Categories (Roads)
INSERT INTO boq_categories (id, code, name, description, parent_id, sort_order) VALUES
('c6000000-0000-0000-0000-000000000001', 'DIV06-01', 'Flexible Pavement', 'Flexible pavement construction', 'c0000000-0000-0000-0000-000000000006', 61),
('c6000000-0000-0000-0000-000000000002', 'DIV06-02', 'Kerbs', 'Kerbs and edging', 'c0000000-0000-0000-0000-000000000006', 62),
('c6000000-0000-0000-0000-000000000003', 'DIV06-03', 'Paving', 'Paved areas and footways', 'c0000000-0000-0000-0000-000000000006', 63);

-- ============================================
-- SEED DATA: BOQ STANDARD ITEMS
-- ============================================

-- Muckaway Items (DIV02-01)
INSERT INTO boq_standard_items (category_id, item_code, description, unit, work_category, material_cost, labour_cost, plant_cost, nrm2_code, cesmm4_class) VALUES
('c2000000-0000-0000-0000-000000000001', 'CES-DIS-010', 'Excavation and disposal of clean inert material - Easement width not exceeding 1.5m', 'm³', 'Muckaway', 0, 18.50, 22.00, 'E.10.10', 'D'),
('c2000000-0000-0000-0000-000000000001', 'CES-DIS-011', 'Excavation and disposal of clean inert material - General site areas', 'm³', 'Muckaway', 0, 12.00, 15.00, 'E.10.10', 'D'),
('c2000000-0000-0000-0000-000000000001', 'CES-DIS-012', 'Excavation and disposal of clean inert material - Road/carriageway', 'm³', 'Muckaway', 0, 22.00, 28.00, 'E.10.10', 'D'),
('c2000000-0000-0000-0000-000000000001', 'CES-DIS-013', 'Excavation and disposal of contaminated inert material - Classified', 'm³', 'Muckaway', 0, 35.00, 40.00, 'E.10.15', 'D'),
('c2000000-0000-0000-0000-000000000001', 'CES-DIS-014', 'Excavation and disposal of hazardous material - Licensed facility', 'm³', 'Muckaway', 0, 85.00, 90.00, 'E.10.20', 'D'),
('c2000000-0000-0000-0000-000000000001', 'CES-DIS-015', 'Excavation and disposal of unsuitable material - Soft/spoil', 'm³', 'Muckaway', 0, 25.00, 32.00, 'E.10.25', 'D');

-- Drainage Pipes (DIV03-01)
INSERT INTO boq_standard_items (category_id, item_code, description, unit, work_category, material_cost, labour_cost, plant_cost, nrm2_code, cesmm4_class) VALUES
('c3000000-0000-0000-0000-000000000001', 'CES-DRN-100A', 'Supply and lay vitrified clay pipe 100mm - trench not exceeding 1.0m depth', 'm', 'Drainage', 12.50, 18.00, 8.00, 'H.10.10', 'E'),
('c3000000-0000-0000-0000-000000000001', 'CES-DRN-100B', 'Supply and lay vitrified clay pipe 100mm - trench 1.0-2.0m depth', 'm', 'Drainage', 12.50, 24.00, 10.00, 'H.10.10', 'E'),
('c3000000-0000-0000-0000-000000000001', 'CES-DRN-150A', 'Supply and lay vitrified clay pipe 150mm - trench not exceeding 1.0m depth', 'm', 'Drainage', 16.50, 20.00, 9.00, 'H.10.15', 'E'),
('c3000000-0000-0000-0000-000000000001', 'CES-DRN-150B', 'Supply and lay vitrified clay pipe 150mm - trench 1.0-2.0m depth', 'm', 'Drainage', 16.50, 28.00, 12.00, 'H.10.15', 'E'),
('c3000000-0000-0000-0000-000000000001', 'CES-DRN-225A', 'Supply and lay concrete pipe 225mm - trench not exceeding 1.5m depth', 'm', 'Drainage', 28.00, 32.00, 15.00, 'H.10.20', 'E'),
('c3000000-0000-0000-0000-000000000001', 'CES-DRN-225B', 'Supply and lay concrete pipe 225mm - trench 1.5-3.0m depth', 'm', 'Drainage', 28.00, 45.00, 20.00, 'H.10.20', 'E'),
('c3000000-0000-0000-0000-000000000001', 'CES-DRN-300A', 'Supply and lay concrete pipe 300mm - trench not exceeding 1.5m depth', 'm', 'Drainage', 38.00, 38.00, 18.00, 'H.10.25', 'E'),
('c3000000-0000-0000-0000-000000000001', 'CES-DRN-300B', 'Supply and lay concrete pipe 300mm - trench 1.5-3.0m depth', 'm', 'Drainage', 38.00, 55.00, 25.00, 'H.10.25', 'E'),
('c3000000-0000-0000-0000-000000000001', 'CES-DRN-375A', 'Supply and lay concrete pipe 375mm - trench not exceeding 1.5m depth', 'm', 'Drainage', 52.00, 45.00, 22.00, 'H.10.30', 'E'),
('c3000000-0000-0000-0000-000000000001', 'CES-DRN-450A', 'Supply and lay concrete pipe 450mm - trench not exceeding 2.0m depth', 'm', 'Drainage', 68.00, 55.00, 28.00, 'H.10.35', 'E'),
('c3000000-0000-0000-0000-000000000001', 'CES-DRN-600A', 'Supply and lay concrete pipe 600mm - trench not exceeding 2.5m depth', 'm', 'Drainage', 95.00, 72.00, 35.00, 'H.10.40', 'E'),
('c3000000-0000-0000-0000-000000000001', 'CES-DRN-HDPE300', 'Supply and lay HDPE pipe 300mm SN8 - trench not exceeding 2.0m depth', 'm', 'Drainage', 45.00, 32.00, 15.00, 'H.10.45', 'E'),
('c3000000-0000-0000-0000-000000000001', 'CES-DRN-HDPE450', 'Supply and lay HDPE pipe 450mm SN8 - trench not exceeding 2.0m depth', 'm', 'Drainage', 75.00, 48.00, 22.00, 'H.10.45', 'E');

-- Manholes and Chambers (DIV03-02)
INSERT INTO boq_standard_items (category_id, item_code, description, unit, work_category, material_cost, labour_cost, plant_cost, nrm2_code, cesmm4_class) VALUES
('c3000000-0000-0000-0000-000000000002', 'CES-CHB-SM1', 'Construct brick manhole 900x900mm - not exceeding 1.0m depth', 'nr', 'Chamber', 350.00, 480.00, 45.00, 'H.20.10', 'E'),
('c3000000-0000-0000-0000-000000000002', 'CES-CHB-SM2', 'Construct brick manhole 900x900mm - 1.0-2.0m depth', 'nr', 'Chamber', 450.00, 720.00, 65.00, 'H.20.10', 'E'),
('c3000000-0000-0000-0000-000000000002', 'CES-CHB-SM3', 'Construct brick manhole 900x900mm - 2.0-3.0m depth', 'nr', 'Chamber', 580.00, 950.00, 85.00, 'H.20.10', 'E'),
('c3000000-0000-0000-0000-000000000002', 'CES-CHB-MED', 'Construct brick manhole 1200x900mm - 3.0-4.0m depth', 'nr', 'Chamber', 850.00, 1250.00, 120.00, 'H.20.15', 'E'),
('c3000000-0000-0000-0000-000000000002', 'CES-CHB-LRG', 'Construct brick manhole 1500x1200mm - exceeding 4.0m depth', 'nr', 'Chamber', 1200.00, 1850.00, 180.00, 'H.20.20', 'E'),
('c3000000-0000-0000-0000-000000000002', 'CES-CHB-COVER', 'Supply and install manhole cover and frame D400', 'nr', 'Chamber', 185.00, 95.00, 15.00, 'H.20.25', 'E'),
('c3000000-0000-0000-0000-000000000002', 'CES-CHB-LADDER', 'Supply and install galvanised steel access ladder', 'nr', 'Chamber', 120.00, 85.00, 10.00, 'H.20.30', 'E'),
('c3000000-0000-0000-0000-000000000002', 'CES-CHB-DROP', 'Construct drop connection into manhole - 150mm', 'nr', 'Chamber', 180.00, 280.00, 25.00, 'H.20.35', 'E');

-- Gullies (DIV03-03)
INSERT INTO boq_standard_items (category_id, item_code, description, unit, work_category, material_cost, labour_cost, plant_cost, nrm2_code, cesmm4_class) VALUES
('c3000000-0000-0000-0000-000000000003', 'CES-GUL-450', 'Construct road gully 450x450mm with grate', 'nr', 'Gully', 95.00, 180.00, 22.00, 'H.25.10', 'E'),
('c3000000-0000-0000-0000-000000000003', 'CES-GUL-600', 'Construct road gully 600x600mm with grate', 'nr', 'Gully', 145.00, 220.00, 28.00, 'H.25.15', 'E'),
('c3000000-0000-0000-0000-000000000003', 'CES-GUL-YARD', 'Construct yard gully 300x300mm', 'nr', 'Gully', 45.00, 95.00, 12.00, 'H.25.20', 'E'),
('c3000000-0000-0000-0000-000000000003', 'CES-ROD-150', 'Construct rodding eye 150mm diameter', 'nr', 'Gully', 35.00, 65.00, 8.00, 'H.25.25', 'E');

-- Bedding and Surround (DIV03-04)
INSERT INTO boq_standard_items (category_id, item_code, description, unit, work_category, material_cost, labour_cost, plant_cost, nrm2_code, cesmm4_class) VALUES
('c3000000-0000-0000-0000-000000000004', 'CES-BED-GRAN', 'Bedding and surround to pipe - granular Type B material', 'm³', 'Bedding', 52.00, 18.00, 15.00, 'H.15.10', 'E'),
('c3000000-0000-0000-0000-000000000004', 'CES-ENV-GRAN', 'Granular pipe envelope - 150mm minimum all round', 'm³', 'Bedding', 48.00, 12.00, 10.00, 'H.15.15', 'E'),
('c3000000-0000-0000-0000-000000000004', 'CES-HDC-CONC', 'Haunching to drainage pipe - concrete', 'm³', 'Bedding', 95.00, 55.00, 18.00, 'H.15.20', 'E'),
('c3000000-0000-0000-0000-000000000004', 'CES-SAND-50', 'Sand bedding 50mm thick', 'm²', 'Bedding', 8.00, 5.00, 3.00, 'H.15.25', 'E');

-- RC Concrete (DIV04-01)
INSERT INTO boq_standard_items (category_id, item_code, description, unit, work_category, material_cost, labour_cost, plant_cost, nrm2_code, cesmm4_class) VALUES
('c4000000-0000-0000-0000-000000000001', 'CES-CON-RC25', 'Supply and place RC25/30 concrete - general works', 'm³', 'Concrete', 95.00, 65.00, 35.00, 'F.10.10', 'G'),
('c4000000-0000-0000-0000-000000000001', 'CES-CON-RC28', 'Supply and place RC28/35 concrete - foundations', 'm³', 'Concrete', 102.00, 68.00, 38.00, 'F.10.15', 'G'),
('c4000000-0000-0000-0000-000000000001', 'CES-CON-RC30', 'Supply and place RC30/37 concrete - structural', 'm³', 'Concrete', 108.00, 72.00, 42.00, 'F.10.20', 'G'),
('c4000000-0000-0000-0000-000000000001', 'CES-CON-RC32', 'Supply and place RC32/40 concrete - high strength', 'm³', 'Concrete', 118.00, 75.00, 45.00, 'F.10.25', 'G'),
('c4000000-0000-0000-0000-000000000001', 'CES-CON-RC35', 'Supply and place RC35/45 concrete - columns/slabs', 'm³', 'Concrete', 128.00, 80.00, 48.00, 'F.10.30', 'G');

-- Foundation Concrete (DIV04-02)
INSERT INTO boq_standard_items (category_id, item_code, description, unit, work_category, material_cost, labour_cost, plant_cost, nrm2_code, cesmm4_class) VALUES
('c4000000-0000-0000-0000-000000000002', 'CES-FND-TF1', 'Concrete trench fill 600mm wide x 300mm depth', 'm³', 'Foundation', 85.00, 42.00, 28.00, 'F.20.10', 'G'),
('c4000000-0000-0000-0000-000000000002', 'CES-FND-TF2', 'Concrete trench fill 600mm wide x 450mm depth', 'm³', 'Foundation', 85.00, 45.00, 30.00, 'F.20.10', 'G'),
('c4000000-0000-0000-0000-000000000002', 'CES-FND-TF3', 'Concrete trench fill 750mm wide x 450mm depth', 'm³', 'Foundation', 88.00, 48.00, 32.00, 'F.20.10', 'G'),
('c4000000-0000-0000-0000-000000000002', 'CES-FND-TF4', 'Concrete trench fill 900mm wide x 600mm depth', 'm³', 'Foundation', 90.00, 55.00, 38.00, 'F.20.15', 'G'),
('c4000000-0000-0000-0000-000000000002', 'CES-FND-STR1', 'Concrete strip foundation 450mm wide x 225mm depth', 'm³', 'Foundation', 82.00, 38.00, 25.00, 'F.20.20', 'G'),
('c4000000-0000-0000-0000-000000000002', 'CES-FND-STR2', 'Concrete strip foundation 600mm wide x 225mm depth', 'm³', 'Foundation', 82.00, 40.00, 27.00, 'F.20.20', 'G'),
('c4000000-0000-0000-0000-000000000002', 'CES-FND-PAD1', 'Concrete pad foundation 1000x1000x600mm', 'm³', 'Foundation', 95.00, 65.00, 42.00, 'F.20.25', 'G');

-- Reinforcement (DIV04-03)
INSERT INTO boq_standard_items (category_id, item_code, description, unit, work_category, material_cost, labour_cost, plant_cost, nrm2_code, cesmm4_class) VALUES
('c4000000-0000-0000-0000-000000000003', 'CES-REBAR-HY', 'Supply and fix high yield reinforcement B500B', 't', 'Reinforcement', 1250.00, 850.00, 120.00, 'F.30.10', 'G'),
('c4000000-0000-0000-0000-000000000003', 'CES-REBAR-8', 'Supply and fix reinforcement 8mm bars', 't', 'Reinforcement', 1280.00, 880.00, 100.00, 'F.30.15', 'G'),
('c4000000-0000-0000-0000-000000000003', 'CES-REBAR-10', 'Supply and fix reinforcement 10mm bars', 't', 'Reinforcement', 1260.00, 860.00, 105.00, 'F.30.15', 'G'),
('c4000000-0000-0000-0000-000000000003', 'CES-REBAR-12', 'Supply and fix reinforcement 12mm bars', 't', 'Reinforcement', 1240.00, 840.00, 110.00, 'F.30.15', 'G'),
('c4000000-0000-0000-0000-000000000003', 'CES-REBAR-16', 'Supply and fix reinforcement 16mm bars', 't', 'Reinforcement', 1220.00, 820.00, 115.00, 'F.30.15', 'G'),
('c4000000-0000-0000-0000-000000000003', 'CES-REBAR-20', 'Supply and fix reinforcement 20mm bars', 't', 'Reinforcement', 1200.00, 800.00, 120.00, 'F.30.15', 'G'),
('c4000000-0000-0000-0000-000000000003', 'CES-MESH-A142', 'Supply and fix reinforcement mesh A142', 'm²', 'Reinforcement', 8.50, 5.50, 2.00, 'F.30.20', 'G'),
('c4000000-0000-0000-0000-000000000003', 'CES-MESH-A193', 'Supply and fix reinforcement mesh A193', 'm²', 'Reinforcement', 11.00, 6.50, 2.50, 'F.30.20', 'G'),
('c4000000-0000-0000-0000-000000000003', 'CES-MESH-A252', 'Supply and fix reinforcement mesh A252', 'm²', 'Reinforcement', 14.50, 8.00, 3.00, 'F.30.20', 'G'),
('c4000000-0000-0000-0000-000000000003', 'CES-MESH-A393', 'Supply and fix reinforcement mesh A393', 'm²', 'Reinforcement', 18.00, 10.00, 3.50, 'F.30.20', 'G');

-- Formwork (DIV04-04)
INSERT INTO boq_standard_items (category_id, item_code, description, unit, work_category, material_cost, labour_cost, plant_cost, nrm2_code, cesmm4_class) VALUES
('c4000000-0000-0000-0000-000000000004', 'CES-FRM-VERT', 'Formwork to concrete - simple finish vertical', 'm²', 'Formwork', 28.00, 35.00, 8.00, 'F.40.10', 'G'),
('c4000000-0000-0000-0000-000000000004', 'CES-FRM-HORI', 'Formwork to concrete - simple finish horizontal', 'm²', 'Formwork', 25.00, 32.00, 7.00, 'F.40.10', 'G'),
('c4000000-0000-0000-0000-000000000004', 'CES-FRM-TUBE2', 'Formwork with tube and fitting scaffold - 2.7m high', 'm²', 'Formwork', 45.00, 55.00, 15.00, 'F.40.15', 'G'),
('c4000000-0000-0000-0000-000000000004', 'CES-FRM-TUBE4', 'Formwork with tube and fitting scaffold - 4.2m high', 'm²', 'Formwork', 58.00, 72.00, 22.00, 'F.40.15', 'G'),
('c4000000-0000-0000-0000-000000000004', 'CES-FRM-TUBE6', 'Formwork with tube and fitting scaffold - 6.0m high', 'm²', 'Formwork', 72.00, 90.00, 30.00, 'F.40.15', 'G');

-- Floor Slabs (DIV04-05)
INSERT INTO boq_standard_items (category_id, item_code, description, unit, work_category, material_cost, labour_cost, plant_cost, nrm2_code, cesmm4_class) VALUES
('c4000000-0000-0000-0000-000000000005', 'CES-SLB-150-F1', 'Concrete floor slab 150mm TR34 Class FM1', 'm²', 'Slab', 28.00, 18.00, 12.00, 'F.50.10', 'G'),
('c4000000-0000-0000-0000-000000000005', 'CES-SLB-150-F2', 'Concrete floor slab 150mm TR34 Class FM2', 'm²', 'Slab', 30.00, 20.00, 14.00, 'F.50.10', 'G'),
('c4000000-0000-0000-0000-000000000005', 'CES-SLB-200-F2', 'Concrete floor slab 200mm TR34 Class FM2', 'm²', 'Slab', 35.00, 24.00, 18.00, 'F.50.15', 'G'),
('c4000000-0000-0000-0000-000000000005', 'CES-SLB-200-F3', 'Concrete floor slab 200mm TR34 Class FM3', 'm²', 'Slab', 38.00, 28.00, 22.00, 'F.50.15', 'G'),
('c4000000-0000-0000-0000-000000000005', 'CES-SLB-250-F4', 'Concrete floor slab 250mm TR34 Class FM4', 'm²', 'Slab', 45.00, 35.00, 28.00, 'F.50.20', 'G'),
('c4000000-0000-0000-0000-000000000005', 'CES-SLB-SFRC', 'Steel fibre reinforced concrete slab 150mm 25kg/m³', 'm²', 'Slab', 32.00, 20.00, 15.00, 'F.50.25', 'G');

-- Sub-base (DIV02-03)
INSERT INTO boq_standard_items (category_id, item_code, description, unit, work_category, material_cost, labour_cost, plant_cost, nrm2_code, cesmm4_class) VALUES
('c2000000-0000-0000-0000-000000000003', 'CES-SUB-T1-150', 'Supply and place Type 1 granular sub-base 150mm', 'm²', 'Sub-base', 12.00, 5.00, 8.00, 'E.20.10', 'D'),
('c2000000-0000-0000-0000-000000000003', 'CES-SUB-T1-225', 'Supply and place Type 1 granular sub-base 225mm', 'm²', 'Sub-base', 18.00, 6.00, 10.00, 'E.20.10', 'D'),
('c2000000-0000-0000-0000-000000000003', 'CES-SUB-T1-300', 'Supply and place Type 1 granular sub-base 300mm', 'm²', 'Sub-base', 24.00, 8.00, 12.00, 'E.20.10', 'D'),
('c2000000-0000-0000-0000-000000000003', 'CES-SUB-6F2-150', 'Supply and place 6F2 recycled aggregate 150mm', 'm²', 'Sub-base', 9.00, 5.00, 8.00, 'E.20.15', 'D'),
('c2000000-0000-0000-0000-000000000003', 'CES-SUB-6F2-225', 'Supply and place 6F2 recycled aggregate 225mm', 'm²', 'Sub-base', 13.50, 6.00, 10.00, 'E.20.15', 'D'),
('c2000000-0000-0000-0000-000000000003', 'CES-SUB-T3-150', 'Supply and place Type 3 granular sub-base 150mm', 'm²', 'Sub-base', 14.00, 5.00, 8.00, 'E.20.20', 'D');

-- Flexible Pavement (DIV06-01)
INSERT INTO boq_standard_items (category_id, item_code, description, unit, work_category, material_cost, labour_cost, plant_cost, nrm2_code, cesmm4_class) VALUES
('c6000000-0000-0000-0000-000000000001', 'CES-RD-SB-150', 'Road sub-base Type 1 150mm', 'm²', 'Pavement', 18.00, 6.00, 12.00, 'Q.10.10', 'M'),
('c6000000-0000-0000-0000-000000000001', 'CES-RD-BC-60', 'Road base course Dense MAC 60mm', 'm²', 'Pavement', 22.00, 8.00, 15.00, 'Q.10.15', 'M'),
('c6000000-0000-0000-0000-000000000001', 'CES-RD-BC-80', 'Road base course Dense MAC 80mm', 'm²', 'Pavement', 28.00, 10.00, 18.00, 'Q.10.15', 'M'),
('c6000000-0000-0000-0000-000000000001', 'CES-RD-BI-50', 'Road binder course Dense MAC 20mm 50mm', 'm²', 'Pavement', 16.00, 6.00, 12.00, 'Q.10.20', 'M'),
('c6000000-0000-0000-0000-000000000001', 'CES-RD-SU-35', 'Road surface course AC 10mm 35mm', 'm²', 'Pavement', 18.00, 7.00, 14.00, 'Q.10.25', 'M'),
('c6000000-0000-0000-0000-000000000001', 'CES-RD-SU-SMA', 'Road surface course SMA 10mm 30mm', 'm²', 'Pavement', 22.00, 8.00, 15.00, 'Q.10.25', 'M');

-- Kerbs (DIV06-02)
INSERT INTO boq_standard_items (category_id, item_code, description, unit, work_category, material_cost, labour_cost, plant_cost, nrm2_code, cesmm4_class) VALUES
('c6000000-0000-0000-0000-000000000002', 'CES-KER-HB1', 'Supply and lay HB1 kerb 125x255mm', 'm', 'Kerb', 8.50, 12.00, 5.00, 'Q.20.10', 'M'),
('c6000000-0000-0000-0000-000000000002', 'CES-KER-HB2', 'Supply and lay HB2 kerb 150x305mm', 'm', 'Kerb', 10.50, 14.00, 6.00, 'Q.20.10', 'M'),
('c6000000-0000-0000-0000-000000000002', 'CES-KER-BULL', 'Supply and lay bullnose kerb 150x305mm', 'm', 'Kerb', 12.00, 15.00, 6.50, 'Q.20.15', 'M'),
('c6000000-0000-0000-0000-000000000002', 'CES-KER-MK2', 'Supply and lay MK2 conservation kerb 150x255mm', 'm', 'Kerb', 14.00, 16.00, 7.00, 'Q.20.20', 'M'),
('c6000000-0000-0000-0000-000000000002', 'CES-KER-EDG-50', 'Supply and lay edging strip 50mm wide', 'm', 'Kerb', 4.50, 7.00, 3.00, 'Q.20.25', 'M'),
('c6000000-0000-0000-0000-000000000002', 'CES-KER-EDG-75', 'Supply and lay edging strip 75mm wide', 'm', 'Kerb', 5.50, 8.00, 3.50, 'Q.20.25', 'M'),
('c6000000-0000-0000-0000-000000000002', 'CES-KER-RAD', 'Radius kerb - various radii', 'nr', 'Kerb', 25.00, 45.00, 12.00, 'Q.20.30', 'M');

-- Ducting (DIV09)
INSERT INTO boq_standard_items (category_id, item_code, description, unit, work_category, material_cost, labour_cost, plant_cost, nrm2_code, cesmm4_class) VALUES
('c0000000-0000-0000-0000-000000000009', 'CES-DUC-050', 'Supply and lay ducting 50mm twin wall uPVC', 'm', 'Ducting', 5.50, 8.00, 4.00, 'J.10.10', 'L'),
('c0000000-0000-0000-0000-000000000009', 'CES-DUC-100', 'Supply and lay ducting 100mm twin wall uPVC', 'm', 'Ducting', 8.00, 10.00, 5.00, 'J.10.15', 'L'),
('c0000000-0000-0000-0000-000000000009', 'CES-DUC-150', 'Supply and lay ducting 150mm twin wall uPVC', 'm', 'Ducting', 12.00, 14.00, 7.00, 'J.10.20', 'L'),
('c0000000-0000-0000-0000-000000000009', 'CES-DUC-200', 'Supply and lay ducting 200mm HDPE', 'm', 'Ducting', 18.00, 18.00, 9.00, 'J.10.25', 'L'),
('c0000000-0000-0000-0000-000000000009', 'CES-DUC-300', 'Supply and lay ducting 300mm HDPE', 'm', 'Ducting', 28.00, 24.00, 12.00, 'J.10.30', 'L'),
('c0000000-0000-0000-0000-000000000009', 'CES-DUC-JB', 'Supply and install junction box 600x600x450mm', 'nr', 'Ducting', 85.00, 95.00, 15.00, 'J.10.35', 'L'),
('c0000000-0000-0000-0000-000000000009', 'CES-DUC-CC', 'Concrete cable cover over ducting 50mm thick', 'm', 'Ducting', 12.00, 15.00, 8.00, 'J.10.40', 'L');

-- Attenuation (DIV10)
INSERT INTO boq_standard_items (category_id, item_code, description, unit, work_category, material_cost, labour_cost, plant_cost, nrm2_code, cesmm4_class) VALUES
('c0000000-0000-0000-0000-000000000010', 'CES-ATT-TANK', 'Supply and install geocellular attenuation tank crates', 'm³', 'Attenuation', 95.00, 45.00, 35.00, 'H.30.10', 'E'),
('c0000000-0000-0000-0000-000000000010', 'CES-ATT-PIPE900', 'Large diameter pipe attenuation 900mm', 'm', 'Attenuation', 185.00, 95.00, 55.00, 'H.30.15', 'E'),
('c0000000-0000-0000-0000-000000000010', 'CES-ATT-PIPE1200', 'Large diameter pipe attenuation 1200mm', 'm', 'Attenuation', 245.00, 125.00, 75.00, 'H.30.15', 'E'),
('c0000000-0000-0000-0000-000000000010', 'CES-ATT-SWM', 'Swale attenuation basin constructed', 'm³', 'Attenuation', 15.00, 25.00, 18.00, 'H.30.20', 'E'),
('c0000000-0000-0000-0000-000000000010', 'CES-ATT-GEO', 'Geotextile filter fabric to attenuation system', 'm²', 'Attenuation', 4.50, 2.50, 1.50, 'H.30.25', 'E'),
('c0000000-0000-0000-0000-000000000010', 'CES-ATT-OFL', 'Overflow/control structure to attenuation', 'nr', 'Attenuation', 450.00, 650.00, 85.00, 'H.30.30', 'E');

-- Ancilliaries (DIV13)
INSERT INTO boq_standard_items (category_id, item_code, description, unit, work_category, material_cost, labour_cost, plant_cost, nrm2_code, cesmm4_class) VALUES
('c0000000-0000-0000-0000-000000000013', 'CES-GEO-TT1K', 'Supply and lay geotextile Terram 1000', 'm²', 'Geotextile', 3.50, 1.50, 0.80, 'R.10.10', 'X'),
('c0000000-0000-0000-0000-000000000013', 'CES-GEO-TT2K', 'Supply and lay geotextile Terram 2000', 'm²', 'Geotextile', 5.00, 1.80, 0.90, 'R.10.10', 'X'),
('c0000000-0000-0000-0000-000000000013', 'CES-GEO-WARN', 'Supply and lay detectible warning tape', 'm', 'Geotextile', 0.80, 1.20, 0.50, 'R.10.15', 'X'),
('c0000000-0000-0000-0000-000000000013', 'CES-GEO-TRAC', 'Supply and lay trace wire for ducting', 'm', 'Geotextile', 0.50, 1.50, 0.60, 'R.10.20', 'X'),
('c0000000-0000-0000-0000-000000000013', 'CES-CON-CUBE', 'Make and cure concrete test cubes set of 4', 'set', 'Testing', 25.00, 35.00, 5.00, 'Z.10.10', 'Z'),
('c0000000-0000-0000-0000-000000000013', 'CES-CON-SLUMP', 'Carry out concrete slump test', 'nr', 'Testing', 0, 25.00, 5.00, 'Z.10.15', 'Z'),
('c0000000-0000-0000-0000-000000000013', 'CES-CON-TEMP', 'Record concrete temperature on delivery', 'nr', 'Testing', 0, 8.00, 0, 'Z.10.20', 'Z');

-- Update total costs based on component costs
UPDATE boq_standard_items SET total_cost = COALESCE(material_cost, 0) + COALESCE(labour_cost, 0) + COALESCE(plant_cost, 0);

-- Update composite rates for items with all-in rates
UPDATE boq_standard_items SET composite_rate = total_cost WHERE work_category IN ('Muckaway', 'Sub-base');

-- ============================================
-- SUMMARY QUERIES (for verification)
-- ============================================

-- SELECT c.code, c.name, COUNT(s.id) as item_count 
-- FROM boq_categories c 
-- LEFT JOIN boq_standard_items s ON c.id = s.category_id 
-- GROUP BY c.code, c.name 
-- ORDER BY c.sort_order;
