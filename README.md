# Fablix Application - Project2: Interactive Movie Shopping

## Project Overview
In this project, we are building an interactive movie browsing and shopping experience for Fabflix. The main features include:
- A **Login Page** that secure access to the application.
- A **Main Page** with movie browsing by category, movie search functionality, and shopping cart access.
- A **Movie List Page** that displays selected movies from the database.
- A **Single Movie Page** that shows the detailed information about a selected movie.
- A **Single Star Page** that shows the detailed information about a selected star.
- A **Checkout Page** to manage items in the shopping cart.
- A **Payment Page** for completing secure transactions.
- Easy navigation between pages via hyperlinks and the browser's back button.

This phase includes most core functionalities, with additional features planned for future development.

## Project Structure
This project is designed with a clear separation of frontend and backend functionality.
It was developed using following technologies:
- Backend: JavaServlets handles API requests, interact the database and return data in JSON format.
- Frontend: HTML provides user interface styled with CSS and Bootstrap for responsive design, JavaScript dynamically fetches and renders data from server.
- Database: MySQL stores all the data that used in this project, including movies, stars, genres, and ratings.
- Database connection: Tomcat handles servlet requests, and manage the connection between the servlets and database.

## Features Implemented
1. Login Page
   - Allows secure user login, with error handling for incorrect email or password.
   - Prevents access to other pages without authentication, redirecting to the Login Page if necessary.
   - On successful login, redirects users to the Main Page.
2. Main Page
   - The home page after login, providing options to browse movies and search by title, year, director, or star’s name with flexible search terms.
3. Movie List Page
   - Displays movie information: title, year, director, genres, stars, and rating.
   - Hyperlinks allow navigation to corresponding Single Movie or Single Star pages.
   - Sorting options by "title then rating" or "rating then title."
   - Pagination with customizable movie count per page and navigation buttons.
   - Allows adding movies to the shopping cart.
4. Single Movie Page
   - Shows details of the selected movie, including all genres, stars, and rating.
   - Hyperlinks to related genres and stars, allowing easy navigation.
   - Back button to return to the Movie List Page with preserved sorting and category filters.
   - Navigation options to the shopping cart and Main Page.
5. Single Star Page
   - Displays the selected star's name, year of birth (N/A if not available), all movies in which this s star performed.
   - Each movie title is a hyperlink that navigates to the Single Movie Page.
   - A back button allows users to return to the Movie List Page with customized sorting or category.
6. Checkout Page
   - The Shopping Cart allows customers to add movies for purchase, including multiple copies of each.
7. Payment Page
   - Customers enter credit card details, then submit the order.
   - Records successful payments and displays a confirmation page; if payment fails, provides an error prompt.
8. Confirmation Page
   - Show order details including sale ID, movies purchased and the quantities, and total price after successfully placing a order.

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
- LoginServlet: emailQuery
- ConfirmationServlet: querySalesSQL
- MainServlet: query
- MovieListServlet: queryBuilder, genreQuery, starQuery, countQuery
- PaymentServlet: cardQuery, saleInsert
- SingleMovieServlet: query
- SingleStarServlet: query


## Demo Video
A screen recording demo is available here:


The demo showcases the application running on an AWS instance, highlighting all key features, including page navigation, movie and star detail displays, and the shopping experience.

## Team Contributions
We collaborated on setting up the environment and demo recording.
Our specific contributions were as follows:
- Xuan Gu: wrote the README.md file,  implemented the single-movie and single-star functionalities, Checkout/Payment/Confirmation frame and components.
- Qizhi Tian: Developed the Login, Movie List, AddtoCart, Checkout, and Payment functionalities; designed the sorting, searching, and pagination features; and worked on page styling.