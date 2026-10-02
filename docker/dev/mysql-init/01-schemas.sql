-- Entorno local: una instancia MySQL con los dos esquemas, como en producción (ADR-003, ADR-011).
-- `chinesereads` lo llena el backend del proyecto matriz (ddl-auto=update + seed de desarrollo).
-- `academy` lo migra Flyway desde el backend de Academy.
CREATE DATABASE IF NOT EXISTS chinesereads CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS academy      CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- Usuario de Academy con privilegio mínimo: todo sobre su esquema, solo lectura de los usuarios del matriz.
-- La contraseña la fija ACADEMY_DB_PASSWORD (inyectada como variable de entorno del contenedor).
CREATE USER IF NOT EXISTS 'academy_app'@'%' IDENTIFIED BY 'academy-dev-password';
GRANT ALL PRIVILEGES ON academy.* TO 'academy_app'@'%';
GRANT SELECT ON chinesereads.user       TO 'academy_app'@'%';
GRANT SELECT ON chinesereads.user_roles TO 'academy_app'@'%';
FLUSH PRIVILEGES;
