## [1.0.0] - 2024-06-09

### Added
- Initial release of the Authentication and Authorization microservice.
- User registration and login endpoints.
- JWT-based authentication (RS256, RSA key pair).
- Secure password handling and input validation (Jakarta Validation).
- PostgreSQL integration (supports AWS, Docker and local setups).
- Profile-based configuration for local and production environments.
- Environment variable and secrets management (dotenv-java, AWS Secrets Manager).
- RESTful API design with OpenAPI (Swagger UI) documentation.
- Health check and monitoring endpoints (Spring Boot Actuator).
- Some unit and integration tests (JUnit, Mockito, Testcontainers).
- CI/CD pipeline with GitHub Actions for build, test, Docker, and deployment. 