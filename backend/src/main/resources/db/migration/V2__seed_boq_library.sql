-- V2__seed_boq_library.sql
-- Construction Resource Management System for UK Groundworks
-- Bill of Quantities Library Seed Data
-- NRM2 and CESMM4 coded items for UK groundwork contractors

-- ============================================
-- MUCKAWAY / EXCAVATION AND DISPOSAL
-- ============================================

-- Clean Inert Material - Easement/Carriageway/Footway Excavations
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-DIS-010', 'Excavation and disposal of clean inert excavated material off site - Easement width not exceeding 1.5m', 'Muckaway', 'm³', NOW(), NOW()),
('CES-DIS-011', 'Excavation and disposal of clean inert excavated material off site - General site areas', 'Muckaway', 'm³', NOW(), NOW()),
('CES-DIS-012', 'Excavation and disposal of clean inert excavated material off site - Road/carriageway excavation', 'Muckaway', 'm³', NOW(), NOW()),
('CES-DIS-013', 'Excavation and disposal of clean inert excavated material off site - Footway/footpath excavation', 'Muckaway', 'm³', NOW(), NOW()),
('CES-DIS-014', 'Excavation and disposal of clean inert excavated material off site - Verge cutting', 'Muckaway', 'm³', NOW(), NOW());

-- Contaminated Inert Material
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-DIS-020', 'Excavation and disposal of contaminated inert excavated material off site - Classification required', 'Muckaway', 'm³', NOW(), NOW()),
('CES-DIS-021', 'Excavation and disposal of contaminated inert excavated material off site - Classified and disposed to licensed facility', 'Muckaway', 'm³', NOW(), NOW()),
('CES-DIS-022', 'Excavation and disposal of asbestos containing contaminated material - Licensed carrier required', 'Muckaway', 'm³', NOW(), NOW());

-- Non-Inert Material
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-DIS-030', 'Excavation and disposal of non-inert excavated material off site - Mixed construction demolition waste', 'Muckaway', 'm³', NOW(), NOW()),
('CES-DIS-031', 'Excavation and disposal of non-inert excavated material off site - Wood and vegetation', 'Muckaway', 'm³', NOW(), NOW()),
('CES-DIS-032', 'Excavation and disposal of non-inert excavated material off site - Metal and plastics', 'Muckaway', 'm³', NOW(), NOW());

-- Hazardous Material
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-DIS-040', 'Excavation and disposal of hazardous excavated material off site - Special waste classification', 'Muckaway', 'm³', NOW(), NOW()),
('CES-DIS-041', 'Excavation and disposal of hazardous excavated material off site - Hydrocarbon contaminated', 'Muckaway', 'm³', NOW(), NOW()),
('CES-DIS-042', 'Excavation and disposal of hazardous excavated material off site - Chemical contamination', 'Muckaway', 'm³', NOW(), NOW()),
('CES-DIS-043', 'Excavation and disposal of hazardous excavated material off site - Radioactive naturally occurring (NORM)', 'Muckaway', 'm³', NOW(), NOW());

-- Unsuitable Material (Soft/Spoil)
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-DIS-050', 'Excavation and disposal of unsuitable material - Soft/alluvial deposits', 'Muckaway', 'm³', NOW(), NOW()),
('CES-DIS-051', 'Excavation and disposal of unsuitable material - Peat', 'Muckaway', 'm³', NOW(), NOW()),
('CES-DIS-052', 'Excavation and disposal of unsuitable material - Made ground with organics', 'Muckaway', 'm³', NOW(), NOW());

-- ============================================
-- DRAINAGE - PIPES BY DIAMETER AND DEPTH
-- ============================================

-- 100mm Diameter Pipes
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-DRN-100A', 'Supply and lay vitrified clay pipe nominal size 100mm - in standard trench not exceeding 1.0m depth', 'Drainage', 'm', NOW(), NOW()),
('CES-DRN-100B', 'Supply and lay vitrified clay pipe nominal size 100mm - in trench 1.0m to 2.0m depth', 'Drainage', 'm', NOW(), NOW()),
('CES-DRN-100C', 'Supply and lay vitrified clay pipe nominal size 100mm - in trench 2.0m to 3.0m depth', 'Drainage', 'm', NOW(), NOW()),
('CES-DRN-100D', 'Supply and lay vitrified clay pipe nominal size 100mm - in trench 3.0m to 4.0m depth', 'Drainage', 'm', NOW(), NOW()),
('CES-DRN-100E', 'Supply and lay vitrified clay pipe nominal size 100mm - in trench exceeding 4.0m depth', 'Drainage', 'm', NOW(), NOW());

-- 150mm Diameter Pipes
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-DRN-150A', 'Supply and lay vitrified clay pipe nominal size 150mm - in standard trench not exceeding 1.0m depth', 'Drainage', 'm', NOW(), NOW()),
('CES-DRN-150B', 'Supply and lay vitrified clay pipe nominal size 150mm - in trench 1.0m to 2.0m depth', 'Drainage', 'm', NOW(), NOW()),
('CES-DRN-150C', 'Supply and lay vitrified clay pipe nominal size 150mm - in trench 2.0m to 3.0m depth', 'Drainage', 'm', NOW(), NOW()),
('CES-DRN-150D', 'Supply and lay vitrified clay pipe nominal size 150mm - in trench 3.0m to 4.0m depth', 'Drainage', 'm', NOW(), NOW()),
('CES-DRN-150E', 'Supply and lay vitrified clay pipe nominal size 150mm - in trench exceeding 4.0m depth', 'Drainage', 'm', NOW(), NOW()),
('CES-DRN-150F', 'Supply and lay uPVC pipe nominal size 150mm - in standard trench not exceeding 1.0m depth', 'Drainage', 'm', NOW(), NOW()),
('CES-DRN-150G', 'Supply and lay uPVC pipe nominal size 150mm - in trench 1.0m to 2.0m depth', 'Drainage', 'm', NOW(), NOW());

