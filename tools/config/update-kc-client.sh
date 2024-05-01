# IAC script for KC Client
# ENVS
CLIENT_SECRET_NAME=grad-graduation-api-client-secret
ENV=$1
COMMON_NAMESPACE=$2
SOAM_KC_REALM_ID=$3
CLIENT_ID=$4
BRANCH=$5
REPO_NAME=$6
SOAM_KC=soam-$ENV.apps.silver.devops.gov.bc.ca

SOAM_KC_LOAD_USER_ADMIN=$(oc -n $COMMON_NAMESPACE-$ENV -o json get secret sso-admin-${ENV} | sed -n 's/.*"username": "\(.*\)"/\1/p' | base64 --decode)
SOAM_KC_LOAD_USER_PASS=$(oc -n $COMMON_NAMESPACE-$ENV -o json get secret sso-admin-${ENV} | sed -n 's/.*"password": "\(.*\)",/\1/p' | base64 --decode)

#### Function declarations
# Retrieves the keycloak client UUID
function fetchClientUUID() {
    echo Retrieving client UUID for $CLIENT_ID
    CLIENT_UUID=$(curl -sX GET "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/clients" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $TKN" \
      | jq '.[] | select(.clientId=="'"$CLIENT_ID"'")' | jq -r '.id')
}

# Fetch client credentials
function fetchClientCredentials() {
      echo Fetching client credentials...
      SERVICE_CLIENT_SECRET=$(curl -sX GET "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/clients/$CLIENT_UUID/client-secret" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TKN" \
        | jq -r '.value')
}

# Creates the oc client secret
function createClientSecret() {
      echo Creating secret for client
      oc create secret generic $CLIENT_SECRET_NAME \
        --from-literal=GRAD_GRADUATION_API_CLIENT_NAME=$CLIENT_ID \
        --from-literal=GRAD_GRADUATION_API_CLIENT_SECRET=$SERVICE_CLIENT_SECRET \
        --dry-run=client -o yaml | oc apply -f -
}

#### Begin
echo Fetching SOAM token
TKN=$(curl -s \
  -d "client_id=admin-cli" \
  -d "username=$SOAM_KC_LOAD_USER_ADMIN" \
  -d "password=$SOAM_KC_LOAD_USER_PASS" \
  -d "grant_type=password" \
  "https://$SOAM_KC/auth/realms/$SOAM_KC_REALM_ID/protocol/openid-connect/token" | jq -r '.access_token')

# Try getting the UUID
fetchClientUUID

if [ "$CLIENT_UUID" = "" ]
then
  # Client not found
  echo "$CLIENT_ID DOES NOT EXIST IN KEYCLOAK! A new client with be created with a new access key. Creating..."
  # Retrieve json, remove secret field if exists (shouldn't)
  CLIENT_JSON=$(curl -s https://raw.githubusercontent.com/bcgov/$REPO_NAME/$BRANCH/tools/config/$CLIENT_ID.json | jq -c 'del(.secret)')
  # Create client
  curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/clients" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TKN" \
    -d "$CLIENT_JSON"
  # Get the UUID
  fetchClientUUID
  # Fetch generated credentials
  fetchClientCredentials
  # Create or update client secret on OS
  createClientSecret
else
  # Get Credentials
  fetchClientCredentials
  # Ensure secret
  createClientSecret
  # Turf the old client
  echo Removing existing client...
  curl -sX DELETE "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/clients/$CLIENT_UUID" \
    -H "Authorization: Bearer $TKN"
  # Recreate the client wth updated info
  echo Creating new client with credentials
  # Get JSON and inject secret
  CLIENT_JSON=$(curl -s https://raw.githubusercontent.com/bcgov/$REPO_NAME/$BRANCH/tools/config/$CLIENT_ID.json | jq -c --arg secret "$SERVICE_CLIENT_SECRET" '.secret = $secret')
  curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/clients" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TKN" \
    -d "$CLIENT_JSON"
fi



