#!/bin/sh

if ! command -v jq &>/dev/null; then
  echo "jq could not be found, check this link to install jq https://stedolan.github.io/jq/download/"
  exit
fi

kubectl config set-context --current --namespace=backbase
DB_HOST=$(kubectl get secret database -o json | jq -r '.data["mysql-endpoint"]' | base64 --decode)
KUBERNETES_CLUSTER_NAME=$(kubectl config current-context | awk -F '/' '{print$2}')

read -p "Wiping data for Approvals on Kubernetes cluster $KUBERNETES_CLUSTER_NAME, Are you sure? " -n 1 -r
if [[ $REPLY =~ ^[Yy]$ ]]; then

  DB_USERNAME=$(kubectl get secret database -o json | jq -r '.data["mysql-username"]' | base64 --decode)
  DB_PASSWORD=$(kubectl get secret database -o json | jq -r '.data["mysql-password"]' | base64 --decode)

  kubectl run mysql --attach=true --rm --image=alpine -- /bin/sh -c \
  'apk add mysql-client; mysql -u'$DB_USERNAME' -h '$DB_HOST' -p'$DB_PASSWORD' -Bse "DELETE FROM \`dbs-approvals\`.\`appr_policy_assignment\`; DELETE FROM \`dbs-approvals\`.\`appr_policy_item\`; DELETE FROM \`dbs-approvals\`.\`appr_policy_logical_item\`; DELETE FROM \`dbs-approvals\`.\`appr_policy\`; DELETE FROM \`dbs-approvals\`.\`appr_approval_type_assignment\`; DELETE FROM \`dbs-approvals\`.\`appr_record\`; DELETE FROM \`dbs-approvals\`.\`appr_approval_type\`; commit;" '

else
  echo -e "\n Existing as you choose not to proceed further"
fi