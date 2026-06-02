CREATE TABLE club (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    colour1 VARCHAR(7) NOT NULL DEFAULT '#2d2d2d',
    colour2 VARCHAR(7) NOT NULL DEFAULT '#f0f0f0'
);

CREATE TABLE club_setup (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL UNIQUE,
    json_data TEXT NOT NULL
);

INSERT INTO club (uuid, name, description, colour1, colour2)
VALUES (RANDOM_UUID(), 'My Club', 'Configure your club.', '#2d2d2d', '#f0f0f0');

INSERT INTO club_setup (uuid, type, json_data)
VALUES (RANDOM_UUID(), 'EVALUATION_LEVEL', '["Debutant", "Advanced", "Skilled"]');

INSERT INTO club_setup (uuid, type, json_data)
VALUES (RANDOM_UUID(), 'FEDERATIVE_UNIT', '["Province 1", "Province 2"]');

