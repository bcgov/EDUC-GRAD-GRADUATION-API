# IAC script for KC Client
ENV=$1
COMMON_NAMESPACE=$2
SOAM_KC_REALM_ID=$3
CLIENT_ID=$4
BRANCH=$5
REPO_NAME=$6
SOAM_KC=soam-$ENV.apps.silver.devops.gov.bc.ca

SOAM_KC_LOAD_USER_ADMIN=$(oc -n $COMMON_NAMESPACE-$ENV -o json get secret sso-admin-${ENV} | sed -n 's/.*"username": "\(.*\)"/\1/p' | base64 --decode)
SOAM_KC_LOAD_USER_PASS=$(oc -n $COMMON_NAMESPACE-$ENV -o json get secret sso-admin-${ENV} | sed -n 's/.*"password": "\(.*\)",/\1/p' | base64 --decode)

echo Fetching SOAM token
TKN=$(curl -s \
  -d "client_id=admin-cli" \
  -d "username=$SOAM_KC_LOAD_USER_ADMIN" \
  -d "password=$SOAM_KC_LOAD_USER_PASS" \
  -d "grant_type=password" \
  "https://$SOAM_KC/auth/realms/$SOAM_KC_REALM_ID/protocol/openid-connect/token" | jq -r '.access_token')

echo Retrieving client UUID for $CLIENT_ID
CLIENT_UUID=$(curl -sX GET "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/clients" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TKN" \
  | jq '.[] | select(.clientId=="'"$CLIENT_ID"'")' | jq -r '.id')

if [ "$CLIENT_UUID" = "" ]
then
  echo "$CLIENT_ID DOES NOT EXIST IN KEYCLOAK! A new client with be created with a new access key. Creating..."
  clientJSON=$(curl -s https://raw.githubusercontent.com/bcgov/$REPO_NAME/$BRANCH/tools/config/$CLIENT_ID.json | jq -c 'del(.secret)')
  curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/clients" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TKN" \
    -d "$clientJSON"
else
  echo Fetching client credentials...
  serviceClientSecret=$(curl -sX GET "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/clients/$CLIENT_UUID/client-secret" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TKN" \
    | jq -r '.value')

  echo Creating secret for client
  oc create secret generic grad-graduation-api-client-secret \
    --from-literal=GRAD_GRADUATION_API_CLIENT_NAME=$CLIENT_ID \
    --from-literal=GRAD_GRADUATION_API_CLIENT_SECRET=$serviceClientSecret \
    --dry-run=client -o yaml | oc apply -f -

  echo Removing existing client...
  curl -sX DELETE "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/clients/$CLIENT_UUID" \
    -H "Authorization: Bearer $TKN"

  echo Creating new client with credentials
  clientJSON=$(curl -s https://raw.githubusercontent.com/bcgov/$REPO_NAME/$BRANCH/tools/config/$CLIENT_ID.json | jq -c --arg secret "$serviceClientSecret" '.secret = $secret')
  curl -sX POST "https://$SOAM_KC/auth/admin/realms/$SOAM_KC_REALM_ID/clients" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TKN" \
    -d "$clientJSON"
fi



