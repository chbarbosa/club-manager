ALTER TABLE player ADD COLUMN team_category VARCHAR(20);
UPDATE player SET team_category = 'MASCULINE' WHERE gender = 'M';
UPDATE player SET team_category = 'FEMININE' WHERE gender = 'F';
ALTER TABLE player ALTER COLUMN team_category SET NOT NULL;
ALTER TABLE player ADD CONSTRAINT chk_player_team_category
    CHECK (team_category IN ('MASCULINE', 'FEMININE'));
ALTER TABLE player DROP COLUMN gender;
