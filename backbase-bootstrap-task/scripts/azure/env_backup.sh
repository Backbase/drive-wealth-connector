#!/bin/sh

if ! command -v jq &> /dev/null
then
    echo "jq could not be found, check this link to install jq https://stedolan.github.io/jq/download/"
    exit
fi

kubectl config set-context --current --namespace=backbase
INSTALLATION_NAME=$(kubectl config current-context| cut -d "-" -f 2)
RUNTIME_NAME=$(kubectl config current-context| awk -F "$INSTALLATION_NAME"- '{print $2}')
IDENTITY_HOST=identity.$RUNTIME_NAME.$INSTALLATION_NAME.azure.backbaseservices.com
IDENTITY_USERNAME=$(kubectl get secret identity  -o json | jq -r '.data["identity-username"]' | base64 --decode)
IDENTITY_PASSWORD=$(kubectl get secret identity  -o json | jq -r '.data["identity-password"]' | base64 --decode)
BACKUP_TIMESTAMP=$(date +%Y%m%d_%H%M)
BACKUP_BASE_DIR_NAME="files/backups/${RUNTIME_NAME}"

echo "Backing up Identity"
mkdir -p "${BACKUP_BASE_DIR_NAME}/Identity/${BACKUP_TIMESTAMP}"
TOKEN=$(curl \
  -d "client_id=admin-cli" \
  -d "username=$IDENTITY_USERNAME" \
  -d "password=$IDENTITY_PASSWORD" \
  -d "grant_type=password" \
  "https://$IDENTITY_HOST/auth/realms/master/protocol/openid-connect/token" --silent |  cut -d ':' -f 2 | cut -d ',' -f 1 | tr -d '"')

realm_list=($(curl -S -s "https://${IDENTITY_HOST}/auth/admin/realms" \
-H "Content-Type: application/json" \
-H "Authorization: bearer $TOKEN" \
| jq -r '.[].realm'))

for realm_name in "${realm_list[@]}"; do

  echo "\n## Writing realm ${realm_name} to ${BACKUP_BASE_DIR_NAME}/Identity/${BACKUP_TIMESTAMP}/${realm_name}.json"
  curl -X POST -Ss "https://${IDENTITY_HOST}/auth/admin/realms/${realm_name}/partial-export" \
  -H "Content-Type: application/json" \
  -H "Authorization: bearer $TOKEN" \
  -d '{ "exportClients": true, "exportGroupsAndRoles:" true }' \
  > "${BACKUP_BASE_DIR_NAME}/Identity/${BACKUP_TIMESTAMP}/${realm_name}.json"

  echo "\n#### Writing realm ${realm_name} roles to ${BACKUP_BASE_DIR_NAME}/Identity/${BACKUP_TIMESTAMP}/${realm_name}-roles.json"
  curl -X GET -Ss "https://${IDENTITY_HOST}/auth/admin/realms/${realm_name}/roles" \
  -H "Content-Type: application/json" \
  -H "Authorization: bearer $TOKEN" \
  > "${BACKUP_BASE_DIR_NAME}/Identity/${BACKUP_TIMESTAMP}/${realm_name}-roles.json"

  echo "\n#### Writing realm ${realm_name} clients to ${BACKUP_BASE_DIR_NAME}/Identity/${BACKUP_TIMESTAMP}/${realm_name}-clients.json"
  curl -X GET -Ss "https://${IDENTITY_HOST}/auth/admin/realms/${realm_name}/clients" \
  -H "Content-Type: application/json" \
  -H "Authorization: bearer $TOKEN" \
  > "${BACKUP_BASE_DIR_NAME}/Identity/${BACKUP_TIMESTAMP}/${realm_name}-clients.json"

  echo "\n#### Writing realm ${realm_name} users to ${BACKUP_BASE_DIR_NAME}/Identity/${BACKUP_TIMESTAMP}/${realm_name}-users.json"
  curl -X GET -Ss "https://${IDENTITY_HOST}/auth/admin/realms/${realm_name}/users" \
  -H "Content-Type: application/json" \
  -H "Authorization: bearer $TOKEN" \
  > "${BACKUP_BASE_DIR_NAME}/Identity/${BACKUP_TIMESTAMP}/${realm_name}-users.json"

done
