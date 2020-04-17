CREATE TABLE persistent_logins(
  username VARCHAR2(64) DEFAULT NULL,
  series VARCHAR2(64) DEFAULT NULL,
  token VARCHAR2(64) DEFAULT NULL,
  last_used date DEFAULT NULL
);