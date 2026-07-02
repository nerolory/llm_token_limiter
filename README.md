# Credits Service

Spring Boot 3.x microservice for token balance management, credit consumption, and text token estimation.

**Russian documentation:** [docs/README.ru.md](docs/README.ru.md)

## Stack

- Java 21 (LTS): records (DTOs), pattern matching switch, virtual threads
- Spring Boot 3.x (Web, Data JPA, Validation)
- H2 (in-memory)
- JTokkit (bonus token estimation endpoint)
- Maven

## Getting Started

```bash
mvn clean package
mvn spring-boot:run
```

The service starts at `http://localhost:8080`.

H2 console: `http://localhost:8080/h2-console`  
JDBC URL: `jdbc:h2:mem:creditsdb`

## Pre-seeded Accounts

| userId           | Balance | Status |
|------------------|---------|--------|
| user-uuid-1111   | 50      | ACTIVE |
| user-uuid-8888   | 200     | ACTIVE |
| user-uuid-9999   | 0       | ACTIVE |

## API

### Consume Credits

```bash
curl -X POST http://localhost:8080/api/v1/credits/consume \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"user-uuid-8888\", \"tokensRequested\": 45}"
```

`200 OK` response:

```json
{
  "userId": "user-uuid-8888",
  "remainingTokens": 155,
  "status": "SUCCESS"
}
```

### Get Balance

```bash
curl http://localhost:8080/api/v1/credits/balance/user-uuid-8888
```

`200 OK` response:

```json
{
  "userId": "user-uuid-8888",
  "currentBalance": 200,
  "accountStatus": "ACTIVE"
}
```

### Estimate Tokens (bonus)

```bash
curl -X POST http://localhost:8080/api/v1/credits/estimate \
  -H "Content-Type: application/json" \
  -d "{\"inputText\": \"Hello, can you please analyze this system architecture and provide feedback?\", \"targetModel\": \"gpt-4o\"}"
```

`200 OK` response:

```json
{
  "estimatedTokens": 14,
  "modelUsed": "gpt-4o"
}
```

## Error Codes

| Code | Condition |
|------|-----------|
| 402  | Insufficient tokens |
| 404  | User not found |
| 422  | Validation error or unknown model |

Errors are returned in RFC 7807 format (`ProblemDetail`).

## Tests

```bash
mvn test
```

- Unit tests for the service layer (JUnit 5 + Mockito)
- Controller integration tests (MockMvc + H2)
- Concurrent token consumption test

## Documentation

- Russian guide: [docs/README.ru.md](docs/README.ru.md)
- Technical specification: [docs/task.md](docs/task.md)
