-- Seed a default factory so that the admin user can be linked to a factory
INSERT INTO factories (id, name, address, timezone, contract_demand_kw, tariff_inr_per_kwh)
VALUES ('7fa85f64-5717-4562-b3fc-2c963f66afa6', 'Mumbai Main Plant', 'Plot 12, MIDC, Andheri, Mumbai, India', 'Asia/Kolkata', 1000.00, 8.50);

-- Insert a default ADMIN user
-- Password is the BCrypt hash of 'Admin@123'
INSERT INTO users (id, factory_id, email, password_hash, first_name, last_name, role, is_active)
VALUES ('0a58da81-30cb-467b-83cc-ef7870932c02', '7fa85f64-5717-4562-b3fc-2c963f66afa6', 'admin@ems.local', '$2a$10$H8z0i05hP/Mtf1W8/4F4Iu7t5M8VwVb2kI120n09j1u7f32/Y/aD.', 'EMS', 'Administrator', 'ADMIN', true);
