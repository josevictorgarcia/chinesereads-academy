-- billing: suscripción del profesor y registro idempotente de eventos de Stripe (ADR-009).
CREATE TABLE teacher_subscription (
  id                     BIGINT       NOT NULL AUTO_INCREMENT,
  teacher_id             BIGINT       NOT NULL,
  source                 VARCHAR(16)  NOT NULL,              -- STRIPE | MANUAL | TRIAL
  status                 VARCHAR(16)  NOT NULL,              -- TRIALING | ACTIVE | PAST_DUE | CANCELED | EXPIRED
  stripe_customer_id     VARCHAR(64)  NULL,
  stripe_subscription_id VARCHAR(64)  NULL,
  stripe_price_id        VARCHAR(64)  NULL,
  seats_included         INT          NOT NULL DEFAULT 10,
  seats_extra            INT          NOT NULL DEFAULT 0,
  current_period_end     DATETIME(6)  NOT NULL,
  cancel_at_period_end   BIT(1)       NOT NULL DEFAULT b'0',
  created_at             DATETIME(6)  NOT NULL,
  updated_at             DATETIME(6)  NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_teacher_subscription_teacher (teacher_id),
  UNIQUE KEY uk_teacher_subscription_stripe (stripe_subscription_id),
  CONSTRAINT fk_teacher_subscription_teacher FOREIGN KEY (teacher_id) REFERENCES teacher_profile (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE stripe_event (
  id           VARCHAR(64)  NOT NULL,
  type         VARCHAR(64)  NOT NULL,
  received_at  DATETIME(6)  NOT NULL,
  processed_at DATETIME(6)  NULL,
  outcome      VARCHAR(16)  NULL,                            -- PROCESSED | IGNORED | FAILED
  error        TEXT         NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
