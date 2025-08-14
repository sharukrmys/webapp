# Multi-Module Spring Boot Application

A multi-tenant Spring Boot 3.5.4 application with Java 21 and Gradle, featuring multiple deployment modules and AWS integration.

## Project Structure

- **app-lib**: Common library module shared across all other modules
- **user-management**: User and role management functionality
- **data**: Data management module
- **attachment**: File attachment handling module
- **metadata**: Metadata management module
- **report**: Reporting functionality module
- **audit**: Audit logging module

## Features

- Java 21 with Spring Boot 3.5.4
- Multi-tenant architecture with dynamic database routing
- AWS integration (SQS, S3, SecretsManager)
- Minio for local S3-compatible storage
- Redis for caching, queuing, and pub/sub
- PostgreSQL database
- Environment-specific configurations
- JWT authentication with Nimbus JOSE

## Multi-Tenant Architecture

The application uses a multi-tenant architecture where each tenant has its own database. The tenant information is stored in a master database table:

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

The application supports multiple environments with specific configurations:

- **local**: Local development environment with Minio instead of AWS S3, disabled Redis
- **dev**: Development environment with AWS services and Redis enabled
- **qa**: QA environment with AWS services and Redis enabled
- **prod**: Production environment with AWS services and Redis enabled

## Building and Running

### Prerequisites

- Java 21
- Gradle
- PostgreSQL
- Redis (optional for local development)
- Minio (optional for local development)

### Build

```bash
./gradlew clean build
```

### Run a Specific Module

```bash
./gradlew :user-management:bootRun
```

### Run with a Specific Profile

```bash
./gradlew :user-management:bootRun --args='--spring.profiles.active=dev'
```

## Tenant Identification

Tenants can be identified in several ways:

1. HTTP Header: `X-Tenant-ID`
2. Request Parameter: `tenantId`
3. Subdomain: `tenant-name.example.com`

If no tenant is specified, the default tenant is used.

## Local Development

For local development, AWS services are disabled by default and replaced with:

- Minio for S3-compatible storage
- Local PostgreSQL for database
- Redis can be optionally disabled

## Security

The application uses Spring Security with JWT authentication. Passwords are encrypted using BCrypt.

