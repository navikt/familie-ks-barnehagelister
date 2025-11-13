# Copilot Instructions for familie-ks-barnehagelister

## Project Overview

This is a Kotlin/Spring Boot 3 API for receiving kindergarten lists (barnehagelister) for the Norwegian welfare service (NAV) Kontantstøtte program. The service is secured using Maskinporten for external authentication.

## Technology Stack

- **Language**: Kotlin 2.2.21
- **Framework**: Spring Boot 3.5.7
- **Build Tool**: Maven
- **Java Version**: 21
- **Database**: PostgreSQL 15
- **Code Style**: ktlint

## Build and Test

- Build: `mvn clean package`
- Run tests: `mvn test`
- Linting: `mvn verify` (runs ktlint automatically)
- Format code: `mvn validate` (runs ktlint -F to auto-format)

## Code Style and Conventions

- This project uses ktlint for Kotlin code formatting
- Always run `mvn validate` before committing to auto-format code
- Follow existing code patterns in the repository
- Keep code changes minimal and focused
- Write tests for new functionality following existing test patterns

## Database

- Uses PostgreSQL 15
- Database migrations are handled by Flyway
- For local development, either use DevLauncherPostgres with `--dbcontainer` VM option or set up a Docker container
- Database name: `familie-ks-barnehagelister`
- Default port: 5432

## Testing

- Integration tests use Testcontainers for PostgreSQL
- Use DevLauncher for local testing (runs on port 8096)
- Swagger UI available at: http://localhost:8096/swagger-ui/index.html (local)
- Test endpoints exist in `UnprotectedBarnehagelisteController` for local testing

## Security

- External API secured with Maskinporten tokens
- Do not commit secrets or credentials
- Authentication configuration in application.yaml files
- Organization numbers must be registered in app-dev.yaml for preprod testing

## Important Paths

- Main source: `src/main/kotlin/no/nav/familie/ks/barnehagelister/`
- Test source: `src/test/kotlin/no/nav/familie/ks/barnehagelister/`
- Database migrations: `src/main/resources/db/migration/`
- Configuration: `src/main/resources/application*.yaml`

## Development Workflow

1. Make minimal, focused changes
2. Write tests following existing patterns
3. Run `mvn validate` to format code
4. Run `mvn test` to verify tests pass
5. Run `mvn verify` to ensure ktlint compliance
6. Test locally using DevLauncher if needed

## Notes

- This repository uses Norwegian language in documentation and some naming
- The project follows NAV's internal conventions for familie-* services
- Pay attention to existing patterns for error handling, validation, and data processing
