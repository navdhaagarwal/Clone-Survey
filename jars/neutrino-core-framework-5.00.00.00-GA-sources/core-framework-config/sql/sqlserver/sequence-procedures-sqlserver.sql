
CREATE PROCEDURE [dbo].[next_val]
@sequenceName varchar(50),
@nextval bigint OUTPUT
AS
DECLARE @sequenceValue  bigint;
SET NOCOUNT ON;
SELECT
	@sequenceValue=sequence_cur_value
FROM
sequence_data
WHERE
sequence_name = @sequenceName ;

--IF begins
IF @sequenceValue IS NOT NULL 
BEGIN
UPDATE
sequence_data
SET
sequence_cur_value = (SELECT CASE WHEN ((sequence_cur_value + sequence_increment) >  sequence_max_value)
			THEN 
			CASE WHEN sequence_cycle=0
				 THEN  sequence_min_value
				ELSE NULL 
				END
			ELSE sequence_cur_value + sequence_increment
			END			
			FROM sequence_data WHERE sequence_name = @sequenceName)
WHERE sequence_name = @sequenceName;

END

SELECT @nextval = @sequenceValue;

RETURN ;

