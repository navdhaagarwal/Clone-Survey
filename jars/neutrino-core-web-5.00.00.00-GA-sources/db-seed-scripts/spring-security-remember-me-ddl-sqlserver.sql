IF NOT EXISTS (SELECT * FROM sysobjects WHERE id = object_id(N'persistent_logins')
AND OBJECTPROPERTY(id, N'IsUserTable') = 1)
CREATE TABLE persistent_logins (
  username VARCHAR(64) DEFAULT NULL,
  series VARCHAR(64) DEFAULT NULL,
  token VARCHAR(64) DEFAULT NULL,
  last_used datetime2(7) DEFAULT NULL
);