ALTER TABLE championship
    ADD COLUMN expected_matches INTEGER NOT NULL DEFAULT 0;

ALTER TABLE championship
    ADD CONSTRAINT ck_championship_expected_matches CHECK (expected_matches >= 0);
