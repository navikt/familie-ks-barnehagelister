# GitHub Copilot Instructions

Dette repoet er et eksternt API for å motta barnehagelister for Kontantstøtte (familie-ks-barnehagelister).

## Teknisk stack

- Kotlin med Spring Boot og Jetty
- Spring Data JDBC (ikke JPA) med PostgreSQL
- Flyway for databasemigrasjoner
- Maskinporten for autentisering av eksterne konsumenter
- Kafka for publisering av barnehagebarn til KS-systemet
- Asynkron oppgavebehandling via `no.nav.familie.prosessering`

## Arkitektur

Applikasjonen følger en lagdelt arkitektur:

- **`rest/`** – REST-kontrollere og DTO-er. Kontrollere er definert som interfaces med `@ProtectedWithClaims` og implementert i separate klasser.
- **`service/`** – Forretningslogikk og orkestrering av repository-kall og oppgaver.
- **`task/`** – Asynkrone oppgavesteg annotert med `@TaskStepBeskrivelse`.
- **`repository/`** – Spring Data JDBC-repositories. Bruker `InsertUpdateRepository`-mønsteret for eksplisitt `insert()`/`update()`.
- **`domene/`** – Domeneentiteter lagret i databasen.
- **`validering/`** – Egendefinerte Jakarta-valideringsannotasjoner og -validatorer.
- **`kafka/`** – Kafka-produsent for publisering av barnehagebarn.
- **`config/`** – Spring-konfigurasjon, sikkerhet og databaseoppsett.

## Navnekonvensjoner

- Domeneord og norskspråklige begreper brukes på norsk (f.eks. `Barnehageliste`, `Barnehagebarn`).
- DTO-er og REST-endepunkter bruker engelske navn (f.eks. `FormV1RequestDto`, `KindergartenRequestDto`).
- Suffiks: `*Dto` for dataoverføringsobjekter, `*Task` for asynkrone steg, `*Service` for tjenestelaget, `*Repository` for datakall, `*Validator` for valideringsklasser.

## Kodekonvensjoner

- Bruk extension functions for mapping mellom lag (f.eks. `fun Barnehageliste.tilKindergartenlistResponse(...)`).
- Feilhåndtering sentraliseres i `ApiExceptionHandler` (`@RestControllerAdvice`) og returnerer ProblemDetail (RFC 7807).
- Bruk `secureLogger` for logging av sensitive data (f.eks. fødselsnumre).
- Profilebasert konfigurasjon: `dev` for lokal utvikling, `preprod`/`prod` for miljøer.
- Valideringsannotasjoner på DTO-felter bruker `@field:`-prefiks (f.eks. `@field:NotNull`).

## Tester

- Integrasjonstester bruker `@SpringBootTest`, `@AutoConfigureMockMvc` og `DbContainerInitializer` (TestContainers).
- Enhetstester bruker MockK for mocking.
- Testdata lages med builder-mønsteret i `FormV1DtoTestdata`.
- Autentisering i tester mockes med `@EnableMockOAuth2Server`.
