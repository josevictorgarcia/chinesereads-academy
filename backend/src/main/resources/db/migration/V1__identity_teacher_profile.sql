-- identity: perfil de profesor. user_id referencia chinesereads.user.id sin FK física (esquema ajeno, ADR-011).
CREATE TABLE teacher_profile (
  id           BIGINT       NOT NULL AUTO_INCREMENT,
  user_id      BIGINT       NOT NULL,
  display_name VARCHAR(120) NOT NULL,
  language     VARCHAR(2)   NOT NULL DEFAULT 'es',
  created_at   DATETIME(6)  NOT NULL,
  updated_at   DATETIME(6)  NOT NULL,
  deleted_at   DATETIME(6)  NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_teacher_profile_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
