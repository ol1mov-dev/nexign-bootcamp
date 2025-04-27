CREATE TABLE tariff_parameters (
    id BIGSERIAL PRIMARY KEY,
    price BIGINT NOT NULL,
    monthly_price BIGINT NOT NULL,
    monthly_amount_of_minutes BIGINT NOT NULL
);