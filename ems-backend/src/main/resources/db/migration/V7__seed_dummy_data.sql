-- =============================================================
-- V7: Seed Dummy Data for EMS Demo
-- Covers: machines, uploads, energy_readings, ai_insights,
--         alerts, alert_notifications, reports
-- Factory: Mumbai Main Plant (7fa85f64-5717-4562-b3fc-2c963f66afa6)
-- Admin:   0a58da81-30cb-467b-83cc-ef7870932c02
-- Manager: 1e48ba92-41cc-477c-94cc-df7870932c03
-- =============================================================

-- ---------------------------------------------------------------
-- MACHINES (8 machines across 4 departments)
-- ---------------------------------------------------------------
INSERT INTO machines (id, factory_id, name, department, machine_type, baseline_kwh, is_active) VALUES
  ('a1000001-0000-0000-0000-000000000001', '7fa85f64-5717-4562-b3fc-2c963f66afa6', 'CNC Machine 1',         'Machining',    'CNC Lathe',         42.500, true),
  ('a1000001-0000-0000-0000-000000000002', '7fa85f64-5717-4562-b3fc-2c963f66afa6', 'CNC Machine 2',         'Machining',    'CNC Milling',       38.750, true),
  ('a1000001-0000-0000-0000-000000000003', '7fa85f64-5717-4562-b3fc-2c963f66afa6', 'Welding Robot A',       'Assembly',     'Arc Welder',        55.200, true),
  ('a1000001-0000-0000-0000-000000000004', '7fa85f64-5717-4562-b3fc-2c963f66afa6', 'Welding Robot B',       'Assembly',     'MIG Welder',        51.800, true),
  ('a1000001-0000-0000-0000-000000000005', '7fa85f64-5717-4562-b3fc-2c963f66afa6', 'Air Compressor Unit 1', 'Utilities',    'Rotary Screw',      22.000, true),
  ('a1000001-0000-0000-0000-000000000006', '7fa85f64-5717-4562-b3fc-2c963f66afa6', 'Conveyor Belt System',  'Logistics',    'Belt Conveyor',     12.500, true),
  ('a1000001-0000-0000-0000-000000000007', '7fa85f64-5717-4562-b3fc-2c963f66afa6', 'Injection Molder 1',   'Plastics',     'Injection Molding', 68.300, true),
  ('a1000001-0000-0000-0000-000000000008', '7fa85f64-5717-4562-b3fc-2c963f66afa6', 'Heat Treatment Oven',  'Machining',    'Industrial Oven',   95.000, false);

-- ---------------------------------------------------------------
-- UPLOADS (3 historical uploads)
-- ---------------------------------------------------------------
INSERT INTO uploads (id, factory_id, uploaded_by, filename, original_name, row_count, status, factory_timezone, created_at, processed_at) VALUES
  ('b2000001-0000-0000-0000-000000000001', '7fa85f64-5717-4562-b3fc-2c963f66afa6', '1e48ba92-41cc-477c-94cc-df7870932c03',
   'telemetry_june_w1.xlsx',  'telemetry_june_w1.xlsx',  2016, 'SUCCESS', 'Asia/Kolkata',
   NOW() - INTERVAL '14 days', NOW() - INTERVAL '14 days' + INTERVAL '45 seconds'),
  ('b2000001-0000-0000-0000-000000000002', '7fa85f64-5717-4562-b3fc-2c963f66afa6', '1e48ba92-41cc-477c-94cc-df7870932c03',
   'telemetry_june_w2.xlsx',  'telemetry_june_w2.xlsx',  2016, 'SUCCESS', 'Asia/Kolkata',
   NOW() - INTERVAL '7 days',  NOW() - INTERVAL '7 days'  + INTERVAL '52 seconds'),
  ('b2000001-0000-0000-0000-000000000003', '7fa85f64-5717-4562-b3fc-2c963f66afa6', '0a58da81-30cb-467b-83cc-ef7870932c02',
   'telemetry_june_w3.xlsx',  'telemetry_june_w3.xlsx',  288,  'FAILED',  'Asia/Kolkata',
   NOW() - INTERVAL '2 days',  NOW() - INTERVAL '2 days'  + INTERVAL '3 seconds');

