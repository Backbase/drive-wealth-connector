#!/bin/sh

# Print Banner
cat ../banner.txt

if ! command -v jq &>/dev/null; then
  echo "jq could not be found, check this link to install jq https://stedolan.github.io/jq/download/"
  exit
fi

DB_TO_DELETE=$@

if $DB_TO_DELETE &>/dev/null; then
  echo "Invalid input argument [ $DB_TO_DELETE ], please specify database name to delete as argument"
  exit
fi

echo "Trying to delete DB [${DB_TO_DELETE[*]}]"

kubectl config set-context --current --namespace=backbase
INSTALLATION_NAME=$(kubectl config current-context | cut -d "-" -f 2)
RUNTIME_NAME=$(kubectl config current-context | awk -F "$INSTALLATION_NAME"- '{print $2}')
IDENTITY_HOST=identity.$RUNTIME_NAME.$INSTALLATION_NAME.azure.backbaseservices.com
IDENTITY_USERNAME=$(kubectl get secret identity -o json | jq -r '.data["identity-username"]' | base64 --decode)
IDENTITY_PASSWORD=$(kubectl get secret identity -o json | jq -r '.data["identity-password"]' | base64 --decode)
KUBERNETES_CLUSTER_NAME=$(kubectl config current-context)

read -p "Wiping data for Identity $IDENTITY_HOST and Kubernetes cluster $KUBERNETES_CLUSTER_NAME, are you sure? " -n 1
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then

  for REALM_NAME in employee retail customer; do
    echo "Retrieving users of $REALM_NAME realm"

    TOKEN=$(curl \
        -d "client_id=admin-cli" \
        -d "username=$IDENTITY_USERNAME" \
        -d "password=$IDENTITY_PASSWORD" \
        -d "grant_type=password" \
        "https://$IDENTITY_HOST/auth/realms/master/protocol/openid-connect/token" --silent | cut -d ':' -f 2 | cut -d ',' -f 1 | tr -d '"')

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

  kubectl config set-context --current --namespace=backbase

  kubectl delete configmap db-cleanup-script | kubectl apply -f db-cleanup-script.yaml

  for DBS_NAME in $DB_TO_DELETE; do
    DB_ENDPOINT=$(kubectl get secret "${DBS_NAME}" -o json | jq -r '.data["db_endpoint"]' | base64 --decode)
    DB_PORT=$(kubectl get secret "${DBS_NAME}" -o json | jq -r '.data["db_port"]' | base64 --decode)
    DB_NAME=$(kubectl get secret "${DBS_NAME}" -o json | jq -r '.data["db_name"]' | base64 --decode)
    DB_USERNAME=$(kubectl get secret "${DBS_NAME}" -o json | jq -r '.data["db_username"]' | base64 --decode)
    DB_PASSWORD=$(kubectl get secret "${DBS_NAME}" -o json | jq -r '.data["db_password"]' | base64 --decode)

    echo "Going to delete DB $DB_NAME"

    kubectl logs -f mssql-script-executor

    kubectl delete pod mssql-script-executor

    cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: Pod
metadata:
  annotations:
    sidecar.istio.io/inject: "false"
  name: mssql-script-executor
  namespace: backbase
spec:
  containers:
  - args:
    - /bin/sh
    - -c
    - 'sqlcmd -S $DB_ENDPOINT,$DB_PORT -U $DB_USERNAME -P $DB_PASSWORD -d $DB_NAME -i /opt/db-clean.sql'
    image: crreference914.azurecr.io/loan-integration-outbound-service:alpine-mssql
    name: busybox
    volumeMounts:
      - mountPath: /opt/db-clean.sql
        subPath: db-clean.sql
        name: hookvolume
    securityContext:
      readOnlyRootFilesystem: true
      seccompProfile:
        type: RuntimeDefault
      allowPrivilegeEscalation: false
      capabilities:
        drop:
          - ALL
    resources:
      limits:
        cpu: 200m
        memory: 256Mi
  restartPolicy: Never
  hostNetwork: false
  volumes:
    - name: hookvolume
      configMap:
        name: db-cleanup-script
        defaultMode: 0755
EOF

    echo "Deletion of DB ${DB_NAME} done"

    sleep 5

    echo "Attempting to delete liquibase job and recreate"

    liquibase_job_name=$(kubectl get jobs | grep liquibase-"${DB_NAME}" | awk '{print $1}')
    echo liquibase job ${liquibase_job_name} is going to be deleted and recreated
    kubectl -nbackbase get job "${liquibase_job_name}" -o json | jq 'del(.spec.selector)' | jq 'del(.spec.template.metadata.labels)' >liquibase-service-job.yaml
    kubectl -nbackbase delete job "${liquibase_job_name}"
    kubectl -nbackbase apply -f liquibase-service-job.yaml
    rm liquibase-service-job.yaml
    echo "Done"

  done

else
  echo -e "\n Exiting as you chose not to proceed further"
fi
