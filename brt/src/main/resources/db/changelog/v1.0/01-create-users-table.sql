CREATE TABLE users (
   id BIGSERIAL PRIMARY KEY,
   first_name VARCHAR(255) NOT NULL,
   name VARCHAR(255) NOT NULL,
   last_name VARCHAR(255),
   msisdn VARCHAR(50) NOT NULL,
   balance VARCHAR(50) NOT NULL,
   tariff_id BIGINT UNIQUE,
   CONSTRAINT fk_users_tariff FOREIGN KEY (tariff_id) REFERENCES tariff(id)
);