-- Usuarios de prueba en la copia del esquema matriz (solo tests).
INSERT INTO chinesereads.user (id, email, name, language, blocked, premium_until) VALUES
  (1, 'teacher@test.local', 'Profe Test', 'es', b'0', NULL),
  (2, 'blocked@test.local', 'Cuenta Bloqueada', 'es', b'1', NULL),
  (3, 'student@test.local', 'Alumno Test', 'en', b'0', NULL);
INSERT INTO chinesereads.user_roles (user_id, roles) VALUES (1, 'USER'), (2, 'USER'), (3, 'USER');
