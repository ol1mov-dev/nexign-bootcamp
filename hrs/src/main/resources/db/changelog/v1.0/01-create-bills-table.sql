CREATE TABLE bills (
   id BIGSERIAL PRIMARY KEY,
   abonent_id BIGINT NOT NULL,
   total_price BIGINT NOT NULL,
   amount_of_minutes BIGINT NOT NULL,
   created_at DATE NOT NULL,
   FOREIGN KEY (abonent_id) REFERENCES abonents(id) ON DELETE CASCADE
);