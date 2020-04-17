CREATE TABLE IF NOT EXISTS `sequence_data` (
`sequence_name` varchar(100) NOT NULL,
`sequence_increment` int(11) unsigned NOT NULL DEFAULT 1,
`sequence_min_value` int(11) unsigned NOT NULL DEFAULT 1,
`sequence_max_value` bigint(20) unsigned NOT NULL DEFAULT 18446744073709551615,
`sequence_cur_value` bigint(20) unsigned DEFAULT 1,
`sequence_cycle` boolean NOT NULL DEFAULT FALSE,
PRIMARY KEY (`sequence_name`)
) ;

insert into sequence_data (sequence_name,sequence_increment,sequence_min_value,sequence_max_value,sequence_cur_value,sequence_cycle) values ('activiti_sequence_generator',1,1,9999999999999999999,1,0);
insert into sequence_data (sequence_name,sequence_increment,sequence_min_value,sequence_max_value,sequence_cur_value,sequence_cycle) values ('ACCESS_LOG_SEQ',1,1,9999999999999999999,1,0);
