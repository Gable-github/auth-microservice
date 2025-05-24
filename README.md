# Authentication Service

A robust authentication and authorization microservice built with Spring Boot, providing secure user management and JWT-based authentication.

## Features

- User registration and login
- JWT-based authentication
- Secure password handling
- PostgreSQL database integration
- Environment-based configuration
- Input validation
- RESTful API design

## Technology Stack

- Java 17
- Spring Boot 3.4.5
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT (JSON Web Tokens)
- Maven
- Lombok
- Hibernate Validator

## Prerequisites

- Java 17 or higher
- Maven
- PostgreSQL database
- Environment variables configured

## Environment Setup

Create a `.env` file in the root directory with the following variables:

```env
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password
JWT_SECRET=your_jwt_secret
```

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

## Building and Running

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

### Database Configuration
The service uses PostgreSQL. Make sure to:
1. Have PostgreSQL installed and running
2. Create a database for the service
3. Configure the database connection in your environment variables

### Security Considerations

#### JWT Token
- Tokens are signed using the JWT_SECRET from environment variables
- Tokens contain user information and expiration time
- Always use HTTPS in production

#### Password Security
- Passwords are securely hashed before storage
- Input validation is enforced
- Password requirements should be configured according to your security needs

## Testing

Run the test suite:
```bash
./mvnw test
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 