-- 225mm Diameter Pipes
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-DRN-225A', 'Supply and lay vitrified clay pipe nominal size 225mm - in standard trench not exceeding 1.0m depth', 'Drainage', 'm', NOW(), NOW()),
('CES-DRN-225B', 'Supply and lay vitrified clay pipe nominal size 225mm - in trench 1.0m to 2.0m depth', 'Drainage', 'm', NOW(), NOW()),
('CES-DRN-225C', 'Supply and lay vitrified clay pipe nominal size 225mm - in trench 2.0m to 3.0m depth', 'Drainage', 'm', NOW(), NOW()),
('CES-DRN-225D', 'Supply and lay vitrified clay pipe nominal size 225mm - in trench 3.0m to 4.0m depth', 'Drainage', 'm', NOW(), NOW()),
('CES-DRN-225E', 'Supply and lay vitrified clay pipe nominal size 225mm - in trench exceeding 4.0m depth', 'Drainage', 'm', NOW(), NOW()),
('CES-DRN-225F', 'Supply and lay concrete pipe nominal size 225mm - in trench not exceeding 1.5m depth', 'Drainage', 'm', NOW(), NOW()),
('CES-DRN-225G', 'Supply and lay concrete pipe nominal size 225mm - in trench 1.5m to 3.0m depth', 'Drainage', 'm', NOW(), NOW());

-- 300mm Diameter Pipes
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-DRN-300A', 'Supply and lay concrete pipe nominal size 300mm - in trench not exceeding 1.5m depth', 'Drainage', 'm', NOW(), NOW()),
('CES-DRN-300B', 'Supply and lay concrete pipe nominal size 300mm - in trench 1.5m to 3.0m depth', 'Drainage', 'm', NOW(), NOW()),
('CES-DRN-300C', 'Supply and lay concrete pipe nominal size 300mm - in trench 3.0m to 4.5m depth', 'Drainage', 'm', NOW(), NOW()),
('CES-DRN-300D', 'Supply and lay concrete pipe nominal size 300mm - in trench exceeding 4.5m depth', 'Drainage', 'm', NOW(), NOW()),
('CES-DRN-300E', 'Supply and lay HDPE pipe nominal size 300mm SN8 - in trench not exceeding 2.0m depth', 'Drainage', 'm', NOW(), NOW()),
('CES-DRN-300F', 'Supply and lay HDPE pipe nominal size 300mm SN8 - in trench 2.0m to 4.0m depth', 'Drainage', 'm', NOW(), NOW());

-- 375mm Diameter Pipes
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-DRN-375A', 'Supply and lay concrete pipe nominal size 375mm - in trench not exceeding 1.5m depth', 'Drainage', 'm', NOW(), NOW()),
('CES-DRN-375B', 'Supply and lay concrete pipe nominal size 375mm - in trench 1.5m to 3.0m depth', 'Drainage', 'm', NOW(), NOW()),
('CES-DRN-375C', 'Supply and lay concrete pipe nominal size 375mm - in trench 3.0m to 4.5m depth', 'Drainage', 'm', NOW(), NOW()),
('CES-DRN-375D', 'Supply and lay concrete pipe nominal size 375mm - in trench exceeding 4.5m depth', 'Drainage', 'm', NOW(), NOW());

-- 450mm Diameter Pipes
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-DRN-450A', 'Supply and lay concrete pipe nominal size 450mm - in trench not exceeding 2.0m depth', 'Drainage', 'm', NOW(), NOW()),
('CES-DRN-450B', 'Supply and lay concrete pipe nominal size 450mm - in trench 2.0m to 3.5m depth', 'Drainage', 'm', NOW(), NOW()),
('CES-DRN-450C', 'Supply and lay concrete pipe nominal size 450mm - in trench 3.5m to 5.0m depth', 'Drainage', 'm', NOW(), NOW()),
('CES-DRN-450D', 'Supply and lay concrete pipe nominal size 450mm - in trench exceeding 5.0m depth', 'Drainage', 'm', NOW(), NOW());

-- 525mm Diameter Pipes
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-DRN-525A', 'Supply and lay concrete pipe nominal size 525mm - in trench not exceeding 2.0m depth', 'Drainage', 'm', NOW(), NOW()),
('CES-DRN-525B', 'Supply and lay concrete pipe nominal size 525mm - in trench 2.0m to 3.5m depth', 'Drainage', 'm', NOW(), NOW()),
('CES-DRN-525C', 'Supply and lay concrete pipe nominal size 525mm - in trench 3.5m to 5.0m depth', 'Drainage', 'm', NOW(), NOW()),
('CES-DRN-525D', 'Supply and lay concrete pipe nominal size 525mm - in trench exceeding 5.0m depth', 'Drainage', 'm', NOW(), NOW());

-- 600mm Diameter Pipes
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-DRN-600A', 'Supply and lay concrete pipe nominal size 600mm - in trench not exceeding 2.5m depth', 'Drainage', 'm', NOW(), NOW()),
('CES-DRN-600B', 'Supply and lay concrete pipe nominal size 600mm - in trench 2.5m to 4.0m depth', 'Drainage', 'm', NOW(), NOW()),
('CES-DRN-600C', 'Supply and lay concrete pipe nominal size 600mm - in trench 4.0m to 5.5m depth', 'Drainage', 'm', NOW(), NOW()),
('CES-DRN-600D', 'Supply and lay concrete pipe nominal size 600mm - in trench exceeding 5.5m depth', 'Drainage', 'm', NOW(), NOW()),
('CES-DRN-600E', 'Supply and lay steel reinforced concrete pipe nominal size 600mm - in trench not exceeding 3.0m depth', 'Drainage', 'm', NOW(), NOW());

