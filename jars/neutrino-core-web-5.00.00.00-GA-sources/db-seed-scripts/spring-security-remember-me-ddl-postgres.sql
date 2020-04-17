CREATE TABLE persistent_logins(
  username VARCHAR(64) DEFAULT NULL,
  series VARCHAR(64) DEFAULT NULL,
  token VARCHAR(64) DEFAULT NULL,
  last_used date DEFAULT NULL
);