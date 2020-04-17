create table oauth_client_details
(
client_id VARCHAR(256) PRIMARY KEY,
resource_ids VARCHAR(256),
client_secret VARCHAR(256),
scope VARCHAR(256),
authorized_grant_types VARCHAR(256),
web_server_redirect_uri VARCHAR(256),
authorities VARCHAR(256),
access_token_validity integer,
refresh_token_validity integer,
additional_information VARCHAR(4000),
autoapprove VARCHAR(256)
);


INSERT INTO oauth_client_details values('353b302c44574fff565045687e534e7d6a',null,'286924697e615a672a646a493545646c','read','password',null,'ROLE_TEST',1200,1200,null,'true');
INSERT INTO oauth_client_details values('mCAS-ANDRIOD',null,'mobility','read,write','password,refresh_token',null,'ROLE_ANDROID',5184000,5184000,null,'true');

create table oauth_client_token (
  token_id VARCHAR(256),
  token varbinary,
  authentication_id VARCHAR(256),
  user_name VARCHAR(256),
  client_id VARCHAR(256)
);

create table oauth_access_token (
  token_id VARCHAR(256),
  token varbinary,
  authentication_id VARCHAR(256),
  user_name VARCHAR(256),
  client_id VARCHAR(256),
  authentication varbinary,
  refresh_token VARCHAR(256)
);

create table oauth_refresh_token (
  token_id VARCHAR(256),
  token varbinary,
  authentication varbinary
);

create table oauth_code (
  code VARCHAR(256), 
  authentication varbinary
);