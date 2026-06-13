# Energy Management System (EMS) - Traditional Setup & Execution Guide

This document describes how to set up, run, and develop the EMS application locally using traditional methods, bypassing Docker.

---

## 🛠️ Prerequisites & Infrastructure Services

Before starting the application services, ensure the following infrastructure dependencies are installed and running locally on your machine.

### 1. Database (TimescaleDB / PostgreSQL 16)
TimescaleDB is an extension of PostgreSQL. You can run a standard PostgreSQL 16 server locally, or install the TimescaleDB extension.
- **Port:** `5432`
- **Username:** `postgres`
- **Password:** `postgres`
- **Database Name:** `emsdb`

> [!NOTE]
> Database migrations are handled automatically by **Flyway** when the Spring Boot backend starts. You only need to create the empty database `emsdb` beforehand.

### 2. Redis 7
Redis is used for caching, session management, and as the message broker for Celery.
- **Port:** `6379`
- **Host:** `localhost`

### 3. MinIO (S3-Compatible Object Storage)
MinIO is used for storing uploaded report files and datasets.
- **API Port:** `9000`
- **Console Port:** `9001`
- **Access Key (Username):** `minioadmin`
- **Secret Key (Password):** `minioadmin`
- **Default Bucket:** `ems-bucket` (Make sure to create this bucket via the MinIO console at http://localhost:9001 or using the client tool `mc`).

---

## 🚀 Running the Application Services

The EMS suite consists of four primary services. Run each service in its own terminal window or session.

### 1. Spring Boot Backend (`ems-backend`)
The backend is a Java 21 / Maven application.

1. Navigate to the backend directory:
   ```bash
   cd ems-backend
   ```
2. Verify that your environment variables match the configuration in `.env`.
3. Build and run the service using Maven:
   ```bash
   mvn spring-boot:run
   ```
   *The backend will boot up on http://localhost:8080.*

---

### 2. Machine Learning API Service (`ems-ml`)
The ML service is a Python 3.12+ FastAPI application.

1. Navigate to the ML service directory:
   ```bash
   cd ems-ml
   ```
2. Activate the virtual environment (if you have one set up):
   ```bash
   source venv/bin/activate
   ```
   *(On Windows: `venv\Scripts\activate`)*
3. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```
4. Run the FastAPI server using Uvicorn:
   ```bash
   uvicorn main:app --host 127.0.0.1 --port 8001
   ```
   *The ML API will start on http://localhost:8001.*

---

### 3. Celery Worker (`ems-ml` background worker)
The Celery worker processes background ML tasks (e.g. anomaly detection, forecasting).

1. Open a new terminal and navigate to the ML service directory:
   ```bash
   cd ems-ml
   ```
2. Activate the virtual environment:
   ```bash
   source venv/bin/activate
   ```
3. Run the Celery worker:
   ```bash
   celery -A celery_app worker --loglevel=info
   ```

---

### 4. React Frontend (`ems-frontend`)
The frontend is a Vite / TypeScript / React application.

1. Navigate to the frontend directory:
   ```bash
   cd ems-frontend
   ```
2. Install the node packages:
   ```bash
   npm install
   ```
3. Start the Vite development server:
   ```bash
   npm run dev
   ```
   *The frontend will run on http://localhost:5173.*

---

## 🧹 Cleaning Up Docker Configuration

If you want to permanently delete the unused Docker configuration files from the project workspace, you can execute the following terminal command in the root folder:

```bash
rm docker-compose.yml ems-backend/Dockerfile ems-frontend/Dockerfile ems-ml/Dockerfile ems-ml/celery_worker.Dockerfile
```
