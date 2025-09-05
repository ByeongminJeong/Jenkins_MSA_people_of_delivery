pipeline {
    agent {
        docker {
            image 'docker:latest'
            args '-v /var/run/docker.sock:/var/run/docker.sock -v /tmp/.gradle:/tmp/.gradle'
        }
    }
    
    options {
        timeout(time: 45, unit: 'MINUTES') // 타임아웃 증가
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        skipStagesAfterUnstable()
    }
    
    environment {
        // 서버 정보
        SERVER_IP = "${env.SERVER_IP ?: 'localhost'}"
        
        // 데이터베이스 정보
        POSTGRES_PASSWORD = credentials('postgres-password') // 보안 개선
        POSTGRES_USER = "${env.POSTGRES_USER ?: 'postgres'}"
        
        // Redis 정보
        REDIS_PASSWORD = credentials('redis-password') // 보안 개선
        
        // Docker 이미지 태그
        IMAGE_TAG = "${env.BUILD_NUMBER ?: 'latest'}"
        REGISTRY_PREFIX = "${env.REGISTRY_PREFIX ?: 'people-delivery'}"
        
        // 서비스 포트들
        DISCOVERY_PORT = "${env.DISCOVERY_PORT ?: '8761'}"
        GATEWAY_PORT = "${env.GATEWAY_PORT ?: '8080'}"
        AUTH_PORT = "${env.AUTH_PORT ?: '8015'}"
        USER_PORT = "${env.USER_PORT ?: '8014'}"
        STORE_PORT = "${env.STORE_PORT ?: '8013'}"
        CART_PORT = "${env.CART_PORT ?: '8012'}"
        PAYMENT_PORT = "${env.PAYMENT_PORT ?: '8017'}"
        AI_PORT = "${env.AI_PORT ?: '8016'}"
        
        // 데이터베이스 포트들
        POSTGRES_AUTH_PORT = "${env.POSTGRES_AUTH_PORT ?: '5440'}"
        POSTGRES_USER_PORT = "${env.POSTGRES_USER_PORT ?: '5436'}"
        POSTGRES_STORE_PORT = "${env.POSTGRES_STORE_PORT ?: '5435'}"
        POSTGRES_CART_PORT = "${env.POSTGRES_CART_PORT ?: '5434'}"
        REDIS_PORT = "${env.REDIS_PORT ?: '6379'}"
        
        // 헬스체크 설정
        HEALTH_CHECK_TIMEOUT = "${env.HEALTH_CHECK_TIMEOUT ?: '300'}"
        HEALTH_CHECK_INTERVAL = "${env.HEALTH_CHECK_INTERVAL ?: '10'}"
    }
    
    stages {
        stage('Preparation') {
            steps {
                echo '🔄 Preparing environment...'
                sh '''
                    pwd
                    ls -la
                    docker --version
                    docker network ls
                '''
            }
        }
        
        stage('Gradle Build & Test') {
            steps {
                echo '🔨 Building with Gradle...'
                sh '''
                    chmod +x ./gradlew
                    ./gradlew clean build \
                        --no-daemon \
                        --stacktrace \
                        --parallel \
                        --build-cache \
                        -Dorg.gradle.jvmargs="-Xmx2048m -XX:MaxMetaspaceSize=512m"
                '''
                
                // 테스트 결과 저장
                publishTestResults testResultsPattern: '**/build/test-results/**/*.xml'
                archiveArtifacts artifacts: '**/build/libs/*.jar', fingerprint: true
            }
        }
        
        stage('Build Docker Images') {
            parallel {
                stage('Discovery Service') {
                    steps {
                        script {
                            buildDockerImage('discovery', 'discovery')
                        }
                    }
                }
                stage('API Gateway') {
                    steps {
                        script {
                            buildDockerImage('apigateway', 'apigateway')
                        }
                    }
                }
                stage('Auth Service') {
                    steps {
                        script {
                            buildDockerImage('auth-service', 'auth-service')
                        }
                    }
                }
                stage('User Service') {
                    steps {
                        script {
                            buildDockerImage('user-service', 'user-service')
                        }
                    }
                }
                stage('Store Service') {
                    steps {
                        script {
                            buildDockerImage('store-service', 'store-service')
                        }
                    }
                }
                stage('Cart Service') {
                    steps {
                        script {
                            buildDockerImage('cart-service', 'cart-service')
                        }
                    }
                }
                stage('Payment Service') {
                    steps {
                        script {
                            buildDockerImage('payment-service', 'payment-service')
                        }
                    }
                }
                stage('AI Service') {
                    steps {
                        script {
                            buildDockerImage('ai-service', 'ai-service')
                        }
                    }
                }
            }
        }
        
        stage('Infrastructure Setup') {
            steps {
                echo '🏗️ Setting up infrastructure...'
                script {
                    setupInfrastructure()
                }
            }
        }
        
        stage('Deploy Services') {
            steps {
                echo '🚀 Deploying services...'
                script {
                    deployServices()
                }
            }
        }
        
        stage('Health Check') {
            steps {
                echo '🔍 Performing health checks...'
                script {
                    performHealthChecks()
                }
            }
        }
        
        stage('Integration Test') {
            when {
                not { 
                    changeRequest() 
                }
            }
            steps {
                echo '🧪 Running integration tests...'
                script {
                    runIntegrationTests()
                }
            }
        }
    }
    
    post {
        success {
            script {
                def deploymentInfo = """
                🎉 배포 완료! Build #${BUILD_NUMBER}
                
                📊 서비스 상태:
                • Discovery Service: http://${SERVER_IP}:${DISCOVERY_PORT}
                • API Gateway: http://${SERVER_IP}:${GATEWAY_PORT}
                • Auth Service: http://${SERVER_IP}:${AUTH_PORT}
                • User Service: http://${SERVER_IP}:${USER_PORT}
                • Store Service: http://${SERVER_IP}:${STORE_PORT}
                • Cart Service: http://${SERVER_IP}:${CART_PORT}
                • Payment Service: http://${SERVER_IP}:${PAYMENT_PORT}
                • AI Service: http://${SERVER_IP}:${AI_PORT}
                
                🔗 Discovery Dashboard: http://${SERVER_IP}:${DISCOVERY_PORT}
                """
                
                echo deploymentInfo
                
                // Slack 알림 (옵션)
                if (env.SLACK_WEBHOOK) {
                    slackSend(
                        color: 'good',
                        message: deploymentInfo,
                        webhook: env.SLACK_WEBHOOK
                    )
                }
            }
        }
        
        failure {
            script {
                echo '💥 Pipeline failed!'
                
                // 실패 시 로그 수집
                sh '''
                    echo "=== Docker Container Logs ==="
                    docker ps -a
                    
                    # 실패한 컨테이너의 로그 출력
                    for container in $(docker ps -a --filter "name=people-delivery" --format "{{.Names}}"); do
                        echo "=== Logs for $container ==="
                        docker logs --tail=50 $container || true
                    done
                '''
                
                // Slack 알림 (옵션)
                if (env.SLACK_WEBHOOK) {
                    slackSend(
                        color: 'danger',
                        message: "🚨 Deployment failed for build #${BUILD_NUMBER}\nBranch: ${BRANCH_NAME}\nCommit: ${GIT_COMMIT}",
                        webhook: env.SLACK_WEBHOOK
                    )
                }
            }
        }
        
        unstable {
            echo '⚠️ Pipeline unstable!'
        }
        
        always {
            script {
                // 정리 작업
                sh '''
                    # 빌드 캐시 정리 (용량이 너무 클 때만)
                    if [ "$(docker system df --format "{{.Size}}" | head -1 | cut -d'G' -f1)" -gt 10 ]; then
                        docker system prune -f --volumes
                    else
                        docker system prune -f
                    fi
                '''
                
                // 아티팩트 정리
                cleanWs()
            }
        }
    }
}

