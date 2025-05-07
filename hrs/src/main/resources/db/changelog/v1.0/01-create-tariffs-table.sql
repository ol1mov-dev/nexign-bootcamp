CREATE TABLE tariffs (
     id BIGSERIAL PRIMARY KEY,
     name VARCHAR(255),
     description TEXT,
     tariff_parameters_id BIGINT,
     is_deleted BOOLEAN DEFAULT FALSE,

     CONSTRAINT fk_tariffs_tariff_parameters
         FOREIGN KEY (tariff_parameters_id)
             REFERENCES tariff_parameters(id)
             ON DELETE SET NULL,

     CONSTRAINT unique_tariff_parameters_id UNIQUE (tariff_parameters_id)
);