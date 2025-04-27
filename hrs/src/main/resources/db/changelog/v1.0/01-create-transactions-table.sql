CREATE TABLE transactions (
  id BIGSERIAL PRIMARY KEY,
  client_id BIGINT NOT NULL,
  total_price BIGINT NOT NULL,
  amount_of_minutes BIGINT NOT NULL,
  created_at DATE NOT NULL,
  FOREIGN KEY (client_id) REFERENCES clients(id)
);