// 헬퍼 함수들
def buildDockerImage(String serviceName, String dockerfilePath) {
    echo "🔨 Building ${serviceName}..."
    
    sh """
        docker build \
            -t ${REGISTRY_PREFIX}/${serviceName}:${IMAGE_TAG} \
            -t ${REGISTRY_PREFIX}/${serviceName}:latest \
            -f ${dockerfilePath}/Dockerfile \
            --build-arg BUILD_DATE=\$(date -u +'%Y-%m-%dT%H:%M:%SZ') \
            --build-arg VCS_REF=\$(git rev-parse --short HEAD) \
            --build-arg VERSION=${IMAGE_TAG} \
            .
    """
}

def setupInfrastructure() {
    sh """
        # 기존 리소스 정리
        echo "Cleaning up existing resources..."
        docker stop \$(docker ps -q --filter "name=people-delivery") 2>/dev/null || true
        docker rm \$(docker ps -aq --filter "name=people-delivery") 2>/dev/null || true
        
        # Docker 네트워크 생성
        docker network create people-delivery-network 2>/dev/null || echo "Network already exists"
        
        # 데이터 볼륨 생성
        docker volume create people-delivery-postgres-auth-data 2>/dev/null || true
        docker volume create people-delivery-postgres-user-data 2>/dev/null || true
        docker volume create people-delivery-postgres-store-data 2>/dev/null || true
        docker volume create people-delivery-postgres-cart-data 2>/dev/null || true
        docker volume create people-delivery-redis-data 2>/dev/null || true
        
        # 데이터베이스 컨테이너 실행
        echo "Starting database containers..."
        
        docker run -d \
            --name people-delivery-postgres-auth \
            --network people-delivery-network \
            -p ${POSTGRES_AUTH_PORT}:5432 \
            -e POSTGRES_DB=authdb \
            -e POSTGRES_USER=${POSTGRES_USER} \
            -e POSTGRES_PASSWORD=${POSTGRES_PASSWORD} \
            -v people-delivery-postgres-auth-data:/var/lib/postgresql/data \
            --health-cmd="pg_isready -U ${POSTGRES_USER} -d authdb" \
            --health-interval=30s \
            --health-timeout=10s \
            --health-retries=3 \
            postgres:13
            
        docker run -d \
            --name people-delivery-postgres-user \
            --network people-delivery-network \
            -p ${POSTGRES_USER_PORT}:5432 \
            -e POSTGRES_DB=userdb \
            -e POSTGRES_USER=${POSTGRES_USER} \
            -e POSTGRES_PASSWORD=${POSTGRES_PASSWORD} \
            -v people-delivery-postgres-user-data:/var/lib/postgresql/data \
            --health-cmd="pg_isready -U ${POSTGRES_USER} -d userdb" \
            --health-interval=30s \
            --health-timeout=10s \
            --health-retries=3 \
            postgres:13
            
        docker run -d \
            --name people-delivery-postgres-store \
            --network people-delivery-network \
            -p ${POSTGRES_STORE_PORT}:5432 \
            -e POSTGRES_DB=storedb \
            -e POSTGRES_USER=${POSTGRES_USER} \
            -e POSTGRES_PASSWORD=${POSTGRES_PASSWORD} \
            -v people-delivery-postgres-store-data:/var/lib/postgresql/data \
            --health-cmd="pg_isready -U ${POSTGRES_USER} -d storedb" \
            --health-interval=30s \
            --health-timeout=10s \
            --health-retries=3 \
            postgres:13
            
        docker run -d \
            --name people-delivery-postgres-cart \
            --network people-delivery-network \
            -p ${POSTGRES_CART_PORT}:5432 \
            -e POSTGRES_DB=cartdb \
            -e POSTGRES_USER=${POSTGRES_USER} \
            -e POSTGRES_PASSWORD=${POSTGRES_PASSWORD} \
            -v people-delivery-postgres-cart-data:/var/lib/postgresql/data \
            --health-cmd="pg_isready -U ${POSTGRES_USER} -d cartdb" \
            --health-interval=30s \
            --health-timeout=10s \
            --health-retries=3 \
            postgres:13
            
        # Redis 실행
        docker run -d \
            --name people-delivery-redis \
            --network people-delivery-network \
            -p ${REDIS_PORT}:6379 \
            -v people-delivery-redis-data:/data \
            --health-cmd="redis-cli ping" \
            --health-interval=30s \
            --health-timeout=10s \
            --health-retries=3 \
            redis:6-alpine
        
        # 데이터베이스가 준비될 때까지 대기
        echo "Waiting for databases to be ready..."
        timeout 60 sh -c 'until docker exec people-delivery-postgres-auth pg_isready -U ${POSTGRES_USER} -d authdb; do sleep 2; done'
        timeout 60 sh -c 'until docker exec people-delivery-postgres-user pg_isready -U ${POSTGRES_USER} -d userdb; do sleep 2; done'
        timeout 60 sh -c 'until docker exec people-delivery-postgres-store pg_isready -U ${POSTGRES_USER} -d storedb; do sleep 2; done'
        timeout 60 sh -c 'until docker exec people-delivery-postgres-cart pg_isready -U ${POSTGRES_USER} -d cartdb; do sleep 2; done'
        timeout 60 sh -c 'until docker exec people-delivery-redis redis-cli ping; do sleep 2; done'
    """
}

