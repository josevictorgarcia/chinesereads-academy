-- classroom: grupos, altas (= asientos), pertenencia y códigos de invitación.
CREATE TABLE study_group (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  teacher_id  BIGINT       NOT NULL,
  name        VARCHAR(80)  NOT NULL,
  level       VARCHAR(8)   NULL,
  description TEXT         NULL,
  created_at  DATETIME(6)  NOT NULL,
  updated_at  DATETIME(6)  NOT NULL,
  deleted_at  DATETIME(6)  NULL,
  PRIMARY KEY (id),
  KEY idx_study_group_teacher (teacher_id, deleted_at),
  CONSTRAINT fk_study_group_teacher FOREIGN KEY (teacher_id) REFERENCES teacher_profile (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Un asiento = un alumno (chinesereads.user.id, sin FK física) dado de alta con un profesor.
CREATE TABLE enrollment (
  id              BIGINT       NOT NULL AUTO_INCREMENT,
  teacher_id      BIGINT       NOT NULL,
  student_user_id BIGINT       NOT NULL,
  status          VARCHAR(16)  NOT NULL,                      -- ACTIVE | DEACTIVATED
  activated_at    DATETIME(6)  NOT NULL,
  deactivated_at  DATETIME(6)  NULL,
  created_at      DATETIME(6)  NOT NULL,
  updated_at      DATETIME(6)  NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_enrollment_teacher_student (teacher_id, student_user_id),
  KEY idx_enrollment_student (student_user_id, status),
  CONSTRAINT fk_enrollment_teacher FOREIGN KEY (teacher_id) REFERENCES teacher_profile (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE group_member (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  group_id      BIGINT       NOT NULL,
  enrollment_id BIGINT       NOT NULL,
  joined_at     DATETIME(6)  NOT NULL,
  left_at       DATETIME(6)  NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_group_member (group_id, enrollment_id),
  CONSTRAINT fk_group_member_group FOREIGN KEY (group_id) REFERENCES study_group (id),
  CONSTRAINT fk_group_member_enrollment FOREIGN KEY (enrollment_id) REFERENCES enrollment (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE invite_code (
  id         BIGINT       NOT NULL AUTO_INCREMENT,
  group_id   BIGINT       NOT NULL,
  code       VARCHAR(8)   NOT NULL,
  expires_at DATETIME(6)  NOT NULL,
  max_uses   INT          NULL,
  uses       INT          NOT NULL DEFAULT 0,
  revoked_at DATETIME(6)  NULL,
  created_at DATETIME(6)  NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_invite_code (code),
  CONSTRAINT fk_invite_code_group FOREIGN KEY (group_id) REFERENCES study_group (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
