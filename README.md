# Fablix Application - Project3: Fabflix Advanced Features

## Project Overview
This project extends Fabflix with enhanced security and employee functionalities, Key additions include:
- A **Security Enhancements**, a integration of reCAPTCHA, HTTPS, and encrypted passwords for user protection.
- A **Employee dashboard** that provides metadata, allows for star and movie additions, and utilizes stored procedures for efficient database management.
- A **XML parsing** that automatic loads additional movie, star, and genre data into the Fabflix database.

This phase includes most core functionalities, with additional features planned for future development.

## Project Structure
This project is designed with a clear separation of frontend and backend functionality.
It was developed using following technologies:
- Backend: JavaServlets handles API requests, interact the database and return data in JSON format.
- Frontend: HTML provides user interface styled with CSS and Bootstrap for responsive design, JavaScript dynamically fetches and renders data from server.
- Database: MySQL stores all the data that used in this project, including movies, stars, genres, and ratings.
- Database connection: Tomcat handles servlet requests, and manage the connection between the servlets and database.

## Features Implemented
1. reCAPTCHA Integration
- Prevents bots from accessing the login by adding reCAPTCHA validation.
- Displays appropriate error messages if reCAPTCHA fails.
2. HTTPS Implementation
- Enforces HTTPS for secure data transmission and redirects HTTP traffic to HTTPS.
- Configured with Tomcat's SSL on port 8443.
3. Prepared Statements
- Utilizes PreparedStatement to prevent SQL injection.
- Prepared statements are used across various servlets for secure database queries (detailed list below).
4. Password Encryption
- Stores encrypted passwords in the database and verifies login by comparing the hashed user input with the stored hash.
5. Employee Dashboard
- Provides secure access for employees to an admin dashboard.
- Displays database metadata and allows employees to add new movies and stars with success/error messages for operations.
- Employee can go to customer page to check the insertion.
6. XML Parsing and Data Import
- Reads large XML files, automatically formats, and imports new movies, stars, and genres into the database.
- Parses mains243.xml, casts124.xml, actors63.xml.
- Logs inconsistent data without interrupting the process (see Inconsistencies Report)

## AWS Deployment
This application is deployed on an AWS EC2 instance, which is configured with the necessary software to support the application, including:
- Java 11
- Tomcat 10
- MySQL 8.0

Deployment Steps:
- Access the EC2 instance via ssh
- Prepared the instance, the existing database and Git repository are cleared, and the movie-data.sql and create_table.sql files are stored for later use.
- Restart the instance to ensure a clean state.
- SSH into the EC2 instance again and perform a fresh Git clone of this project.
- Use the prepared SQL files to create and populate the MySQL database
- Use Maven to package the project and deploy the WAR file
- Once the deployment is complete, the application is accessible via the EC2 instance’s public IP


## Substring matching design
The substring matching feature in the MovieListServlet is designed to provide flexible search options for users by allowing partial matches on movie titles, directors, and star names. This is implemented using SQL LIKE queries with wildcard characters (%), enabling users to find results that match their input in various ways.

**Matching Patterns**:
- Starts with ('ABC%'): Matches titles starting with "ABC".
- Contains ('%AN%'): Matches text containing "AN" anywhere.
- Ends with ('%XYZ'): Matches names ending with "XYZ".

**The matching happens in two main areas:**
- Browsing Title Matching:
  - If you are browsing titles, and you provide a letter, it will find titles starting with that letter.
  The line:
  `countStatement.setString(paramIndex, browseTitle.toLowerCase() + "%");`
  adds the % after the browseTitle, meaning it matches all titles that start with the given browseTitle (case-insensitive).
  
- Searching Specific Fields (Title, Director, Star):
  - When searching for a movie title, director, or star, the matching is more flexible.
  The line:
  `countStatement.setString(paramIndex++, "%" + searchTitle.toLowerCase() + "%");`
  adds % to both sides of the search text, which means it will match the given text anywhere in the field. For example, if searchTitle is "star", it will match "Star Wars", "The Starry Night", or "A Star is Born".

## Prepared Statements
Prepared statements are used in the following files to secure database operations:
- LoginServlet: emailQuery
- MainServlet: query
- MovieListServlet: queryBuilder, genreQuery, starQuery, countQuery
- SingleMovieServlet: query
- SingleStarServlet: query
- PaymentServlet: cardQuery, saleInsert
- ConfirmationServlet: querySalesSQL
- UpdateTable: statement, insertStarSQL
- AddMovieServlet:getMovieId, getStarId, getGenreId
- AddStarServlet: getStarIdSQL

## Stored Procedure - [stored-procedure.sql](stored-procedure.sql)

## Optimization of XML Parsing
To enhance XML parsing efficiency, the following strategies were implemented:
1. StringBuilder: Used for efficient string manipulation.
2. HashMap Caching: Stores existing star and genre entries to avoid redundant selections and inserts.
3. Selective Processing: Processes only relevant elements.
- For example: `if (qName.equalsIgnoreCase("actor"))`, `else if (qName.equalsIgnoreCase("stagename"))`
4. Batch(): Groups insert operations of parsed data from xml to reduce database transaction frequency.
- refer UpdateTable.java, eg. insert stars from parsed stars.
5. Single Database Connection: Maintains a single, secure database connection throughout parsing, enhancing both security and efficiency.
- Execute parse at UpdateTable.
These optimizations resulted in a notable decrease in XML parsing time compared to the naive approach.

## Parsing Structures:
- mains243.xml - MainSAXParser: Stores attributes in MainsItem class instances and put them into a Arraylist. Ignores inconsistent data (no stars, no genres, the type of year, etc.).
- casts124.xml - CastsSAXParser: Stores attributes in CastsItem class instances and put them into a Arraylist. Ignores inconsistent data (no starName, starName is 'sa' or 's a', etc.).
- actors63.xml - StarsSAXParser: Utilizes a HashMap with the structure HashMap<name, birthYear>. The primary key for checking duplicates is name; if an entry with the same name already exists, it will not be inserted.
- Inserting - UpdateTable: Inserts new data and ignore duplications (same entries in tables: movies, stars, genres, stars_in_movies, genres_in_movies).

## Inconsistencies Report
During XML parsing, some data inconsistencies were encountered, such as:
- Invalid data types (non-integer values for numeric fields).
Running time Record:
- mains243.xml
- casts124.xml
- actors63.xml
Inconsistent entries are logged in the file:
- [CastsInconsistent.txt](CastsInconsistent.txt)
- [MovieInconsistent.txt](MovieInconsistent.txt)
- [StarDuplicateEntries.txt](StarDuplicateEntries.txt)
- [StarInconsistentEntries.txt](StarInconsistentEntries.txt)
, which can be found in the project directory. The program continues processing after logging these issues.

## Demo Video
A demo video showcasing the setup and features can be found here:
https://www.youtube.com/watch?v=zFvi9Kceqec

## Team Contributions
We collaborated on setting up the environment, database (eg. Encryption Password, domain set) and demo recording.
Our specific contributions were as follows:
- Xuan Gu: wrote the README.md file, developed dashboard functionalities, and implemented the StarParser.
- Qizhi Tian: integrated reCAPTCHA, enabled HTTPS, developed employee dashboard login, and implemented both the CastParser and MovieParser.