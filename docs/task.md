# Technical Assessment: Credit & Token Consumption Microservice

## Project Overview
The objective of this assessment is to implement a production-grade, highly reliable RESTful API microservice using **Spring Boot 3.x** that manages user credit balances and handles token consumption for metered AI product capabilities (Billing/Metered Usage domain).

This project evaluates your understanding of Enterprise Java standards: strict layer separation, database transaction management, concurrency safety, and robust global exception handling.

---

## Technical Stack Requirements
- **Language:** Java 17 or higher
- **Framework:** Spring Boot 3.x (Spring Web, Spring Data JPA)
- **Database:** In-Memory H2 Database or PostgreSQL
- **Build Tool:** Maven or Gradle

---

## API Specifications & Endpoints

### 1. Consume Credits
- **Endpoint:** `POST /api/v1/credits/consume`
- **Content-Type:** `application/json`
- **Request Body:**
  ```json
  {
    "userId": "user-uuid-8888",
    "tokensRequested": 45
  }
  ```
- **Behavior:**
  1. Retrieve the user's current token balance from the database.
  2. Validate if the user has sufficient funds/tokens.
  3. **Concurrency Protection:** Ensure the check-and-write operation is safe from race conditions under high concurrent volume.
  4. Deduct the tokens, commit the transaction, and log the balance change history.
- **Success Response:** `200 OK`
  ```json
  {
    "userId": "user-uuid-8888",
    "remainingTokens": 155,
    "status": "SUCCESS"
  }
  ```
- **Error Responses:** 
  - `402 Payment Required` if tokens are insufficient.
  - `422 Unprocessable Entity` for invalid payload inputs.

### 2. Get User Balance
- **Endpoint:** `GET /api/v1/credits/balance/{userId}`
- **Success Response:** `200 OK`
  ```json
  {
    "userId": "user-uuid-8888",
    "currentBalance": 200,
    "accountStatus": "ACTIVE"
  }
  ```

---

## Evaluation & Assessment Criteria (Enterprise Standards)

### 1. Concurrency & Transaction Management
- Demonstrate proper mitigation against **Race Conditions** during multi-threaded credit updates using database locks (`@Lock` types) or transactional isolation levels.
- Idiomatic use of Spring's `@Transactional` boundary controls.

### 2. Clean Layer Architecture
- Strict boundaries between `Controllers` (handling DTO validations), `Services` (domain transactional logic), and `Repositories`.
- **Zero Database Entity Leakage:** Database Entities must never be returned directly by controllers; use explicit DTO mappers.

### 3. Unified Error Responses (RFC 7807)
- Implement a global exception interceptor using `@ControllerAdvice` / `@RestControllerAdvice`.
- Map business anomalies (`InsufficientCreditsException`, `UserNotFoundException`) into structured HTTP error models, completely preventing unhandled `500 Internal Server Errors`.

### 4. Automated Testing Suite
- Unit test coverage using **JUnit 5** and **Mockito** for isolating service layers.
- Integration tests targeting the controller edge using `MockMvc`.

---

## 🌟 Advanced Bonus Requirement (Optional Extra Points)

### Dynamic Token Calculation & Estimation

In a real-world AI production environment, client applications rarely know the exact token cost before sending a prompt. To simulate a true production billing engine, you can implement an automated in-memory token calculator.

#### Technical Requirements:
- Integrate an industry-standard Java token counting library, such as **JTokkit** (the Java counterpart to OpenAI's `tiktoken`).
- Encapsulate this logic inside an isolated, reusable service layer (e.g., `TokenEstimationService`) implementing a clean interface.

#### New Optional Endpoint:
- **Endpoint:** `POST /api/v1/credits/estimate`
- **Content-Type:** `application/json`
- **Request Body:**
  ```json
  {
    "inputText": "Hello, can you please analyze this system architecture and provide feedback?",
    "targetModel": "gpt-4o"
  }
  ```
- **Behavior:** 
  1. The service must intercept the raw text payload.
  2. Dynamically load the appropriate BPE (Byte Pair Encoding) tokenizer based on the `targetModel` parameter.
  3. Calculate the exact structural token count locally on the JVM without making any external API HTTP calls.
- **Success Response:** `200 OK`
  ```json
  {
    "estimatedTokens": 14,
    "modelUsed": "gpt-4o"
  }
  ```

#### Architecture Note for the Bonus:
The `TokenEstimationService` must remain strictly independent of the database and transaction domains. It should be designed as a pure utility/infrastructure component that can be easily plugged into your main controllers or orchestration services via Spring's Dependency Injection.
