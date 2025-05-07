CREATE TABLE balances (
 id BIGSERIAL PRIMARY KEY,
 amount_of_minutes_for_incoming_call INT NOT NULL,
 amount_of_minutes_for_outcoming_call INT NOT NULL
);