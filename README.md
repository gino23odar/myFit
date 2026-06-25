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

PostgreSQL only applies `POSTGRES_DB`, `POSTGRES_USER`, and `POSTGRES_PASSWORD` when its data volume is first created. If you previously started this project with different credentials, recreate the local development volume before running the backend:

```bash
docker compose down --volumes --remove-orphans
docker compose up -d postgres
```

You can confirm the configured local credentials with:

```bash
docker compose exec postgres pg_isready -U wardrove -d wardrove
```

### Run the backend

```bash
cd backend
mvn spring-boot:run
```

The backend defaults to the same local database credentials as `docker-compose.yml`. If you use a different local PostgreSQL user, set all three datasource environment variables before starting the app.

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/wardrove \
SPRING_DATASOURCE_USERNAME=wardrove \
SPRING_DATASOURCE_PASSWORD=wardrove \
mvn spring-boot:run
```

The backend uses Flyway to apply migrations on startup and exposes Actuator health at:

```bash
curl http://localhost:8080/actuator/health
```


### Auth endpoints

The first auth iteration exposes:

- `POST /api/v1/auth/signup`
- `POST /api/v1/auth/signin`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/signout`

Signup and signin accept:

```json
{
  "email": "user@example.com",
  "password": "change-me-123"
}
```

Signup, signin, and refresh return a JWT access token plus an opaque refresh token. Signout accepts the refresh token and revokes it.


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


### Troubleshooting local PostgreSQL authentication

If startup fails with `FATAL: password authentication failed for user "wardrove"`, the application is reaching PostgreSQL, but the password in the existing database does not match the backend configuration. For the local Docker database, the quickest fix is to recreate the dev-only volume:

```bash
docker compose down --volumes --remove-orphans
docker compose up -d postgres
cd backend
mvn spring-boot:run
```

Only use `--volumes` for local development data you are comfortable deleting. If you are connecting to a non-Docker PostgreSQL instance, keep the database and instead set `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD` to match that instance.
