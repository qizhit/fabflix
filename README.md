# Fablix Application - Project1: Movie list, Single Movie and Single Star Page

## Project Overview
In this project, we implemented the core functionalities of Fablix Application, which includes:
- A **Movie List Page** that shows the top 20 rated movies in the database.
- A **Single Movie Page** that shows the detailed information about a selected movie.
- A **Single Star Page** that shows the detailed information about a selected star.
- Users can navigate between these pages via hyperlinks or back button.

This is the first part of this application, and additional features will be developed later.

## Project Structure
This project is designed with a clear separation of frontend and backend functionality.
It was developed using following technologies:
- Backend: JavaServlets handles API requests, interact the database and return data in JSON format.
- Frontend: HTML provides user interface styled with CSS and Bootstrap for responsive design, JavaScript dynamically fetches and renders data from server.
- Database: MySQL stores all the data that used in this project, including movies, stars, genres, and ratings.
- Database connection: Tomcat handles servlet requests, and manage the connection between the servlets and database.

## Features Implemented
1. Movie List Page
   - Displays the top 20 rated movies, sorted by rating. Each movie contains title, year, director, first three genres, first three stars, and rating.
   - Hyperlinks are provided for each movie and star, allowing users to navigate to the corresponding Single Movie or Single Star page for more details.
2. Single Movie Page
   - Displays the selected movie's title, year, director, all genres, all stars that acted, and rating.
   - Each star's name is a hyperlink that navigates to the Single Star Page.
   - A back button allows users to return to the Movie List Page
3. Single Star Page
   - Displays the selected star's name, year of birth (N/A if not available), all movies in which this s star performed.
   - Each movie title is a hyperlink that navigates to the Single Movie Page.
   - A back button allows users to return to the Movie List Page

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

## Demo Video
A screen recording demo is available here: https://www.youtube.com/watch?v=8VkIdEvpiic

The demo shows the application running on an AWS instance and covers all the required functionalities, including navigating between pages and displaying movie and star details.

## Team Contributions
We collaborated on setting up the environment (Tasks 1-5) and worked together on the Movie List Page (MovieListServlet, index.html, and movie-list.js). Our specific contributions were as follows:
- Xuan Gu: Developed the Single Movie feature, wrote the README.md file, and created the moviedb database.
- Qizhi Tian:  Developed the Single Star feature, created the style.css file, and set up the AWS instance.