#!/bin/bash

# Create S3 bucket
awslocal s3 mb s3://multi-tenant-bucket

# Create SQS queue
awslocal sqs create-queue --queue-name multi-tenant-queue

# Create a secret in Secrets Manager
awslocal secretsmanager create-secret --name multi-tenant-secret --secret-string '{"username":"admin","password":"password"}'

echo "LocalStack initialized with S3 bucket, SQS queue, and Secrets Manager secret"

