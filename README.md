# Authentication and Authorization Microservice

A reusable authentication and authorization microservice built with Spring Boot.
This microservice provides secure user management and JWT-based authentication.

## Features

- User registration and login
- JWT-based authentication
- Secure password handling (RSA public/private key pair)
- PostgreSQL database integration (containerized with Docker)
- Environment-based configuration with profile support (local/prod) and secure secrets management
- Input validation using Jakarta Validation (Bean Validation 3.0)
- RESTful API design

## Technology Stack

- Java 17
- Spring Boot 3.4.5
- Spring Security
- Spring Data JPA
- Jakarta Validation (Bean Validation 3.0)
- Lombok
- Maven
- JWT (JSON Web Tokens)
- PostgreSQL

## Prerequisites (for local development)

- Java 17 or higher
- Maven
- PostgreSQL database
- Environment variables configured
- Docker & Docker Compose (for containerized setup)

## Environment Setup

This application supports **profile-based configuration** for different environments:

### Local Development (Profile: `local`)

For local development, create a `.env` file in the root directory:

```env
DB_USERNAME=your_local_db_username
DB_PASSWORD=your_local_db_password
# Then generate RSA key pair in com/gab/authservice/resources/keys/private.pem and com/gab/authservice/resources/keys/public.pem
```

**Run locally:**
```bash
# Uses .env file and local PostgreSQL
./mvnw spring-boot:run -Dspring.profiles.active=local
```

- **rebuild.sh** — Script to automate rebuilding the JAR, Docker image, and restarting containers for development.


### Production (Profile: `prod` - Default)

CI/CD pipeline using GitHub Actions that:
- Runs on every push to main branch
- Builds and tests the application
- Builds and pushes Docker image to GitHub Container Registry (GHCR) - you will have to use your own GHCR token (until i make this a saas for fun)
- Deploys to EC2 instance using appleboy SSH action
- Sets up production environment with AWS Secrets Manager for RSA keys
- Verifies deployment health using Spring Boot Actuator endpoints

Production uses **AWS Secrets Manager** for secure configuration management:
- Database credentials from Github Actions Secrets
- JWT RSA keys from AWS Secrets Manager  
- No `.env` file stored in container or repo

!!! [note] To check logs, you can `docker ps` and then `docker logs <container_id>` or `docker exec -it <container-name-or-id> /bin/bash (or /bin/sh)` if bash not installed

### Running with Docker (local docker env)

1. Build and run the containers:
   ```bash
   ./rebuild.sh
   ```
   Or manually:
   ```bash
   ./mvnw clean package
   docker-compose build --no-cache
   docker-compose up
   ```
2. The service will be available at `http://localhost:8080`.
3. PostgreSQL will be available at `localhost:5432` (inside the Docker network, use `postgres` as the hostname).

## API Documentation

### Authentication Endpoints

#### Signup
```http
POST /auth/signup
Content-Type: application/json

{
    "email": "string",
    "password": "string"
}
```

Response:
```http
200 OK
"User registered successfully"
```

#### Login
```http
POST /auth/login
Content-Type: application/json

{
    "username": "string",
    "password": "string"
}
```

Response:
```http
200 OK
"jwt_token_string"
```

## Building and Running (without Docker)

### Build
```bash
./mvnw clean install
```

### Run
```bash
./mvnw spring-boot:run
```

## Development

### Project Structure
```
src/main/java/com/gab/authservice/
├── config/         # Configuration classes
├── controller/     # REST controllers
├── dto/           # Data Transfer Objects
├── entity/        # Database entities
├── repository/    # Data access layer
└── service/       # Business logic
```

Store your own RSA keys in 
```
src/main/java/com/gab/resources/keys
├── public.pem
└── private.pem
```

### Database Configuration
The service uses PostgreSQL. Make sure to:
1. Have PostgreSQL installed and running
2. Create a database for the service
3. Configure the database connection in your environment variables (db password and username)

### Security Considerations

#### JWT Token
- Tokens are signed using RSA private key (RS256 algorithm)
- Tokens contain user information and expiration time

#### Password Security
- Passwords are hashed before storage
- Input validation is enforced
- Password requirements should be configured according to your security needs

## Testing

This project uses a comprehensive testing strategy:

- **Unit Tests**: Service layer logic is tested in isolation using JUnit and Mockito. Dependencies like repositories, password encoders, and JWT services are mocked to ensure business logic is correct and robust.
- **Integration Tests**: Controller endpoints are tested using Spring Boot's `@SpringBootTest` and `MockMvc`, with a real PostgreSQL database spun up by Testcontainers. This ensures the full stack (controller, service, repository, and database) works as expected.
- **JWT Validation**: Integration tests verify that login returns a valid JWT token (correct format, not null).

!!! [note] TestContainer tests are run in CICD Github Actions runners as well.

### Running the Test Suite

```bash
./mvnw test
```

- Unit tests run by default.
- Integration tests automatically start a temporary PostgreSQL container (no need for a running local DB).

### What is Covered
- User signup (success and error cases)
- User login (success and error cases)
- JWT token generation and format
- Full controller-to-database integration

### Example: Integration Test
- Simulates real HTTP requests to `/auth/signup` and `/auth/login`
- Verifies correct responses and JWT format
