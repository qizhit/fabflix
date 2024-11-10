DELIMITER //

CREATE PROCEDURE add_movie(
    IN movie_title VARCHAR(100),
    IN movie_year INT,
    IN movie_director VARCHAR(100),
    IN star_name VARCHAR(100),
    IN genre_name VARCHAR(32)
)
BEGIN
    DECLARE movie_id VARCHAR(10);
    DECLARE star_id VARCHAR(10);
    DECLARE movie_price DECIMAL(10, 2);
    DECLARE genre_id INT;
    DECLARE last_movie_id INT;
    DECLARE last_star_id INT;
    DECLARE last_numeric_part INT;

    SET movie_price = ROUND(5.00 + (RAND() * (30.00 - 5.00)), 2);

    -- Step 1: Generate a unique movie ID in the format 'tt0000001'
SELECT COALESCE(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)), 0) INTO last_numeric_part
FROM movies
WHERE id LIKE 'tt%';

SET movie_id = CONCAT('tt', LPAD(last_numeric_part + 1, 7, '0'));

    -- Step 2: Insert the new movie if it doesn’t already exist
    IF NOT EXISTS (SELECT 1 FROM movies WHERE title = movie_title AND year = movie_year AND director = movie_director) THEN
        INSERT INTO movies (id, title, year, director, price)
        VALUES (movie_id, movie_title, movie_year, movie_director, movie_price);
END IF;

    -- Step 3: Check if the star exists; if not, generate a new star ID and insert
SELECT id INTO star_id FROM stars WHERE name = star_name LIMIT 1;

IF star_id IS NULL THEN
        -- Generate a new star ID in the format 'nm0000001'
SELECT COALESCE(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)), 0) + 1 INTO last_star_id FROM stars;
SET star_id = CONCAT('nm', LPAD(last_star_id, 7, '0'));

        -- Insert the new star
INSERT INTO stars (id, name)
VALUES (star_id, star_name);
END IF;

    -- Step 4: Check if the genre exists; if not, insert it and get the new genre ID
SELECT id INTO genre_id FROM genres WHERE name = genre_name;

IF genre_id IS NULL THEN
        INSERT INTO genres (name) VALUES (genre_name);
        SET genre_id = LAST_INSERT_ID();  -- Retrieve the new genre ID (auto-incremented)
END IF;

    -- Step 5: Associate the star with the movie in stars_in_movies
    IF NOT EXISTS (SELECT 1 FROM stars_in_movies WHERE starId = star_id AND movieId = movie_id) THEN
        INSERT INTO stars_in_movies (starId, movieId)
        VALUES (star_id, movie_id);
END IF;

    -- Step 6: Associate the genre with the movie in genres_in_movies
    IF NOT EXISTS (SELECT 1 FROM genres_in_movies WHERE genreId = genre_id AND movieId = movie_id) THEN
        INSERT INTO genres_in_movies (genreId, movieId)
        VALUES (genre_id, movie_id);
END IF;
END //

DELIMITER ;