-- Pipe Envelopes and Bedding
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-DRN-BED', 'Bedding and surround to drainage pipe - granular material Type B', 'Drainage', 'm³', NOW(), NOW()),
('CES-DRN-ENV', 'Granular pipe envelope - 150mm minimum all round', 'Drainage', 'm³', NOW(), NOW()),
('CES-DRN-HDC', 'Haunching to drainage pipe - concrete', 'Drainage', 'm³', NOW(), NOW());

-- ============================================
-- CHAMBERS AND MANHOLES
-- ============================================
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-CHB-SM1', 'Construct brick/blockwork manhole - size 900x900mm - not exceeding 1.0m depth', 'Drainage', 'nr', NOW(), NOW()),
('CES-CHB-SM2', 'Construct brick/blockwork manhole - size 900x900mm - 1.0m to 2.0m depth', 'Drainage', 'nr', NOW(), NOW()),
('CES-CHB-SM3', 'Construct brick/blockwork manhole - size 900x900mm - 2.0m to 3.0m depth', 'Drainage', 'nr', NOW(), NOW()),
('CES-CHB-MED', 'Construct brick/blockwork manhole - size 1200x900mm - exceeding 3.0m depth', 'Drainage', 'nr', NOW(), NOW()),
('CES-CHB-LRG', 'Construct brick/blockwork manhole - size 1500x1200mm - any depth', 'Drainage', 'nr', NOW(), NOW()),
('CES-CHB-ECL', 'Supply and install chamber access ladder - galvanised steel', 'Drainage', 'nr', NOW(), NOW()),
('CES-CHB-DROP', 'Construct drop connection into manhole - pipe size 150mm', 'Drainage', 'nr', NOW(), NOW()),
('CES-CHB-COVER', 'Supply and install manhole cover and frame - D400 heavy duty', 'Drainage', 'nr', NOW(), NOW()),
('CES-CHB-GULLY', 'Construct road gully - 450x450mm with grate', 'Drainage', 'nr', NOW(), NOW()),
('CES-CHB-ROD', 'Construct rodding eye - 150mm diameter', 'Drainage', 'nr', NOW(), NOW());

-- ============================================
-- CONCRETE WORKS
-- ============================================

-- RC (Reinforced Concrete) by BS 8500 Designation
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-CON-RC25', 'Supply and place ready-mixed concrete Designation RC25/30 - general works', 'Concrete', 'm³', NOW(), NOW()),
('CES-CON-RC28', 'Supply and place ready-mixed concrete Designation RC28/35 - foundations', 'Concrete', 'm³', NOW(), NOW()),
('CES-CON-RC30', 'Supply and place ready-mixed concrete Designation RC30/37 - structural', 'Concrete', 'm³', NOW(), NOW()),
('CES-CON-RC32', 'Supply and place ready-mixed concrete Designation RC32/40 - high strength', 'Concrete', 'm³', NOW(), NOW()),
('CES-CON-RC35', 'Supply and place ready-mixed concrete Designation RC35/45 - columns/slabs', 'Concrete', 'm³', NOW(), NOW()),
('CES-CON-RC40', 'Supply and place ready-mixed concrete Designation RC40/50 - specialist', 'Concrete', 'm³', NOW(), NOW()),
('CES-CON-RC40-D', 'Supply and place ready-mixed concrete Designation RC40/50 - in drilled shafts', 'Concrete', 'm³', NOW(), NOW());

-- FND (Foundation Concrete) by BS 8500 Designation
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-CON-FND2', 'Supply and place ready-mixed concrete Designation FND2 - strip foundations min comp 15N/mm²', 'Concrete', 'm³', NOW(), NOW()),
('CES-CON-FND3', 'Supply and place ready-mixed concrete Designation FND3 - strip foundations min comp 20N/mm²', 'Concrete', 'm³', NOW(), NOW()),
('CES-CON-FND3Z', 'Supply and place ready-mixed concrete Designation FND3Z - strip foundations with zinc', 'Concrete', 'm³', NOW(), NOW()),
('CES-CON-FND4', 'Supply and place ready-mixed concrete Designation FND4 - reduced workability', 'Concrete', 'm³', NOW(), NOW()),
('CES-CON-FND4C', 'Supply and place ready-mixed concrete Designation FND4C - C4 freeze/thaw', 'Concrete', 'm³', NOW(), NOW()),
('CES-CON-PFA20', 'Supply and place ready-mixed concrete with 20% PFA replacement - general works', 'Concrete', 'm³', NOW(), NOW()),
('CES-CON-GGBS30', 'Supply and place ready-mixed concrete with 30% GGBS replacement - structural', 'Concrete', 'm³', NOW(), NOW());

-- Concrete Test Cubes
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-CON-CUBE', 'Make and cure concrete test cubes - 100mm set of 4', 'Concrete', 'set', NOW(), NOW()),
('CES-CON-SLUMP', 'Carry out concrete slump test on delivery', 'Concrete', 'nr', NOW(), NOW()),
('CES-CON-TEMP', 'Record concrete temperature on delivery', 'Concrete', 'nr', NOW(), NOW());

-- ============================================
-- FOUNDATIONS
-- ============================================

