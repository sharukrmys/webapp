# Multi-Module Spring Boot Application

A multi-tenant Spring Boot application with multiple deployment modules, AWS integration, and Redis support.

## Technology Stack

- **Java**: 21
- **Spring Boot**: 3.2.4
- **Gradle**: 8.7
- **Database**: PostgreSQL
- **Cache**: Redis (standalone and sentinel modes)
- **AWS Services**: S3, SQS, SecretsManager, Athena
- **Local Development**: Minio (S3-compatible storage)
- **Documentation**: Swagger/OpenAPI
- **Security**: Nimbus JOSE JWT
- **Utilities**: Guava, Apache Commons

## Project Structure

The project consists of the following modules:

- **app-lib**: Common library module with shared configurations and utilities
- **data**: Data service module
- **attachment**: Attachment service module
- **metadata**: Metadata service module
- **user-management**: User Management service module
- **report**: Report service module
- **audit**: Audit service module

## Multi-Tenant Architecture

The application uses a multi-tenant architecture with the following components:

- **Master Tenant Table**: Stores tenant database configurations
- **JPA for Fixed Schemas**: Used for master tenant and S3 config tables
- **JDBC Template for Dynamic Schemas**: Used for tenant-specific operations
- **Named JDBC Templates**:
  - `masterJdbcTemplate`: For master tenant database
  - `tacJdbcTemplate`: For TAC database
  - `flexJdbcTemplate`: For FLEX database
  - `readJdbcTemplate`: For READ database
  - `appstoreJdbcTemplate`: For APPSTORE database
- **Tenant Context**: ThreadLocal-based tenant identification
- **Tenant Interceptor**: Extracts tenant ID from X-TenantID header

## Prerequisites

- Java 21
- Docker and Docker Compose (for local development)
- PostgreSQL
- Redis
- Minio (for local development)

## Getting Started

### Clone the Repository

```bash
git clone https://github.com/yourusername/multi-module-spring-boot.git
cd multi-module-spring-boot
```

### Build the Project

```bash
./gradlew clean build
```

### Run the Application Locally

1. Start the required services using Docker Compose:

```bash
docker-compose up -d
```

2. Run the application with the local profile:

```bash
./gradlew data:bootRun --args='--spring.profiles.active=local'
```

### Access the Application

- **API**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **Actuator**: http://localhost:8080/api/actuator

## Environment Configuration

The application supports different environments through Spring profiles:

- **local**: Local development with Minio instead of S3, disabled Redis/Kafka
- **dev**: Development environment
- **qa**: QA environment with Redis Sentinel
- **prod**: Production environment with Redis Sentinel

### Environment Variables

The following environment variables can be set for different environments:

```bash
# Database
export DB_USERNAME=postgres
export DB_PASSWORD=postgres

# Redis
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=

# AWS
export AWS_REGION=us-east-1
export AWS_ACCESS_KEY_ID=your-access-key
export AWS_SECRET_ACCESS_KEY=your-secret-key

# Minio (for local development)
export MINIO_ENDPOINT=http://localhost:9000
export MINIO_ACCESS_KEY=minioadmin
export MINIO_SECRET_KEY=minioadmin
```

## Docker and Kubernetes Deployment

### Build Docker Images

```bash
# Build all modules
./gradlew clean build

# Build Docker images
docker build -t multi-module-spring-boot/data:latest -f data/Dockerfile .
docker build -t multi-module-spring-boot/attachment:latest -f attachment/Dockerfile .
docker build -t multi-module-spring-boot/metadata:latest -f metadata/Dockerfile .
docker build -t multi-module-spring-boot/user-management:latest -f user-management/Dockerfile .
docker build -t multi-module-spring-boot/report:latest -f report/Dockerfile .
docker build -t multi-module-spring-boot/audit:latest -f audit/Dockerfile .
```

### Deploy to Kubernetes

```bash
# Apply Kubernetes manifests
kubectl apply -f kubernetes/
```

## VS Code Setup

1. Install the following extensions:
   - Extension Pack for Java
   - Spring Boot Extension Pack
   - Gradle for Java
   - Docker
   - Kubernetes

2. Configure VS Code settings:

```json
{
  "java.configuration.updateBuildConfiguration": "automatic",
  "java.compile.nullAnalysis.mode": "automatic",
  "java.format.enabled": true,
  "editor.formatOnSave": true,
  "java.format.settings.url": ".vscode/java-formatter.xml",
  "java.format.settings.profile": "GoogleStyle",
  "java.test.config": {
    "vmArgs": [
      "-Dspring.profiles.active=test"
    ]
  }
}
```

3. Create a launch configuration:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Data Service (Local)",
      "request": "launch",
      "mainClass": "com.example.data.DataApplication",
      "projectName": "data",
      "args": "--spring.profiles.active=local",
      "env": {
        "DB_USERNAME": "postgres",
        "DB_PASSWORD": "postgres"
      }
    }
  ]
}
```

## API Documentation

The API documentation is available through Swagger UI at:

```
http://localhost:8080/api/swagger-ui.html
```

## Testing

Run the tests with:

```bash
./gradlew test
```

Generate test coverage reports:

```bash
./gradlew jacocoTestReport
```

## License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

