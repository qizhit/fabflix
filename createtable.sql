-- Database: moviedb

CREATE DATABASE IF NOT EXISTS moviedb;
USE moviedb;

-- Creating the 'movies' table
CREATE TABLE movies (
    id VARCHAR(10) PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    year INT NOT NULL,
    director VARCHAR(100) NOT NULL
    price DECIMAL(10, 2)
);

-- Creating the 'stars' table
CREATE TABLE stars (
    id VARCHAR(10) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    birthYear INT
);

-- Creating the 'stars_in_movies' table
CREATE TABLE stars_in_movies (
    starId VARCHAR(10),
    movieId VARCHAR(10),
    PRIMARY KEY (starId, movieId),
    FOREIGN KEY (starId) REFERENCES stars(id),
    FOREIGN KEY (movieId) REFERENCES movies(id)
);

-- Creating the 'genres' table
CREATE TABLE genres (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(32) NOT NULL
);

-- Creating the 'genres_in_movies' table
CREATE TABLE genres_in_movies (
    genreId INT,
    movieId VARCHAR(10),
    PRIMARY KEY (genreId, movieId),
    FOREIGN KEY (genreId) REFERENCES genres(id),
    FOREIGN KEY (movieId) REFERENCES movies(id)
);

-- Creating the 'creditcards' table
CREATE TABLE creditcards (
    id VARCHAR(20) PRIMARY KEY,
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50) NOT NULL,
    expiration DATE NOT NULL
);

-- Creating the 'customers' table
CREATE TABLE customers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50) NOT NULL,
    ccId VARCHAR(20) NOT NULL,
    address VARCHAR(200) NOT NULL,
    email VARCHAR(50) NOT NULL,
    password VARCHAR(20) NOT NULL,
	FOREIGN KEY (ccId) REFERENCES creditcards(id)
);

-- Creating the 'sales' table
CREATE TABLE sales (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customerId INT NOT NULL,
    movieId VARCHAR(10) NOT NULL,
    saleDate DATE NOT NULL,
    quantity INT DEFAULT 1,
    FOREIGN KEY (customerId) REFERENCES customers(id),
    FOREIGN KEY (movieId) REFERENCES movies(id)
);

-- Creating the 'ratings' table
CREATE TABLE ratings (
    movieId VARCHAR(10),
    rating FLOAT NOT NULL,
    numVotes INT NOT NULL,
    PRIMARY KEY (movieId),
    FOREIGN KEY (movieId) REFERENCES movies(id)
);