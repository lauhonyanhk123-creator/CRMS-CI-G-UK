-- V3__seed_rams_templates.sql
-- Construction Resource Management System for UK Groundworks
-- RAMS (Risk Assessment and Method Statement) Templates Seed Data

-- Template 1: CAT & Genny Scanning per HSG47
INSERT INTO rams_templates (title, description, trade, risk_assessment, method_statement, ppe_required, frequency_days, is_active, created_at, updated_at)
VALUES (
'CAT & Genny Utility Detection and Avoidance',
'Location of underground services using Cable Avoidance Tool (CAT) and signal generator (Genny) in accordance with HSG47 and GS6. All excavation works must be preceded by a full service detection survey.',
'General',
'The principal risks are: (1) Contact with live electrical cables resulting in electrocution or severe burns; (2) Contact with gas mains leading to explosion and fire; (3) Strike of water pipes causing flooding and injury; (4) Strike of fibre optic cables resulting in data disruption and fines; (5) Strike of oil/fuel pipelines causing environmental contamination. Risk level: HIGH. Control measures must be strictly adhered to before any excavation commences.',
'1. Obtain service drawings and plans from client/utility companies before works commence. 2. Conduct desktop study of all known services in the area. 3. Perform CAT and Genny sweep of the proposed excavation area following manufacturers instructions. 4. Mark all detected services with標準 paint or marker posts. 5. Maintain minimum 600mm clearance from all detected services where practicable. 6. Use safe digging techniques (hand digging or vacuum excavation) within 500mm of marked services. 7. Maintain trained operative with CAT/Genny on site at all times during excavation. 8. Stop works immediately if unmarked service is encountered. 9. Report all near-misses and service strikes.',
'Safety helmet, high-visibility vest, steel toe-capped boots, gloves, hard hat lamp for dark conditions, CAT/Genny certification card',
90, true, NOW(), NOW()
);

-- Template 2: Deep Excavation with Trench Box
INSERT INTO rams_templates (title, description, trade, risk_assessment, method_statement, ppe_required, frequency_days, is_active, created_at, updated_at)
VALUES (
'Deep Excavation with Trench Box Installation',
'Excavation of deep trenches exceeding 1.2m depth requiring the installation of proprietary trench box or drag box for person protection in accordance with CDM 2015 and HSE guidance.',
'Excavation',
'Risks include: (1) Trench collapse and burial of operatives - FATAL hazard; (2) Ingress of water causing instability; (3) Strike of underground services in deep excavations; (4) Manual handling injuries during box installation; (5) Falls into excavation; (6) Struck by plant and equipment. Risk level: VERY HIGH. No personnel to enter unshored excavations exceeding 1.2m depth.',
'1. Appoint competent supervisor to oversee all deep excavation works. 2. Complete excavations in short sections to minimise exposure time. 3. Install appropriate trench box before personnel entry - box must extend 600mm above trench bottom. 4. Lower trench box using plant - never jump into box. 5. Personnel must remain within the protection zone of the trench box at all times. 6. Maintain safe egress via ladder or ramp every 7.5m maximum. 7. Monitor weather conditions - stop works if heavy rain forecast. 8. Ensure emergency rescue equipment is readily available. 9. Carry out daily inspections and record in site register.',
'Safety helmet, safety boots, high-visibility clothing, gloves, safety harness with lanyard for entry/exit, rescue equipment',
30, true, NOW(), NOW()
);

-- Template 3: Confined Space Entry - Manholes and Chambers
INSERT INTO rams_templates (title, description, trade, risk_assessment, method_statement, ppe_required, frequency_days, is_active, created_at, updated_at)
VALUES (
'Confined Space Entry to Manholes and Chambers',
'Entry into confined spaces such as manholes, chambers, and storage tanks for inspection, cleaning, repair, or installation works. Complies with the Confined Spaces Regulations 1997.',
'Maintenance',
'Confined space entry presents multiple life-threatening hazards: (1) Oxygen deficiency leading to asphyxiation; (2) Toxic atmospheres from sewer gases (hydrogen sulphide, methane); (3) Flammable atmospheres and explosion risk; (4) Drowning hazard from ingress of water; (5) Entrapment by materials or structural collapse; (6) Heat stress in confined spaces. Risk level: VERY HIGH. Entry must only occur after atmosphere testing confirms safe conditions.',
'1. Obtain permit to work for all confined space entries. 2. Test atmosphere using calibrated 4-gas detector before entry - oxygen 19.5-23.5%, LEL below 10%, H2S below 10ppm, CO below 10ppm. 3. Ventilate space using forced air extraction equipment. 4. Establish safety standby person outside at all times. 5. Maintain continuous atmospheric monitoring. 6. Use tripod and full-body harness for emergency extraction. 7. Ensure communication system between entrant and standby person. 8. Have emergency response team on standby with resuscitation equipment. 9. Complete all works efficiently and exit immediately if conditions change.',
'Full-face respirator with appropriate filters, safety harness, gas monitor, safety boots, gloves, waterproof clothing, emergency escape set',
30, true, NOW(), NOW()
);