-- Trench Fill Foundations
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-FND-TF1', 'Concrete trench fill foundation - 600mm wide x 300mm depth', 'Foundations', 'm³', NOW(), NOW()),
('CES-FND-TF2', 'Concrete trench fill foundation - 600mm wide x 450mm depth', 'Foundations', 'm³', NOW(), NOW()),
('CES-FND-TF3', 'Concrete trench fill foundation - 750mm wide x 450mm depth', 'Foundations', 'm³', NOW(), NOW()),
('CES-FND-TF4', 'Concrete trench fill foundation - 750mm wide x 600mm depth', 'Foundations', 'm³', NOW(), NOW()),
('CES-FND-TF5', 'Concrete trench fill foundation - 900mm wide x 600mm depth', 'Foundations', 'm³', NOW(), NOW()),
('CES-FND-TF6', 'Concrete trench fill foundation - 900mm wide x 750mm depth', 'Foundations', 'm³', NOW(), NOW()),
('CES-FND-TF7', 'Concrete trench fill foundation - 1200mm wide x 750mm depth', 'Foundations', 'm³', NOW(), NOW()),
('CES-FND-TF8', 'Concrete trench fill foundation - 1200mm wide x 900mm depth', 'Foundations', 'm³', NOW(), NOW());

-- Strip Foundations
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-FND-STR1', 'Concrete strip foundation - 450mm wide x 225mm depth', 'Foundations', 'm³', NOW(), NOW()),
('CES-FND-STR2', 'Concrete strip foundation - 600mm wide x 225mm depth', 'Foundations', 'm³', NOW(), NOW()),
('CES-FND-STR3', 'Concrete strip foundation - 600mm wide x 300mm depth', 'Foundations', 'm³', NOW(), NOW()),
('CES-FND-STR4', 'Concrete strip foundation - 750mm wide x 300mm depth', 'Foundations', 'm³', NOW(), NOW()),
('CES-FND-STR5', 'Concrete strip foundation - 750mm wide x 450mm depth', 'Foundations', 'm³', NOW(), NOW()),
('CES-FND-STR6', 'Concrete strip foundation - 900mm wide x 450mm depth', 'Foundations', 'm³', NOW(), NOW());

-- Pad Foundations
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-FND-PAD1', 'Concrete pad foundation - 1000x1000x600mm deep', 'Foundations', 'm³', NOW(), NOW()),
('CES-FND-PAD2', 'Concrete pad foundation - 1500x1500x600mm deep', 'Foundations', 'm³', NOW(), NOW()),
('CES-FND-PAD3', 'Concrete pad foundation - 2000x2000x750mm deep', 'Foundations', 'm³', NOW(), NOW()),
('CES-FND-PAD4', 'Concrete pad foundation - 2500x2500x900mm deep', 'Foundations', 'm³', NOW(), NOW());

-- ============================================
-- SLABS - TR34 CLASSIFICATION
-- ============================================

-- Floor Slabs by Thickness and TR34 Class
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-SLB-150-F1', 'Supply and place in situ concrete floor slab - 150mm thick TR34 Class FM1', 'Slabs', 'm²', NOW(), NOW()),
('CES-SLB-150-F2', 'Supply and place in situ concrete floor slab - 150mm thick TR34 Class FM2', 'Slabs', 'm²', NOW(), NOW()),
('CES-SLB-150-F3', 'Supply and place in situ concrete floor slab - 150mm thick TR34 Class FM3', 'Slabs', 'm²', NOW(), NOW()),
('CES-SLB-150-F4', 'Supply and place in situ concrete floor slab - 150mm thick TR34 Class FM4', 'Slabs', 'm²', NOW(), NOW()),
('CES-SLB-200-F1', 'Supply and place in situ concrete floor slab - 200mm thick TR34 Class FM1', 'Slabs', 'm²', NOW(), NOW()),
('CES-SLB-200-F2', 'Supply and place in situ concrete floor slab - 200mm thick TR34 Class FM2', 'Slabs', 'm²', NOW(), NOW()),
('CES-SLB-200-F3', 'Supply and place in situ concrete floor slab - 200mm thick TR34 Class FM3', 'Slabs', 'm²', NOW(), NOW()),
('CES-SLB-200-F4', 'Supply and place in situ concrete floor slab - 200mm thick TR34 Class FM4', 'Slabs', 'm²', NOW(), NOW()),
('CES-SLB-225-F2', 'Supply and place in situ concrete floor slab - 225mm thick TR34 Class FM2', 'Slabs', 'm²', NOW(), NOW()),
('CES-SLB-225-F3', 'Supply and place in situ concrete floor slab - 225mm thick TR34 Class FM3', 'Slabs', 'm²', NOW(), NOW()),
('CES-SLB-250-F2', 'Supply and place in situ concrete floor slab - 250mm thick TR34 Class FM2', 'Slabs', 'm²', NOW(), NOW()),
('CES-SLB-250-F4', 'Supply and place in situ concrete floor slab - 250mm thick TR34 Class FM4', 'Slabs', 'm²', NOW(), NOW()),
('CES-SLB-300-F4', 'Supply and place in situ concrete floor slab - 300mm thick TR34 Class FM4', 'Slabs', 'm²', NOW(), NOW());

-- Steel Fibre Reinforced Slabs
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-SLB-SFRC', 'Supply and place steel fibre reinforced concrete slab - 150mm - 25kg/m³ fibre', 'Slabs', 'm²', NOW(), NOW()),
('CES-SLB-SFRC2', 'Supply and place steel fibre reinforced concrete slab - 200mm - 30kg/m³ fibre', 'Slabs', 'm²', NOW(), NOW());

-- ============================================
-- SUB-BASES AND GRANULAR MATERIALS
-- ============================================

