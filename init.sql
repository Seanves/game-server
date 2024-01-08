CREATE TABLE IF NOT EXISTS Users (
    id serial             PRIMARY KEY,
    login varchar(20)     NOT NULL UNIQUE,
    password varchar(128) NOT NULL,
    nickname varchar(20)  NOT NULL,
    rating int            NOT NULL DEFAULT 100,
    max_rating int        NOT NULL DEFAULT 100,
    games_played int      NOT NULL DEFAULT 0,
    wins int              NOT NULL DEFAULT 0
);