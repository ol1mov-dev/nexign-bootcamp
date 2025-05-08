CREATE TABLE abonents (
          id BIGSERIAL PRIMARY KEY,
          user_id BIGINT NOT NULL,
          tariff_id BIGINT,
          balance_id BIGINT,
          created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
          is_deleted BOOLEAN DEFAULT FALSE,

          CONSTRAINT fk_abonents_tariff
              FOREIGN KEY (tariff_id)
                  REFERENCES tariffs(id)
                  ON DELETE SET NULL,

          CONSTRAINT fk_abonents_balance
              FOREIGN KEY (balance_id)
                  REFERENCES balances(id)
                  ON DELETE CASCADE
);