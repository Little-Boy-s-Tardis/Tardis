# Tardis: Setup, Operation & Verification Manual (Hướng dẫn sử dụng & Test)

This is the main operations manual for **Tardis**. It details prerequisites, step-by-step setup guides for both developer and production modes, and how to verify and test the ingestion pipeline.

For system architecture, data flows, and team profile information, please refer to the [.github Architecture README](file:///d:/Tardis/.github/README.md).

--- ## Prerequisites

Ensure you have the following installed on your machine:
* **Java 21** (JDK 21)
* **Node.js** (v18 or higher) & **npm**
* **Docker Desktop** (only required for Production/Docker Mode)

--- ## How to Run the Application

The system supports two execution profiles: **Standalone Developer Mode** (zero external database/broker dependency) and **Production Mode** (utilizes full PostgreSQL and RabbitMQ databases).

### Mode A: Standalone Developer Mode (Recommended for quick evaluation)
In this mode, you do not need PostgreSQL, RabbitMQ, or Docker. The backend runs with an in-memory H2 database, a simulated in-memory message queue, and a simulated LLM processor fallback.

#### 1. Start the Backend Service
1. Open a terminal and navigate to the backend directory:
   ```bash
   cd backend
   ```
2. Run the application with the `dev` profile:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```
3. The backend server will boot on `http://localhost:8080`.
4. **H2 Console**: Accessible at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:antigravity`, User: `sa`, Password: `password`).

#### 2. Start the Frontend Dashboard
1. Open a new terminal and navigate to the frontend directory:
   ```bash
   cd frontend
   ```
2. Install the necessary dependencies:
   ```bash
   npm install
   ```
3. Run the Vite development server:
   ```bash
   npm run dev
   ```
4. Access the UI dashboard at `http://localhost:5173`.

--- ### Mode B: Production / Docker Mode (Uses actual DB & Message Queue)
Use this mode to test full data persistence and asynchronous message broker queueing.

#### 1. Start Docker Containers
From the root workspace directory, run:
```bash
docker compose up -d
```
*This starts a **PostgreSQL** database on port `5432` and **RabbitMQ** (with Management UI) on ports `5672` (broker) and `15672` (management UI).* #### 2. Configure Qwen LLM API Key (Optional)
If you want to use real AI summaries:
* Add your Alibaba DashScope API key in `backend/src/main/resources/application.yml` under `app.qwen.api-key`, or set it as an environment variable:
    ```cmd
    set APP_QWEN_API_KEY=your_real_api_key_here
    ```

#### 3. Start the Services
1. **Run Backend**:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```
2. **Run Frontend**:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

--- ## Testing & Verification Guide

### 1. Webhook Simulator (React UI)
On the right-hand panel of the dashboard (`http://localhost:5173`), you can use the **Webhook Simulator** widget:
1. Choose the platform (**Discord Webhook** or **WhatsApp Cloud API**).
2. Set the importance level (**HIGH**, **MEDIUM**, or **LOW**).
3. Type in a sender name and a long, detailed announcement message.
4. Click **Send Webhook & Summarize**.
5. Watch the active pipeline indicator transition live:
   `Webhook Ingestion`  `RabbitMQ Queue`  `AI Processing`  `Realtime Broadcast`
6. The compiled summary card will immediately pop up in the Live Feed on the left.

### 2. Manual Webhook Trigger via Terminal (cURL / PowerShell)
We have prepared 5 test scenarios matching real hackathon events in [test_scenarios.md](file:///d:/Tardis/test_scenarios.md).

Open a terminal and execute any command from that file. Here is an example:

#### **PowerShell Scenario 1 (Technical Announcement)**:
```powershell
$body = @{
  object = "whatsapp_business_account"
  entry = @(@{
    id = "test-msg-id-1"
    changes = @(@{
      value = @{
        messaging_product = "whatsapp"
        contacts = @(@{ profile = @{ name = "Judge David (Tech Lead)" }; wa_id = "84911112222" })
        messages = @(@{
          from = "84911112222"
          id = "wamid.testmsg1"
          timestamp = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds().ToString()
          text = @{ body = "Attention teams: The automated grading system is starting its scan. Ensure all Spring Boot controllers use the /api/v1 prefix and have CORS properly configured." }
          type = "text"
        })
      }
      field = "messages"
    })
  })
} | ConvertTo-Json -Depth 10
$bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($body)
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/webhooks/whatsapp" -Method Post -ContentType "application/json" -Body $bodyBytes
```

*(Note: In the command above, we have set the endpoint to `http://localhost:8080` for local verification. If testing the deployed cloud version, use `https://tardis-production.up.railway.app` as documented in `test_scenarios.md`)*.

#### **CMD/cURL Scenario 1 (Technical Announcement)**:
```bash
curl -X POST http://localhost:8080/api/v1/webhooks/whatsapp -H "Content-Type: application/json" -d "{\"object\":\"whatsapp_business_account\",\"entry\":[{\"id\":\"test-msg-id-1\",\"changes\":[{\"value\":{\"messaging_product\":\"whatsapp\",\"contacts\":[{\"profile\":{\"name\":\"Judge David (Tech Lead)\"},\"wa_id\":\"84911112222\"}],\"messages\":[{\"from\":\"84911112222\",\"id\":\"wamid.testmsg1\",\"timestamp\":\"1697041663\",\"text\":{\"body\":\"Attention teams: The automated grading system is starting its scan. Ensure all Spring Boot controllers use the /api/v1 prefix and have CORS properly configured.\"},\"type\":\"text\"}]},\"field\":\"messages\"}]}]}"
```

### 3. Running Automated Tests
To run the offline unit and integration test suites on the Spring Boot backend:
```bash
cd backend
./mvnw clean test
```