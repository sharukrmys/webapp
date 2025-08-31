# Multi-Module Spring Boot Project

This is a multi-module Spring Boot project with Java 21 compatibility, Spring Boot 3.5.3, and Spring Cloud 2025.0.0.

## Project Structure

```
multi-module-spring-boot/
├── app-lib/           # Common library module used by all other modules
├── data/              # Data service module
├── attachment/        # Attachment service module
├── metadata/          # Metadata service module
├── user-management/   # User Management service module
├── report/            # Report service module
└── audit/             # Audit service module
```

## Technology Stack

- Java 21.0.8
- Spring Boot 3.5.3
- Spring Cloud 2025.0.0
- Gradle 8.5
- PostgreSQL
- Redis (Cache, Queue, Pub/Sub)
- AWS Services (SQS, S3, Secrets Manager)
- Minio
- Nimbus JOSE JWT

## Multi-Tenant Architecture

The project uses a multi-tenant architecture where a master tenant table contains database details for each tenant. The system dynamically selects the appropriate database based on the tenant ID in the request.

Master tenant table structure:
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

## Environment Configuration

The project includes environment-specific configuration files in the app-lib module:
- local-application.yml
- dev-application.yml
- qa-application.yml
- prod-application.yml

These configurations include options to disable Kafka and Redis when running in local mode.

## Building the Project

```bash
# Build the project
./gradlew clean build

# Run tests
./gradlew test

# Build without running tests
./gradlew clean build -x test
```

## Java Version

The project is configured to use Java 21.0.8. A `.java-version` file is included for tools like jenv or sdkman.

If you need to build with a different Java version, you can modify the `sourceCompatibility` and `targetCompatibility` settings in the `build.gradle` file.

## Feature Toggles

The project includes feature toggles to enable/disable certain components:
- Redis: `spring.redis.enabled=true/false`
- Kafka: `spring.kafka.enabled=true/false`
- AWS Services: `aws.enabled=true/false`
- AWS S3: `aws.s3.enabled=true/false`
- AWS SQS: `aws.sqs.enabled=true/false`
- AWS Secrets Manager: `aws.secretsmanager.enabled=true/false`

## Dependencies

The project uses the following key dependencies:
- Spring Boot Starters (Web, Data JPA, Redis, Cache, Validation, Actuator)
- Spring Cloud OpenFeign for service-to-service communication
- PostgreSQL and HikariCP for database connectivity
- Redisson for Redis operations
- AWS SDK v2 for AWS services
- Minio for S3-compatible storage
- Guava for utility functions
- Nimbus JOSE JWT for JWT handling
- Apache POI and DocX4j for document processing

