CREATE TABLE admin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO admin (uuid, name, email, username, password_hash)
VALUES (
    RANDOM_UUID(),
    'Admin',
    'admin@clubmanager.com',
    'admin',
    '$2a$10$9LLrvx1fFfshHwjCRYX99epDQSYK9YMrcY.2hjsMK/yu2/B68zIeO'
);