-- Template 4: Hydraulic Breaker Operation
INSERT INTO rams_templates (title, description, trade, risk_assessment, method_statement, ppe_required, frequency_days, is_active, created_at, updated_at)
VALUES (
'Hydraulic Breaker Operation',
'Use of hydraulic breakers attached to excavators or stand-alone units for breaking concrete, rock, or hard surfaces. Controls vibration and noise hazards in accordance with the Control of Vibration at Work Regulations 2005.',
'Demolition',
'Significant hazards include: (1) Noise exposure exceeding 85dB(A) requiring hearing protection; (2) Hand-arm vibration syndrome (HAVS) from prolonged breaker use; (3) Flying debris and projectiles; (4) Dust inhalation including silica; (5) Struck by breaker attachment; (6) Overturning of plant on unstable ground; (7) Underground service strike. Risk level: HIGH. Daily exposure limits for HAV must be controlled.',
'1. Ensure breaker is in good working condition with appropriate attachments. 2. Position plant on stable, level ground with outriggers deployed if fitted. 3. Establish exclusion zone minimum 10m from breaker works. 4. Operatives within the zone must wear hearing protection and dust masks. 5. Monitor vibration exposure time - refer to manufacturer data for vibration levels. 6. Implement task rotation to limit individual exposure. 7. Use water suppression for dust control. 8. Inspect work area for underground services before commencing. 9. Check for and remove any loose reinforcement before breaking.',
'Safety helmet, ear defenders, dust mask (FFP3 minimum), safety boots, high-visibility clothing, anti-vibration gloves, eye protection',
90, true, NOW(), NOW()
);

-- Template 5: Hot Works on Site
INSERT INTO rams_templates (title, description, trade, risk_assessment, method_statement, ppe_required, frequency_days, is_active, created_at, updated_at)
VALUES (
'Hot Works Operations Including Cutting and Welding',
'All hot works including metal cutting, welding, brazing, and grinding operations on site. Must comply with the Fire Safety (England) Regulations 2022 and HSE guidance on fire prevention.',
'Fabrication',
'Hot works present fire and explosion risks including: (1) Fire ignition from sparks and hot metal; (2) Explosion where cutting near flammable atmospheres; (3) Burns to operatives from hot materials and UV radiation; (4) Toxic fumes from welding and cutting including metal fumes; (5) Eye damage from arc flash. Risk level: HIGH. Hot work permit mandatory.',
'1. Obtain hot work permit from site manager before commencing. 2. Clear area of all combustible materials within 10m. 3. Provide appropriate fire extinguishers (CO2 and water) and fire blanket. 3. Post fire watch operative during and for minimum 60 minutes after works. 4. Screen work area to contain sparks. 5. Wear appropriate PPE for welding/cutting - welding mask, gauntlet gloves, leather apron. 6. Ensure adequate ventilation for fume extraction. 7. Use local exhaust ventilation for enclosed spaces. 8. Check work area thoroughly 30 minutes after completion. 9. Record all hot works in permit register.',
'Welding helmet with appropriate lenses, leather gauntlets, leather apron, safety boots, hearing protection, respirator with particle filter for grinding',
30, true, NOW(), NOW()
);

-- Template 6: Lifting Operations with Quick-Hitch Excavator
INSERT INTO rams_templates (title, description, trade, risk_assessment, method_statement, ppe_required, frequency_days, is_active, created_at, updated_at)
VALUES (
'Lifting Operations Using Quick-Hitch Excavator',
'Lifting operations using an excavator fitted with quick-hitch attachment for lifting purpose. Requires appointed person for lifting operations and compliance with LOLER 1998 and PUWER 1998.',
'Plant Operations',
'Lifting with quick-hitch excavators presents significant hazards: (1) Uncontrolled release of load due to incorrect hitch engagement; (2) Failure of lifting accessories; (3) Overturning of excavator during lift; (4) Struck by swinging load; (5) Collision with structures or operatives. Risk level: VERY HIGH. Quick-hitch must be checked and secured before every lift.',
'1. Appoint competent banksman/signalman for all lifting operations. 2. Ensure quick-hitch is inspected and certified - check safety pin and latch operation. 3. Verify excavator has valid LOLER certificate for lifting duty. 4. Ensure lifting accessories are appropriate for load and have current thorough examination. 5. Establish lift plan including load weight calculation and exclusion zone. 6. Carry out trial lift at low level before full lift. 7. Maintain communications between banksman and operator at all times. 8. Never allow personnel under suspended loads. 9. Record all lifts in site lifting register.',
'High-visibility clothing, safety boots, hard hat, gloves, safety harness if working under load area',
90, true, NOW(), NOW()
);

-- Template 7: Working at Height - Kerb Gangs
INSERT INTO rams_templates (title, description, trade, risk_assessment, method_statement, ppe_required, frequency_days, is_active, created_at, updated_at)
VALUES (
'Working at Height for Kerb Laying Operations',
'Kerb laying operations often require working at height when placing kerbs above ground level or working from vehicles. Complies with Work at Height Regulations 2005.',
'Kerbing',
'Working at height risks include: (1) Falls from height during kerb placement; (2) Manual handling injuries from kerb weights (up to 100kg); (3) Struck by falling kerbs; (4) Slips on wet concrete or uneven ground; (5) Vehicle strikes on busy roads. Risk level: HIGH. Hierarchy of controls must be applied.',
'1. Eliminate working at height where possible - use mechanical aids. 2. Use appropriate equipment: kerb grab, hydraulic kerb machine, or 2-person lift technique. 3. Where working at height is unavoidable, use mobile elevated work platform or correctly erected scaffold platform. 4. Ensure all personnel are trained in working at height. 5. Use kick boards and guardrails on any MEWP. 6. Establish safe zone around kerb laying area. 7. Use spotter/signaler when working near traffic. 8. Wear appropriate PPE including eye protection for debris.',
'Safety helmet, safety boots, high-visibility clothing, gloves, eye protection, safety harness if using MEWP',
90, true, NOW(), NOW()
);