-- MOT Type Materials (by weight)
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-SUB-T1-01', 'Supply and place Type 1 granular sub-base material - 150mm depth', 'Sub-base', 'm²', NOW(), NOW()),
('CES-SUB-T1-02', 'Supply and place Type 1 granular sub-base material - 225mm depth', 'Sub-base', 'm²', NOW(), NOW()),
('CES-SUB-T1-03', 'Supply and place Type 1 granular sub-base material - 300mm depth', 'Sub-base', 'm²', NOW(), NOW()),
('CES-SUB-T1-04', 'Supply and place Type 1 granular sub-base material - 450mm depth', 'Sub-base', 'm²', NOW(), NOW()),
('CES-SUB-T3-01', 'Supply and place Type 3 granular sub-base material - 150mm depth', 'Sub-base', 'm²', NOW(), NOW()),
('CES-SUB-T3-02', 'Supply and place Type 3 granular sub-base material - 225mm depth', 'Sub-base', 'm²', NOW(), NOW());

-- 6F Materials ( recycled)
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-SUB-6F2-01', 'Supply and place 6F2 recycled aggregate - 150mm depth', 'Sub-base', 'm²', NOW(), NOW()),
('CES-SUB-6F2-02', 'Supply and place 6F2 recycled aggregate - 225mm depth', 'Sub-base', 'm²', NOW(), NOW()),
('CES-SUB-6F2-03', 'Supply and place 6F2 recycled aggregate - 300mm depth', 'Sub-base', 'm²', NOW(), NOW()),
('CES-SUB-6F5-01', 'Supply and place 6F5 recycled aggregate - 100mm depth', 'Sub-base', 'm²', NOW(), NOW()),
('CES-SUB-6F5-02', 'Supply and place 6F5 recycled aggregate - 150mm depth', 'Sub-base', 'm²', NOW(), NOW());

-- Sand and Gravel Bedding
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-SUB-SS001', 'Supply and place sharp sand bedding - 50mm depth', 'Sub-base', 'm²', NOW(), NOW()),
('CES-SUB-SS002', 'Supply and place sharp sand bedding - 75mm depth', 'Sub-base', 'm²', NOW(), NOW()),
('CES-SUB-SS003', 'Supply and place sharp sand bedding - 100mm depth', 'Sub-base', 'm²', NOW(), NOW()),
('CES-SUB-PSA001', 'Supply and place 10/20mm pea gravel - 50mm depth', 'Sub-base', 'm²', NOW(), NOW()),
('CES-SUB-PSA002', 'Supply and place 10/20mm pea gravel - 75mm depth', 'Sub-base', 'm²', NOW(), NOW()),
('CES-SUB-GRB-01', 'Supply and place granite rubble bedding - 150mm depth', 'Sub-base', 'm³', NOW(), NOW()),
('CES-SUB-GRB-02', 'Supply and place granite rubble bedding - 225mm depth', 'Sub-base', 'm³', NOW(), NOW());

-- ============================================
-- KERBS AND EDGING
-- ============================================

INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-KER-HB1', 'Supply and lay HC1/HB1 precast concrete kerb - 125x255mm', 'Kerbs', 'm', NOW(), NOW()),
('CES-KER-HB2', 'Supply and lay HC2/HB2 precast concrete kerb - 150x305mm', 'Kerbs', 'm', NOW(), NOW()),
('CES-KER-BULL', 'Supply and lay bullnose kerb - 150x305mm', 'Kerbs', 'm', NOW(), NOW()),
('CES-KER-MK2', 'Supply and lay MK2 conservation kerb - 150x255mm', 'Kerbs', 'm', NOW(), NOW()),
('CES-KER-BEVEL', 'Supply and lay splay kerb/b到r bevel edged - 150x305mm', 'Kerbs', 'm', NOW(), NOW()),
('CES-KER-CHAN', 'Supply and lay channel block - 125x150mm', 'Kerbs', 'm', NOW(), NOW()),
('CES-KER-EDG-50', 'Supply and lay precast concrete edging strip - 50mm wide', 'Kerbs', 'm', NOW(), NOW()),
('CES-KER-EDG-75', 'Supply and lay precast concrete edging strip - 75mm wide', 'Kerbs', 'm', NOW(), NOW()),
('CES-KER-BUS', 'Supply and lay tactile paving kerb - dropped crossing', 'Kerbs', 'm', NOW(), NOW()),
('CES-KER-RAD', 'Radius kerb - various radii', 'Kerbs', 'nr', NOW(), NOW());

-- ============================================
-- ROAD CONSTRUCTION
-- ============================================

-- Sub-base Courses
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-RD-SB-01', 'Road sub-base - Type 1 granular material - 150mm depth', 'Road Construction', 'm²', NOW(), NOW()),
('CES-RD-SB-02', 'Road sub-base - Type 1 granular material - 225mm depth', 'Road Construction', 'm²', NOW(), NOW()),
('CES-RD-SB-03', 'Road sub-base - Type 1 granular material - 300mm depth', 'Road Construction', 'm²', NOW(), NOW());

-- Base Courses
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-RD-BC-D1', 'Road base course - Dense bitumen macadam - 60mm depth', 'Road Construction', 'm²', NOW(), NOW()),
('CES-RD-BC-D2', 'Road base course - Dense bitumen macadam - 80mm depth', 'Road Construction', 'm²', NOW(), NOW()),
('CES-RD-BC-HM', 'Road base course - Hot rolled asphalt - 60mm depth', 'Road Construction', 'm²', NOW(), NOW()),
('CES-RD-BC-BB', 'Road base course - Bitumen bound macadam - 60mm depth', 'Road Construction', 'm²', NOW(), NOW());

-- Binder Courses
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-RD-BI-D1', 'Road binder course - Dense bitumen macadam 20mm - 50mm depth', 'Road Construction', 'm²', NOW(), NOW()),
('CES-RD-BI-D2', 'Road binder course - Dense bitumen macadam 20mm - 60mm depth', 'Road Construction', 'm²', NOW(), NOW()),
('CES-RD-BI-HM', 'Road binder course - Hot rolled asphalt - 40mm depth', 'Road Construction', 'm²', NOW(), NOW());

