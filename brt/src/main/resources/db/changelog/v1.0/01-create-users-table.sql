CREATE TABLE users(
   id BIGSERIAL PRIMARY KEY,
   first_name VARCHAR(255) NOT NULL,
   name VARCHAR(255) NOT NULL,
   last_name VARCHAR(255),
   msisdn VARCHAR(50) NOT NULL UNIQUE ,
   balance NUMERIC(12,2) NOT NULL,
   tariff_id BIGINT,
   CONSTRAINT fk_users_tariff FOREIGN KEY (tariff_id) REFERENCES tariffs(id)
);