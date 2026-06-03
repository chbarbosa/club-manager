CREATE TABLE player (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    birth_country VARCHAR(100) NOT NULL,
    living_country VARCHAR(100) NOT NULL,
    birthdate DATE NOT NULL,
    gender CHAR(1) NOT NULL CHECK (gender IN ('M', 'F')),
    registration_number VARCHAR(50),
    register_date DATE NOT NULL,
    member_since DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE UNIQUE INDEX ux_player_registration_number
    ON player (registration_number);
