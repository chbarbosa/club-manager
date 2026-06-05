CREATE TABLE trainer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    birth_country VARCHAR(100),
    living_country VARCHAR(100),
    birthdate DATE,
    email VARCHAR(150),
    phone VARCHAR(30),
    register_date DATE NOT NULL,
    member_since DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);