-- ---------------------------------------------------------------
-- ENERGY READINGS — last 7 days, hourly, 7 machines
-- Uses generate_series for realistic time-series data
-- ---------------------------------------------------------------
INSERT INTO energy_readings (machine_id, recorded_at, energy_kwh, active_kw, apparent_kva, reactive_kvar, power_factor, frequency, voltage_r, voltage_y, voltage_b, current_r, current_y, current_b, parts_produced, upload_id, source)
SELECT
  m.id                                                               AS machine_id,
  ts                                                                 AS recorded_at,
  ROUND((m.baseline_kwh * (0.80 + 0.30 * random()))::NUMERIC, 3)   AS energy_kwh,
  ROUND((m.baseline_kwh * (0.80 + 0.25 * random()))::NUMERIC, 2)   AS active_kw,
  ROUND((m.baseline_kwh * (0.95 + 0.20 * random()))::NUMERIC, 2)   AS apparent_kva,
  ROUND((m.baseline_kwh * (0.10 + 0.05 * random()))::NUMERIC, 2)   AS reactive_kvar,
  ROUND((0.88 + 0.09 * random())::NUMERIC, 3)                       AS power_factor,
  ROUND((49.80 + 0.40 * random())::NUMERIC, 2)                      AS frequency,
  ROUND((225 + 10 * random())::NUMERIC, 2)                          AS voltage_r,
  ROUND((224 + 10 * random())::NUMERIC, 2)                          AS voltage_y,
  ROUND((226 + 10 * random())::NUMERIC, 2)                          AS voltage_b,
  ROUND((18 + 8 * random())::NUMERIC, 2)                            AS current_r,
  ROUND((17 + 8 * random())::NUMERIC, 2)                            AS current_y,
  ROUND((19 + 8 * random())::NUMERIC, 2)                            AS current_b,
  FLOOR(10 + 40 * random())::INTEGER                                 AS parts_produced,
  'b2000001-0000-0000-0000-000000000001'                             AS upload_id,
  'excel'                                                            AS source
FROM machines m
CROSS JOIN generate_series(
  NOW() - INTERVAL '7 days',
  NOW(),
  INTERVAL '1 hour'
) AS ts
WHERE m.factory_id = '7fa85f64-5717-4562-b3fc-2c963f66afa6'
  AND m.is_active = true
  AND m.id != 'a1000001-0000-0000-0000-000000000008';

-- ---------------------------------------------------------------
-- AI INSIGHTS (6 diverse insight records)
-- ---------------------------------------------------------------
INSERT INTO ai_insights (id, factory_id, machine_id, insight_type, message, savings_potential_kwh, savings_potential_inr, status, resolution_notes, created_at, resolved_at, resolved_by) VALUES
  ('c3000001-0000-0000-0000-000000000001', '7fa85f64-5717-4562-b3fc-2c963f66afa6', 'a1000001-0000-0000-0000-000000000001',
   'ANOMALY',
   'CNC Machine 1 consumed 28% more energy than baseline between 02:00–04:00 AM on June 10. Suspected idle spindle rotation.',
   45.20, 384.20, 'OPEN', NULL, NOW() - INTERVAL '3 days', NULL, NULL),

  ('c3000001-0000-0000-0000-000000000002', '7fa85f64-5717-4562-b3fc-2c963f66afa6', 'a1000001-0000-0000-0000-000000000003',
   'OPTIMIZATION',
   'Welding Robot A shows consistent power factor below 0.88 during peak shift. Installing capacitor bank could save ₹12,000/month.',
   180.00, 1530.00, 'OPEN', NULL, NOW() - INTERVAL '5 days', NULL, NULL),

  ('c3000001-0000-0000-0000-000000000003', '7fa85f64-5717-4562-b3fc-2c963f66afa6', 'a1000001-0000-0000-0000-000000000005',
   'PEAK_DEMAND',
   'Air Compressor Unit 1 spikes to 28 kW during 6–8 AM, contributing to contract demand breach. Recommend staggered start.',
   62.50, 531.25, 'RESOLVED', 'Scheduled compressor warm-up shifted to 5:30 AM. Demand breach eliminated.', NOW() - INTERVAL '10 days', NOW() - INTERVAL '6 days', '0a58da81-30cb-467b-83cc-ef7870932c02'),

  ('c3000001-0000-0000-0000-000000000004', '7fa85f64-5717-4562-b3fc-2c963f66afa6', 'a1000001-0000-0000-0000-000000000007',
   'ANOMALY',
   'Injection Molder 1 shows abnormal reactive power (kVAR > 18) over the past 48 hours. Possible capacitor bank degradation.',
   95.00, 807.50, 'OPEN', NULL, NOW() - INTERVAL '2 days', NULL, NULL),

  ('c3000001-0000-0000-0000-000000000005', '7fa85f64-5717-4562-b3fc-2c963f66afa6', 'a1000001-0000-0000-0000-000000000002',
   'OPTIMIZATION',
   'CNC Machine 2 energy-per-part ratio improved by 12% after lubrication maintenance on June 5. Pattern indicates maintenance schedule adherence saves ~35 kWh/week.',
   35.00, 297.50, 'OPEN', NULL, NOW() - INTERVAL '7 days', NULL, NULL),

  ('c3000001-0000-0000-0000-000000000006', '7fa85f64-5717-4562-b3fc-2c963f66afa6', NULL,
   'PEAK_DEMAND',
   'Factory-wide peak demand of 820 kW recorded on June 8 at 10:15 AM, exceeding contract demand of 1000 kW by 82%. Risk zone. Review shift loading.',
   0.00, 0.00, 'OPEN', NULL, NOW() - INTERVAL '5 days', NULL, NULL);

