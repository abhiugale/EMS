-- Insert extra dummy users with different roles for testing
-- Password for all users is the BCrypt hash of 'Admin@123': $2a$10$H8z0i05hP/Mtf1W8/4F4Iu7t5M8VwVb2kI120n09j1u7f32/Y/aD.

INSERT INTO users (id, factory_id, email, password_hash, first_name, last_name, role, is_active)
VALUES 
  ('1e48ba92-41cc-477c-94cc-df7870932c03', '7fa85f64-5717-4562-b3fc-2c963f66afa6', 'manager@ems.local', '$2a$10$H8z0i05hP/Mtf1W8/4F4Iu7t5M8VwVb2kI120n09j1u7f32/Y/aD.', 'Energy', 'Manager', 'ENERGY_MGR', true),
  ('2e58ca03-52dd-488d-a5dd-ef7870932c04', '7fa85f64-5717-4562-b3fc-2c963f66afa6', 'supervisor@ems.local', '$2a$10$H8z0i05hP/Mtf1W8/4F4Iu7t5M8VwVb2kI120n09j1u7f32/Y/aD.', 'Shift', 'Supervisor', 'SUPERVISOR', true),
  ('3e68da14-63ee-499e-b6ee-ff7870932c05', '7fa85f64-5717-4562-b3fc-2c963f66afa6', 'viewer@ems.local', '$2a$10$H8z0i05hP/Mtf1W8/4F4Iu7t5M8VwVb2kI120n09j1u7f32/Y/aD.', 'Guest', 'Viewer', 'VIEWER', true)
ON CONFLICT (email) DO NOTHING;
