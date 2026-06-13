# 🚀 Execution Guide: How to Run the Energy Management System (EMS)

This guide provides step-by-step instructions to get all the services in the Energy Management System (EMS) up and running natively on your host machine.

---

## 🛠️ Table of Contents
1. [Prerequisites](#-prerequisites)
2. [Step 1: Start Databases & Infrastructure Services](#step-1-start-databases--infrastructure-services)
3. [Step 2: Start the Java Backend (ems-backend)](#step-2-start-the-java-backend-ems-backend)
4. [Step 3: Start the Python ML Service (ems-ml)](#step-3-start-the-python-ml-service-ems-ml)
5. [Step 4: Start the Celery Worker](#step-4-start-the-celery-worker)
6. [Step 5: Start the React Frontend (ems-frontend)](#step-5-start-the-react-frontend-ems-frontend)
7. [Port Map & Default Access URLs](#-port-map--default-access-urls)
8. [Troubleshooting & Environment Tips](#-troubleshooting--environment-tips)

---

## 📋 Prerequisites

Before starting, navigate to the project root directory and copy the environment configuration template:

```bash
cp .env.example .env
```

---

## 💻 Traditional Local Setup (Step-by-Step)

Follow these steps to run all services natively on your host machine.

### Step 1: Start Databases & Infrastructure Services
Ensure the following services are running locally on your system:
* **TimescaleDB / PostgreSQL 16:** Connect using `localhost:5432` with username `postgres` / password `postgres`, and create an empty database named `emsdb`.
* **Redis 7:** Start a Redis server listening on `127.0.0.1:6379`.
* **MinIO Console:** Start MinIO, login to the console (usually http://localhost:9001) using credentials `minioadmin`/`minioadmin`, and create an S3 bucket named `ems-bucket`.

### Step 2: Start the Java Backend (`ems-backend`)
1. Open a terminal and navigate to the backend folder:
   ```bash
   cd ems-backend
   ```
2. Verify that you have **Java SDK 21** active (`java -version`).
3. Run the application using Maven:
   ```bash
   mvn spring-boot:run
   ```
   *The API server will boot up at http://localhost:8080. Flyway will automatically run migrations to set up database schemas.*

### Step 3: Start the Python ML Service (`ems-ml`)
1. Open a new terminal and navigate to the ML folder:
   ```bash
   cd ems-ml
   ```
2. Create and activate a Python virtual environment:
   ```bash
   python3 -m venv venv
   source venv/bin/activate
   ```
3. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```
4. Start the FastAPI API server:
   ```bash
   uvicorn main:app --host 127.0.0.1 --port 8001
   ```

### Step 4: Start the Celery Worker
1. Open another terminal, navigate to the ML folder, activate the virtual environment, and start the Celery background worker:
   ```bash
   cd ems-ml
   source venv/bin/activate
   celery -A celery_app worker --loglevel=info
   ```

### Step 5: Start the React Frontend (`ems-frontend`)
1. Open a terminal and navigate to the frontend folder:
   ```bash
   cd ems-frontend
   ```
2. Install the frontend dependencies (Node 18+ required):
   ```bash
   npm install
   ```
3. Start the Vite development server:
   ```bash
   npm run dev
   ```
   *The frontend application will start at http://localhost:5173.*

---

## 🔌 Port Map & Default Access URLs

| Service | Host URL | Description |
| :--- | :--- | :--- |
| **React Frontend Client** | [http://localhost:5173](http://localhost:5173) | Main UI Dashboard |
| **Spring Boot API Backend** | [http://localhost:8080](http://localhost:8080) | Core Business Logic & Endpoints |
| **Backend Swagger UI** | [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) | API Interactive Documentation |
| **FastAPI ML Service** | [http://localhost:8001](http://localhost:8001) | ML Microservice endpoints |
| **ML Service OpenAPI Docs** | [http://localhost:8001/docs](http://localhost:8001/docs) | Interactive FastAPI Documentation |
| **MinIO Storage Console** | [http://localhost:9001](http://localhost:9001) | Object storage administration interface |

---

## 🛠️ Troubleshooting & Environment Tips

* **Python Dependency Compilation Errors:**
  If you are running a host with a bleed-edge Python version (like Python 3.13 or 3.14), library dependencies such as `pandas` or `prophet` might fail to compile locally due to version conflicts. It is recommended to use Python 3.12.

* **Java Version target 21 compilation error:**
  Verify that your IDE or command line shell environment is pointed to Java 21. Check that `JAVA_HOME` points to a JDK 21 installation path.
