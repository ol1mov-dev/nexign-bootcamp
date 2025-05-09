CREATE TABLE users (
   id BIGSERIAL PRIMARY KEY,
   first_name VARCHAR(255) NOT NULL,
   name VARCHAR(255) NOT NULL,
   last_name VARCHAR(255) NOT NULL,
   email VARCHAR(255) NOT NULL,
   password VARCHAR(255) NOT NULL,
   role VARCHAR(50),
   is_deleted BOOLEAN DEFAULT FALSE
);

-- Уникальный индекс для email (если требуется уникальность)
CREATE UNIQUE INDEX idx_users_email ON users (email);