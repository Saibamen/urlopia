CREATE TABLE Users (
  id              SERIAL        PRIMARY KEY,
  principal_name  VARCHAR(63)   NOT NULL UNIQUE ,
  ad_name         VARCHAR(100)  NOT NULL UNIQUE ,
  mail            VARCHAR(63)   NOT NULL,
  first_name      VARCHAR(30),
  last_name       VARCHAR(30),
  admin           BOOLEAN       NOT NULL DEFAULT FALSE,
  leader          BOOLEAN       NOT NULL DEFAULT FALSE,
  b2b             BOOLEAN       NOT NULL DEFAULT FALSE,
  ec              BOOLEAN       NOT NULL DEFAULT TRUE,
  active          BOOLEAN       NOT NULL DEFAULT TRUE,
  lang            VARCHAR(2)    NOT NULL DEFAULT 'pl',
  work_time       REAL          DEFAULT 8.0
);

CREATE UNIQUE INDEX users_principal_name_index ON Users(principal_name);
CREATE UNIQUE INDEX users_ad_name_index ON Users(ad_name);
CREATE UNIQUE INDEX users_mail_index ON Users(mail);

CREATE TABLE Teams (
  name        VARCHAR(50)   PRIMARY KEY,
  ad_name     VARCHAR(100)  NOT NULL UNIQUE,
  leader_id   INT           REFERENCES Users(id)
);

CREATE UNIQUE INDEX teams_ad_name_index ON Teams(ad_name);

CREATE TABLE Users_Teams (
  id       SERIAL       PRIMARY KEY,
  user_id  INT          NOT NULL REFERENCES Users(id),
  team_id  VARCHAR(50)  NOT NULL REFERENCES Teams(name)
);

CREATE INDEX users_teams_user_index ON Users_Teams(user_id);
CREATE INDEX users_teams_team_index ON Users_Teams(team_id);

CREATE TABLE Requests (
  id           SERIAL                     PRIMARY KEY,
  created      TIMESTAMP                  NOT NULL,
  modified     TIMESTAMP                  NOT NULL,
  requester_id INT REFERENCES Users (id)  NOT NULL,
  start_date   DATE                       NOT NULL,
  end_date     DATE                       NOT NULL,
  type         VARCHAR(25)                NOT NULL   DEFAULT 'NORMAL',
  type_info    VARCHAR(25),
  status       VARCHAR(255)               NOT NULL   DEFAULT 'PENDING'
);

CREATE INDEX requests_modified_index ON Requests(modified);
CREATE INDEX requests_requester_id_index ON Requests(requester_id);

CREATE TABLE Acceptances (
  id         SERIAL                         PRIMARY KEY,
  request_id INT REFERENCES Requests (id)   NOT NULL,
  leader_id  INT REFERENCES Users (id)      NOT NULL,
  decider_id INT REFERENCES Users (id),
  accepted   BOOLEAN                        NOT NULL   DEFAULT FALSE
);

CREATE INDEX acceptances_request_id_index ON Acceptances(request_id);
CREATE INDEX acceptances_leader_id_index ON Acceptances(leader_id);

CREATE TABLE History (
  id         SERIAL PRIMARY KEY,
  created    TIMESTAMP                    NOT NULL,
  user_id    INT REFERENCES Users (id)    NOT NULL,
  decider_id INT REFERENCES Users (id),
  request_id INT REFERENCES Requests (id),
  hours      DECIMAL(6, 2)                NOT NULL,
  work_time  DECIMAL(4, 2)                NOT NULL,
  comment    VARCHAR(255)
);

-- CREATE UNIQUE INDEX history_userId_index ON History (user_id);

CREATE TABLE Holidays (
  id   SERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  date DATE         NOT NULL
);

ALTER SEQUENCE users_id_seq RESTART;

ALTER SEQUENCE requests_id_seq RESTART;

ALTER SEQUENCE acceptances_id_seq RESTART;

ALTER SEQUENCE history_id_seq RESTART;

ALTER SEQUENCE holidays_id_seq RESTART;