CREATE TABLE calls (
   id BIGSERIAL PRIMARY KEY,
   abonent_id BIGINT NOT NULL,
   stranger_msisdn VARCHAR(50) NOT NULL,
   call_type VARCHAR(50) NOT NULL,
   start_time VARCHAR(50) NOT NULL,
   end_time VARCHAR(50) NOT NULL,
   duration TIME NOT NULL,
   CONSTRAINT fk_calls_abonent FOREIGN KEY (abonent_id) REFERENCES abonents(id)
);
