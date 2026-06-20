-- Seed inicial para WTD en Neon
-- Ejecutar UNA sola vez después de que el backend haya creado las tablas

INSERT INTO users (id, email, nombre, password_hash, role, created_at) VALUES
(1, 'renkineluciano1@gmail.com', 'Lulo',          'hash_temporal_fran',  'user',  NOW()),
(2, 'franmaine1@gmail.com',      'Ju',            'hash_temporal_novia', 'user',  NOW()),
(3, 'admin@mail.com',            'Administrador', 'hash_temporal_admin', 'admin', NOW())
ON CONFLICT (id) DO NOTHING;

-- Resetear la secuencia de IDs para que no choque con los insertados
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));

INSERT INTO categories (id, name, color, owner_id, created_at) VALUES
(1, 'Estudio',  '#3498db', 1, NOW()),
(2, 'Trabajo',  '#27ae60', 1, NOW()),
(3, 'Personal', '#9b59b6', 1, NOW()),
(4, 'Urgente',  '#e74c3c', 1, NOW())
ON CONFLICT (id) DO NOTHING;

SELECT setval('categories_id_seq', (SELECT MAX(id) FROM categories));