-- Template 8: DSEAR - Fuel Storage on Site
INSERT INTO rams_templates (title, description, trade, risk_assessment, method_statement, ppe_required, frequency_days, is_active, created_at, updated_at)
VALUES (
'DSEAR Assessment - Fuel Storage and Handling',
'Storage and handling of diesel, petrol, and other flammable liquids on site in accordance with the Dangerous Substances and Explosive Atmospheres Regulations 2002 (DSEAR).',
'Site Management',
'DSEAR hazards include: (1) Fire and explosion from flammable vapour ignition; (2) Environmental pollution from fuel spills; (3) Toxic fumes from fuel evaporation; (4) Skin contact and absorption of hydrocarbons; (5) Static discharge during refuelling. Risk level: HIGH. Fuel storage areas must be designated and controlled.',
'1. Store all fuel in appropriately bunded tanks meeting PPG2 guidelines - 110% capacity of stored fuel. 2. Position fuel storage minimum 10m from buildings and drainage. 3. Prohibit smoking and naked lights within 5m of fuel storage. 4. Maintain plant and equipment away from storage area during refuelling. 5. Use drip trays during refuelling operations. 6. Have fire extinguisher (foam or CO2) available during refuelling. 7. Wear appropriate PPE: chemical-resistant gloves, safety boots, eye protection. 8. Dispose of contaminated materials as hazardous waste. 9. Maintain records of all fuel deliveries and usage.',
'Chemical-resistant gloves, safety boots, eye protection, high-visibility clothing, fire extinguisher within reach',
90, true, NOW(), NOW()
);

-- Template 9: COSHH - Cement and Silica Dust
INSERT INTO rams_templates (title, description, trade, risk_assessment, method_statement, ppe_required, frequency_days, is_active, created_at, updated_at)
VALUES (
'COSHH Assessment - Cementitious Products and Silica Dust',
'Handling and use of cement, concrete, mortar, and exposure to silica dust from excavation and cutting operations. Complies with COSHH Regulations 2002.',
'General',
'Health hazards from cement and silica include: (1) Cement burns - wet cement causes severe chemical burns; (2) Occupational asthma from cement dust; (3) Allergic dermatitis from chromium VI; (4) Silicosis from respirable crystalline silica - irreversible lung disease; (5) Lung cancer from prolonged silica exposure; (6) COPD from dust inhalation. Risk level: HIGH. Engineering controls and PPE essential.',
'1. Use water suppression or local exhaust ventilation for cutting/drilling concrete. 2. Wear RPE - FFP3 dust mask minimum for cement work. 3. Wear chemical-resistant gloves and waterproof clothing when handling wet cement. 4. Wash hands before eating, drinking, or smoking. 5. Provide welfare facilities including showers where exposed to cement. 6. Store cement products in dry conditions. 7. Avoid dry-sweeping of concrete dust - use vacuum or wet methods. 8. Health surveillance for operatives regularly exposed. 9. Display COSHH signage at work areas.',
'FFP3 respirator, chemical-resistant gloves, waterproof clothing, safety boots, eye protection, barrier cream',
90, true, NOW(), NOW()
);

-- Template 10: General Plant Movement
INSERT INTO rams_templates (title, description, trade, risk_assessment, method_statement, ppe_required, frequency_days, is_active, created_at, updated_at)
VALUES (
'General Plant Movement and Plant Management',
'Movement of construction plant including excavators, dumpers, rollers, and telehandlers around site. Includes loading/unloading from transport and crossing public highways.',
'Plant Operations',
'Plant movement hazards include: (1) Collision with pedestrians - fatal risk; (2) Overturning of plant on uneven ground; (3) Falls from plant during mounting/dismounting; (4) Struck by moving plant; (5) Contact with overhead power lines; (6) Noise exposure from plant engines. Risk level: HIGH. Segregation of plant and pedestrians essential.',
'1. Establish traffic management plan for site with designated plant routes. 2. Maintain minimum 1m clearance between plant and pedestrians where possible. 3. Use banksmen for all reversing operations. 4. Wear high-visibility clothing at all times in plant areas. 5. Use designed crossing points for pedestrians. 6. Ensure plant is maintained and has valid inspection/test certificates. 7. Daily pre-use inspections by plant operators. 8. Wheel chocks and vehicle stop blocks when parked on inclines. 9. No passengers on plant unless seat provided.',
'High-visibility vest, safety boots, hard hat, gloves, ear defenders for noisy plant',
90, true, NOW(), NOW()
);

