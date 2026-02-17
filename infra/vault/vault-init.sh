#!/bin/sh

# Wait for Vault to be ready
until vault status > /dev/null 2>&1; do
  echo "Waiting for Vault..."
  sleep 1
done

echo "Vault is up. Configuring secrets..."

# Enable KV engine version 2 at 'secret/'
vault secrets enable -path=secret kv-v2 || true

########################################### GLOBAL SHARED CONFIG (secret/application)##########################################
vault kv put secret/application \
    aws.access.key.id=test \
    aws.secret.access.key=test \
    aws.region=us-east-1 \
    spring.cloud.aws.sqs.endpoint=http://localstack:4566 \
    spring.cloud.aws.sns.endpoint=http://localstack:4566 \
    spring.security.oauth2.resourceserver.jwt.issuer-uri=http://keycloak:8080/realms/insure-cloud \
    eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka/

########################################### API GATEWAY (secret/api-gateway) ##########################################
vault kv put secret/api-gateway \
    server.ssl.key-store-password=changeit

########################################### POLICY SERVICE (secret/policy-service)##########################################
vault kv put secret/policy-service \
    spring.datasource.url=jdbc:postgresql://postgres:5432/insure_db \
    spring.datasource.username=user \
    spring.datasource.password=password

########################################### QUOTE SERVICE (secret/quote-service)##########################################
vault kv put secret/quote-service \
    spring.data.redis.host=redis \
    spring.data.redis.port=6379

########################################### SEARCH SERVICE (secret/search-service)##########################################
vault kv put secret/search-service \
    spring.elasticsearch.uris=http://elasticsearch:9200

########################################### NOTIFICATION SERVICE (secret/notification-service)##########################################
vault kv put secret/notification-service \
    spring.mail.username=test \
    spring.mail.password=test

########################################### DISCOVERY SERVICE (secret/discovery-service)##########################################
vault kv put secret/discovery-service \
    spring.security.user.name=admin \
    spring.security.user.password=password

echo "Vault configuration complete."
