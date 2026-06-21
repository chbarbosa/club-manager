CREATE TABLE support_access (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    created_by_admin_id BIGINT NOT NULL,
    CONSTRAINT fk_support_access_created_by_admin FOREIGN KEY (created_by_admin_id) REFERENCES admin(id)
);

CREATE INDEX idx_support_access_email ON support_access(email);
CREATE INDEX idx_support_access_expires_at ON support_access(expires_at);

CREATE TABLE support_access_view_event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uuid UUID NOT NULL UNIQUE,
    support_access_id BIGINT NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    feature VARCHAR(100) NOT NULL,
    http_method VARCHAR(10) NOT NULL,
    path VARCHAR(500) NOT NULL,
    entity_uuid UUID,
    CONSTRAINT fk_support_view_support_access FOREIGN KEY (support_access_id) REFERENCES support_access(id)
);

CREATE INDEX idx_support_view_access ON support_access_view_event(support_access_id, occurred_at DESC);