-- Template 11: Traffic Management
INSERT INTO rams_templates (title, description, trade, risk_assessment, method_statement, ppe_required, frequency_days, is_active, created_at, updated_at)
VALUES (
'Traffic Management on Public Roads and Site',
'Setting out and maintaining traffic management systems for works on or adjacent to public highways. Includes temporary traffic lights, road closures, convoy operations, and pedestrian management.',
'Traffic Management',
'Traffic management risks include: (1) Struck by moving traffic - potentially fatal; (2) Collision between vehicles; (3) Pedestrian conflicts with traffic; (4) Reversing accidents; (5) Fatigue of traffic management operatives. Risk level: VERY HIGH. Competent traffic management operative must supervise.',
'1. Ensure operatives are trained and qualified for traffic management role. 2. Use appropriate traffic management design for works type. 3. Wear high-visibility clothing to Class 3 standard. 4. Work facing traffic where possible. 5. Use vehicles as barriers where available. 6. Brief all personnel on emergency procedures. 7. Inspect traffic management daily and after severe weather. 8. Use cone-based systems for night-time visibility. 9. Maintain clear egress route from work area.',
'Class 3 high-visibility clothing, safety boots, hard hat, traffic management qualification card, torch for night work',
90, true, NOW(), NOW()
);

-- Template 12: Overhead Power Line Proximity
INSERT INTO rams_templates (title, description, trade, risk_assessment, method_statement, ppe_required, frequency_days, is_active, created_at, updated_at)
VALUES (
'Works Near Overhead Power Lines',
'Any works conducted in proximity to overhead electrical power lines including excavation, plant operation, and material handling. Electricity can arc through air causing fatal shock.',
'General',
'Overhead power line hazards are extremely severe: (1) Electrocution from direct contact with lines; (2) Electrocution from arcing/discharge through air; (3) Plant or materials contacting lines causing energisation of equipment; (4) Fire following electrical contact. Risk level: FATAL. Safe distances must be maintained or lines isolated.',
'1. Contact electricity supplier to identify and mark all overhead lines. 2. Establish safe clearances: 10m horizontal and vertical for normal voltage lines. 3. Erect warning signs and goal posts at access points. 4. Use banksman for all plant operations within 15m of lines. 5. Prepare written scheme of work for works near lines. 6. Do not exceed machinery height/dimension without specific approval. 7. If lines must be contacted, treat as live and call 105 for power isolation. 8. Never assume lines are safe - always verify with electricity company.',
'Safety helmet, safety boots, high-visibility clothing, insulated tools if required for work near lines, fire extinguisher',
90, true, NOW(), NOW()
);

-- Template 13: Working Near Railways
INSERT INTO rams_templates (title, description, trade, risk_assessment, method_statement, ppe_required, frequency_days, is_active, created_at, updated_at)
VALUES (
'Works Adjacent to or Over Railway Lines',
'Construction and maintenance works within the railway boundary or in close proximity to operational railway lines. Subject to Network Rail or Train Operating Company requirements.',
'Railway Works',
'Railway hazards include: (1) Struck by train - fatal; (2) Electrocution from 25kV overhead line equipment (OHLE); (3) Fall from height onto tracks; (4) Trespass by public onto works area; (5) Vibration from passing trains affecting structural stability. Risk level: FATAL. Railway works require formal authority and briefing.',
'1. Obtain necessary authority from Network Rail/asset owner before works. 2. Complete Safe Work Package with railway undertaker. 3. Erect exclusion fencing to prevent access to tracks. 4. Maintain safe distance from OHLE - minimum 2.75m horizontal and vertical. 5. Use Lookout Person for works near running lines. 6. Report any irregular occurrences to Network Rail immediately. 7. Wear colour-coded PPE as required by railway standards. 8. No works during possessions without formal handback procedure. 9. Maintain emergency communication equipment.',
'Railway-standard PPE including orange hi-vis, safety boots, hard hat, safety glasses, ear defenders, Personal Track Safety (PTS) card if required',
90, true, NOW(), NOW()
);

-- Template 14: Asbestos-Containing Materials
INSERT INTO rams_templates (title, description, trade, risk_assessment, method_statement, ppe_required, frequency_days, is_active, created_at, updated_at)
VALUES (
'Work with Asbestos-Containing Materials (Notifiable Work)',
'Works that may disturb asbestos-containing materials (ACMs) found in buildings, roads, or buried services. Asbestos is the biggest occupational killer in the UK. All notifiable work requires HSE notification.',
'General',
'Asbestos exposure causes: (1) Mesothelioma - cancer of lung lining - invariably fatal; (2) Asbestosis - scarring of lung tissue; (3) Lung cancer; (4) Pleural plaques. Asbestos is fatal when fibres are inhaled. Risk level: VERY HIGH for notifiable work. Only licensed contractors can remove certain ACMs.',
'1. Conduct asbestos survey before works commence. 2. If ACMs identified, engage licensed asbestos removal contractor. 3. Obtain HSE notification for notifiable work. 4. Establish exclusion zone with decontamination unit. 5. Wear full-face RPE and disposable coveralls. 6. Use wet methods to suppress dust. 7. Double-bag waste in UN-approved asbestos waste bags. 8. Dispose at licensed waste facility. 9. Provide health records for exposed operatives.',
'Full-face respirator (FFP3), disposable coveralls (Type 5/6), safety boots, gloves, eye protection, decontamination facilities',
90, true, NOW(), NOW()
);

