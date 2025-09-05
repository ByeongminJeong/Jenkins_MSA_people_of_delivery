pipeline {
    agent any
    
    options {
        timeout(time: 45, unit: 'MINUTES')  // 시간 증가
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
        SHORT_SHA = "${env.GIT_COMMIT?.take(7) ?: 'latest'}"
        IMAGE_TAG = "jenkins-${SHORT_SHA}"
        
        // ECR 리포지토리 접두사 (GitHub Actions와 동일)
        ECR_PREFIX = "peopleofdelivery"
        
        // Docker 빌드 최적화
        DOCKER_BUILDKIT = "1"
        COMPOSE_DOCKER_CLI_BUILD = "1"
    }
    
    stages {
        stage('Preparation') {
            steps {
                echo 'Preparing environment...'
                sh '''
                    echo "Current directory: $(pwd)"
                    echo "Git commit: ${GIT_COMMIT}"
                    echo "Short SHA: ${SHORT_SHA}"
                    echo "Build number: ${BUILD_NUMBER}"
                    echo "Image tag: ${IMAGE_TAG}"
                    echo "ECR Registry: ${ECR_REGISTRY}"
                    
                    # 시스템 리소스 확인
                    echo "=== System Resources ==="
                    free -h
                    df -h
                    
                    # 필요한 도구 확인
                    docker --version
                    aws --version
                    
                    # 기존 Docker 프로세스 정리
                    echo "=== Cleaning up existing Docker processes ==="
                    docker ps -q | xargs -r docker kill || true
                    docker system prune -f
                    
                    # 디렉토리 구조 확인
                    echo "=== Directory Structure ==="
                    ls -la
                    
                    # 각 서비스 디렉토리와 Dockerfile 확인
                    for service in auth-service user-service store-service cart-service ai-service; do
                        if [ -d "$service" ]; then
                            echo "Found $service directory"
                            if [ -f "$service/Dockerfile" ]; then
                                echo "Found $service/Dockerfile"
                            else
                                echo "Missing $service/Dockerfile"
                            fi
                        else
                            echo "Missing $service directory"
                        fi
                    done
                '''
            }
        }
        
        stage('Configure AWS & ECR Login') {
            steps {
                echo 'Using EC2 IAM role for ECR access...'
                sh '''
                    # ECR 로그인 (IAM 역할 사용)
                    echo "Logging in to ECR..."
                    aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}
                    echo "ECR login successful"
                '''
            }
        }
        
        stage('Gradle Build') {
            steps {
                echo 'Building with Gradle...'
                sh '''
                    # Gradle wrapper 실행 권한 부여
                    chmod +x ./gradlew
                    
                    # Java 버전 확인
                    java -version
                    
                    # 메모리 사용량 확인
                    echo "=== Memory before Gradle build ==="
                    free -h
                    
                    # Gradle 빌드 실행 (메모리 설정 최적화)
                    ./gradlew clean build \
                        -x test \
                        --no-daemon \
                        --stacktrace \
                        --build-cache \
                        --max-workers=2 \
                        -Dorg.gradle.jvmargs="-Xmx1536m -XX:MaxMetaspaceSize=384m -XX:+UseG1GC"
                    
                    echo "=== Build Results ==="
                    find . -name "*.jar" -path "*/build/libs/*" | head -10
                    
                    # 메모리 사용량 확인
                    echo "=== Memory after Gradle build ==="
                    free -h
                '''
                
                // JAR 파일 아카이브
                archiveArtifacts artifacts: '**/build/libs/*.jar', fingerprint: true, allowEmptyArchive: true
            }
        }
        
        // 병렬 빌드를 배치로 나누어 메모리 사용량 최적화
        stage('Build & Push Docker Images - Batch 1') {
            parallel {
                stage('Auth Service') {
                    when {
                        expression { 
                            return fileExists('auth-service/Dockerfile')
                        }
                    }
                    steps {
                        script {
                            buildAndPushToECR('auth-service')
                        }
                    }
                    post {
                        always {
                            sh '''
                                echo "Cleaning up auth-service build artifacts..."
                                docker images | grep ${ECR_REGISTRY}/${ECR_PREFIX}/auth-service | awk '{print $3}' | xargs -r docker rmi -f 2>/dev/null || true
                            '''
                        }
                    }
                }
                stage('User Service') {
                    when {
                        expression { 
                            return fileExists('user-service/Dockerfile')
                        }
                    }
                    steps {
                        script {
                            buildAndPushToECR('user-service')
                        }
                    }
                    post {
                        always {
                            sh '''
                                echo "Cleaning up user-service build artifacts..."
                                docker images | grep ${ECR_REGISTRY}/${ECR_PREFIX}/user-service | awk '{print $3}' | xargs -r docker rmi -f 2>/dev/null || true
                            '''
                        }
                    }
                }
            }
        }
        
        stage('Build & Push Docker Images - Batch 2') {
            parallel {
                stage('Store Service') {
                    when {
                        expression { 
                            return fileExists('store-service/Dockerfile')
                        }
                    }
                    steps {
                        script {
                            buildAndPushToECR('store-service')
                        }
                    }
                    post {
                        always {
                            sh '''
                                echo "Cleaning up store-service build artifacts..."
                                docker images | grep ${ECR_REGISTRY}/${ECR_PREFIX}/store-service | awk '{print $3}' | xargs -r docker rmi -f 2>/dev/null || true
                            '''
                        }
                    }
                }
                stage('Cart Service') {
                    when {
                        expression { 
                            return fileExists('cart-service/Dockerfile')
                        }
                    }
                    steps {
                        script {
                            buildAndPushToECR('cart-service')
                        }
                    }
                    post {
                        always {
                            sh '''
                                echo "Cleaning up cart-service build artifacts..."
                                docker images | grep ${ECR_REGISTRY}/${ECR_PREFIX}/cart-service | awk '{print $3}' | xargs -r docker rmi -f 2>/dev/null || true
                            '''
                        }
                    }
                }
            }
        }
        
        stage('Build & Push Docker Images - Batch 3') {
            steps {
                script {
                    if (fileExists('ai-service/Dockerfile')) {
                        buildAndPushToECR('ai-service')
                    }
                }
            }
            post {
                always {
                    sh '''
                        echo "Cleaning up ai-service build artifacts..."
                        docker images | grep ${ECR_REGISTRY}/${ECR_PREFIX}/ai-service | awk '{print $3}' | xargs -r docker rmi -f 2>/dev/null || true
                    '''
                }
            }
        }
        
        stage('Verify ECR Push') {
            steps {
                echo 'Verifying ECR push results...'
                sh '''
                    echo "=== ECR Push Verification ==="
                    
                    # 각 서비스의 최신 이미지 확인
                    for service in auth-service user-service store-service cart-service ai-service; do
                        if [ -f "${service}/Dockerfile" ]; then
                            echo "Checking ${ECR_PREFIX}/${service}:${IMAGE_TAG}"
                            aws ecr describe-images \
                                --repository-name ${ECR_PREFIX}/${service} \
                                --image-ids imageTag=${IMAGE_TAG} \
                                --region ${AWS_REGION} \
                                --query 'imageDetails[0].imagePushedAt' \
                                --output text 2>/dev/null && echo "Image verified" || echo "Image not found"
                        fi
                    done
                '''
            }
        }
    }
    
    post {
        success {
            script {
                def deploymentInfo = """
ECR 푸시 완료! Build #${BUILD_NUMBER}

업로드된 ECR 이미지들:
• Auth Service: ${ECR_REGISTRY}/${ECR_PREFIX}/auth-service:${IMAGE_TAG}
• User Service: ${ECR_REGISTRY}/${ECR_PREFIX}/user-service:${IMAGE_TAG}
• Store Service: ${ECR_REGISTRY}/${ECR_PREFIX}/store-service:${IMAGE_TAG}
• Cart Service: ${ECR_REGISTRY}/${ECR_PREFIX}/cart-service:${IMAGE_TAG}
• AI Service: ${ECR_REGISTRY}/${ECR_PREFIX}/ai-service:${IMAGE_TAG}

Tag: ${IMAGE_TAG}
ECR 리포지토리: https://console.aws.amazon.com/ecr/repositories?region=${AWS_REGION}

다음 단계: ECS/EKS에서 배포
                """
                
                echo deploymentInfo
            }
        }
        
        failure {
            echo 'Pipeline failed!'
            sh '''
                echo "=== Failure Diagnostics ==="
                echo "=== System Resources ==="
                free -h
                df -h
                echo "=== Docker Images ==="
                docker images | head -10
                echo "=== Docker Processes ==="
                docker ps -a | head -10
                echo "=== Recent Docker Logs ==="
                docker system events --since 10m --until 0s || true
                
                # 실패한 서비스 확인
                for service in auth-service user-service store-service cart-service ai-service; do
                    if [ -f "${service}/Dockerfile" ]; then
                        echo "=== ${service}/Dockerfile content ==="
                        head -10 ${service}/Dockerfile
                    fi
                done
            '''
        }
        
        always {
            sh '''
                echo "Final cleanup..."
                # 모든 ECR 관련 이미지 정리
                docker images | grep ${ECR_REGISTRY}/${ECR_PREFIX} | awk '{print $3}' | xargs -r docker rmi -f 2>/dev/null || echo "No ECR images to clean"
                # 시스템 정리
                docker system prune -f
                # 최종 리소스 확인
                echo "=== Final System Resources ==="
                free -h
                df -h
            '''
        }
    }
}