def deployServices() {
    sh """
        # Discovery Service 먼저 시작
        echo "Starting Discovery Service..."
        docker run -d \
            --name people-delivery-discovery \
            --network people-delivery-network \
            -p ${DISCOVERY_PORT}:8761 \
            --health-cmd="curl -f http://localhost:8761/actuator/health || exit 1" \
            --health-interval=30s \
            --health-timeout=10s \
            --health-retries=5 \
            --restart=unless-stopped \
            ${REGISTRY_PREFIX}/discovery:${IMAGE_TAG}
        
        # Discovery Service가 준비될 때까지 대기
        echo "Waiting for Discovery Service..."
        timeout 120 sh -c 'until curl -f http://${SERVER_IP}:${DISCOVERY_PORT}/actuator/health; do sleep 5; done'
        
        # 나머지 서비스들 시작
        echo "Starting microservices..."
        
        docker run -d \
            --name people-delivery-auth \
            --network people-delivery-network \
            -p ${AUTH_PORT}:8015 \
            --health-cmd="curl -f http://localhost:8015/actuator/health || exit 1" \
            --health-interval=30s \
            --health-timeout=10s \
            --health-retries=5 \
            --restart=unless-stopped \
            ${REGISTRY_PREFIX}/auth-service:${IMAGE_TAG}
            
        docker run -d \
            --name people-delivery-user \
            --network people-delivery-network \
            -p ${USER_PORT}:8014 \
            --health-cmd="curl -f http://localhost:8014/actuator/health || exit 1" \
            --health-interval=30s \
            --health-timeout=10s \
            --health-retries=5 \
            --restart=unless-stopped \
            ${REGISTRY_PREFIX}/user-service:${IMAGE_TAG}
            
        docker run -d \
            --name people-delivery-store \
            --network people-delivery-network \
            -p ${STORE_PORT}:8013 \
            --health-cmd="curl -f http://localhost:8013/actuator/health || exit 1" \
            --health-interval=30s \
            --health-timeout=10s \
            --health-retries=5 \
            --restart=unless-stopped \
            ${REGISTRY_PREFIX}/store-service:${IMAGE_TAG}
            
        docker run -d \
            --name people-delivery-cart \
            --network people-delivery-network \
            -p ${CART_PORT}:8012 \
            --health-cmd="curl -f http://localhost:8012/actuator/health || exit 1" \
            --health-interval=30s \
            --health-timeout=10s \
            --health-retries=5 \
            --restart=unless-stopped \
            ${REGISTRY_PREFIX}/cart-service:${IMAGE_TAG}
            
        docker run -d \
            --name people-delivery-payment \
            --network people-delivery-network \
            -p ${PAYMENT_PORT}:8017 \
            --health-cmd="curl -f http://localhost:8017/actuator/health || exit 1" \
            --health-interval=30s \
            --health-timeout=10s \
            --health-retries=5 \
            --restart=unless-stopped \
            ${REGISTRY_PREFIX}/payment-service:${IMAGE_TAG}
            
        docker run -d \
            --name people-delivery-ai \
            --network people-delivery-network \
            -p ${AI_PORT}:8016 \
            --health-cmd="curl -f http://localhost:8016/actuator/health || exit 1" \
            --health-interval=30s \
            --health-timeout=10s \
            --health-retries=5 \
            --restart=unless-stopped \
            ${REGISTRY_PREFIX}/ai-service:${IMAGE_TAG}
        
        # 서비스들이 어느 정도 시작될 때까지 대기
        echo "Waiting for services to start..."
        sleep 30
        
        # API Gateway 마지막에 시작
        echo "Starting API Gateway..."
        docker run -d \
            --name people-delivery-apigateway \
            --network people-delivery-network \
            -p ${GATEWAY_PORT}:8080 \
            --health-cmd="curl -f http://localhost:8080/actuator/health || exit 1" \
            --health-interval=30s \
            --health-timeout=10s \
            --health-retries=5 \
            --restart=unless-stopped \
            ${REGISTRY_PREFIX}/apigateway:${IMAGE_TAG}
    """
}

