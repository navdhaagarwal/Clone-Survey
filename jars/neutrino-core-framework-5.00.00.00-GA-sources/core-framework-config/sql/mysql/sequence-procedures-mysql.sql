DROP FUNCTION IF EXISTS `nextval` $$
CREATE FUNCTION `nextval` (`seq_name` varchar(100),`seq_incr` int(4))
RETURNS bigint(20) NOT DETERMINISTIC
BEGIN
DECLARE cur_val bigint(20);
SELECT
sequence_cur_value INTO cur_val
FROM
sequence_data
WHERE
sequence_name = seq_name for update;
IF cur_val IS NOT NULL THEN
UPDATE
sequence_data
SET
sequence_cur_value = IF (
(sequence_cur_value + ifnull(seq_incr,sequence_increment)) > sequence_max_value,
IF (
sequence_cycle = TRUE,
sequence_min_value,
NULL
),
sequence_cur_value + ifnull(seq_incr,sequence_increment)
)
WHERE
sequence_name = seq_name;
END IF;
RETURN cur_val;
END $$