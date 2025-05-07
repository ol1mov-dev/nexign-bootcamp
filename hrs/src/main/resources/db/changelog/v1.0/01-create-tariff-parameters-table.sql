CREATE TABLE tariff_parameters (
   id BIGSERIAL PRIMARY KEY,
   price NUMERIC(10,2),
   payment_period_in_days INTEGER,
   limit_id BIGINT,

   CONSTRAINT fk_tariff_parameters_limit
       FOREIGN KEY (limit_id)
           REFERENCES limits(id)
           ON DELETE SET NULL
);