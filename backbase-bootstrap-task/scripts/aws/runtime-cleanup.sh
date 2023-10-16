#!/bin/sh

# Print Banner
cat ../banner.txt

if ! command -v jq &>/dev/null; then
  echo "jq could not be found, check this link to install jq https://stedolan.github.io/jq/download/"
  exit
fi

kubectl config set-context --current --namespace=backbase
DB_HOST=$(kubectl get secret database -o json | jq -r '.data["mysql-endpoint"]' | base64 --decode)
INSTALLATION_NAME=$(echo "$DB_HOST" | awk -F'-' '{print$1}')
TEMP=$(echo "$DB_HOST" | awk -F '-(us(-gov)?|ap|ca|cn|eu|sa)-(central|(north|south)?(east|west)?)' '{print$1}')
RUNTIME_NAME=$(cut -d "-" -f2- <<< "$TEMP")
IDENTITY_HOST=identity.$RUNTIME_NAME.$INSTALLATION_NAME.live.backbaseservices.com
IDENTITY_USERNAME=$(kubectl get secret identity -o json | jq -r '.data["identity-username"]' | base64 --decode)
IDENTITY_PASSWORD=$(kubectl get secret identity -o json | jq -r '.data["identity-password"]' | base64 --decode)
KUBERNETES_CLUSTER_NAME=$(kubectl config current-context | awk -F '/' '{print$2}')

read -p "Wiping data for Identity $IDENTITY_HOST and Kubernetes cluster $KUBERNETES_CLUSTER_NAME, Are you sure? " -n 1 -r
if [[ $REPLY =~ ^[Yy]$ ]]; then

  TOKEN=$(curl \
    -d "client_id=admin-cli" \
    -d "username=$IDENTITY_USERNAME" \
    -d "password=$IDENTITY_PASSWORD" \
    -d "grant_type=password" \
    "https://$IDENTITY_HOST/auth/realms/master/protocol/openid-connect/token" --silent | cut -d ':' -f 2 | cut -d ',' -f 1 | tr -d '"')

  for REALM_NAME in employee retail customer; do
    echo "Retrieving user id of $REALM_NAME realm"

    USER_ARRAY=$(curl \
      -H "Authorization: bearer $TOKEN" \
      -X GET \
      "https://$IDENTITY_HOST/auth/admin/realms/$REALM_NAME/users" --silent | jq .[].id | sed -e 's/^"//' -e 's/"$//')

    for USER_ID in $USER_ARRAY; do
      echo "Deleting $USER_ID from $REALM_NAME"
      curl \
        -H "Authorization: bearer $TOKEN" \
        -X DELETE \
        "https://$IDENTITY_HOST/auth/admin/realms/$REALM_NAME/users/$USER_ID" --silent

      echo "$USER_ID deleted successfully"
    done
  done

  ACESS_CONTROL_DB_NAME=$(kubectl get secret db-dbs-accesscontrol -o json | jq -r '.data["db_name"]' | base64 --decode)
  ARRANGEMENT_MANAGER_DB_NAME=$(kubectl get secret db-dbs-arrangementmanager -o json | jq -r '.data["db_name"]' | base64 --decode)
  USER_MANAGER_DB_NAME=$(kubectl get secret db-dbs-usermanager -o json | jq -r '.data["db_name"]' | base64 --decode)
  TRANSACTION_MANAGER_DB_NAME=$(kubectl get secret db-dbs-transactionmanager -o json | jq -r '.data["db_name"]' | base64 --decode)
  CONTACT_MANAGER_DB_NAME=$(kubectl get secret db-dbs-contactmanager -o json | jq -r '.data["db_name"]' | base64 --decode)
  APPROVALS_DB_NAME=$(kubectl get secret db-dbs-approvals -o json | jq -r '.data["db_name"]' | base64 --decode)

  DB_USERNAME=$(kubectl get secret database -o json | jq -r '.data["mysql-username"]' | base64 --decode)
  DB_PASSWORD=$(kubectl get secret database -o json | jq -r '.data["mysql-password"]' | base64 --decode)

  kubectl run mysql --attach=true --rm --image=alpine -- /bin/sh -c 'apk add mysql-client; mysql -u'$DB_USERNAME' -h '$DB_HOST' -p'$DB_PASSWORD' -Bse "DROP SCHEMA \`'$ACESS_CONTROL_DB_NAME'\`; commit; CREATE SCHEMA \`'$ACESS_CONTROL_DB_NAME'\`; commit; DROP SCHEMA \`'$ARRANGEMENT_MANAGER_DB_NAME'\`; commit; CREATE SCHEMA \`'$ARRANGEMENT_MANAGER_DB_NAME'\`; commit; DROP SCHEMA \`'$USER_MANAGER_DB_NAME'\`; commit; CREATE SCHEMA \`'$USER_MANAGER_DB_NAME'\`; commit; DROP SCHEMA \`'$TRANSACTION_MANAGER_DB_NAME'\`; commit; CREATE SCHEMA \`'$TRANSACTION_MANAGER_DB_NAME'\`; commit; DROP SCHEMA \`'$CONTACT_MANAGER_DB_NAME'\`; commit; CREATE SCHEMA \`'$CONTACT_MANAGER_DB_NAME'\`; commit; DROP SCHEMA \`'$APPROVALS_DB_NAME'\`; commit; CREATE SCHEMA \`'$APPROVALS_DB_NAME'\`; commit;" '

  kubectl delete pod $(kubectl get pods | grep ^access-control-accesscontrol -m1 | awk '{print$1}') &
  kubectl delete pod $(kubectl get pods | grep ^user-manager-usermanager -m1 | awk '{print$1}') &
  kubectl delete pod $(kubectl get pods | grep ^arrangement-manager-arrangement-manager -m1 | awk '{print$1}') &
  kubectl delete pod $(kubectl get pods | grep ^transaction-manager-transaction-manager -m1 | awk '{print$1}') &
  kubectl delete pod $(kubectl get pods | grep ^contact-manager-contactmanager -m1 | awk '{print$1}') &
  kubectl delete pod $(kubectl get pods | grep ^approval-service-approvalservice -m1 | awk '{print$1}')

else
  echo -e "\n Existing as you choose not to proceed further"
fi
