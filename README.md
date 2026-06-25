# Wardrove

Wardrove is a personal clothing and outfit application. This repository currently contains the initial backend foundation only.

## Backend local development

### Prerequisites

- Java 21
- Maven 3.9+
- Docker and Docker Compose

### Start PostgreSQL

```bash
docker compose up -d postgres
```

The local database runs at `localhost:5432` with:

- Database: `wardrove`
- Username: `wardrove`
- Password: `wardrove`

### Run the backend

```bash
cd backend
./mvnw spring-boot:run
```

If the Maven wrapper is not present, use your installed Maven:

```bash
cd backend
mvn spring-boot:run
```

The backend uses Flyway to apply migrations on startup and exposes Actuator health at:

```bash
curl http://localhost:8080/actuator/health
```

### Build and test

```bash
cd backend
mvn test
```

### Configuration

The backend defaults to local PostgreSQL values, and each can be overridden with environment variables:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
