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
        
        // 서비스 포트들 (테스트용: Auth, User만 사용)
        AUTH_PORT = "${env.AUTH_PORT ?: '8015'}"
        USER_PORT = "${env.USER_PORT ?: '8014'}"
        
        // 데이터베이스 포트들 (테스트용: Auth, User DB만 사용)
        POSTGRES_AUTH_PORT = "${env.POSTGRES_AUTH_PORT ?: '5440'}"
        POSTGRES_USER_PORT = "${env.POSTGRES_USER_PORT ?: '5436'}"
        REDIS_PORT = "${env.REDIS_PORT ?: '6379'}"
        
        // 데이터베이스 정보 (Jenkins 환경 변수에서만 가져옴)
        DB_URL = "${env.DB_URL}"
        DB_USERNAME = "${env.DB_USERNAME}"
        DB_PASSWORD = "${env.DB_PASSWORD}"
        POSTGRES_PASSWORD = "${env.POSTGRES_PASSWORD}"
        POSTGRES_USER = "${env.POSTGRES_USER}"
        
        // Redis 정보 (Jenkins 환경 변수에서만 가져옴)
        REDIS_HOST = "${env.REDIS_HOST}"
        REDIS_PASSWORD = "${env.REDIS_PASSWORD}"
        
        // JWT 설정 (Jenkins 환경 변수에서만 가져옴)
        JWT_SECRET = "${env.JWT_SECRET}"
        JWT_REFRESH_SECRET = "${env.JWT_REFRESH_SECRET}"
        
        // Google OAuth 설정 (Jenkins 환경 변수에서만 가져옴)
        GOOGLE_CLIENT_ID = "${env.GOOGLE_CLIENT_ID}"
        GOOGLE_CLIENT_SECRET_ID = "${env.GOOGLE_CLIENT_SECRET_ID}"
        
        // 이메일 설정 (Jenkins 환경 변수에서만 가져옴)
        MAIL_USERNAME = "${env.MAIL_USERNAME}"
        MAIL_PASSWORD = "${env.MAIL_PASSWORD}"
        
        // Toss 결제 설정 (Jenkins 환경 변수에서만 가져옴)
        TOSS_CLIENT = "${env.TOSS_CLIENT}"
        TOSS_SECRET = "${env.TOSS_SECRET}"
        
        // API 키들 (Jenkins 환경 변수에서만 가져옴)
        GEMINI_API_KEY = "${env.GEMINI_API_KEY}"
        WEATHER_API_KEY = "${env.WEATHER_API_KEY}"
        
        // MongoDB 설정 (Jenkins 환경 변수에서만 가져옴)
        MONGO_URI = "${env.MONGO_URI}"
        
        // AWS Cognito 설정 (Jenkins 환경 변수에서만 가져옴)
        COGNITO_USER_POOL_ID = "${env.COGNITO_USER_POOL_ID}"
        COGNITO_CLIENT_ID = "${env.COGNITO_CLIENT_ID}"
        
        // Docker Hub 설정 (Jenkins 환경 변수에서만 가져옴)
        DOCKERHUB_USERNAME = "${env.DOCKERHUB_USERNAME}"
        DOCKERHUB_TOKEN = "${env.DOCKERHUB_TOKEN}"
        
        // EC2 설정 (Jenkins 환경 변수에서만 가져옴)
        EC2_HOST = "${env.EC2_HOST}"
        EC2_USERNAME = "${env.EC2_USERNAME}"
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
                    aws --version || echo "AWS CLI not found"
                    
                    # 디렉토리 구조 확인
                    ls -la
                '''
            }
        }
        
        stage('Configure AWS & ECR Login') {
            steps {
                echo '🔐 Configuring AWS credentials and ECR login...'
                script {
                    withCredentials([
                        [
                            $class: 'AmazonWebServicesCredentialsBinding',
                            credentialsId: 'aws-credentials',
                            accessKeyVariable: 'AWS_ACCESS_KEY_ID',
                            secretKeyVariable: 'AWS_SECRET_ACCESS_KEY'
                        ]
                    ]) {
                        sh '''
                            # AWS 설정 확인
                            echo "AWS Account: $(aws sts get-caller-identity --query Account --output text)"
                            
                            # ECR 로그인
                            echo "Logging in to ECR..."
                            aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}
                            echo "✅ ECR login successful"
                        '''
                    }
                }
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
                script {
                    withCredentials([
                        [
                            $class: 'AmazonWebServicesCredentialsBinding',
                            credentialsId: 'aws-credentials',
                            accessKeyVariable: 'AWS_ACCESS_KEY_ID',
                            secretKeyVariable: 'AWS_SECRET_ACCESS_KEY'
                        ]
                    ]) {
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
        }
    }
    
    post {
        success {
            script {
                def deploymentInfo = """
🎉 ECR 푸시 완료! Build #${BUILD_NUMBER}

📦 업로드된 ECR 이미지들 (테스트용: Auth, User만):
• Auth Service: ${ECR_REGISTRY}/${ECR_PREFIX}/auth-service:${IMAGE_TAG}
• User Service: ${ECR_REGISTRY}/${ECR_PREFIX}/user-service:${IMAGE_TAG}

🚀 다음 단계: EKS에서 배포
- kubectl set image 또는
- ArgoCD/Flux GitOps 배포

🔗 ECR 리포지토리 확인:
https://console.aws.amazon.com/ecr/repositories?region=${AWS_REGION}
                """
                
                echo deploymentInfo
            }
        }
        
        failure {
            echo '💥 Pipeline failed!'
            sh '''
                echo "=== Failure Diagnostics ==="
                docker images | grep ${ECR_REGISTRY} || echo "No ECR images built locally"
                
                # AWS 연결 확인
                aws sts get-caller-identity || echo "AWS connection failed"
            '''
        }
        
        always {
            sh '''
                echo "🧹 Cleaning up local Docker images..."
                # 로컬의 ECR 이미지들만 정리
                docker images | grep ${ECR_REGISTRY}/${ECR_PREFIX} | awk '{print $3}' | xargs -r docker rmi -f 2>/dev/null || echo "No images to clean"
                docker system prune -f
            '''
        }
    }
}

// ECR 빌드 및 푸시 함수
def buildAndPushToECR(String serviceName, String dockerfilePath) {
    echo "🔨 Building and pushing ${serviceName} to ECR..."
    
    def ecrRepo = "${ECR_REGISTRY}/${ECR_PREFIX}/${serviceName}"
    
    sh """
        if [ -f ${dockerfilePath}/Dockerfile ]; then
            echo "✅ Found Dockerfile for ${serviceName}"
            
            # Docker 이미지 빌드 (환경 변수 포함)
            echo "🔨 Building ${serviceName} with environment variables..."
            docker build \
                --build-arg DB_URL="${DB_URL}" \
                --build-arg DB_USERNAME="${DB_USERNAME}" \
                --build-arg DB_PASSWORD="${DB_PASSWORD}" \
                --build-arg REDIS_HOST="${REDIS_HOST}" \
                --build-arg REDIS_PORT="${REDIS_PORT}" \
                --build-arg REDIS_PASSWORD="${REDIS_PASSWORD}" \
                --build-arg JWT_SECRET="${JWT_SECRET}" \
                --build-arg JWT_REFRESH_SECRET="${JWT_REFRESH_SECRET}" \
                --build-arg GOOGLE_CLIENT_ID="${GOOGLE_CLIENT_ID}" \
                --build-arg GOOGLE_CLIENT_SECRET_ID="${GOOGLE_CLIENT_SECRET_ID}" \
                --build-arg MAIL_USERNAME="${MAIL_USERNAME}" \
                --build-arg MAIL_PASSWORD="${MAIL_PASSWORD}" \
                --build-arg TOSS_CLIENT="${TOSS_CLIENT}" \
                --build-arg TOSS_SECRET="${TOSS_SECRET}" \
                --build-arg GEMINI_API_KEY="${GEMINI_API_KEY}" \
                --build-arg WEATHER_API_KEY="${WEATHER_API_KEY}" \
                --build-arg MONGO_URI="${MONGO_URI}" \
                --build-arg COGNITO_USER_POOL_ID="${COGNITO_USER_POOL_ID}" \
                --build-arg COGNITO_CLIENT_ID="${COGNITO_CLIENT_ID}" \
                -t ${ecrRepo}:${IMAGE_TAG} \
                -t ${ecrRepo}:latest \
                -f ${dockerfilePath}/Dockerfile \
                . || (echo "❌ Build failed for ${serviceName}" && exit 1)
            
            echo "📤 Pushing ${serviceName} to ECR..."
            
            # ECR에 푸시
            docker push ${ecrRepo}:${IMAGE_TAG} || (echo "❌ Push failed for ${serviceName}:${IMAGE_TAG}" && exit 1)
            docker push ${ecrRepo}:latest || (echo "❌ Push failed for ${serviceName}:latest" && exit 1)
            
            echo "✅ Successfully pushed ${serviceName} to ECR"
            echo "📦 Image: ${ecrRepo}:${IMAGE_TAG}"
            
        else
            echo "❌ Dockerfile not found at ${dockerfilePath}/Dockerfile"
            echo "📁 Available files:"
            ls -la ${dockerfilePath}/ || echo "Directory not found"
            exit 1
        fi
    """
}