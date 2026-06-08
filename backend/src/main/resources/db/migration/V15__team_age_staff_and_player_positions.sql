ALTER TABLE team
    ADD COLUMN IF NOT EXISTS age_category VARCHAR(20) DEFAULT 'U13' NOT NULL;

ALTER TABLE team
    ADD CONSTRAINT IF NOT EXISTS chk_team_age_category
    CHECK (age_category IN ('U7', 'U8', 'U9', 'U10', 'U11', 'U12', 'U13', 'U14', 'U15', 'U16', 'U17_18', 'U19_PLUS'));

ALTER TABLE team
    ADD COLUMN IF NOT EXISTS sub_trainer_id BIGINT;

ALTER TABLE team
    ADD COLUMN IF NOT EXISTS assistant_admin_id BIGINT;

ALTER TABLE team
    ADD CONSTRAINT IF NOT EXISTS fk_team_sub_trainer
    FOREIGN KEY (sub_trainer_id) REFERENCES trainer(id);

ALTER TABLE team
    ADD CONSTRAINT IF NOT EXISTS fk_team_assistant_admin
    FOREIGN KEY (assistant_admin_id) REFERENCES admin(id);

CREATE TABLE IF NOT EXISTS player_position (
    player_id BIGINT NOT NULL,
    position VARCHAR(20) NOT NULL CHECK (position IN ('GOALKEEPER', 'DEFENSE', 'MIDFIELD', 'ATTACK')),
    CONSTRAINT fk_player_position_player FOREIGN KEY (player_id) REFERENCES player(id)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_player_position
    ON player_position(player_id, position);