-- Template 15: Vibratory Compaction
INSERT INTO rams_templates (title, description, trade, risk_assessment, method_statement, ppe_required, frequency_days, is_active, created_at, updated_at)
VALUES (
'Vibratory Plate Compactors and Rollers',
'Operation of pedestrian-operated vibratory plate compactors and ride-on rollers for ground compaction. Subject to Control of Vibration at Work Regulations 2005.',
'Groundwork',
'Vibratory compaction hazards: (1) Hand-arm vibration syndrome (HAVS) from prolonged plate use; (2) Noise exposure from machinery; (3) Musculoskeletal disorders from whole-body vibration on rollers; (4) Noise-induced hearing loss; (5) Burns from hot exhausts; (6) Entrapment/crushing between roller and obstacles. Risk level: MEDIUM-HIGH. Exposure time limits must be monitored.',
'1. Select appropriate compactor for material type and layer thickness. 2. Conduct daily pre-use inspection of compactor. 3. Use hearing protection - noise levels can exceed 85dB(A). 4. Rotate operators to control vibration exposure. 5. Maintain firm grip on plate compactor handles. 6. Wear vibration-dampening gloves. 7. Check area for underground services before compaction. 8. Keep hands away from vibrating plate. 9. Allow machine to reach full speed before beginning compaction.',
'Ear defenders, safety boots, gloves with vibration damping, high-visibility clothing, dust mask (FFP2 for dusty conditions)',
90, true, NOW(), NOW()
);

-- Template 16: Piling and Piling Mat Construction
INSERT INTO rams_templates (title, description, trade, risk_assessment, method_statement, ppe_required, frequency_days, is_active, created_at, updated_at)
VALUES (
'Piling Operations and Piling Mat Construction',
'Construction of piling mat and associated piling operations including bored piles, driven piles, and CFA piles. Requires appointed person for lifting and piling.',
'Piling',
'Piling hazards include: (1) Struck by falling pile sections or augers; (2) Crushed by plant and equipment; (3) Falls from elevated work platforms; (4) Contact with underground services; (5) Noise and vibration exposure; (6) Ground collapse during bore construction; (7) Chemical exposure from concrete. Risk level: VERY HIGH.',
'1. Appoint competent piling supervisor and banksman. 2. Construct piling mat to adequate specification for plant loads. 3. Establish exclusion zone during piling operations. 4. Inspect all lifting equipment and pile frames before use. 5. Use correct lifting techniques with tag lines. 6. Maintain clear communication between rig operator and ground crew. 7. Wear hearing protection near piling rig. 8. Control concrete pours to prevent segregation. 9. Monitor for ground movement near existing structures.',
'Safety helmet, ear defenders, safety boots, high-visibility clothing, gloves, eye protection, safety harness for elevated work',
90, true, NOW(), NOW()
);

-- Template 17: Concrete Pours - Ready Mixed
INSERT INTO rams_templates (title, description, trade, risk_assessment, method_statement, ppe_required, frequency_days, is_active, created_at, updated_at)
VALUES (
'Concrete Pouring Operations - Ready Mixed Concrete',
'Receiving, placing, vibrating, and finishing ready-mixed concrete from ready-mix trucks. Includes pumping operations. Subject to concrete health hazards and workplace exposure limits.',
'Concrete',
'Concrete pouring hazards: (1) Cement burns from wet concrete; (2) Splashes to eyes; (3) Manual handling injuries from shuttering and reinforcement; (4) Slips on wet concrete; (5) Trapped by flowing concrete - FATAL; (6) Vehicle movements from concrete trucks; (7) Electric shock from vibrator equipment. Risk level: HIGH.',
'1. Prepare works area and ensure adequate access for concrete trucks. 2. Brief all operatives on concrete hazards and emergency procedures. 3. Wear appropriate PPE: waterproof boots, gloves, eye protection. 4. Use pump line or tremie for deep pours. 5. Never enter formwork with wet concrete unless safe system of work established. 6. Position pumps away from overhead power lines. 7. Maintain clean work area to prevent slips. 8. Provide washing facilities for cement removal. 9. Record all concrete deliveries and test cubes taken.',
'Waterproof boots, rubber gloves, safety glasses, high-visibility clothing, hard hat, knee pads for finishing work',
90, true, NOW(), NOW()
);

-- Template 18: Steel Fixers
INSERT INTO rams_templates (title, description, trade, risk_assessment, method_statement, ppe_required, frequency_days, is_active, created_at, updated_at)
VALUES (
'Steel Fixing Operations',
'Cutting, bending, placing, and tying reinforcement steel bars for concrete structures. Manual handling hazards from heavy reinforcement bundles.',
'Reinforcement',
'Steel fixing hazards: (1) Musculoskeletal injuries from manual handling of heavy rebar; (2) Cuts and lacerations from rebar ends; (3) Eye injuries from wire snapping; (4) Slips, trips, and falls on uneven ground; (5) Struck by suspended loads during crane placement; (6) Heat stress when working with dark rebar in summer. Risk level: MEDIUM-HIGH.',
'1. Use mechanical handling aids where possible (cranes, forklifts for bundles). 2. Wear cut-resistant gloves and eye protection. 3. Use bending stools and rebar cutters with guards. 4. Cap or bend over protruding rebar ends. 5. Establish safe zones for manual handling. 6. Wear steel toe-capped boots with sole protection. 7. Maintain tidy work area free of tripping hazards. 8. Use knee pads when working at low levels. 9. Use bar crimpers rather than bending by hand.',
'Cut-resistant gloves, safety glasses, steel toe-capped boots, high-visibility clothing, knee pads, dust mask when cutting',
90, true, NOW(), NOW()
);

