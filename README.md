# Fablix Application - Project 4: Fabflix Advanced Features

## Project Overview
Fabflix is an advanced web application for browsing, searching, shopping, and managing movie information.
In this phase of the project, we implemented advanced features to enhance the application's performance, scalability, and reliability.
Key features include:
- Full-text search and autocomplete for efficient user experience.
- JDBC Connection Pooling for optimized query handling.
- Load balancing between master and slave databases for read/write operations.
- Advanced database routing for optimized performance.

## Demo Video
A demo video showcasing the setup and features can be found here:

## Instruction of deployment
This application is deployed on an AWS EC2 instance, which is configured with the necessary software to support the application, including:
- Java 11
- Tomcat 10
- MySQL 8.0

Deployment Steps:
- Access the EC2 instance(main, master, slave) via ssh
- Prepared the instance, the existing database and Git repository are cleared, and the movie-data.sql and create_table.sql files are stored for later use.
- Restart the instance to ensure a clean state.
- SSH into the EC2 instance again and perform a fresh Git clone of this project.
- Use the prepared SQL files to create and populate the MySQL database
- Use Maven to package the project and deploy the WAR file
- Once the deployment is complete, the application is accessible via the EC2 instance’s public IP

## Connection Pooling
1. Code and Configuration Files
- META-INF/context.xml (pool configuration)
- All files using PreparedStatement, listed in the Prepared Statements section.

2. How Connection Pooling is Utilized
- The context.xml file defines a connection pool using org.apache.tomcat.jdbc.pool.DataSourceFactory.
- The pool is initialized and managed by Tomcat, allowing servlets to lease and release connections efficiently.
- Servlets obtain connections from the pool via a DataSource (separating read/write operations), reducing the overhead of creating new connections for every request.

3. Connection Pooling with Two Backend SQL Databases
- Separate connection pools are configured for the master and slave databases in context.xml.
- Queries are routed dynamically based on type: Write queries go to the master database. Read queries go to the slave database.
- This setup allows for load balancing and performance optimization.

## Master/Slave
1. Code and Configuration Files
- META-INF/context.xml (database configurations)
- All files using PreparedStatement, listed in Prepared Statements section.

2. Query Routing to Master/Slave SQL 
- Write Requests: Write queries are routed to the master database to ensure data consistency.
- Read Requests: Read queries are routed to the slave database to offload workload from the master and enhance performance. Servlets like MovieListServlet dynamically determine whether a query is read-only and route it accordingly.

## Project Structure
This project is designed with a clear separation of frontend and backend functionality.
It was developed using following technologies:
- Backend: JavaServlets handles API requests, interact the database and return data in JSON format.
- Frontend: HTML provides user interface styled with CSS and Bootstrap for responsive design, JavaScript dynamically fetches and renders data from server.
- Database: MySQL stores all the data that used in this project, including movies, stars, genres, and ratings.
- Database connection: Tomcat handles servlet requests, and manage the connection between the servlets and database.

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

## Team Contributions
We collaborated on demo recording and implementation JDBC Connection Pooling functionality.
Our specific contributions were as follows:
- Xuan Gu: wrote the README file, Load balancing functionalities.
- Qizhi Tian: Developed full text search and autocomplete functionalities.