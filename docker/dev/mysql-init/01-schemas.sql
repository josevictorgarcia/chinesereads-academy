-- Entorno local: una instancia MySQL con los dos esquemas, como en producción (ADR-003, ADR-011).
-- `chinesereads` lo llena el backend del proyecto matriz (ddl-auto=update + seed de desarrollo).
-- `academy` lo migra Flyway desde el backend de Academy.
CREATE DATABASE IF NOT EXISTS chinesereads CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS academy      CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- Usuario de Academy con privilegio mínimo: todo sobre su esquema y SOLO LECTURA del esquema matriz.
-- En desarrollo el permiso es a nivel de esquema porque MySQL 8 no admite GRANT sobre tablas que aún no
-- existen (las crea el backend del matriz al arrancar). En producción, una vez existan, se afina a
-- `chinesereads.user` y `chinesereads.user_roles` (ver docs/integracion-chinesereads/CONTRACT.md §2).
CREATE USER IF NOT EXISTS 'academy_app'@'%' IDENTIFIED BY 'academy-dev-password';
GRANT ALL PRIVILEGES ON academy.*  TO 'academy_app'@'%';
GRANT SELECT         ON chinesereads.* TO 'academy_app'@'%';
FLUSH PRIVILEGES;
