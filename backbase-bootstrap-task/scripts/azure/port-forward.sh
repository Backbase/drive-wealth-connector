#!/bin/sh
TC=$(ps -ef | grep 18080:8080| grep "kubectl -n backbase"| awk '{print $2}')
UM=$(ps -ef | grep 18081:8080| grep "kubectl -n backbase"| awk '{print $2}')
AC=$(ps -ef | grep 18082:8080| grep "kubectl -n backbase"| awk '{print $2}')
AM=$(ps -ef | grep 18083:8080| grep "kubectl -n backbase"| awk '{print $2}')
LM=$(ps -ef | grep 18084:8080| grep "kubectl -n backbase"| awk '{print $2}')
CM=$(ps -ef | grep 18085:8080| grep "kubectl -n backbase"| awk '{print $2}')
IN=$(ps -ef | grep 18086:8080| grep "kubectl -n backbase"| awk '{print $2}')
ID=$(ps -ef | grep 18087:8080| grep "kubectl -n backbase"| awk '{print $2}')
AP=$(ps -ef | grep 18088:8080| grep "kubectl -n backbase"| awk '{print $2}')
FS=$(ps -ef | grep 18089:8080| grep "kubectl -n backbase"| awk '{print $2}')
MS=$(ps -ef | grep 18090:8080| grep "kubectl -n backbase"| awk '{print $2}')
PF=$(ps -ef | grep 18091:8080| grep "kubectl -n backbase"| awk '{print $2}')
CS=$(ps -ef | grep 19090:8080| grep "kubectl -n backbase"| awk '{print $2}')
PI=$(ps -ef | grep 17090:8080| grep "kubectl -n backbase"| awk '{print $2}')

for portforward in $TC $UM $AC $AM $LM $CM $IN $ID $AP $FS $MS $CS $PI $PF; do
  if [ -n "$portforward" ]; then
    echo "Port forward $portforward exists, killing it now"
    kill "$portforward"
  fi
done

kubectl -n backbase port-forward "$(kubectl -n backbase get pods| grep '^oidc-token-converter' -m1 | cut -d' ' -f1)" 18080:8080 &
kubectl -n backbase port-forward "$(kubectl -n backbase get pods| grep '^user-manager' -m1 | cut -d' ' -f1)" 18081:8080 &
kubectl -n backbase port-forward "$(kubectl -n backbase get pods| grep '^access-control' -m1 | cut -d' ' -f1)" 18082:8080 &
kubectl -n backbase port-forward "$(kubectl -n backbase get pods| grep '^arrangement-manager' -m1 | cut -d' ' -f1)" 18083:8080 &
kubectl -n backbase port-forward "$(kubectl -n backbase get pods| grep '^limit' -m1 | cut -d' ' -f1)" 18084:8080 &
kubectl -n backbase port-forward "$(kubectl -n backbase get pods| grep '^contact-manager' -m1 | cut -d' ' -f1)" 18085:8080 &
kubectl -n backbase port-forward "$(kubectl -n backbase get pods| grep '^identity-integration' -m1 | cut -d' ' -f1)" 18086:8080 &
kubectl -n backbase port-forward "$(kubectl -n backbase get pods| grep '^backbase-identity' -m1 | cut -d' ' -f1)" 18087:8080 &
kubectl -n backbase port-forward "$(kubectl -n backbase get pods| grep '^approval' -m1 | cut -d' ' -f1)" 18088:8080 &
kubectl -n backbase port-forward "$(kubectl -n backbase get pods| grep '^fido-service' -m1 | cut -d' ' -f1)" 18089:8080 &
kubectl -n backbase port-forward "$(kubectl -n backbase get pods| grep '^messages-service' -m1 | cut -d' ' -f1)" 18090:8080 &
kubectl -n backbase port-forward "$(kubectl -n backbase get pods| grep '^contentservices' -m1 | cut -d' ' -f1)" 19090:8080 &
kubectl -n backbase port-forward "$(kubectl -n backbase get pods| grep '^push-integration-service' -m1 | cut -d' ' -f1)" 17090:8080 &
kubectl -n backbase port-forward "$(kubectl -n backbase get pods| grep '^portfolio' -m1 | cut -d' ' -f1)" 18091:8080
