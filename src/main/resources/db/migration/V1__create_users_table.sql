CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

INSERT INTO users (name, email, role) VALUES
    ('Alice Admin', 'alice@example.com', 'ADMIN'),
    ('Bob Regular', 'bob@example.com', 'USER'),
    ('Carol Regular', 'carol@example.com', 'USER');
