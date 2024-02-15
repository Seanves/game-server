CREATE TABLE IF NOT EXISTS Users (
    id serial             PRIMARY KEY,
    login varchar(20)     NOT NULL UNIQUE,
    password varchar(128) NOT NULL,
    nickname varchar(20)  NOT NULL,
    rating int            NOT NULL DEFAULT 0,
    max_rating int        NOT NULL DEFAULT 0,
    games_played int      NOT NULL DEFAULT 0,
    wins int              NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS Game_result (
    id serial               PRIMARY KEY,
    winner_id int,
    loser_id  int,
    winner_change smallint  NOT NULL,
    loser_change  smallint  NOT NULL,
    time timestamp          NOT NULL,
    FOREIGN KEY (winner_id) REFERENCES Users(id),
    FOREIGN KEY (loser_id)  REFERENCES Users(id)
);