-- Surface Courses
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-RD-SU-AC', 'Road surface course - Asphalt concrete 10mm - 35mm depth', 'Road Construction', 'm²', NOW(), NOW()),
('CES-RD-SU-HM', 'Road surface course - Hot rolled asphalt - 30mm depth', 'Road Construction', 'm²', NOW(), NOW()),
('CES-RD-SU-SMA', 'Road surface course - Stone mastic asphalt 10mm - 30mm depth', 'Road Construction', 'm²', NOW(), NOW()),
('CES-RD-SU-TAC', 'Thin asphalt course - 25mm depth', 'Road Construction', 'm²', NOW(), NOW());

-- Coloured/Special Surfaces
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-RD-SU-RED', 'Coloured asphalt surface - red - 35mm depth', 'Road Construction', 'm²', NOW(), NOW()),
('CES-RD-SU-GRP', 'Surface dressing - single layer', 'Road Construction', 'm²', NOW(), NOW()),
('CES-RD-SU-GR2', 'Surface dressing - two layer', 'Road Construction', 'm²', NOW(), NOW()),
('CES-RD-MC', 'Micro-asphalt surface course - 15mm depth', 'Road Construction', 'm²', NOW(), NOW());

-- Pavement Construction by Thickness
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-RD-PAV-40', 'Flexible pavement construction - 40mm surface / 60mm base', 'Road Construction', 'm²', NOW(), NOW()),
('CES-RD-PAV-50', 'Flexible pavement construction - 50mm surface / 80mm base', 'Road Construction', 'm²', NOW(), NOW()),
('CES-RD-PAV-60', 'Flexible pavement construction - 60mm surface / 100mm base', 'Road Construction', 'm²', NOW(), NOW());

-- ============================================
-- DUCTING
-- ============================================

INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-DUC-050', 'Supply and lay ducting - 50mm diameter - twin wall uPVC', 'Ducting', 'm', NOW(), NOW()),
('CES-DUC-100', 'Supply and lay ducting - 100mm diameter - twin wall uPVC', 'Ducting', 'm', NOW(), NOW()),
('CES-DUC-150', 'Supply and lay ducting - 150mm diameter - twin wall uPVC', 'Ducting', 'm', NOW(), NOW()),
('CES-DUC-200', 'Supply and lay ducting - 200mm diameter - HDPE', 'Ducting', 'm', NOW(), NOW()),
('CES-DUC-225', 'Supply and lay ducting - 225mm diameter - HDPE', 'Ducting', 'm', NOW(), NOW()),
('CES-DUC-300', 'Supply and lay ducting - 300mm diameter - HDPE', 'Ducting', 'm', NOW(), NOW()),
('CES-DUC-450', 'Supply and lay ducting - 450mm diameter - HDPE', 'Ducting', 'm', NOW(), NOW()),
('CES-DUC-600', 'Supply and lay ducting - 600mm diameter - HDPE', 'Ducting', 'm', NOW(), NOW()),
('CES-DUC-CC', 'Concrete cable cover over ducting - 50mm thick', 'Ducting', 'm', NOW(), NOW()),
('CES-DUC-JB', 'Supply and install junction box - 600x600x450mm', 'Ducting', 'nr', NOW(), NOW()),
('CES-DUC-TB', 'Supply and install tee box - 600x600x600mm', 'Ducting', 'nr', NOW(), NOW()),
('CES-DUC-TERM', 'Supply and install terminal ducting marker - rodding eye', 'Ducting', 'nr', NOW(), NOW());

-- ============================================
-- ATTENUATION
-- ============================================

INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-ATT-TANK', 'Supply and install geocellular attenuation tank - crates', 'Attenuation', 'm³', NOW(), NOW()),
('CES-ATT-PIPE', 'Large diameter pipe attenuation system - 900mm', 'Attenuation', 'm', NOW(), NOW()),
('CES-ATT-PIP2', 'Large diameter pipe attenuation system - 1200mm', 'Attenuation', 'm', NOW(), NOW()),
('CES-ATT-PIP3', 'Large diameter pipe attenuation system - 1500mm', 'Attenuation', 'm', NOW(), NOW()),
('CES-ATT-SWM', 'Swale attenuation basin - constructed', 'Attenuation', 'm³', NOW(), NOW()),
('CES-ATT-PND', 'Pond attenuation system - lined', 'Attenuation', 'm³', NOW(), NOW()),
('CES-ATT-VLT', 'Storage crate vertical tower system', 'Attenuation', 'nr', NOW(), NOW()),
('CES-ATT-FLT', 'Storage crate flat modular system', 'Attenuation', 'm³', NOW(), NOW()),
('CES-ATT-FLT2', 'Storage crate high flow system', 'Attenuation', 'm³', NOW(), NOW()),
('CES-ATT-GEO', 'Geotextile filter fabric to attenuation system', 'Attenuation', 'm²', NOW(), NOW()),
('CES-ATT-GRD', 'Inspection/maintenance chamber to attenuation system', 'Attenuation', 'nr', NOW(), NOW()),
('CES-ATT-OFL', 'Overflow/control structure to attenuation', 'Attenuation', 'nr', NOW(), NOW());

-- ============================================
-- STRUCTURAL WORKS
-- ============================================

