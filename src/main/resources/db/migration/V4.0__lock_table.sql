create table LOCK(
    ID bigint not null,
    LOCKED boolean not null,
    primary key (ID)
);

insert into LOCK (ID, LOCKED) values (1, false);

