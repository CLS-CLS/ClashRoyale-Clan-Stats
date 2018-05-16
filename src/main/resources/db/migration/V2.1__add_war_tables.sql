create sequence pwars_sequence start with 100 increment by 50;

CREATE TABLE player_war_stat (
    id bigint NOT NULL,
    cards_won smallint NOT NULL,
    collection_games_lost smallint,
    collection_games_won smallint,
    war_eligible boolean NOT NULL,
    games_granted smallint,
    war_games_lost smallint,
    war_games_won smallint,
    player_tag varchar(255),
    war_league_start_date timestamp NOT NULL
);

CREATE TABLE war_league (
    start_date timestamp NOT NULL,
    name varchar(255),
    rank smallint,
    trophies smallint,
    war_season_id bigint
);


CREATE TABLE war_season (
    id bigint NOT NULL,
    rank smallint,
    stard_date timestamp NOT NULL
);

ALTER TABLE player_war_stat ADD CONSTRAINT player_war_stat_pkey PRIMARY KEY (id);
ALTER TABLE war_league ADD CONSTRAINT war_league_pkey PRIMARY KEY (start_date);
ALTER TABLE war_season ADD CONSTRAINT war_season_pkey PRIMARY KEY (id);

ALTER TABLE war_season ADD CONSTRAINT war_season_unique_start_date UNIQUE (stard_date);
ALTER TABLE player_war_stat ADD CONSTRAINT pwars_unique_tag_date UNIQUE (player_tag, war_league_start_date);

ALTER TABLE player_war_stat ADD CONSTRAINT pwars_FK_war_league FOREIGN KEY (war_league_start_date) REFERENCES war_league(start_date);
ALTER TABLE player_war_stat ADD CONSTRAINT pwars_FK_player FOREIGN KEY (player_tag) REFERENCES player(tag);
ALTER TABLE war_league ADD CONSTRAINT war_league_FK_war_season FOREIGN KEY (war_season_id) REFERENCES war_season(id);











