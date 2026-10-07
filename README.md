# Library Management System

[![build](https://github.com/Layth-Ibraheem/LibraryManagementSystem/actions/workflows/build.yml/badge.svg)](https://github.com/Layth-Ibraheem/LibraryManagementSystem/actions/workflows/build.yml)

A REST API for a small library. Staff users manage books, patrons (library members) and
librarians, lend books to patrons and record their return. Users log in with a user name and
password and get a JWT; what each user may do is set by permission flags that an administrator
grants.

## Tech stack

- Java 21, Spring Boot 3.3 (Spring Web, Spring Data JPA with Hibernate, Spring Security, Bean Validation)
- JWT with JJWT 0.11.5, passwords hashed with BCrypt
- H2 in-memory database for local runs and tests; Microsoft SQL Server through a profile
- Maven (wrapper included), JUnit 5, Mockito, AssertJ, MockMvc, spring-security-test
- GitHub Actions runs the test suite on every push and pull request

## Quick start

You need JDK 21. Nothing else: Maven is downloaded by the wrapper and the database is in memory.

```bash
git clone https://github.com/Layth-Ibraheem/LibraryManagementSystem.git
cd LibraryManagementSystem/Library-Management-System
./mvnw test              # optional: runs the test suite
./mvnw spring-boot:run   # starts the API on http://localhost:8080
```

On Windows use `.\mvnw.cmd test` and `.\mvnw.cmd spring-boot:run`. If `java -version` does
not print 21, point `JAVA_HOME` to a JDK 21 first.

To use another port, for example 8089:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8089
```

(In PowerShell, quote the argument: `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8089"`.)

Without a profile the application uses the `h2` profile: an in-memory H2 database that is created
on start and lost on stop. This profile also creates two users so you can try the API at once:

| User    | Password                  | Permissions                         |
|---------|---------------------------|-------------------------------------|
| `admin` | `dev-only-admin-password` | all (`-1`), can grant permissions   |
| `clerk` | `dev-only-clerk-password` | ManagePatrons (`4`)                 |

These passwords and the JWT signing key of the `h2` profile are public, dev-only values in
`src/main/resources/application-h2.properties`. No other profile creates users or has a key.

## Running on SQL Server

The `sqlserver` profile connects to the database `LibraryManagementSystemDB` on `localhost`
(see `application-sqlserver.properties`). Credentials and the signing key come from environment
variables, and the application refuses to start without a valid `JWT_SECRET`.

```bash
export SPRING_PROFILES_ACTIVE=sqlserver
export SPRING_DATASOURCE_USERNAME=library_app
export SPRING_DATASOURCE_PASSWORD='<password>'
export JWT_SECRET='<at least 32 random characters, for example from: openssl rand -base64 48>'
./mvnw spring-boot:run
```

PowerShell: `$env:SPRING_PROFILES_ACTIVE='sqlserver'` and so on for each variable.
`SPRING_DATASOURCE_URL` overrides the connection URL. The token lifetime is `jwt.expiration`
(default `10h`).

Hibernate creates and updates the tables (`ddl-auto=update`); there are no migration scripts.
No users are created on SQL Server: register one, then make it an administrator once in SQL:
`UPDATE users SET roles = -1 WHERE user_name = '<name>';`

A database created by the 2024 version needs manual changes, because `ddl-auto=update` does not
alter existing columns:

- `ALTER TABLE users ALTER COLUMN password varchar(100) NOT NULL;` (BCrypt hashes are 60 characters).
  Old plain-text passwords no longer work; those users must be created again.
- `books.isbn` is now the client's ISBN-13, 13 digits, unique. The old generated values do not fit,
  so start with an empty `books` table or migrate the data.

## Authentication and permissions

1. `POST /api/auth/register` with `{"userName": "...", "password": "..."}` creates a user with
   no permissions (`roles = 0`) and returns `201` with a token.
2. `POST /api/auth/login` with the same body returns `200` with a token.
3. Send the token on every other request: `Authorization: Bearer <token>`.

Permissions are bit flags stored in the user's `roles` column. Only the server sets them; a
`roles` field sent on register is ignored.

| Flag             | Value | Allows                                     |
|------------------|-------|--------------------------------------------|
| ManageLibrarians | 1     | `/api/librarians` endpoints                |
| ManageBooks      | 2     | `/api/books` endpoints                     |
| ManagePatrons    | 4     | `/api/patrons` endpoints, borrow and return |
| (all)            | -1    | everything, including granting permissions |

Flags combine by adding them: `6` is ManageBooks and ManagePatrons. An administrator (`-1`)
grants them with `PUT /api/users/{id}/roles` and `{"roles": 6}`. The permissions are copied
into the token, so a change applies when the user logs in again.

## Endpoints

| Method | Path                                    | Permission       | Success |
|--------|-----------------------------------------|------------------|---------|
| POST   | `/api/auth/register`                    | none (public)    | 201     |
| POST   | `/api/auth/login`                       | none (public)    | 200     |
| PUT    | `/api/users/{id}/roles`                 | all (`-1`)       | 200     |
| GET    | `/api/books`                            | ManageBooks      | 200     |
| GET    | `/api/books/{id}`                       | ManageBooks      | 200     |
| POST   | `/api/books`                            | ManageBooks      | 201     |
| PUT    | `/api/books/{id}`                       | ManageBooks      | 200     |
| DELETE | `/api/books/{id}`                       | ManageBooks      | 204     |
| GET    | `/api/patrons`                          | ManagePatrons    | 200     |
| GET    | `/api/patrons/{id}`                     | ManagePatrons    | 200     |
| POST   | `/api/patrons`                          | ManagePatrons    | 201     |
| PUT    | `/api/patrons/{id}`                     | ManagePatrons    | 200     |
| DELETE | `/api/patrons/{id}`                     | ManagePatrons    | 204     |
| GET    | `/api/librarians`                       | ManageLibrarians | 200     |
| GET    | `/api/librarians/{id}`                  | ManageLibrarians | 200     |
| POST   | `/api/librarians`                       | ManageLibrarians | 201     |
| PUT    | `/api/librarians/{id}`                  | ManageLibrarians | 200     |
| DELETE | `/api/librarians/{id}`                  | ManageLibrarians | 204     |
| POST   | `/api/borrow/{bookId}/patron/{patronId}` | ManagePatrons    | 200     |
| PUT    | `/api/return/{bookId}/patron/{patronId}` | ManagePatrons    | 200     |

Request bodies:

- Book (create): `title`, `author`, `publicationYear` (1450 to the current year) and `isbn`
  (ISBN-13, digits with optional hyphens). Update takes the same fields without `isbn`; the ISBN
  cannot change, and a second book with the same ISBN is a `409`.
- Patron: `name`, `email`, `phoneNumber`.
- Librarian: `firstName`, `lastName`.
- Borrow and return have no body and answer with the loan:
  `{"id", "bookId", "patronId", "borrowingDate", "returnedDate"}`.

Business rules: a book that is on loan cannot be lent again (`409`); a return without an open
loan of that book by that patron is a `409`; books and patrons with loan history cannot be
deleted (`409`), so the history is kept. The `201` responses carry a `Location` header.

## Example session

The commands use bash and curl (Git Bash works on Windows). They assume a fresh start, so the
first book, patron and registered user get the ids 1, 1 and 3.

```bash
BASE=http://localhost:8080

# Log in as the seeded admin and keep the token
TOKEN=$(curl -s -X POST "$BASE/api/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"userName":"admin","password":"dev-only-admin-password"}' \
  | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')

# Add a book: 201 with a Location header for /api/books/1
curl -i -X POST "$BASE/api/books" \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"title":"Domain-Driven Design","author":"Eric Evans","publicationYear":2003,"isbn":"978-0-321-12521-7"}'

# Add a patron: 201 with a Location header for /api/patrons/1
curl -i -X POST "$BASE/api/patrons" \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"name":"Ada Reader","email":"ada@example.com","phoneNumber":"+963 11 555 0100"}'

# Lend book 1 to patron 1, try again (409 Conflict), then return it
curl -i -X POST "$BASE/api/borrow/1/patron/1" -H "Authorization: Bearer $TOKEN"
curl -i -X POST "$BASE/api/borrow/1/patron/1" -H "Authorization: Bearer $TOKEN"
curl -i -X PUT  "$BASE/api/return/1/patron/1" -H "Authorization: Bearer $TOKEN"

# Register a user (no permissions yet), then let the admin grant ManageBooks (2)
curl -i -X POST "$BASE/api/auth/register" \
  -H 'Content-Type: application/json' \
  -d '{"userName":"reader","password":"reader-password-1"}'
curl -i -X PUT "$BASE/api/users/3/roles" \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"roles":2}'
```

## Errors

Every error is an RFC 9457 problem detail with `Content-Type: application/problem+json`.
Validation errors list one message per field under `errors`:

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "The request has invalid fields",
  "instance": "/api/books",
  "errors": {
    "author": "size must be between 2 and 200",
    "isbn": "invalid ISBN"
  }
}
```

| Status | When |
|--------|------|
| 400 | invalid fields, malformed JSON, a path variable of the wrong type |
| 401 | no token or an invalid or expired token (with a `WWW-Authenticate: Bearer` header), or a failed login |
| 403 | the token lacks the permission the endpoint needs |
| 404 | unknown id, or unknown URL (when a valid token is sent) |
| 405 | wrong HTTP method (with an `Allow` header) |
| 409 | book already on loan, no open loan to return, delete with loan history, duplicate ISBN or user name |
| 415 | a request body whose `Content-Type` is not JSON |
| 500 | anything unexpected; the details are logged, never returned |

## Running the tests

```bash
cd Library-Management-System
./mvnw test
```

The suite needs no database server; everything runs on in-memory H2. It has:

- unit tests with Mockito and no Spring context (`BorrowingServiceTest`, `UserServiceTest`,
  validation and permission-flag tests),
- slice tests: `@WebMvcTest` for `BookController` with the real security configuration, and
  `@DataJpaTest` for the loan queries and the row lock,
- full-application tests with MockMvc for login, permissions, error responses, caching, N+1
  query counts and two concurrent borrows of the same book.

## Design notes

- **Layers.** Controllers handle HTTP, request validation and the mapping to response DTOs
  (`requestsAndResponses`). Services hold the transactions and business rules. Spring Data JPA
  repositories do the data access. Controllers return DTOs, never JPA entities.
- **JWT in the security chain.** `JwtRequestFilter` runs inside the Spring Security filter chain,
  before `UsernamePasswordAuthenticationFilter`. It verifies the token's signature and expiry and
  puts a `CurrentUser` (id, user name, permissions) into the security context. A bad token does
  not throw from the filter: the request continues unauthenticated and the entry point answers
  `401`. The API is stateless (no session, no cookie), so CSRF protection is off.
- **Permission checks.** Endpoints are annotated with `@RequireRole(role = UserRoles.ManageBooks)`.
  `RoleCheckAspect`, a Spring AOP `@Around` advice, compares the flag with the bits of the
  current user and throws `AccessDeniedException`, which becomes a `403` problem detail. The
  rule for every endpoint is visible next to its mapping.
- **Locking on borrow.** Borrow, return and book delete load the book with a pessimistic write
  lock (`SELECT ... FOR UPDATE`), so two requests for the same book run one after the other and
  the second one sees the first one's loan. Open loans (no `returnedDate`) are the source of truth.
- **Caching.** `GET /api/books/{id}` is cached as an immutable DTO. Update replaces the entry;
  delete, borrow and return evict it. The cache advice is ordered outside the transaction, so
  evictions happen after the commit.
- **Errors.** `GlobalExceptionHandler` extends `ResponseEntityExceptionHandler`, so Spring MVC's
  own errors (400, 404, 405, 415) and the application's exceptions share one problem-detail format.
- **Configuration.** Profiles separate H2 and SQL Server. The JWT settings are a validated
  `@ConfigurationProperties` record, so a missing or short key stops the application at startup.

## Known limitations

- No refresh tokens and no logout: a token is valid until it expires (10 hours by default).
  Because permissions are read from the token, a granted or revoked permission applies only
  after the next login.
- List endpoints return everything; there is no pagination or filtering.
- With the `h2` profile all data is lost when the application stops.
- The schema comes from Hibernate (`ddl-auto`), not from migration scripts such as Flyway, and
  the tests run on H2 only, not on SQL Server.
- The book cache is in memory, per application instance and unbounded. Several instances behind
  a load balancer could serve stale books.
- Login takes longer for an existing user name than for an unknown one (BCrypt runs only for
  existing users), so user names can be guessed by timing. There is no rate limiting.
- Without a token, an unknown URL returns `401` instead of `404`. Errors raised before Spring MVC
  (for example a URL the security firewall rejects) use Spring Boot's default error JSON, not a
  problem detail.
- Librarians are plain records; they are not linked to user accounts.
- The Java packages (`com.layth.Library.Management.System`) keep the name generated by Spring
  Initializr, which does not follow the lower-case package convention.

## History

First written in October 2024. Revisited in October 2026 to fix security issues (password
hashing, server-assigned permissions, externalized secrets), correct the error responses and
add tests.
