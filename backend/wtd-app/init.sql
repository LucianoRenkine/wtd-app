-- init.sql
-- Datos de prueba para la tabla users

INSERT INTO users (email, nombre, password_hash, role, created_at) VALUES
('fran@mail.com', 'Fran Principale', 'hash_temporal_fran', 'user', NOW()),
('novia@mail.com', 'Tu Novia', 'hash_temporal_novia', 'user', NOW()),
('admin@mail.com', 'Administrador', 'hash_temporal_admin', 'admin', NOW());

-- Categorías de prueba
INSERT INTO categories (name, color, owner_id, created_at) VALUES
('Estudio', '#3498db', 1, NOW()),
('Trabajo', '#27ae60', 1, NOW()),
('Personal', '#9b59b6', 1, NOW()),
('Urgente', '#e74c3c', 1, NOW()),
('Estudio', '#3498db', 2, NOW()),
('Trabajo', '#27ae60', 2, NOW()),
('Personal', '#9b59b6', 2, NOW()),
('Urgente', '#e74c3c', 2, NOW());

-- Tareas de prueba
INSERT INTO tasks (title, description, start_date, end_date, start_time, end_time, category_id, created_by, completed, priority, recurrence_type, created_at, updated_at) VALUES
('Estudiar SQL', 'Repasar conceptos de bases de datos', '2026-06-05', '2026-06-05', '14:00:00', '16:00:00', 1, 1, false, 'HIGH', 'NONE', NOW(), NOW()),
('Reunión importante', 'Reunión con el cliente', '2026-06-06', '2026-06-06', '10:00:00', '11:00:00', 2, 1, false, 'HIGH', 'NONE', NOW(), NOW()),
('Cita con novia', 'Cena a las 19:00', '2026-06-07', '2026-06-07', '19:00:00', '21:00:00', 3, 1, false, 'MEDIUM', 'NONE', NOW(), NOW());