-- Template 19: Drainage Installation
INSERT INTO rams_templates (title, description, trade, risk_assessment, method_statement, ppe_required, frequency_days, is_active, created_at, updated_at)
VALUES (
'Drainage Installation Works',
'Installation of surface water and foul drainage systems including pipes, chambers, manholes, and connections. May involve deep excavation and confined space entry.',
'Drainage',
'Drainage installation hazards: (1) Trench collapse during pipe bedding; (2) Underground service strikes; (3) Confined space entry for manhole construction; (4) Manual handling of heavy pipes and chamber rings; (5) Working near roads and traffic; (6) Environmental pollution from drainage works. Risk level: HIGH.',
'1. Locate all underground services before excavation. 2. Implement appropriate trench support or battering. 3. Use mechanical aids for pipe handling where possible. 4. Implement confined space procedures for manhole entry. 5. Maintain clean water environment - prevent pollution to watercourses. 6. Wear appropriate PPE: safety boots, gloves, high-vis, hard hat. 7. Use traffic management when working on roads. 8. Inspect chambers before entry. 9. Test completed drainage before backfilling.',
'Safety boots, gloves, high-visibility clothing, hard hat, eye protection, confined space equipment when required',
90, true, NOW(), NOW()
);

-- Template 20: Attenuation Tank Installation
INSERT INTO rams_templates (title, description, trade, risk_assessment, method_statement, ppe_required, frequency_days, is_active, created_at, updated_at)
VALUES (
'Geocellular Attenuation Tank Installation',
'Installation of geocellular storage crates, flow controls, and associated drainage for stormwater attenuation systems. Includes excavation, membrane laying, and backfill.',
'Drainage',
'Attenuation tank hazards: (1) Deep excavation collapse; (2) Underground service strikes; (3) Manual handling of large crate units; (4) Working with geomembranes - slip hazards; (5) Confined space entry for flow control chambers; (6) Plant movement on soft ground. Risk level: HIGH.',
'1. Carry out service location and mark all services. 2. Excavate to design level with stable batters or support. 3. Place and compact sub-base before membrane installation. 4. Install geotextile membrane ensuring overlap and jointing. 5. Assemble attenuation crates following manufacturer instructions. 6. Use mechanical lifting aids for crate placement. 7. Maintain access for plant during backfill. 8. Install flow control chamber with confined space precautions. 9. Test system before covering.',
'Safety boots, gloves, high-visibility clothing, hard hat, eye protection, life jacket if working near water',
90, true, NOW(), NOW()
);

-- Template 21: Section 278 Traffic Management
INSERT INTO rams_templates (title, description, trade, risk_assessment, method_statement, ppe_required, frequency_days, is_active, created_at, updated_at)
VALUES (
'Section 278 Highway Works Traffic Management',
'Implementation of traffic management schemes for Section 278 highway improvement works on public roads. Requires TMP approval from Highway Authority.',
'Highways',
'Section 278 traffic management hazards: (1) Struck by live traffic; (2) Vehicle collisions within works area; (3) Pedestrian conflicts with traffic; (4) Reversing plant and vehicles; (5) Fatigue during long works; (6) Night-time works with reduced visibility. Risk level: VERY HIGH.',
'1. Obtain Traffic Management Plan approval from Highway Authority. 2. Implement traffic management following Chapter 8 of the Traffic Signs Manual. 3. Use qualified traffic management operatives. 4. Brief all site personnel on TMP requirements. 5. Wear Class 3 high-visibility clothing. 6. Use vehicles as barriers where possible. 7. Provide adequate cone and sign inventory. 8. Inspect TM arrangement daily and after breaks. 9. Maintain pedestrian and cycle access throughout.',
'Class 3 high-visibility suit, safety boots, hard hat, traffic management qualification, torch for night works',
90, true, NOW(), NOW()
);

-- Template 22: Scaffold Erection and Dismantling
INSERT INTO rams_templates (title, description, trade, risk_assessment, method_statement, ppe_required, frequency_days, is_active, created_at, updated_at)
VALUES (
'Scaffolding Erection, Modification and Dismantling',
'Erection, modification, and dismantling of scaffolding systems for access to construction works. Requires competent scaffolder and compliance with TG20:21.',
'Scaffolding',
'Scaffolding hazards: (1) Falls from height during erection/dismantling - FATAL; (2) Struck by falling scaffold components; (3) Structural collapse of incomplete scaffold; (4) Overloading of scaffold platforms; (5) Electrocution from overhead lines; (6) Scaffolder struck by plant. Risk level: VERY HIGH.',
'1. Ensure only competent scaffolders erect/modify scaffolding. 2. Prepare scaffold specification for complex structures. 3. Erect toe boards, guard rails, and brick guards. 4. Use safety harnesses during erection and dismantling. 5. Maintain clear zone below scaffold works. 6. Use appropriate base plates and sole boards on soft ground. 7. Tag scaffold on completion with safe working load. 8. Inspect scaffold after severe weather. 9. Carry out handover inspection before use.',
'Safety harness, safety boots, hard hat, gloves, high-visibility clothing, tool lanyard',
90, true, NOW(), NOW()
);

