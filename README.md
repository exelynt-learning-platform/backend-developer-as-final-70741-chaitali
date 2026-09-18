# Resource Booking System

This is a RESTful Resource Booking System developed using Spring Boot, Java, Spring Security, JWT, JPA/Hibernate, and MySQL.

The main purpose of this project is to manage resources and their reservations. Users can log in, view available resources, and create reservations. Administrators have additional access to manage resources and reservations.

## Assignment

**Assignment Title:** Backend Developer Assignment

**Deadline:** 30th September 2026

## Features

The application includes the following features:

- JWT-based login and authentication
- Password encryption using BCrypt
- Two user roles: ADMIN and USER
- Role-based access to APIs
- Resource management
- Reservation management
- Reservation ownership protection
- Reservation status management
- Reservation filtering
- Pagination and sorting
- Decimal values for reservation prices
- MySQL database integration
- JPA/Hibernate for database operations
- Swagger/OpenAPI documentation
- Test users created automatically when the application starts
- Input validation and error handling

## Technology Stack

### Backend

- Java 21
- Spring Boot 4.1.1
- Spring Web
- Spring Data JPA
- Spring Security
- JWT
- BCrypt
- MySQL
- Hibernate
- Lombok
- Bean Validation
- Swagger / OpenAPI
- Maven

## Test Users

Two users are created automatically when the application starts if they do not already exist in the database.

### Admin User

- **Username:** `admin`
- **Email:** `admin@example.com`
- **Password:** `Admin@123`
- **Role:** `ADMIN`

The admin user can manage resources and reservations.

### Normal User

- **Username:** `user`
- **Email:** `user@example.com`
- **Password:** `User@123`
- **Role:** `USER`

The normal user can view resources, create reservations, and view their own reservations.

These accounts are provided for testing the application.

## Project Structure

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
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md