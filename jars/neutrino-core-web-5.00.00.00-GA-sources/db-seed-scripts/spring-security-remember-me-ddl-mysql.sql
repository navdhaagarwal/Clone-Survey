CREATE TABLE IF NOT EXISTS `persistent_logins` (
  `username` VARCHAR(64) DEFAULT NULL,
  `series` VARCHAR(64) DEFAULT NULL,
  `token` VARCHAR(64) DEFAULT NULL,
  `last_used` datetime DEFAULT NULL
);