-- Template 23: Temporary Works
INSERT INTO rams_templates (title, description, trade, risk_assessment, method_statement, ppe_required, frequency_days, is_active, created_at, updated_at)
VALUES (
'Temporary Works Management',
'Design, installation, use, and removal of temporary works including shoring, falsework, cofferdams, and temporary bridges. Appointed Temporary Works Coordinator required.',
'General',
'Temporary works hazards: (1) Structural failure of temporary works - potentially fatal; (2) Overloading of temporary structures; (3) Ground instability affecting temporary works; (4) Incompatible temporary and permanent works; (5) Removal causing collapse of permanent works. Risk level: VERY HIGH. Formal TW procedures essential.',
'1. Appoint Temporary Works Coordinator before works commence. 2. Prepare design brief and obtain design by competent person. 3. Check design against site conditions. 4. Issue permit to load where applicable. 5. Maintain register of all temporary works on site. 6. Inspect temporary works before each use. 7. Brief all operatives on loading restrictions. 8. Obtain formal sign-off before removing temporary works. 9. Record all temporary works in register.',
'Safety boots, hard hat, high-visibility clothing, gloves appropriate to task, safety harness if working at height',
90, true, NOW(), NOW()
);

-- Template 24: Site Decommissioning
INSERT INTO rams_templates (title, description, trade, risk_assessment, method_statement, ppe_required, frequency_days, is_active, created_at, updated_at)
VALUES (
'Site Decommissioning and Reinstatement',
'Decommissioning of site compound, welfare facilities, and plant; removal of temporary works; and final site reinstatement. Includes works to adopted standard.',
'Site Management',
'Decommissioning hazards: (1) Structural collapse during removal of temporary works; (2) Underground service strikes during reinstatement; (3) Vehicle movements with increased plant activity; (4) Dust and noise affecting neighbours; (5) Environmental contamination from site activities. Risk level: MEDIUM-HIGH.',
'1. Prepare decommissioning plan identifying all items to be removed. 2. Ensure temporary works are removed in correct sequence. 3. Check for buried services before ground reinstatement. 4. Remove all temporary drainage connections. 5. Reinstate damaged areas to specification. 6. Remove all waste from site legally. 7. Carry out land quality assessment if required. 8. Restore site compound area to original condition. 9. Obtain sign-off from client and Highway Authority.',
'Safety boots, gloves, high-visibility clothing, hard hat, dust mask, eye protection',
90, true, NOW(), NOW()
);

-- Template 25: Lone Working
INSERT INTO rams_templates (title, description, trade, risk_assessment, method_statement, ppe_required, frequency_days, is_active, created_at, updated_at)
VALUES (
'Lone Working Procedures',
'Works conducted where operatives are alone without direct supervision, including early starts, night works, and isolated locations. Requires formal lone working risk assessment.',
'General',
'Lone working hazards: (1) Injury with no one to assist; (2) Sudden illness with no one to raise alarm; (3) Violence and aggression from members of public; (4) Getting lost or trapped; (5) Inclement weather causing hypothermia/heat stress; (6) Reduced communication ability. Risk level: HIGH. Lone working must be pre-approved.',
'1. Obtain line manager approval for lone working. 2. Ensure mobile phone signal available or radio communication. 3. Brief supervisor on location, duration, and expected finish time. 4. Check in with supervisor at agreed intervals. 5. Carry out dynamic risk assessment on arrival if conditions changed. 6. Wear mobile phone on person at all times. 7. Maintain regular check-in calls. 8. Have emergency contact procedure. 9. If no contact made, initiate emergency response.',
'Mobile phone with emergency contacts, hi-vis clothing if near traffic, hard hat, safety boots, torch for dark conditions, first aid kit',
90, true, NOW(), NOW()
);

-- Template 26: Mental Health Awareness
INSERT INTO rams_templates (title, description, trade, risk_assessment, method_statement, ppe_required, frequency_days, is_active, created_at, updated_at)
VALUES (
'Mental Health Awareness and Stress Management',
'Recognition and management of stress, mental health issues, and wellbeing in the construction industry. Addresses high suicide rates and industry pressures.',
'Site Management',
'Mental health hazards: (1) Work-related stress leading to mental ill health; (2) Depression and anxiety from work pressures; (3) Substance misuse as coping mechanism; (4) Suicide risk - construction has highest rate of any industry; (5) Cumulative stress affecting home life. Risk level: HIGH. Mental health is a leading cause of absence.',
'1. Train all supervisors in mental health first aid. 2. Display mental health support information prominently. 3. Encourage open conversations about mental health. 4. Monitor workloads to prevent excessive pressure. 5. Provide access to employee assistance programme. 6. Ensure working hours do not cause excessive fatigue. 7. Encourage work-life balance. 8. Reduce stigma around mental health discussions. 9. Know how to refer to professional support services.',
'No specific PPE required for awareness training; comfortable clothing for wellbeing discussions',
180, true, NOW(), NOW()
);

