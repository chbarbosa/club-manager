ALTER TABLE evaluation_event_attendance
    DROP COLUMN IF EXISTS skill_level;

ALTER TABLE evaluation_result
    ALTER COLUMN source_event_id DROP NOT NULL;
