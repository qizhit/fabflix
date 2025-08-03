# Fabflix - Full-Stack Movie Database Web App

## Project Overview
**Notes:** Originally developed at UC Irvine and hosted under my school GitHub account ([QZT168](https://github.com/QZT168)), this project is now forked to my personal GitHub for long-term access.

This project was developed over the course of a quarter, evolving through five iterative milestones.  
Each milestone introduced new features, improvements, and refinements based on feedback and design goals.

This repository contains the **final milestone**, which includes major functionalities, polish, and testing.  
For context, earlier milestones are briefly summarized below and demonstrated in the video section.

### Final Milestone:
Fabflix is a full-featured web application enabling users to browse, search, shop, and manage movie information. In the final milestone, we focused on implementing advanced features and deploying the application to a Kubernetes (K8s) cluster for enhanced performance, scalability, and reliability.

Key features include:
- **JDBC Connection Pooling** for optimized query handling (updates).
- **Kubernetes** Deployment with scalability, load balancing and Master-Slave Database Replication.
- Performance Testing using Apache **JMeter**.

### Previous Phase - Milestone 1:
In this project, we implemented the core functionalities of Fablix Application, which includes:

- A **Movie List Page** that shows the top 20 rated movies in the database.
- A **Single Movie Page** that shows the detailed information about a selected movie.
- A **Single Star Page** that shows the detailed information about a selected star.
- Users can navigate between these pages via hyperlinks or back button.

This is the first part of this application, and additional features will be developed later.

### Previous Phase - Milestone 2:
In this phase, we are building an interactive movie browsing and shopping experience for Fabflix. The main features include:

- A **Login Page** that secure access to the application.
- A **Main Page** with movie browsing by category, movie search functionality, and shopping cart access.
- A **Movie List Page** that displays selected movies from the database.
- A **Single Movie Page** that shows the detailed information about a selected movie.
- A **Single Star Page** that shows the detailed information about a selected star.
- A **Checkout Page** to manage items in the shopping cart.
- A **Payment Page** for completing secure transactions.
- Easy navigation between pages via hyperlinks and the browser's back button.

### Previous Phase - Milestone 3:
This phase extends Fabflix with enhanced security and employee functionalities, Key additions include:

- A **Security Enhancements**, an integration of reCAPTCHA, HTTPS, and encrypted passwords for user protection.
- A **Employee dashboard** that provides metadata, allows for star and movie additions, and utilizes stored procedures for efficient database management.
- A **XML parsing** that automatically loads additional movie, star, and genre data into the Fabflix database.

This phase includes most core functionalities, with additional features planned for future development.

### Previous Phase - Milestone 4:
In this phase of the project, we implemented advanced features to enhance the application's performance, scalability, and reliability.
Key features include:

- **Full-text search** and **autocomplete** for an efficient user experience.
- **JDBC Connection Pooling** for optimized query handling.
- **Load balancing** between master and slave databases.
- **Advanced database routing** for optimized performance.

## Demo Video
For this final milestone, a demo video showcasing the setup and features can be found here: https://www.youtube.com/watch?v=Ot35snQ_FZg

Previous demos:
- Milestone 1: https://www.youtube.com/watch?v=8VkIdEvpiic
- Milestone 2: https://youtu.be/A4ngkQ2XeKw
- Milestone 3: https://www.youtube.com/watch?v=zFvi9Kceqec
- Milestone 4: https://youtu.be/PlvibSJA1MM

## Instruction of deployment
This application is deployed on an AWS EC2 instance, which is configured with the necessary software to support the application, including:
- Kubernetes Tools: Kops, kubectl.
- Application Components: Java 11, Tomcat 10, MySQL 8.0.
- Docker: Build and push Fabflix Docker images.
- JMeter: Test performance.

Deployment Steps:
- Access the EC2 instance(k8s-admin) via ssh.
- Prepared the instance, the existing database and Git repository are cleared, and the movie-data.sql and create_table.sql files are stored for later use.
- Restart the instance to ensure a clean state.
- SSH into the EC2 instance again and perform a fresh Git clone of this project.
- Use the prepared SQL files to create and populate the MySQL database.
- Deploy the application using docker image.
- Once the deployment is complete, the application is accessible via the EC2 instance’s public IP.

## Performance Testing with JMeter
Objectives: Stress test the Fabflix search feature using 10 threads.

Measure throughput under:
- 2 Fabflix pods and 3 worker nodes.
- 3 Fabflix pods and 4 worker nodes.

Test Plan:
- Use query_load.txt as input for search titles.
- Create a JMeter test plan (fabflix.jmx):
- Login and send search requests.
- Iterate through query_load.txt in an infinite loop.
- Run test
- Collect Results

## Result of Performance Testing
JMeter TS/TJ Time Measurement Report:

| **3 nodes  Test Plan** | **Nodes States Screenshot**             | **Graph Results Screenshot**             | **Throughout**  | **Average** |
|------------------------|-----------------------------------------|------------------------------------------|-----------------|-------------|
| HTTP/10 thread         | ![3nodes_states.jpg](3nodes_states.jpg) | ![3nodes_result.jpg](3nodes_result.jpg)  | 9923.177/minute | 57          |


| **4 nodes Test Plan** | **Nodes States Screenshot**             | **Graph Results Screenshot**            | **Throughout**   | **Average** |
|-----------------------|-----------------------------------------|-----------------------------------------|------------------|-------------|
| HTTP/10 thread        | ![4nodes_states.jpg](4nodes_states.jpg) | ![4nodes_result.jpg](4nodes_result.jpg) | 10012.464/minute | 57          |

## Project Structure
This project is designed with a clear separation of frontend and backend functionality.

Key Configuration Files:
- Dockerfile: Defines the Docker image for the application.
- fabflix-deployment-service.yaml: Kubernetes deployment configuration.
- ingress.yaml: Ingress setup for load balancing.
- fabflix.jmx: Jmeter test file.

## Team Contributions
Milestone 1: 
- Qizhi Tian:  Developed the Single Star feature, created the style.css file, and set up the AWS instance.
- Xuan Gu: Developed the Single Movie feature, wrote the README.md file, and created the moviedb database.

Milestone 2:
- Qizhi Tian: Developed the Login, Movie List, AddtoCart, Checkout, and Payment functionalities; designed the sorting, searching, and pagination features; and worked on page styling.
- Xuan Gu: wrote the README.md file, implemented the single-movie and single-star functionalities, Checkout/Payment/Confirmation frame and components.

Milestone 3:
- Qizhi Tian: integrated reCAPTCHA, enabled HTTPS, developed employee dashboard login, and implemented both the CastParser and MovieParser.
- Xuan Gu: wrote the README.md file, developed dashboard functionalities, and implemented the StarParser.

Milestone 4:
- Qizhi Tian: Developed full text search and autocomplete functionalities.
- Xuan Gu: wrote the README file, Load balancing functionalities.

Milestone 5:
- Qizhi Tian: implemented Docker functionalities, measured the performance using Jmeter, finalized the Fabflix application.
- Xuan Gu: wrote the README file, implemented K8s cluster functionalities, measured the performance using Jmeter.

  
This project was developed collaboratively over five milestones as part of a team project.

**Qizhi Tian** contributed to:
- Frontend and backend development, including key features such as login, movie listing, shopping cart, payment flow, and autocomplete search
- Infrastructure setup (AWS), security (reCAPTCHA, HTTPS), performance optimization (JMeter, Docker), and documentation (README),
- Final system integration and overall styling

**Xuan Gu** contributed to:
- Frontend components including single-movie and single-star pages, Kubernetes cluster configuration, and load balancing
- Database initialization, documentation (README), and dashboard development
- Performance benchmarking and infrastructure deployment
