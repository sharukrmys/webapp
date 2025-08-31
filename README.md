# Multi-Module Spring Boot Application

This is a multi-module Spring Boot application with AWS services integration and multi-tenant architecture.

## Project Structure

- Java 21
- Spring Boot 3.2.4 (will be updated to 3.5.3 when available)
- Gradle 8.5 (will be updated to 8.14+ when available)
- Multi-module architecture with shared library module

## Modules

1. **app-lib**: Shared library module with common utilities and configurations
2. **data**: Data service for database operations
3. **attachment**: Service for file attachments and S3 operations
4. **metadata**: Service for metadata management
5. **user-management**: Service for user authentication and authorization
6. **report**: Service for report generation
7. **audit**: Service for audit logging

## Features

- Multi-tenant architecture with master tenant table
- AWS service integrations (S3, SQS, Secrets Manager)
- Redis for caching, queue, and pub/sub (supports both standalone and sentinel modes)
- PostgreSQL database support
- Environment-specific configuration files
- MinIO for local development

## Configuration

### Redis Configuration

The application supports both standalone Redis and Redis Sentinel configurations:

#### Standalone Redis (Default for local development)

```yaml
spring:
  redis:
    enabled: true
    mode: standalone
    host: localhost
    port: 6379
    password: 
```

#### Redis Sentinel (For production environments)

```yaml
spring:
  redis:
    enabled: true
    mode: sentinel
    sentinel:
      master: mymaster
      nodes: redis-sentinel-0:26379,redis-sentinel-1:26379,redis-sentinel-2:26379
    password: ${REDIS_PASSWORD}
```

### AWS Configuration

By default, the application uses IAM instance profile credentials for AWS services in non-local environments:

```yaml
aws:
  region: us-east-1
  use-instance-profile: true  # Uses EC2/EKS/K8s instance profile
  s3:
    enabled: true
  sqs:
    enabled: true
  secretsmanager:
    enabled: true
```

For local development, you can provide explicit credentials:

```yaml
aws:
  region: us-east-1
  use-instance-profile: false
  access-key: your-access-key
  secret-key: your-secret-key
  s3:
    enabled: true
  sqs:
    enabled: true
  secretsmanager:
    enabled: true
```

### Feature Toggles

The application supports feature toggles to enable/disable various components, especially useful for local development:

```yaml
# Feature toggles
features:
  kafka:
    enabled: false  # Disable Kafka
  redis:
    enabled: false  # Disable Redis
  s3:
    enabled: false  # Disable S3
  sqs:
    enabled: false  # Disable SQS
  secretsmanager:
    enabled: false  # Disable Secrets Manager
```

You can enable/disable these features using environment variables:

```bash
export FEATURE_KAFKA_ENABLED=false
export FEATURE_REDIS_ENABLED=true
export FEATURE_S3_ENABLED=false
export FEATURE_SQS_ENABLED=false
export FEATURE_SECRETSMANAGER_ENABLED=false
```

Or via command-line arguments:

```bash
java -jar app.jar --features.kafka.enabled=false --features.redis.enabled=true
```

## How to Run

### Local Development

1. Start the required services using Docker Compose:

```bash
docker-compose up -d
```

2. Run the application with the local profile:

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

### Production Deployment

1. Build the application:

```bash
./gradlew clean build
```

2. Deploy the application using Kubernetes:

```bash
kubectl apply -f kubernetes/
```

## Multi-Tenant Architecture

The application uses a master tenant table to store connection details for each tenant:

```sql
CREATE TABLE public.master_tenant (
    id bigserial NOT NULL,
    dialect varchar(255) NULL,
    password varchar(30) NULL,
    tenant_id varchar(30) NULL,
    url varchar(256) NULL,
    username varchar(30) NULL,
    version int4 NOT NULL,
    flexdb varchar(255) NULL,
    procedures_filename varchar(255) DEFAULT 'procedures.sql'::character varying NULL,
    readdb varchar NULL,
    appstoredb varchar(256) NULL,
    db_properties text DEFAULT '{"minIdle": 1,"maxPoolSize":3,"connectionTimeout":1,"idleTimeout":1}'::text NULL,
    isactive bool DEFAULT true NULL,
    connectiontimeout int8 NULL,
    idletimeout int8 NULL,
    maxpoolsize int4 NULL,
    minidle int4 NULL,
    CONSTRAINT master_tenant_pkey PRIMARY KEY (id)
);
```

The application uses a tenant-aware data source to route database requests to the appropriate tenant database.

