# Multi-Module Spring Boot Application

This is a multi-tenant Spring Boot application with multiple modules for different functionalities.

## Technology Stack

- Java 17 (with support for Java 21 in production)
- Spring Boot 3.2.5
- Spring Cloud 2023.0.0
- Gradle 8.5
- PostgreSQL
- Redis (Cache, Queue, Pub/Sub)
- AWS Services (SQS, S3, Secrets Manager)
- Minio
- Nimbus JOSE JWT
- Guava

## Project Structure

The project is organized into the following modules:

- **app-lib**: Common library module used by all other modules
- **data**: Data access module
- **attachment**: File handling module
- **metadata**: Metadata management module
- **user-management**: User authentication and authorization module
- **report**: Reporting module
- **audit**: Audit logging module

## Multi-Tenant Architecture

The application uses a multi-tenant architecture where each tenant has its own database. The master tenant table contains the database connection details for each tenant:

```sql
CREATE TABLE public.master_tenant (
    id bigserial NOT NULL,
    dialect varchar(255) NULL,
    "password" varchar(30) NULL,
    tenant_id varchar(30) NULL,
    url varchar(256) NULL,
    username varchar(30) NULL,
    "version" int4 NOT NULL,
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

The application supports different environments with specific configurations:

- **Local**: Development environment with disabled Redis/Kafka and local AWS services
- **Dev**: Development environment with enabled services
- **QA**: Testing environment
- **Prod**: Production environment

Configuration files are located in the app-lib module:

- `application.yml`: Common configuration
- `application-local.yml`: Local environment configuration
- `application-dev.yml`: Development environment configuration
- `application-qa.yml`: QA environment configuration
- `application-prod.yml`: Production environment configuration

## Feature Toggles

The application supports feature toggles to enable/disable specific features in different environments:

- Redis cache and messaging
- Kafka event streaming
- AWS S3 storage
- AWS SQS messaging
- Minio object storage
- Secrets Manager

## Building and Running

### Prerequisites

- Java 17 or higher
- PostgreSQL
- Redis (optional, can be disabled in local mode)
- AWS services (optional, can be disabled in local mode)

### Build

```bash
./gradlew clean build
```

### Run

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

## Module Descriptions

### app-lib

Common library module containing:

- Multi-tenant configuration
- Database connection management
- Feature toggle configuration
- Common utilities
- Environment-specific configurations

### data

Data access module for:

- Database operations
- Data models
- Repositories
- Data services

### attachment

File handling module for:

- File upload/download
- File storage (S3/Minio)
- File metadata

### metadata

Metadata management module for:

- Tenant management
- System metadata
- Configuration management

### user-management

User management module for:

- Authentication
- Authorization
- User profiles
- Role management

### report

Reporting module for:

- Report generation
- Data export
- Scheduled reports

### audit

Audit logging module for:

- Activity logging
- Audit trails
- Compliance reporting

