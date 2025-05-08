CREATE TABLE limits (
    id BIGSERIAL PRIMARY KEY,
    minutes_for_outcoming INTEGER NOT NULL,
    minutes_for_incoming INTEGER NOT NULL,
    price_per_additional_minute_outcoming  NUMERIC(10, 2) NOT NULL,
    price_per_additional_minute_incoming NUMERIC(10, 2) NOT NULL
);