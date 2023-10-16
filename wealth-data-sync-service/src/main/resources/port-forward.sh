#!/bin/sh
TC=$(ps -ef | grep 18080:8080| grep "kubectl -n backbase"| awk '{print $2}')
UM=$(ps -ef | grep 18081:8080| grep "kubectl -n backbase"| awk '{print $2}')
PI=$(ps -ef | grep 18082:8080| grep "kubectl -n backbase"| awk '{print $2}')
AM=$(ps -ef | grep 18083:8080| grep "kubectl -n backbase"| awk '{print $2}')
AC=$(ps -ef | grep 18084:8080| grep "kubectl -n backbase"| awk '{print $2}')

for portforward in $TC $UM $PI $AM $AM $AC
    do
        if [ -n "$portforward" ]; then
            echo "Port forward $portforward exist, killing it now"
            kill "$portforward"
        fi
    done
kubectl -n backbase port-forward "$(kubectl -n backbase get pods| grep '^oidc-token-converter' -m1 | cut -d' ' -f1)" 18080:8080 &
kubectl -n backbase port-forward "$(kubectl -n backbase get pods| grep '^user-manager' -m1 | cut -d' ' -f1)" 18081:8080 &
kubectl -n backbase port-forward "$(kubectl -n backbase get pods| grep '^portfolio' -m1 | cut -d' ' -f1)" 18082:8080 &
kubectl -n backbase port-forward "$(kubectl -n backbase get pods| grep '^arrangement-manager' -m1 | cut -d' ' -f1)" 18083:8080 &
kubectl -n backbase port-forward "$(kubectl -n backbase get pods| grep '^access-control' -m1 | cut -d' ' -f1)" 18084:8080