-- Reinforced Concrete Structures
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-STR-RC01', 'Reinforced concrete culvert - 1200x900mm box', 'Structural', 'm', NOW(), NOW()),
('CES-STR-RC02', 'Reinforced concrete culvert - 1500x1200mm box', 'Structural', 'm', NOW(), NOW()),
('CES-STR-RC03', 'Reinforced concrete culvert - 1800x1500mm box', 'Structural', 'm', NOW(), NOW()),
('CES-STR-RC04', 'Reinforced concrete headwall - small', 'Structural', 'nr', NOW(), NOW()),
('CES-STR-RC05', 'Reinforced concrete headwall - medium', 'Structural', 'nr', NOW(), NOW()),
('CES-STR-RC06', 'Reinforced concrete headwall - large', 'Structural', 'nr', NOW(), NOW()),
('CES-STR-RC07', 'Reinforced concrete retaining wall - 3.0m height', 'Structural', 'm³', NOW(), NOW()),
('CES-STR-RC08', 'Reinforced concrete retaining wall - 4.5m height', 'Structural', 'm³', NOW(), NOW()),
('CES-STR-RC09', 'Reinforced concrete wingwall - per m³', 'Structural', 'm³', NOW(), NOW());

-- Sheet Piling
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-STR-SP1A', 'Drive and extract steel sheet piling - Larssen type - 5m length', 'Structural', 'm²', NOW(), NOW()),
('CES-STR-SP1B', 'Drive and extract steel sheet piling - Larssen type - 8m length', 'Structural', 'm²', NOW(), NOW()),
('CES-STR-SP1C', 'Drive and extract steel sheet piling - Larssen type - 12m length', 'Structural', 'm²', NOW(), NOW()),
('CES-STR-SP2A', 'Drive and extract steel sheet piling - U-section - 6m length', 'Structural', 'm²', NOW(), NOW()),
('CES-STR-SP2B', 'Drive and extract steel sheet piling - U-section - 10m length', 'Structural', 'm²', NOW(), NOW()),
('CES-STR-SPC', 'Supply only steel sheet piles - per tonne', 'Structural', 't', NOW(), NOW());

-- Timber Piling
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-STR-TP01', 'Drive and extract timber piles - 200x200mm - 6m length', 'Structural', 'nr', NOW(), NOW()),
('CES-STR-TP02', 'Drive and extract timber piles - 200x200mm - 9m length', 'Structural', 'nr', NOW(), NOW()),
('CES-STR-TP03', 'Drive and extract timber piles - 250x250mm - 12m length', 'Structural', 'nr', NOW(), NOW());

-- Bored Piling
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-STR-BP01', 'Bored cast-in-place concrete pile - 450mm diameter - 10m depth', 'Structural', 'm', NOW(), NOW()),
('CES-STR-BP02', 'Bored cast-in-place concrete pile - 600mm diameter - 15m depth', 'Structural', 'm', NOW(), NOW()),
('CES-STR-BP03', 'Bored cast-in-place concrete pile - 750mm diameter - 20m depth', 'Structural', 'm', NOW(), NOW()),
('CES-STR-BP04', 'Bored cast-in-place concrete pile - 900mm diameter - 25m depth', 'Structural', 'm', NOW(), NOW()),
('CES-STR-BP05', 'Bored cast-in-place concrete pile - 1050mm diameter - 30m depth', 'Structural', 'm', NOW(), NOW()),
('CES-STR-RCAGE', 'Supply and install reinforcement cage to pile - 450mm', 'Structural', 'nr', NOW(), NOW()),
('CES-STR-RCAG2', 'Supply and install reinforcement cage to pile - 600mm', 'Structural', 'nr', NOW(), NOW()),
('CES-STR-RCAG3', 'Supply and install reinforcement cage to pile - 750mm', 'Structural', 'nr', NOW(), NOW()),
('CES-STR-PILES', 'Pile testing - dynamic load test', 'Structural', 'nr', NOW(), NOW()),
('CES-STR-PILR', 'Pile testing - integrity test (PIT)', 'Structural', 'nr', NOW(), NOW());

-- Piling Mat
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-STR-PMAT', 'Construct piling mat - 600mm thick granite - compacted', 'Structural', 'm²', NOW(), NOW()),
('CES-STR-PMAT2', 'Construct piling mat - 900mm thick hardcore - compacted', 'Structural', 'm²', NOW(), NOW()),
('CES-STR-GEO5', 'Geotextile membrane to piling mat - 5T non-woven', 'Structural', 'm²', NOW(), NOW());

-- Reinforcement
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-STR-REBAR', 'Supply and fix reinforcement - high yield steel bars - B500B', 'Structural', 't', NOW(), NOW()),
('CES-STR-REB8', 'Supply and fix reinforcement - 8mm bars', 'Structural', 't', NOW(), NOW()),
('CES-STR-REB10', 'Supply and fix reinforcement - 10mm bars', 'Structural', 't', NOW(), NOW()),
('CES-STR-REB12', 'Supply and fix reinforcement - 12mm bars', 'Structural', 't', NOW(), NOW()),
('CES-STR-REB16', 'Supply and fix reinforcement - 16mm bars', 'Structural', 't', NOW(), NOW()),
('CES-STR-REB20', 'Supply and fix reinforcement - 20mm bars', 'Structural', 't', NOW(), NOW()),
('CES-STR-REB25', 'Supply and fix reinforcement - 25mm bars', 'Structural', 't', NOW(), NOW()),
('CES-STR-REB32', 'Supply and fix reinforcement - 32mm bars', 'Structural', 't', NOW(), NOW()),
('CES-STR-MESH-A', 'Supply and fix reinforcement mesh - A142', 'Structural', 'm²', NOW(), NOW()),
('CES-STR-MESH-B', 'Supply and fix reinforcement mesh - A193', 'Structural', 'm²', NOW(), NOW()),
('CES-STR-MESH-C', 'Supply and fix reinforcement mesh - A252', 'Structural', 'm²', NOW(), NOW()),
('CES-STR-MESH-D', 'Supply and fix reinforcement mesh - A393', 'Structural', 'm²', NOW(), NOW()),
('CES-STR-SPCR', 'Spacer blocks/cover blocks to reinforcement', 'Structural', 'nr', NOW(), NOW()),
('CES-STR-TIE', 'Wire ties to reinforcement', 'Structural', 'kg', NOW(), NOW());

