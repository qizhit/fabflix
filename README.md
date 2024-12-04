# Fablix Application - Project 5: Fabflix Advanced Features

## Project Overview
Fabflix is a full-featured web application enabling users to browse, search, shop, and manage movie information. In Project 5, we focused on implementing advanced features and deploying the application to a Kubernetes (K8s) cluster for enhanced performance, scalability, and reliability.
Key features include:
- JDBC Connection Pooling for optimized query handling.
- Kubernetes Deployment with scalability and load balancing.
- Performance Testing using Apache JMeter.

## Instruction of deployment
This application is deployed on an AWS EC2 instance, which is configured with the necessary software to support the application, including:
- Kubernetes Tools: Kops, kubectl.
- Application Components: Java 11, Tomcat 10, MySQL 8.0.
- Docker: Build and push Fabflix Docker images.
- JMeter: Test performance.

Deployment Steps:
- Access the EC2 instance(k8s-admin) via ssh
- Prepared the instance, the existing database and Git repository are cleared, and the movie-data.sql and create_table.sql files are stored for later use.
- Restart the instance to ensure a clean state.
- SSH into the EC2 instance again and perform a fresh Git clone of this project.
- Use the prepared SQL files to create and populate the MySQL database
- Use Maven to package the project (master, slave) and deploy the WAR file
- Once the deployment is complete, the application is accessible via the EC2 instance’s public IP

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
- Disable reCAPTCHA for testing.
- Run test
- Collect Results

# Result of Performance Testing

- # JMeter TS/TJ Time Logs
    - #### Instructions of how to use the `log_processing.*` script to process the JMeter logs.


- # JMeter TS/TJ Time Measurement Report
| **Single-instance Version Test Plan**          | **Graph Results Screenshot** | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis** |
|------------------------------------------------|------------------------------|----------------------------|-------------------------------------|---------------------------|--------------|
| Case 1: HTTP/1 thread                          | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |
| Case 2: HTTP/10 threads                        | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |
| Case 3: HTTPS/10 threads                       | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |
| Case 4: HTTP/10 threads/No connection pooling  | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |


| **Scaled Version Test Plan**                   | **Graph Results Screenshot** | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis** |
|------------------------------------------------|------------------------------|----------------------------|-------------------------------------|---------------------------|--------------|
| Case 1: HTTP/1 thread                          | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |
| Case 2: HTTP/10 threads                        | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |
| Case 3: HTTP/10 threads/No connection pooling  | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |

## Demo Video
A demo video showcasing the setup and features can be found here:

## Application Features
Master-Slave Database Replication:
- Write requests: Routed to the master database.
- Read requests: Routed to the slave database for load balancing.
JDBC Connection Pooling:
- Defined in META-INF/context.xml for both master and slave databases.
- Reduces connection overhead and improves query efficiency.

## Project Structure
This project is designed with a clear separation of frontend and backend functionality.
It was developed using following technologies:
- Backend: JavaServlets handles API requests, interact the database and return data in JSON format.
- Frontend: HTML provides user interface styled with CSS and Bootstrap for responsive design, JavaScript dynamically fetches and renders data from server.
- Database: MySQL stores all the data that used in this project, including movies, stars, genres, and ratings.
- Database connection: Tomcat handles servlet requests, and manage the connection between the servlets and database.
Key Configuration Files:
- Dockerfile: Defines the Docker image for the application.
- fabflix-deployment-service.yaml: Kubernetes deployment configuration.
- ingress.yaml: Ingress setup for load balancing.

## Team Contributions
We collaborated on demo recording
Our specific contributions were as follows:
- Xuan Gu: wrote the README file, 
- Qizhi Tian: 