CREATE TABLE clients (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    tariff_id BIGINT NOT NULL,
    created_at DATE NOT NULL,
    is_deleted BOOLEAN NOT NULL,
    FOREIGN KEY (tariff_id) REFERENCES tariffs(id)
);