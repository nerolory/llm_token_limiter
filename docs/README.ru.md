# Credits Service

Микросервис учёта токенов на Spring Boot 3.x для списания кредитов и оценки количества токенов в тексте.

## Стек

- Java 21 (LTS): records (DTO), pattern matching switch, virtual threads
- Spring Boot 3.x (Web, Data JPA, Validation)
- H2 (in-memory)
- JTokkit (бонусный эндпоинт оценки токенов)
- Maven

## Запуск

```bash
mvn clean package
mvn spring-boot:run
```

Сервис поднимается на `http://localhost:8080`.

H2-консоль: `http://localhost:8080/h2-console`  
JDBC URL: `jdbc:h2:mem:creditsdb;LOCK_TIMEOUT=10000` (в H2 2.x MVStore уже обеспечивает multi-versioning)

## Предзаполненные аккаунты

| userId           | Баланс | Статус |
|------------------|--------|--------|
| user-uuid-1111   | 50     | ACTIVE |
| user-uuid-8888   | 200    | ACTIVE |
| user-uuid-9999   | 0      | ACTIVE |

## API

### Списание токенов

```bash
curl -X POST http://localhost:8080/api/v1/credits/consume \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"user-uuid-8888\", \"tokensRequested\": 45}"
```

Ответ `200 OK`:

```json
{
  "userId": "user-uuid-8888",
  "remainingTokens": 155,
  "status": "SUCCESS"
}
```

### Получение баланса

```bash
curl http://localhost:8080/api/v1/credits/balance/user-uuid-8888
```

Ответ `200 OK`:

```json
{
  "userId": "user-uuid-8888",
  "currentBalance": 200,
  "accountStatus": "ACTIVE"
}
```

### Оценка токенов (бонус)

```bash
curl -X POST http://localhost:8080/api/v1/credits/estimate \
  -H "Content-Type: application/json" \
  -d "{\"inputText\": \"Hello, can you please analyze this system architecture and provide feedback?\", \"targetModel\": \"gpt-4o\"}"
```

Ответ `200 OK`:

```json
{
  "estimatedTokens": 14,
  "modelUsed": "gpt-4o"
}
```

## Коды ошибок

| Код | Ситуация |
|-----|----------|
| 402 | Недостаточно токенов |
| 404 | Пользователь не найден |
| 422 | Ошибка валидации или неизвестная модель |

Ошибки возвращаются в формате RFC 7807 (`ProblemDetail`).

## Тесты

```bash
mvn test
```

- Модульные тесты сервисного слоя (JUnit 5 + Mockito)
- Интеграционные тесты контроллера (MockMvc + H2)
- Тест конкурентного списания токенов
