# TODO After Code Freeze

This file tracks features and improvements to be addressed after the current code freeze (testing/stabilization phase).

## Testing
- Add more unit and integration tests for edge cases and error handling
- Add tests for protected endpoints (JWT required)
- Add performance/load tests (later)
- Add security tests (e.g., invalid/expired JWT, role-based access)

## Features
- Implement role-based authorization:
  - Create admin-only endpoints for user management
  - Add role hierarchy (e.g., ADMIN > USER)
  - Add role assignment during signup and user management (currently defaults to USER)
  - Add integration tests for role-based access control (currently only tests for JWT)
  - Document role-based security model
- Add an admin panel (web UI or API endpoints)
- Add OAuth2 login (Google, GitHub, etc.)
- Add email verification for new users
- Add password reset functionality
- Add user profile management endpoints
- Add account deletion/deactivation
- Add refresh token support for JWT
- Add rate limiting for login attempts
- Add API versioning
- Add request/response validation improvements
- Add request logging and audit trails
- Add OpenAPI/Swagger documentation enhancements
- Add deployment scripts for cloud (AWS EC2, etc.)
- Add monitoring/alerting (Prometheus, Grafana, etc.)

## Other Ideas
- Add multi-factor authentication (MFA)
- Add user activity logs/history
- Add support for user avatars/profile pictures
- Add localization/internationalization support