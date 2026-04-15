# Splito

Splito is a Splitwise-like backend application built with **Java**, **Spring Boot**, and **PostgreSQL**.  
It provides APIs for managing users, groups, expenses, settlements, balances, and authentication.

The project is designed as a RESTful backend with support for:

- User management
- Group creation and membership
- Expense creation and split calculation
- Group balance tracking
- Settlement tracking
- JWT-based authentication
- Redis caching
- Flyway database migrations
- OpenAPI / Swagger UI documentation

---

## Tech Stack

- Java 17
- Spring Boot 4
- Spring Web
- Spring Data JPA
- Spring Security
- PostgreSQL
- Redis
- Flyway
- JWT
- Lombok
- Spring Cache
- Springdoc OpenAPI

---

## Features

### User Management
- Create users
- Fetch user details
- Manage user-related operations

### Group Management
- Create groups
- Add members to groups
- Support for direct groups
- Fetch group details and members

### Expense Management
- Add expenses to groups
- Split expenses equally or with exact amounts
- Track who paid and who owes
- Validate split participants and amounts

### Balance Calculation
- Calculate balances for each user within a group
- Simplify debts where applicable

### Settlements
- Record settlements between users
- Adjust balances after settlement

### Authentication
- Signup
- Login
- JWT access token generation
- Refresh token flow
- Logout

### Caching
- Redis-based caching for frequently accessed data such as:
    - users
    - groups
    - group balances
    - group lists
    - user lists
    - how to start redis container
    - docker run -d --name splito-redis -p 6379:6379 redis:7

---

## Project Structure

```text
src/main/java/com/splito
├── config         # Security, Redis, Jackson, OpenAPI, etc.
├── controller     # REST controllers
├── dto            # Request / response DTOs
├── entity         # JPA entities
├── exception      # Custom exceptions and handlers
├── repository     # JPA repositories
├── service        # Business logic
├── security       # JWT filters, auth services
└── SplitoApplication.java