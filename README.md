Fablix Application - Project1: Movie list, Single Movie and Single Star Page

Project Overview
In this project, we implemented the core functionalities for Fablix Application, which includes:
- A **Movie List Page** that shows the top 20 rated movies in the database.
- A **Single Movie Page** that shows the detailed information about a selected movie.
- A **Single Star Page** that shows the detailed information about a selected star.
- Users can navigate between these pages via hyperlinks or back botton.

This is the first part of this application, and additional features will be developed later.

Project Structure
This project is designed with a clear separation of frontend and backend functionality.
It was developed using following technologies:
- Backend: JavaServlets handles API requests, interact the database and return data in JSON format.
- Frontend: HTML provides user interface styled with ------??CSS??, JavaScript dynamically fetches and renders data from server.
- Database: MySQL stores all the data that used in this project, including movies, stars, genres, and ratings.
- Database connection: Tomcat handles servlet requests, and manage the connection between the servlets and database.

Features Implemented
1. Movie List Page
   - Displays the top 20 rated movies, sorted by rating.
   - Includes hyperlinks for each movie and star, allowing users to navigate to the corresponding Single Movie Page or Single Star Page for more detailed information.
2. Single Movie Page
   - Displays the selected movie's title, year, director, all genres, all stars that acted, rating.
   - Each star's name is a hyperlink that navigates to the Single Star Page.
   - A button is provided to navigate back to the Movie List Page
3. Single Star Page
   - Displays the selected star's name, year of birth (N/A if not available), all movies in which this s star performed.
   - Each movie title is a hyperlink that navigates to the Single Movie Page.
   - A button is provided to navigate back to the Movie List Page.

AWS Deployment
This application is deployed on an AWS instance. Tomcat is used to run the web application,
and MySQL is hosted on the same AWS instance.
--------detail??

Demo Video
A scree recording demo is available -------here-------
The demo shows the application running on an AWS instance and covers all the required functionalities, including navigating between pages and displaying movie and star details.

Team Contributions
- Xuan Gu: 
- 