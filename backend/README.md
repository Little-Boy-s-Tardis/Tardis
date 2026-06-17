# Tardis Backend - Chat Processor

This is the foundational Spring Boot chat processing backend for **Tardis**, configured to handle high-volume Discord and WhatsApp webhook message ingestion, RabbitMQ queuing, thread-safe debounce/timeout conversation batching, and stubbed AI summarization.

--- ## Prerequisites

* **Java**: Version **21** (JDK 21) installed.
* **Build Tool**: Maven is used, but you do **not** need a global Maven installation; the project includes a local Maven wrapper (`mvnw`).

--- ## How to Run the Application

The backend supports two run profiles: **Standalone (Development)** and **Production/Docker**.

### Mode A: Standalone Developer Mode (Recommended for Frontend Integration)
In this mode, you do **not** need to install or run PostgreSQL or RabbitMQ. The application runs entirely in-memory using an H2 database and a simulated RabbitMQ queue.

1. Navigate to the backend folder:
   ```bash
   cd backend
   ```
2. Start the application with the `dev` profile:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```
3. The server will boot on `http://localhost:8080`.
4. **H2 Database Console**: Accessible at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:antigravity`, User: `sa`, Password: `password`).

--- ### Mode B: Production/Docker Mode (Uses real PostgreSQL & RabbitMQ)
Use this mode to test with real database persistence and message queuing.

1. Ensure the Docker daemon is running, and spin up the dependency containers from the project root:
   ```bash
   docker compose up -d
   ```
   *This starts **PostgreSQL** on `5432` and **RabbitMQ** (with Management UI) on `5672` / `15672`.* 2. Start the application:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

--- ## How to Test and Verify

### 1. Running Automated Tests
Run the test suite using the Maven wrapper:
```bash
./mvnw clean test
```
*Note: The context load test automatically excludes JPA/AMQP configurations, meaning the test suite compiles and runs successfully offline without any running containers.* ### 2. Manual Webhook Verification
While the server is running on `localhost:8080`, you can simulate Discord and WhatsApp webhooks using `curl`:

#### **Discord Webhook**
```bash
curl -i -X POST http://localhost:8080/api/v1/webhooks/discord \
  -H "X-Webhook-Token: antigravity-secret-verify-token" \
  -H "Content-Type: application/json" \
  -d '{"sender": "DiscordUser", "content": "Hello world from Discord!", "conversationId": "channel-123"}'
```

#### **WhatsApp Webhook**
```bash
curl -i -X POST http://localhost:8080/api/v1/webhooks/whatsapp \
  -H "X-Webhook-Token: antigravity-secret-verify-token" \
  -H "Content-Type: application/json" \
  -d '{"sender": "+1234567890", "content": "Hello world from WhatsApp!", "conversationId": "chat-456"}'
```

*(If you are running in **Standalone Mode**, you will see the logs showing message ingestion, simulated RabbitMQ routing, conversation batching, and the AI Summarizer trigger 5 seconds later in the server console).* --- ## Project Structure

* **`config/`** * `RabbitMQConfig.java`: Queue, exchange, bindings, and DLQ/DLX configuration.
  * `DevMessagingConfig.java`: Custom in-memory simulated message queue provider for the `dev` profile.
* **`controller/`** * `WebhookController.java`: HTTP POST webhooks and GET challenge verification endpoints.
* **`dto/`** * `ChatMessageDto.java`: Structured webhook message payload.
* **`exception/`** * `GlobalExceptionHandler.java`: Central RestControllerAdvice mapping unhandled exceptions to clean JSON details.
* **`service/`** * `MessageConsumer.java`: Worker background receiver consuming messages from RabbitMQ.
  * `ChatBatcherService.java`: Buffers messages in-memory using concurrent mapping, debouncing silence timers, and max timeouts per conversation.
  * `AISummarizerService.java`: Worker entrypoint processing complete batches.