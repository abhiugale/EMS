# ⚡ Energy Management System (EMS)

Welcome to the **Energy Management System (EMS)**. This is a full-stack, enterprise-grade application designed to monitor industrial machinery energy consumption, run Isolation Forest anomaly detection, forecast weekly/daily load demands using Meta Prophet, and generate automated PDF reports.

## 🏗️ Project Architecture & Services

The system is composed of several independent services communicating over REST, WebSockets (STOMP), and Celery queues:

| Service | Technology Stack | Port | Description |
| :--- | :--- | :--- | :--- |
| **`ems-frontend`** | React 18, TypeScript, Vite | `5173` | Rich interactive dashboard with ECharts load curves. |
| **`ems-backend`** | Spring Boot 3.3, Java 21, Maven | `8080` | Core API, Flyway migrations, report generator (OpenPDF). |
| **`ems-ml`** | Python 3.12, FastAPI, SQLAlchemy | `8001` | ML API service triggering parallel background jobs. |
| **`ems-ml-worker`** | Celery, Redis | *Internal* | Asynchronous worker processing Isolation Forest & Prophet models. |
| **`timescaledb`** | PostgreSQL 16 + TimescaleDB extension | `5432` | Relational & hypertable time-series database. |
| **`redis`** | Redis 7 | `6379` | Key-value store for session cache, token blacklisting, & Celery broker. |
| **`minio`** | MinIO Object Store (S3-compatible) | `9000` (API), `9001` (Console) | Secure PDF report storage bucket. |

---

## 🚀 How to Run the Project (Traditional Local Setup)

Please follow these step-by-step instructions to set up and run the project natively on your host machine.

### 📋 Prerequisites

Before starting, copy the example environment file to create your active local `.env`:
```bash
cp .env.example .env
```

---

### Step 1: Start Infrastructure Dependencies
Ensure the following services are installed and running locally:
1. **TimescaleDB / PostgreSQL 16:**
   - Create an empty database named `emsdb` under user `postgres` (password: `postgres`).
   - *Note: Database schemas & migrations are managed by Flyway and run automatically when the Spring Boot backend starts.*
2. **Redis 7:**
   - Ensure Redis is running locally on port `6379`.
3. **MinIO:**
   - Start a local MinIO server.
   - Go to http://localhost:9001, sign in (`minioadmin`/`minioadmin`), and create a bucket named `ems-bucket`.

---

### Step 2: Run the Spring Boot Backend (`ems-backend`)
1. Navigate to the backend directory:
   ```bash
   cd ems-backend
   ```
2. Verify Java 21 is active on your host (`java -version`).
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```
   *The backend starts on http://localhost:8080.*

---

### Step 3: Run the Machine Learning Microservice (`ems-ml`)
1. Open a new terminal and navigate to the ML directory:
   ```bash
   cd ems-ml
   ```
2. Create and activate a Python virtual environment:
   ```bash
   python3 -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   ```
3. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```
4. Start the FastAPI server:
   ```bash
   uvicorn main:app --host 127.0.0.1 --port 8001
   ```

---

### Step 4: Run the Celery Worker (`ems-ml` queue)
1. Open a new terminal and navigate to the ML directory:
   ```bash
   cd ems-ml
   ```
2. Activate your virtual environment:
   ```bash
   source venv/bin/activate
   ```
3. Run the background worker:
   ```bash
   celery -A celery_app worker --loglevel=info
   ```

---

### Step 5: Run the React Frontend (`ems-frontend`)
1. Open a new terminal and navigate to the frontend directory:
   ```bash
   cd ems-frontend
   ```
2. Install Node packages (Node 18+ required):
   ```bash
   npm install
   ```
3. Run the development server:
   ```bash
   npm run dev
   ```
   *The client web app starts on http://localhost:5173.*

---

## 🛠️ Troubleshooting & Configuration Gotchas

> [!IMPORTANT]
> **Java Version Error:** 
> If the backend fails to compile with a JVM target version mismatch, verify that your IDE or local terminal environment is set to JDK 21. If using IntelliJ or VS Code, check that the project runtime SDK points to OpenJDK 21.

> [!WARNING]
> **Python Dependency Compilation Errors:**
> If you are running a host with a bleed-edge Python version (like Python 3.13 or 3.14), library dependencies such as `pandas` or `prophet` might fail to compile locally due to version conflicts. Make sure to use Python 3.12 for local installation.
