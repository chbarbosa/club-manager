ALTER TABLE trainer ADD COLUMN password_hash VARCHAR(255);
ALTER TABLE trainer ADD COLUMN password_setup_code_hash VARCHAR(255);
ALTER TABLE trainer ADD COLUMN password_setup_code_expires_at TIMESTAMP;
ALTER TABLE trainer ADD COLUMN password_reset_code_hash VARCHAR(255);
ALTER TABLE trainer ADD COLUMN password_reset_code_expires_at TIMESTAMP;
ALTER TABLE trainer ADD COLUMN access_invited_at TIMESTAMP;
