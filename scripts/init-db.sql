-- Create master_tenant table
CREATE TABLE IF NOT EXISTS public.master_tenant (
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

-- Insert sample tenant data
INSERT INTO public.master_tenant (
    dialect, password, tenant_id, url, username, version, 
    flexdb, procedures_filename, readdb, appstoredb, 
    db_properties, isactive, connectiontimeout, idletimeout, maxpoolsize, minidle
) VALUES (
    'org.hibernate.dialect.PostgreSQL9Dialect', 'postgres', 'tenant1', 
    'jdbc:postgresql://postgres:5432/tenant1_db', 'postgres', 0,
    'jdbc:postgresql://postgres:5432/tenant1_flex', 'procedures.sql', 
    'jdbc:postgresql://postgres:5432/tenant1_read', NULL,
    '{"minIdle": 1,"maxPoolSize":3,"connectionTimeout":1,"idleTimeout":1}', 
    true, 30000, 30000, 10, 2
);

INSERT INTO public.master_tenant (
    dialect, password, tenant_id, url, username, version, 
    flexdb, procedures_filename, readdb, appstoredb, 
    db_properties, isactive, connectiontimeout, idletimeout, maxpoolsize, minidle
) VALUES (
    'org.hibernate.dialect.PostgreSQL9Dialect', 'postgres', 'tenant2', 
    'jdbc:postgresql://postgres:5432/tenant2_db', 'postgres', 0,
    'jdbc:postgresql://postgres:5432/tenant2_flex', 'procedures.sql', 
    'jdbc:postgresql://postgres:5432/tenant2_read', NULL,
    '{"minIdle": 1,"maxPoolSize":3,"connectionTimeout":1,"idleTimeout":1}', 
    true, 30000, 30000, 10, 2
);

-- Create tenant databases
CREATE DATABASE tenant1_db;
CREATE DATABASE tenant1_flex;
CREATE DATABASE tenant1_read;
CREATE DATABASE tenant2_db;
CREATE DATABASE tenant2_flex;
CREATE DATABASE tenant2_read;

