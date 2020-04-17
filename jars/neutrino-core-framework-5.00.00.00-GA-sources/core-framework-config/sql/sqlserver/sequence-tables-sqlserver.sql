IF NOT EXISTS (SELECT * FROM sysobjects WHERE id = object_id(N'sequence_data')
AND OBJECTPROPERTY(id, N'IsUserTable') = 1)
CREATE TABLE sequence_data ( sequence_name varchar(50) NOT NULL,sequence_increment int  NOT NULL DEFAULT 1,	sequence_min_value int  NOT NULL DEFAULT 1,	sequence_max_value bigint  NOT NULL DEFAULT 9223372036854775807,sequence_cur_value bigint  NOT NULL DEFAULT 9223372036854775807,sequence_cycle bit NOT NULL DEFAULT 'FALSE',PRIMARY KEY (sequence_name) WITH (IGNORE_DUP_KEY = ON));

IF EXISTS (SELECT * FROM sys.objects WHERE type = 'P' AND name = 'next_val')
DROP PROCEDURE next_val;

insert into sequence_data (sequence_name,sequence_increment,sequence_min_value,sequence_max_value,sequence_cur_value,sequence_cycle) values ('activiti_sequence_generator',1,1,9999999999999999999,1,0);
insert into sequence_data (sequence_name,sequence_increment,sequence_min_value,sequence_max_value,sequence_cur_value,sequence_cycle) values ('ACCESS_LOG_SEQ',1,1,9999999999999999999,1,0);