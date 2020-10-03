ALTER TABLE river_race DROP COLUMN finished;

ALTER TABLE river_race_clan ADD COLUMN finished boolean NOT NULL default FALSE;

