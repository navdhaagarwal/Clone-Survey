create table oauth_client_details
(
client_id VARCHAR2(256) PRIMARY KEY,
resource_ids VARCHAR2(256),
client_secret VARCHAR2(256),
scope VARCHAR2(256),
authorized_grant_types VARCHAR2(256),
web_server_redirect_uri VARCHAR2(256),
authorities VARCHAR2(256),
access_token_validity number(6),
refresh_token_validity number(6),
additional_information VARCHAR2(4000),
autoapprove VARCHAR2(256)
);


INSERT INTO oauth_client_details values('353b302c44574fff565045687e534e7d6a',null,'286924697e615a672a646a493545646c','read','password',null,'ROLE_TEST',1200,1200,null,'true');
INSERT INTO oauth_client_details values('mCAS-ANDRIOD',null,'mobility','read,write','password,refresh_token',null,'ROLE_ANDROID',5184000,5184000,null,'true');

create table oauth_client_token (
  token_id VARCHAR2(256),
  token BLOB,
  authentication_id VARCHAR2(256),
  user_name VARCHAR2(256),
  client_id VARCHAR2(256)
);

create table oauth_access_token (
  token_id VARCHAR2(256),
  token BLOB,
  authentication_id VARCHAR2(256),
  user_name VARCHAR2(256),
  client_id VARCHAR2(256),
  authentication blob,
  refresh_token VARCHAR2(256)
);

create table oauth_refresh_token (
  token_id VARCHAR2(256),
  token BLOB,
  authentication blob
);

create table oauth_code (
  code VARCHAR2(256), 
  authentication blob
);
