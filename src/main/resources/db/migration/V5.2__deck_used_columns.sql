ALTER TABLE river_race_participant
ADD COLUMN practice_decks int NOT NULL default 0,
ADD COLUMN war_decks int NOT NULL default 0,
ADD COLUMN required_decks int NULL default 0;