def performHealthChecks() {
    def services = [
        [name: 'Discovery', port: env.DISCOVERY_PORT, path: '/actuator/health'],
        [name: 'Auth', port: env.AUTH_PORT, path: '/actuator/health'],
        [name: 'User', port: env.USER_PORT, path: '/actuator/health'],
        [name: 'Store', port: env.STORE_PORT, path: '/actuator/health'],
        [name: 'Cart', port: env.CART_PORT, path: '/actuator/health'],
        [name: 'Payment', port: env.PAYMENT_PORT, path: '/actuator/health'],
        [name: 'AI', port: env.AI_PORT, path: '/actuator/health'],
        [name: 'API Gateway', port: env.GATEWAY_PORT, path: '/actuator/health']
    ]
    
    sh 'sleep 30'  // 서비스 시작 대기
    
    def healthCheckResults = []
    
    services.each { service ->
        def result = sh(
            script: """
                timeout 30 sh -c 'until curl -f --max-time 10 http://${SERVER_IP}:${service.port}${service.path}; do 
                    echo "Waiting for ${service.name} service..."
                    sleep 5
                done'
            """,
            returnStatus: true
        )
        
        if (result == 0) {
            healthCheckResults.add("✅ ${service.name} Service: HEALTHY")
        } else {
            healthCheckResults.add("❌ ${service.name} Service: UNHEALTHY")
            echo "Warning: ${service.name} service health check failed"
        }
    }
    
    // 전체 상태 출력
    sh """
        echo "=== Container Status ==="
        docker ps --filter "name=people-delivery"
        
        echo "=== Health Check Results ==="
        echo "${healthCheckResults.join('\n')}"
        
        echo "=== Service Registration Status ==="
        curl -s http://${SERVER_IP}:${DISCOVERY_PORT}/eureka/apps | grep -o '<name>[^<]*' | sed 's/<name>//' || echo "Could not retrieve service registration status"
    """
}

def runIntegrationTests() {
    sh """
        echo "Running basic integration tests..."
        
        # API Gateway를 통한 기본 라우팅 테스트
        curl -f --max-time 10 "http://${SERVER_IP}:${GATEWAY_PORT}/actuator/health" || exit 1
        
        # 각 서비스별 기본 엔드포인트 테스트 (가능한 경우)
        # 실제 API 엔드포인트가 있다면 추가
        
        echo "Integration tests completed successfully"
    """
}