# SyncVault

A cloud file-sync microservices platform built with Spring Boot 4, PostgreSQL, Kafka, and S3.

## Services

| Service               | Port | Description                        |
|-----------------------|------|------------------------------------|
| api-gateway           | 8080 | Spring Cloud Gateway, JWT auth     |
| user-service          | 8081 | Registration, auth, storage quota  |
| file-service          | 8082 | Upload, versioning, AI, search     |
| notification-service  | 8083 | Kafka consumer, email via Gmail    |

## Prerequisites

- Java 21
- Maven 3.9+
- Docker Desktop

## Setup

### 1. Clone the repository
```bash
git clone <repo-url>
cd user-entity-api
```

### 2. Create environment file
```bash
cp .env.example .env
```
Edit `.env` and fill in your values (OpenAI key, Gmail credentials).

### 3. Start infrastructure
```bash
docker-compose up -d
```
Wait ~90 seconds for all containers to become healthy.

### 4. Build the project
```bash
mvn clean install -DskipTests
```

### 5. Run a service
```bash
cd user-service
mvn spring-boot:run
```

## Infrastructure Services

| Service                | Port  | Image                    |
|------------------------|-------|--------------------------|
| postgres-users         | 5432  | postgres:15              |
| postgres-files         | 5433  | pgvector/pgvector:pg15   |
| postgres-notifications | 5434  | postgres:15              |
| zookeeper              | 2181  | confluentinc/cp-zookeeper|
| kafka                  | 9092  | confluentinc/cp-kafka    |
| localstack (S3)        | 4566  | localstack/localstack    |

## Environment Variables

See `.env.example` for all required variables including:
- `OPENAI_API_KEY` — required for AI summarization features
- `GMAIL_USERNAME` — optional, for email notifications
- `GMAIL_APP_PASSWORD` — optional, Gmail App Password