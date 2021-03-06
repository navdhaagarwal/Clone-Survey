CREATE TABLE ACCESS_LOG 
(	ID NUMBER(19,0) NOT NULL ENABLE, 
	REQUEST_DATE_TIME TIMESTAMP (6), 
	METHOD VARCHAR2(255 CHAR), 
	MODULE VARCHAR2(255 CHAR), 
	QUERY_STRING VARCHAR2(4000 CHAR), 
	QUERY_STRING_FRAGMENT VARCHAR2(4000 CHAR), 
	REMOTEHOST VARCHAR2(255 CHAR), 
	SERVER_IP VARCHAR2(255 CHAR), 
	SESSION_ID VARCHAR2(255 CHAR), 
	URI VARCHAR2(4000 CHAR), 
	URI_FRAGMENT VARCHAR2(4000 CHAR), 
	USER_NAME VARCHAR2(255 CHAR), 
	STATUS_CODE NUMBER(10,0) NOT NULL ENABLE, 
	WEB_URI_REPOSITORY VARCHAR2(255 CHAR), 
	 PRIMARY KEY (ID)	 
);
