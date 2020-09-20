CREATE TABLE river_race (
    id bigint NOT NULL,
    active boolean NOT NULL,
    created_on timestamp without time zone,
    finished boolean NOT NULL,
    season_id integer NOT NULL,
    section_index integer NOT NULL,
    super_cell_created_date timestamp without time zone,
    updated_on timestamp without time zone,
    clan_id integer,
	CONSTRAINT river_race_pkey PRIMARY KEY (id),
	CONSTRAINT river_race_UC unique (section_index, season_id)
);


CREATE TABLE river_race_clan (
    id integer NOT NULL,
    fame integer NOT NULL,
    finish_time timestamp without time zone,
    name character varying(255),
    rank integer NOT NULL,
    repair_points integer NOT NULL,
    tag character varying(255),
    trophies integer NOT NULL,
    trophy_change integer NOT NULL,
    river_race_fk bigint,
	CONSTRAINT river_race_clan_pkey PRIMARY KEY (id)
);


CREATE TABLE river_race_participant (
    id bigint NOT NULL,
    active_fame integer NOT NULL,
    fame integer NOT NULL,
    name character varying(255),
    repair_points integer NOT NULL,
    tag character varying(255),
    river_race_clan_fk integer,
	CONSTRAINT river_race_participant_pkey PRIMARY KEY (id)
);

ALTER TABLE ONLY river_race_participant
    ADD CONSTRAINT rr_participant_clan_fk FOREIGN KEY (river_race_clan_fk) REFERENCES river_race_clan(id);

ALTER TABLE ONLY river_race_clan
    ADD CONSTRAINT rr_clan_rr_fk FOREIGN KEY (river_race_fk) REFERENCES river_race(id);

ALTER TABLE ONLY river_race
    ADD CONSTRAINT rr_rr_clan_fk FOREIGN KEY (clan_id) REFERENCES river_race_clan(id);



CREATE SEQUENCE river_race_clan_sequence
    START WITH 1     INCREMENT BY 50     NO MINVALUE    NO MAXVALUE     CACHE 1;

CREATE SEQUENCE river_race_participant_sequence
    START WITH 1    INCREMENT BY 50    NO MINVALUE    NO MAXVALUE    CACHE 1;

CREATE SEQUENCE river_race_sequence
    START WITH 1     INCREMENT BY 50    NO MINVALUE    NO MAXVALUE    CACHE 1;