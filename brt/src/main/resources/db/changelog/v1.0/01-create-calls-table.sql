CREATE TABLE call (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  stranger_msisdn VARCHAR(50) NOT NULL,
  call_type VARCHAR(50) NOT NULL,
  start_time VARCHAR(50) NOT NULL,
  end_time VARCHAR(50) NOT NULL,
  duration VARCHAR(50) NOT NULL,
  CONSTRAINT fk_call_user FOREIGN KEY (user_id) REFERENCES users(id)
);