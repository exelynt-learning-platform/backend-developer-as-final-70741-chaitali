Resource Booking System
A secure RESTful Resource Booking System built using Spring Boot, Java, Spring Security, JWT, JPA/Hibernate, and MySQL.

The system allows authenticated users to view available resources and create reservations, while administrators have full access to manage resources and reservations.

Assignment
Assignment Title: Backend Developer Assignment

Deadline: 30th September 2026

Features
JWT-based authentication
BCrypt password encryption
ADMIN and USER role-based access control
Secure protected REST APIs
Resource management
Reservation management
Reservation ownership protection
Reservation status management
Reservation filtering
Pagination
Sorting
Reservation price using decimal values
MySQL database integration
JPA/Hibernate persistence
Swagger/OpenAPI API documentation
Seed users for testing
Validation and error handling
Technology Stack
Backend
Java 21
Spring Boot 4.1.1
Spring Web
Spring Data JPA
Spring Security
JWT
BCrypt
MySQL
Hibernate
Lombok
Bean Validation
Swagger / OpenAPI
Maven
Frontend
React
Vite
JavaScript
CSS

# Project Structure

```text
resource-booking-system/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/booking/resource_booking_system/
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── dto/
│   │   │       ├── entity/
│   │   │       ├── exception/
│   │   │       ├── repository/
│   │   │       ├── security/
│   │   │       └── service/
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│
├── resource-booking-frontend/
│
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```