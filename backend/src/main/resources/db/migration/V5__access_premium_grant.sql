-- access: auditoría y reintentos de la concesión de premium en ChineseReads por asiento.
CREATE TABLE premium_grant (
  id              BIGINT       NOT NULL AUTO_INCREMENT,
  enrollment_id   BIGINT       NOT NULL,
  student_user_id BIGINT       NOT NULL,
  action          VARCHAR(8)   NOT NULL,                      -- GRANT | REVOKE
  granted_until   DATETIME(6)  NULL,
  status          VARCHAR(16)  NOT NULL,                      -- PENDING | DONE | FAILED
  attempts        INT          NOT NULL DEFAULT 0,
  last_error      VARCHAR(500) NULL,
  created_at      DATETIME(6)  NOT NULL,
  updated_at      DATETIME(6)  NOT NULL,
  PRIMARY KEY (id),
  KEY idx_premium_grant_status (status, updated_at),
  CONSTRAINT fk_premium_grant_enrollment FOREIGN KEY (enrollment_id) REFERENCES enrollment (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