// ECR 빌드 및 푸시 함수 (메모리 최적화 버전)
def buildAndPushToECR(String serviceName) {
    echo "Building and pushing ${serviceName} to ECR..."
    
    def ecrRepo = "${ECR_REGISTRY}/${ECR_PREFIX}/${serviceName}"
    def dockerfilePath = "${serviceName}/Dockerfile"
    
    sh """
        # 빌드 전 메모리 확인
        echo "=== Memory before ${serviceName} build ==="
        free -h
        
        # Dockerfile 존재 확인
        if [ ! -f ${dockerfilePath} ]; then
            echo "Dockerfile not found at ${dockerfilePath}"
            ls -la ${serviceName}/ || echo "Directory ${serviceName}/ not found"
            exit 1
        fi
        
        echo "Found Dockerfile for ${serviceName}"
        echo "Dockerfile content preview:"
        head -5 ${dockerfilePath}
        
        # ECR 리포지토리 존재 확인 및 생성
        echo "Checking ECR repository: ${ECR_PREFIX}/${serviceName}"
        aws ecr describe-repositories \
            --repository-names ${ECR_PREFIX}/${serviceName} \
            --region ${AWS_REGION} 2>/dev/null || {
            echo "Creating ECR repository: ${ECR_PREFIX}/${serviceName}"
            aws ecr create-repository \
                --repository-name ${ECR_PREFIX}/${serviceName} \
                --region ${AWS_REGION}
        }
        
        # Docker 이미지 빌드 (메모리 효율적인 방식)
        echo "Building Docker image..."
        DOCKER_BUILDKIT=1 docker build \
            --memory=2g \
            --memory-swap=2g \
            -t ${ecrRepo}:${IMAGE_TAG} \
            -t ${ecrRepo}:latest \
            -f ${dockerfilePath} \
            .
        
        # 빌드 후 메모리 확인
        echo "=== Memory after ${serviceName} build ==="
        free -h
        
        # ECR에 푸시
        echo "Pushing to ECR..."
        docker push ${ecrRepo}:${IMAGE_TAG}
        docker push ${ecrRepo}:latest
        
        echo "Successfully pushed ${serviceName}:"
        echo "   ${ecrRepo}:${IMAGE_TAG}"
        echo "   ${ecrRepo}:latest"
        
        # 이미지 정보 확인
        docker images ${ecrRepo} --format "table {{.Repository}}:{{.Tag}}\\t{{.Size}}\\t{{.CreatedAt}}"
        
        # 로컬 이미지 즉시 정리 (메모리 절약)
        echo "Cleaning up local images for ${serviceName}..."
        docker rmi ${ecrRepo}:${IMAGE_TAG} ${ecrRepo}:latest 2>/dev/null || true
    """
}