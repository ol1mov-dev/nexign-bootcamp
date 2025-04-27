CREATE TABLE cdrs (
     id BIGINT AUTO_INCREMENT PRIMARY KEY,
     call_type VARCHAR(255) NOT NULL,
     first_msisdn VARCHAR(255) NOT NULL,
     second_msisdn VARCHAR(255) NOT NULL,
     start_time varchar(255) NOT NULL,
     end_time varchar(255) NOT NULL
);
