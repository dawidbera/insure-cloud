#!/bin/bash

# Configuration
KEYCLOAK_URL="http://localhost:8088"
REALM="insure-cloud"
CLIENT_ID="insure-cloud-public"
PASSWORD="password"

# Default to agent if no argument provided
ROLE=${1:-"AGENT"}
USERNAME="agent-001"

if [ "$ROLE" == "ADMIN" ]; then
    USERNAME="admin-user"
elif [ "$ROLE" == "CUSTOMER" ]; then
    USERNAME="customer-001"
fi

echo "Attempting to get token for user: $USERNAME (Role: $ROLE) in realm: $REALM..."

RESPONSE=$(curl -s -X POST "$KEYCLOAK_URL/realms/$REALM/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=$CLIENT_ID" \
  -d "username=$USERNAME" \
  -d "password=$PASSWORD")

TOKEN=$(echo $RESPONSE | grep -oP '(?<="access_token":")[^"]*')

if [ -z "$TOKEN" ]; then
    echo "Failed to get token. Response from Keycloak:"
    echo "$RESPONSE"
    echo ""
    echo "Note: Ensure that the realm '$REALM', client '$CLIENT_ID' (public), and user '$USERNAME' exist in Keycloak."
else
    echo "Token retrieved successfully!"
    echo ""
    echo "Bearer $TOKEN"
    echo ""
    echo "You can now use this token in the 'Authorize' button in Swagger UI."
fi
