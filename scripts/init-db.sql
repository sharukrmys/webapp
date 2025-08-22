-- Create master_tenant table
CREATE TABLE IF NOT EXISTS public.master_tenant (
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

-- Create turbos3config table
CREATE TABLE IF NOT EXISTS public.turbos3config (
    id bigserial NOT NULL,
    awsaccesskeyid varchar(255) NULL,
    awssecretaccesskey varchar(255) NULL,
    region varchar(50) NULL,
    bucketname varchar(255) NULL,
    schemabucketname varchar(255) NULL,
    imagebucketname varchar(255) NULL,
    flexbucketname varchar(255) NULL,
    companyprofilebucketname varchar(255) NULL,
    datamanagementbucketname varchar(255) NULL,
    CONSTRAINT turbos3config_pkey PRIMARY KEY (id)
);

-- Insert sample data into master_tenant
INSERT INTO public.master_tenant (
    dialect, password, tenant_id, url, username, version, 
    flexdb, procedures_filename, readdb, appstoredb, 
    db_properties, isactive
) VALUES (
    'org.hibernate.dialect.PostgreSQL9Dialect', 
    'db_password', 
    'tenant1', 
    'jdbc:postgresql://localhost:5432/tenant1_tac', 
    'db_user', 
    0, 
    'jdbc:postgresql://localhost:5432/tenant1_flex', 
    'procedures.sql', 
    'jdbc:postgresql://localhost:5432/tenant1_read', 
    'jdbc:postgresql://localhost:5432/tenant1_appstore', 
    '{"minIdle": 1,"maxPoolSize":3,"connectionTimeout":30000,"idleTimeout":600000}', 
    true
);

-- Insert sample data into turbos3config
INSERT INTO public.turbos3config (
    awsaccesskeyid, awssecretaccesskey, region, 
    bucketname, schemabucketname, imagebucketname, 
    flexbucketname, companyprofilebucketname, datamanagementbucketname
) VALUES (
    'your_access_key', 
    'your_secret_key', 
    'us-east-1', 
    'main-bucket', 
    'schema-bucket', 
    'image-bucket', 
    'flex-bucket', 
    'profile-bucket', 
    'data-bucket'
);

-- Create tenant databases
CREATE DATABASE tenant1_tac;
CREATE DATABASE tenant1_flex;
CREATE DATABASE tenant1_read;
CREATE DATABASE tenant1_appstore;

