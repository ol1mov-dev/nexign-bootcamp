CREATE TABLE tariffs (
     id BIGSERIAL PRIMARY KEY,
     name VARCHAR(255) NOT NULL,
     description TEXT,
     parameters_id BIGINT NOT NULL,
     FOREIGN KEY (parameters_id) REFERENCES tariff_parameters(id)
);