-- ============================================
-- MASONRY
-- ============================================

INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-MAS-BRK1', 'Brickwork - Class A engineering bricks - standard mortar', 'Masonry', 'm²', NOW(), NOW()),
('CES-MAS-BRK2', 'Brickwork - Class B engineering bricks - standard mortar', 'Masonry', 'm²', NOW(), NOW()),
('CES-MAS-BLK1', 'Blockwork - 100mm dense concrete blocks - standard mortar', 'Masonry', 'm²', NOW(), NOW()),
('CES-MAS-BLK2', 'Blockwork - 140mm dense concrete blocks - standard mortar', 'Masonry', 'm²', NOW(), NOW()),
('CES-MAS-BLK3', 'Blockwork - 190mm dense concrete blocks - standard mortar', 'Masonry', 'm²', NOW(), NOW()),
('CES-MAS-BLK4', 'Blockwork - 215mm dense concrete blocks - standard mortar', 'Masonry', 'm²', NOW(), NOW()),
('CES-MAS-REN1', 'Brickwork - brick on edge facing', 'Masonry', 'm²', NOW(), NOW()),
('CES-MAS-REN2', 'Brickwork - soldier course', 'Masonry', 'm²', NOW(), NOW());

-- ============================================
-- FORMVOORK
-- ============================================

INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-FRM-SIMP', 'Formwork to concrete - simple finish - vertical', 'Formwork', 'm²', NOW(), NOW()),
('CES-FRM-HORI', 'Formwork to concrete - simple finish - horizontal', 'Formwork', 'm²', NOW(), NOW()),
('CES-FRM-TUBE', 'Formwork to concrete - 2.7m high tube and fitting scaffold', 'Formwork', 'm²', NOW(), NOW()),
('CES-FRM-TUBE2', 'Formwork to concrete - 4.2m high tube and fitting scaffold', 'Formwork', 'm²', NOW(), NOW()),
('CES-FRM-TUBE3', 'Formwork to concrete - 6.0m high tube and fitting scaffold', 'Formwork', 'm²', NOW(), NOW()),
('CES-FRM-STOP', 'Stop ends to formwork', 'Formwork', 'nr', NOW(), NOW()),
('CES-FRM-WDWF', 'Woodwool permanent formwork slabs - 50mm', 'Formwork', 'm²', NOW(), NOW());

-- ============================================
-- ANCILLARY ITEMS
-- ============================================

-- Geotextiles
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-GEO-TT001', 'Supply and lay geotextile separation membrane - Terram 1000', 'Ancillary', 'm²', NOW(), NOW()),
('CES-GEO-TT002', 'Supply and lay geotextile separation membrane - Terram 2000', 'Ancillary', 'm²', NOW(), NOW()),
('CES-GEO-DRAIN', 'Supply and lay geotextile drainage membrane', 'Ancillary', 'm²', NOW(), NOW());

-- Warning/Tegular Tapes
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-GEO-WARN', 'Supply and lay cable/pipe warning tape - detectible', 'Ancillary', 'm', NOW(), NOW()),
('CES-GEO-TRAC', 'Supply and lay trace wire for ducting', 'Ancillary', 'm', NOW(), NOW());

-- Voids/Fill Materials
INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-VOID-FILL', 'Foam void fill system', 'Ancillary', 'm³', NOW(), NOW()),
('CES-VOID-SAND', 'Sand voids fill to pipe crossing', 'Ancillary', 'm³', NOW(), NOW()),
('CES-VOID-GRVEL', 'Granular voids fill - 10mm gravel', 'Ancillary', 'm³', NOW(), NOW());

-- ============================================
-- SECTION 278 / ADOPTIONS
-- ============================================

INSERT INTO boq_items (item_code, description, trade, unit, created_at, updated_at) VALUES
('CES-S278-ROAD', 'Section 278 works - carriageway construction - 5.5m width', 'Adoption', 'm²', NOW(), NOW()),
('CES-S278-FOOT', 'Section 278 works - footway construction - 2.0m width', 'Adoption', 'm²', NOW(), NOW()),
('CES-S278-CROSS', 'Section 278 works - vehicular crossing - standard', 'Adoption', 'nr', NOW(), NOW()),
('CES-S278-GULL', 'Section 278 works - gully connection', 'Adoption', 'nr', NOW(), NOW()),
('CES-S278-DRN1', 'Section 278 works - drainage 150mm', 'Adoption', 'm', NOW(), NOW()),
('CES-S278-DRN2', 'Section 278 works - drainage 300mm', 'Adoption', 'm', NOW(), NOW()),
('CES-S278-DRN3', 'Section 278 works - drainage 450mm', 'Adoption', 'm', NOW(), NOW()),
('CES-S278-SIGN', 'Section 278 works - traffic signs and posts', 'Adoption', 'nr', NOW(), NOW()),
('CES-S278-MARK', 'Section 278 works - road markings', 'Adoption', 'm²', NOW(), NOW()),
('CES-S278-LAMP', 'Section 278 works - street lighting columns', 'Adoption', 'nr', NOW(), NOW()),
('CES-S278-ROOT', 'Section 278 works - tree pit installation', 'Adoption', 'nr', NOW(), NOW()),
('CES-S278-SIGNL', 'Section 278 works - signal installation', 'Adoption', 'nr', NOW(), NOW());