-- Template 27: Drug and Alcohol
INSERT INTO rams_templates (title, description, trade, risk_assessment, method_statement, ppe_required, frequency_days, is_active, created_at, updated_at)
VALUES (
'Drug and Alcohol Policy and Testing',
'Implementation of site drug and alcohol policy including testing procedures and support for substance misuse. Addresses impairment risks on site.',
'Site Management',
'Drug and alcohol hazards: (1) Impaired judgment leading to accidents; (2) Slowed reaction times; (3) Increased likelihood of risk-taking behaviour; (4) Violence and aggression; (5) Absenteeism and presenteeism. Risk level: HIGH. Impairment can cause fatal accidents.',
'1. Implement site drug and alcohol policy. 2. Conduct pre-employment screening. 3. Perform random testing programme. 4. Test following incidents or near-misses. 5. Provide confidential support for those seeking help. 6. Ensure testing is conducted professionally with chain of custody. 7. Apply disciplinary procedures for positive tests. 8. Provide access to rehabilitation programmes. 9. Train supervisors in recognising impairment signs.',
'No specific PPE; testing kits, breathalyser, chain of custody documentation',
180, true, NOW(), NOW()
);

-- Template 28: COVID-Secure Working
INSERT INTO rams_templates (title, description, trade, risk_assessment, method_statement, ppe_required, frequency_days, is_active, created_at, updated_at)
VALUES (
'COVID-19 Secure Working Practices',
'Maintaining COVID-19 secure working practices to reduce transmission of respiratory infections including COVID-19, flu, and colds in construction workplace.',
'Site Management',
'Infectious disease hazards: (1) COVID-19 and variant transmission; (2) Seasonal flu outbreaks; (3) General respiratory infections; (4) Close contact during work activities. Risk level: MEDIUM. Control measures reduce transmission risk.',
'1. Maintain good ventilation in enclosed spaces. 2. Encourage vaccination against respiratory diseases. 3. Provide hand sanitiser at welfare facilities. 4. Clean frequently touched surfaces regularly. 5. Encourage staying home when unwell. 6. Use masks where social distancing not possible indoors. 7. Maximise outdoor working where possible. 8. Display symptom awareness information. 9. Review risk assessment if outbreak occurs.',
'Hand sanitiser, face masks (where required), cleaning materials, symptom information cards',
90, true, NOW(), NOW()
);

-- Template 29: Environmental - Spill Containment
INSERT INTO rams_templates (title, description, trade, risk_assessment, method_statement, ppe_required, frequency_days, is_active, created_at, updated_at)
VALUES (
'Environmental Management - Spill Containment',
'Prevention and response to fuel, oil, and chemical spills to protect watercourses, groundwater, and soil. Complies with Environmental Permitting Regulations and PPG2.',
'Environmental',
'Environmental spill hazards: (1) Pollution of watercourses - harm to wildlife; (2) Contamination of groundwater sources; (3) Soil contamination requiring remediation; (4) Regulatory fines and prosecution; (5) Clean-up costs. Risk level: HIGH. Prevention is essential - remediation is costly.',
'1. Store all fuels and chemicals in double-skinned or bunded containers. 2. Maintain drip trays under all static plant. 3. Position refuelling points away from drains and watercourses. 4. Provide spill kits at strategic locations. 5. Train operatives in spill response. 6. Report all spills immediately to supervisor. 7. Contain spills using sand, booms, or absorbent materials. 8. Dispose of contaminated materials as hazardous waste. 9. Investigate cause and implement preventive measures.',
'Chemical-resistant gloves, safety boots, eye protection, hi-vis clothing, spill kit, disposable coveralls for clean-up',
90, true, NOW(), NOW()
);

-- Template 30: Skip-Lift and Gantry Operations
INSERT INTO rams_templates (title, description, trade, risk_assessment, method_statement, ppe_required, frequency_days, is_active, created_at, updated_at)
VALUES (
'Skip-Lift and Gantry Crane Operations',
'Operation of skip-lift equipment and gantry cranes for loading and unloading materials. LOLER and PUWER requirements apply to lifting equipment.',
'Plant Operations',
'Skip-lift and gantry hazards: (1) Falling loads from inadequate slinging; (2) Overturning of skip-lift equipment; (3) Crush injuries from uncontrolled load movement; (4) Electrocution from overhead lines; (5) Struck by moving skip or load; (6) Falls from skips during loading. Risk level: VERY HIGH.',
'1. Ensure all lifting equipment has valid LOLER examination certificate. 2. Appoint competent slinger/signaler for all lifts. 3. Check weight of loads before lifting - do not exceed SWL. 4. Use appropriate lifting accessories for load type. 5. Ensure safe slinging techniques are used. 6. Establish exclusion zone during lifting operations. 7. Do not ride on skips or gantry platforms. 8. Maintain safe distance from overhead power lines. 9. Inspect all lifting equipment before each use.',
'Safety helmet, safety boots, gloves, high-visibility clothing, safety harness for elevated positions, ear defenders near plant',
90, true, NOW(), NOW()
);
