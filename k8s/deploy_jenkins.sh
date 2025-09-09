#!/bin/bash

set -e

echo "Jenkins Ingress 배포 시작..."

# 1. Namespace 생성
echo "Jenkins 네임스페이스 생성..."
kubectl apply -f jenkins_namespace.yaml

# 2. StorageClass 생성
echo "StorageClass 생성..."
kubectl apply -f jenkins_storageclass.yaml

# 3. PVC 생성
echo "PVC 생성..."
kubectl apply -f jenkins_pvc.yaml

# 4. Deployment 배포
echo "Jenkins Deployment 배포..."
kubectl apply -f jenkins_deployment.yaml

# 5. Service 배포 (ClusterIP)
echo "Jenkins Service 배포..."
kubectl apply -f jenkins_service.yaml

# 6. Ingress 배포
echo "Jenkins Ingress 배포..."
kubectl apply -f jenkins_ingress.yaml

# 7. 배포 상태 확인
echo "배포 상태 확인 중..."
kubectl rollout status deployment/jenkins -n jenkins --timeout=300s

# 8. Pod 상태 확인
echo "Pod 상태 확인..."
kubectl get pods -n jenkins

# 9. Service 상태 확인
echo "Service 상태 확인..."
kubectl get svc -n jenkins

# 10. Ingress 상태 확인
echo "Ingress 상태 확인..."
kubectl get ingress -n jenkins

# 11. ALB URL 확인
echo "ALB URL 확인..."
ALB_URL=$(kubectl get ingress jenkins-ingress -n jenkins -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')
if [ ! -z "$ALB_URL" ]; then
    echo "Jenkins 접속 URL: https://jenkins.goorm4.site"
    echo "ALB Hostname: $ALB_URL"
else
    echo "ALB 생성 중... 잠시 후 다시 확인하세요."
fi

# 12. 초기 비밀번호 확인
echo "Jenkins 초기 비밀번호 확인..."
echo "다음 명령어로 초기 비밀번호를 확인하세요:"
echo "kubectl exec -it \$(kubectl get pods -n jenkins -l app=jenkins -o jsonpath='{.items[0].metadata.name}') -n jenkins -- cat /var/jenkins_home/secrets/initialAdminPassword"

echo "Jenkins Ingress 배포 완료!"
