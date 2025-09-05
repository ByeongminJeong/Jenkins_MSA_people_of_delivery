pipeline {
    agent any
    
    options {
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        skipStagesAfterUnstable()
    }
    
    environment {
        // AWS 설정
        AWS_REGION = 'ap-northeast-2'
        AWS_ACCOUNT_ID = '061039771693'
        ECR_REGISTRY = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
        
        // Docker 이미지 태그
        IMAGE_TAG = "jenkins-${env.BUILD_NUMBER ?: 'latest'}"
        
        // ECR 리포지토리 접두사 (기존과 맞춤)
        ECR_PREFIX = "peopleofdelivery"
    }
    
    stages {
        stage('Preparation') {
            steps {
                echo '🔄 Preparing environment...'
                sh '''
                    echo "Current directory: $(pwd)"
                    echo "Git commit: ${GIT_COMMIT}"
                    echo "Build number: ${BUILD_NUMBER}"
                    echo "Image tag: ${IMAGE_TAG}"
                    echo "ECR Registry: ${ECR_REGISTRY}"
                    
                    # 필요한 도구 확인
                    docker --version
                    aws --version
                    
                    # 디렉토리 구조 확인
                    ls -la
                '''
            }
        }
        
        stage('Configure AWS & ECR Login') {
            steps {
                echo '🔐 Using EC2 IAM role for ECR access...'
                sh '''
                    # ECR 로그인 (IAM 역할 사용)
                    echo "Logging in to ECR..."
                    aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}
                    echo "✅ ECR login successful"
                '''
            }
        }
        
        stage('Gradle Build') {
            steps {
                echo '🔨 Building with Gradle...'
                sh '''
                    # Gradle wrapper 실행 권한 부여
                    chmod +x ./gradlew
                    
                    # Java 버전 확인
                    java -version
                    
                    # Gradle 빌드 실행 (테스트 제외)
                    ./gradlew clean build \
                        -x test \
                        --no-daemon \
                        --stacktrace \
                        --build-cache \
                        --parallel \
                        -Dorg.gradle.jvmargs="-Xmx2048m -XX:MaxMetaspaceSize=512m"
                    
                    echo "=== Build Results ==="
                    find . -name "*.jar" -path "*/build/libs/*" | head -10
                '''
                
                // JAR 파일 아카이브
                archiveArtifacts artifacts: '**/build/libs/*.jar', fingerprint: true, allowEmptyArchive: true
            }
        }
        
        stage('Build & Push Docker Images') {
            parallel {
                stage('Auth Service') {
                    steps {
                        script {
                            buildAndPushToECR('auth-service', 'auth-service')
                        }
                    }
                }
                stage('User Service') {
                    steps {
                        script {
                            buildAndPushToECR('user-service', 'user-service')
                        }
                    }
                }
            }
        }
        
        stage('Verify ECR Push') {
            steps {
                echo '🔍 Verifying ECR push results...'
                sh '''
                    echo "=== ECR Push Verification ==="
                    
                    # 각 서비스의 최신 이미지 확인
                    for service in auth-service user-service; do
                        echo "Checking ${ECR_PREFIX}/${service}:${IMAGE_TAG}"
                        aws ecr describe-images \
                            --repository-name ${ECR_PREFIX}/${service} \
                            --image-ids imageTag=${IMAGE_TAG} \
                            --region ${AWS_REGION} \
                            --query 'imageDetails[0].imagePushedAt' \
                            --output text 2>/dev/null || echo "❌ Image not found"
                    done
                '''
            }
        }
    }
    
    post {
        success {
            script {
                def deploymentInfo = """
🎉 ECR 푸시 완료! Build #${BUILD_NUMBER}

📦 업로드된 ECR 이미지들:
• Auth Service: ${ECR_REGISTRY}/${ECR_PREFIX}/auth-service:${IMAGE_TAG}
• User Service: ${ECR_REGISTRY}/${ECR_PREFIX}/user-service:${IMAGE_TAG}

🚀 다음 단계: EKS에서 배포
🔗 ECR 리포지토리: https://console.aws.amazon.com/ecr/repositories?region=${AWS_REGION}
                """
                
                echo deploymentInfo
            }
        }
        
        failure {
            echo '💥 Pipeline failed!'
            sh '''
                echo "=== Failure Diagnostics ==="
                docker images | grep ${ECR_REGISTRY} || echo "No ECR images built locally"
            '''
        }
        
        always {
            sh '''
                echo "🧹 Cleaning up local Docker images..."
                docker images | grep ${ECR_REGISTRY}/${ECR_PREFIX} | awk '{print $3}' | xargs -r docker rmi -f 2>/dev/null || echo "No images to clean"
                docker system prune -f
            '''
        }
    }
}

// ECR 빌드 및 푸시 함수 (간단 버전)
def buildAndPushToECR(String serviceName, String dockerfilePath) {
    echo "🔨 Building and pushing ${serviceName} to ECR..."
    
    def ecrRepo = "${ECR_REGISTRY}/${ECR_PREFIX}/${serviceName}"
    
    sh """
        if [ -f ${dockerfilePath}/Dockerfile ]; then
            echo "✅ Found Dockerfile for ${serviceName}"
            
            # Docker 이미지 빌드
            docker build -t ${ecrRepo}:${IMAGE_TAG} -t ${ecrRepo}:latest -f ${dockerfilePath}/Dockerfile .
            
            # ECR에 푸시
            docker push ${ecrRepo}:${IMAGE_TAG}
            docker push ${ecrRepo}:latest
            
            echo "✅ Successfully pushed ${serviceName} to ECR"
            
        else
            echo "❌ Dockerfile not found at ${dockerfilePath}/Dockerfile"
            ls -la ${dockerfilePath}/ || echo "Directory not found"
            exit 1
        fi
    """
}