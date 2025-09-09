#!/bin/bash

echo "Jenkins 리소스 삭제 시작..."

# Service 삭제
kubectl delete -f jenkins_service.yaml

# Deployment 삭제
kubectl delete -f jenkins_deployment.yaml

# PVC 삭제 (데이터도 함께 삭제됨)
kubectl delete -f jenkins_pvc.yaml

# StorageClass 삭제
kubectl delete -f jenkins_storageclass.yaml

# Namespace 삭제
kubectl delete -f jenkins_namespace.yaml

echo "Jenkins 리소스 삭제 완료!"
