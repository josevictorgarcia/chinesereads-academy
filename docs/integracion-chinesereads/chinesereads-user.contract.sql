-- Contrato de LECTURA de Academy sobre el esquema de ChineseReads.
-- Solo las columnas que Academy consulta. Lo ejecutan los tests de Academy (Testcontainers)
-- para crear una copia mínima del esquema matriz; `ddl-auto=validate` falla si la entidad
-- de solo lectura de Academy deja de cuadrar con esto.
-- Fuente: backend/src/main/java/com/chinesereads/backend/Model/User.java (nombres por defecto de Hibernate).
CREATE DATABASE IF NOT EXISTS chinesereads CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS chinesereads.user (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  email         VARCHAR(255) NULL,
  name          VARCHAR(255) NULL,
  language      VARCHAR(255) NULL,
  blocked       BIT(1)       NOT NULL DEFAULT b'0',
  premium_until DATETIME(6)  NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS chinesereads.user_roles (
  user_id BIGINT       NOT NULL,
  roles   VARCHAR(255) NULL,
  CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES chinesereads.user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
