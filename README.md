# College Attendance Management System — Backend

Spring Boot backend for a multi-college College Attendance Management System.

## Tech Stack

* Java 21
* Spring Boot 4.1.1
* Spring Security
* Supabase PostgreSQL
* Supabase Auth / JWT
* Spring Data JPA / Hibernate
* Maven
* OpenAPI / Swagger

## Main Features

### Authentication & Authorization

* Supabase JWT authentication
* Role-based authorization
* Supported roles:

    * `super_admin`
    * `college_admin`
    * `hod`
    * `teacher`
    * `student`
* College-level data isolation

### Academic Management

* Colleges
* Departments
* Semesters
* Academic sessions
* Programs
* Sections
* Subjects
* Subject offerings
* Teacher assignments

### Timetable & Classes

* Timetable management
* Extra class requests
* Class session lifecycle
* Session start and submission
* Teacher and student class views

### Attendance

* Teacher attendance marking
* Present / absent attendance
* Attendance summaries
* Subject-wise attendance
* Student attendance history
* Attendance validation
* Attendance correction requests

### Student & Teacher APIs

* Student CRUD
* Student profile
* Student dashboard
* Student notifications
* Teacher CRUD
* Teacher profile
* Teacher dashboard

## Project Structure

```text
src/
├── main/
│   ├── java/
│   │   └── com/attendance/system/
│   │       ├── config/
│   │       ├── controller/
│   │       ├── entity/
│   │       ├── repository/
│   │       ├── security/
│   │       └── service/
│   └── resources/
│       ├── application.properties
│       └── application-prod.properties
└── test/
    └── java/
```

## Configuration

Production configuration uses environment variables for database and Supabase authentication settings.

Required environment variables:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
SUPABASE_JWT_ISSUER
```

Example:

```bash
export DB_URL="your-database-url"
export DB_USERNAME="your-database-user"
export DB_PASSWORD="your-database-password"
export SUPABASE_JWT_ISSUER="your-supabase-auth-issuer"
```

Do not commit real passwords, JWTs, API keys, or other secrets to GitHub.

## Database

The backend uses PostgreSQL hosted through Supabase.

Hibernate schema validation is enabled:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

The backend does not automatically modify the production database schema.

## Running Locally

### Development

Set the required environment variables and run:

```bash
./mvnw spring-boot:run
```

The application runs on:

```text
http://localhost:8080
```

### Production Profile

Run with the production profile:

```bash
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
```

The production profile disables Swagger/OpenAPI documentation.

## Build & Test

Run the complete Maven verification:

```bash
./mvnw clean verify -DskipTests=false
```

Run the security unit test:

```bash
./mvnw -Dtest=RoleServiceTest test
```

## Health Check

The health endpoint is:

```text
GET /actuator/health
```

Example:

```bash
curl -i http://localhost:8080/actuator/health
```

## API Security

Protected API endpoints require a valid Supabase JWT:

```text
Authorization: Bearer <JWT>
```

Swagger/OpenAPI documentation is available during development but disabled in the production profile.

## Git Workflow

Main branch:

```text
main
```

Typical workflow:

```bash
git status
git add .
git commit -m "your message"
git push
```

## Repository

GitHub repository:

`https://github.com/ravi4gitCommit/college-attendance-management-system-backend`

## Project Status

The backend has been tested across authentication, authorization, academic management, timetable, class sessions, attendance, student APIs, teacher APIs, and attendance correction workflows.

Production configuration and local production-profile startup have also been verified.
