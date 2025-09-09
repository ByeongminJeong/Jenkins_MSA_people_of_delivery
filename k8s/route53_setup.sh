#!/bin/bash

echo "Route53 설정 시작..."

# 1. ALB Hostname 확인
echo "ALB Hostname 확인 중..."
ALB_HOSTNAME=$(kubectl get ingress jenkins-ingress -n jenkins -o jsonpath='{.status.loadBalancer.ingress[0].hostname}')

if [ -z "$ALB_HOSTNAME" ]; then
    echo "ALB Hostname을 찾을 수 없습니다. Jenkins가 배포되었는지 확인하세요."
    exit 1
fi

echo "ALB Hostname: $ALB_HOSTNAME"

# 2. Hosted Zone ID 확인
echo "Hosted Zone ID 확인 중..."
HOSTED_ZONE_ID=$(aws route53 list-hosted-zones --query 'HostedZones[?Name==`goorm4.site.`].Id' --output text | cut -d'/' -f3)

if [ -z "$HOSTED_ZONE_ID" ]; then
    echo "Hosted Zone ID를 찾을 수 없습니다."
    exit 1
fi

echo "Hosted Zone ID: $HOSTED_ZONE_ID"

# 3. A 레코드 생성
echo "A 레코드 생성 중..."
aws route53 change-resource-record-sets --hosted-zone-id $HOSTED_ZONE_ID --change-batch '{
  "Changes": [{
    "Action": "CREATE",
    "ResourceRecordSet": {
      "Name": "jenkins.goorm4.site",
      "Type": "A",
      "AliasTarget": {
        "DNSName": "'$ALB_HOSTNAME'",
        "EvaluateTargetHealth": false,
        "HostedZoneId": "ZWKZPGTI48KDX"
      }
    }
  }]
}'

echo "Route53 설정 완료!"
echo "Jenkins 접속 URL: https://jenkins.goorm4.site"
echo "DNS 전파까지 1-2분 소요될 수 있습니다."
