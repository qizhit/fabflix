DELIMITER //

CREATE PROCEDURE add_star(
    IN star_name VARCHAR(100),
    IN birth_year INT
)
BEGIN
    DECLARE new_id VARCHAR(10);
    DECLARE last_numeric_part INT;

    -- Step 1: Get the numeric part of the latest star ID
SELECT COALESCE(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)), 0) INTO last_numeric_part FROM stars;

-- Step 2: Increment the numeric part and format it with 'nm' prefix
SET new_id = CONCAT('nm', LPAD(last_numeric_part + 1, 7, '0'));

    -- Step 3: Insert the new star with the generated ID
    -- If birth_year is NULL, set birthYear to NULL in the database; otherwise, use the provided birth_year value
INSERT INTO stars (id, name, birthYear)
VALUES (new_id, star_name, IFNULL(birth_year, NULL));
END //

DELIMITER ;