-- ---------------------------------------------------------------
-- ALERTS (7 alert records across severities)
-- ---------------------------------------------------------------
INSERT INTO alerts (id, factory_id, machine_id, alert_type, severity, message, status, threshold_value, actual_value, created_at, resolved_at, resolved_by) VALUES
  ('d4000001-0000-0000-0000-000000000001', '7fa85f64-5717-4562-b3fc-2c963f66afa6', 'a1000001-0000-0000-0000-000000000001',
   'HIGH_CONSUMPTION', 'HIGH',
   'CNC Machine 1 exceeded energy threshold: 58.3 kWh recorded vs 42.5 kWh baseline.',
   'OPEN', 42.50, 58.30, NOW() - INTERVAL '3 days', NULL, NULL),

  ('d4000001-0000-0000-0000-000000000002', '7fa85f64-5717-4562-b3fc-2c963f66afa6', 'a1000001-0000-0000-0000-000000000003',
   'LOW_POWER_FACTOR', 'MEDIUM',
   'Welding Robot A power factor dropped to 0.81, below minimum threshold of 0.85.',
   'OPEN', 0.85, 0.81, NOW() - INTERVAL '2 days', NULL, NULL),

  ('d4000001-0000-0000-0000-000000000003', '7fa85f64-5717-4562-b3fc-2c963f66afa6', 'a1000001-0000-0000-0000-000000000005',
   'PEAK_DEMAND_BREACH', 'CRITICAL',
   'Air Compressor Unit 1 caused peak demand spike of 28 kW during morning ramp-up.',
   'RESOLVED', 22.00, 28.10, NOW() - INTERVAL '10 days', NOW() - INTERVAL '6 days', '0a58da81-30cb-467b-83cc-ef7870932c02'),

  ('d4000001-0000-0000-0000-000000000004', '7fa85f64-5717-4562-b3fc-2c963f66afa6', 'a1000001-0000-0000-0000-000000000007',
   'HIGH_REACTIVE_POWER', 'HIGH',
   'Injection Molder 1 reactive power at 19.2 kVAR, exceeding limit of 15.0 kVAR.',
   'OPEN', 15.00, 19.20, NOW() - INTERVAL '2 days', NULL, NULL),

  ('d4000001-0000-0000-0000-000000000005', '7fa85f64-5717-4562-b3fc-2c963f66afa6', 'a1000001-0000-0000-0000-000000000004',
   'VOLTAGE_IMBALANCE', 'LOW',
   'Welding Robot B phase voltage imbalance detected: R=229V, Y=218V, B=231V.',
   'OPEN', 5.00, 13.00, NOW() - INTERVAL '1 day', NULL, NULL),

  ('d4000001-0000-0000-0000-000000000006', '7fa85f64-5717-4562-b3fc-2c963f66afa6', 'a1000001-0000-0000-0000-000000000006',
   'HIGH_CONSUMPTION', 'MEDIUM',
   'Conveyor Belt System running 2.5 hours beyond scheduled shift window. Possible operator oversight.',
   'RESOLVED', 12.50, 18.70, NOW() - INTERVAL '4 days', NOW() - INTERVAL '3 days', '1e48ba92-41cc-477c-94cc-df7870932c03'),

  ('d4000001-0000-0000-0000-000000000007', '7fa85f64-5717-4562-b3fc-2c963f66afa6', NULL,
   'PEAK_DEMAND_BREACH', 'CRITICAL',
   'Factory-wide peak demand approached 82% of contract limit (820/1000 kW). Review shift loading.',
   'OPEN', 1000.00, 820.00, NOW() - INTERVAL '5 days', NULL, NULL);

