DROP SCHEMA rtdsm CASCADE;

CREATE SCHEMA rtdsm;
CREATE TABLE rtdsm.temperature (
  Src varchar(40) NOT NULL,
  Chnl  varchar(40) NOT NULL,
  genTime timestamp,
  temp double precision
);
CREATE TABLE rtdsm.light (
  Src varchar(40) NOT NULL,
  Chnl  varchar(40) NOT NULL,
  genTime timestamp,
  light double precision
);
