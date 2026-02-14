#!/bin/bash
awslocal sns create-topic --name policy-issued-topic

# Notification Service setup
awslocal sqs create-queue --name notification-queue
awslocal sns subscribe --topic-arn arn:aws:sns:us-east-1:000000000000:policy-issued-topic \
    --protocol sqs --notification-endpoint arn:aws:sqs:us-east-1:000000000000:notification-queue

# Document Service setup
awslocal sqs create-queue --name document-queue
awslocal sns subscribe --topic-arn arn:aws:sns:us-east-1:000000000000:policy-issued-topic \
    --protocol sqs --notification-endpoint arn:aws:sqs:us-east-1:000000000000:document-queue
awslocal s3 mb s3://policy-documents

# Search Service setup
awslocal sqs create-queue --name search-queue
awslocal sns subscribe --topic-arn arn:aws:sns:us-east-1:000000000000:policy-issued-topic \
    --protocol sqs --notification-endpoint arn:aws:sqs:us-east-1:000000000000:search-queue