-- ---------------------------------------------------------------
-- ALERT NOTIFICATIONS (for key alerts)
-- ---------------------------------------------------------------
INSERT INTO alert_notifications (id, alert_id, channel, sent_at, status, error_message) VALUES
  ('e5000001-0000-0000-0000-000000000001', 'd4000001-0000-0000-0000-000000000001', 'EMAIL',    NOW() - INTERVAL '3 days',  'SENT',   NULL),
  ('e5000001-0000-0000-0000-000000000002', 'd4000001-0000-0000-0000-000000000001', 'WHATSAPP', NOW() - INTERVAL '3 days',  'FAILED', 'WhatsApp API token not configured'),
  ('e5000001-0000-0000-0000-000000000003', 'd4000001-0000-0000-0000-000000000003', 'EMAIL',    NOW() - INTERVAL '10 days', 'SENT',   NULL),
  ('e5000001-0000-0000-0000-000000000004', 'd4000001-0000-0000-0000-000000000003', 'SMS',      NOW() - INTERVAL '10 days', 'SENT',   NULL),
  ('e5000001-0000-0000-0000-000000000005', 'd4000001-0000-0000-0000-000000000004', 'EMAIL',    NOW() - INTERVAL '2 days',  'SENT',   NULL),
  ('e5000001-0000-0000-0000-000000000006', 'd4000001-0000-0000-0000-000000000007', 'EMAIL',    NOW() - INTERVAL '5 days',  'SENT',   NULL),
  ('e5000001-0000-0000-0000-000000000007', 'd4000001-0000-0000-0000-000000000007', 'WHATSAPP', NOW() - INTERVAL '5 days',  'FAILED', 'WhatsApp API token not configured');

-- ---------------------------------------------------------------
-- REPORTS (4 generated reports)
-- ---------------------------------------------------------------
INSERT INTO reports (id, factory_id, generated_by, report_type, status, file_path, created_at, completed_at) VALUES
  ('f6000001-0000-0000-0000-000000000001', '7fa85f64-5717-4562-b3fc-2c963f66afa6', '1e48ba92-41cc-477c-94cc-df7870932c03',
   'DAILY', 'SUCCESS', 'reports/daily_2026-06-01.pdf',
   NOW() - INTERVAL '12 days', NOW() - INTERVAL '12 days' + INTERVAL '8 seconds'),

  ('f6000001-0000-0000-0000-000000000002', '7fa85f64-5717-4562-b3fc-2c963f66afa6', '0a58da81-30cb-467b-83cc-ef7870932c02',
   'MONTHLY', 'SUCCESS', 'reports/monthly_2026-05.pdf',
   NOW() - INTERVAL '13 days', NOW() - INTERVAL '13 days' + INTERVAL '22 seconds'),

  ('f6000001-0000-0000-0000-000000000003', '7fa85f64-5717-4562-b3fc-2c963f66afa6', '1e48ba92-41cc-477c-94cc-df7870932c03',
   'DAILY', 'SUCCESS', 'reports/daily_2026-06-07.pdf',
   NOW() - INTERVAL '6 days', NOW() - INTERVAL '6 days' + INTERVAL '9 seconds'),

  ('f6000001-0000-0000-0000-000000000004', '7fa85f64-5717-4562-b3fc-2c963f66afa6', '0a58da81-30cb-467b-83cc-ef7870932c02',
   'DAILY', 'PROCESSING', NULL,
   NOW() - INTERVAL '30 minutes', NULL);
