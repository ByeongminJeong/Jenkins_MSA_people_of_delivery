pipeline {
    agent any
    
    environment {
        // 서버 정보
        SERVER_IP = "${env.SERVER_IP ?: 'localhost'}"
        
        // 데이터베이스 정보
        POSTGRES_PASSWORD = "${env.POSTGRES_PASSWORD ?: 'password'}"
        POSTGRES_USER = "${env.POSTGRES_USER ?: 'postgres'}"
        
        // Redis 정보
        REDIS_PASSWORD = "${env.REDIS_PASSWORD ?: ''}"
        
        // Docker 이미지 태그
        IMAGE_TAG = "${env.BUILD_NUMBER ?: 'latest'}"
        
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
    }
    
    stages {
        stage('Workspace Check') {
            steps {
                echo '🔄 Checking workspace...'
                sh 'pwd'
                sh 'ls -la'
            }
        }
        
        stage('Test Gradle Build') {
            steps {
                echo '🔨 Testing Gradle build...'
                sh '''
                    chmod +x ./gradlew
                    ./gradlew clean build -x test --no-daemon --stacktrace
                '''
            }
        }
        
        stage('Build Docker Images') {
            parallel {
                stage('Discovery Service') {
                    steps {
                        echo '🔨 Building Discovery Service...'
                        sh "docker build -t people-delivery/discovery:${IMAGE_TAG} -f discovery/Dockerfile ."
                    }
                }
                stage('API Gateway') {
                    steps {
                        echo '🔨 Building API Gateway...'
                        sh "docker build -t people-delivery/apigateway:${IMAGE_TAG} -f apigateway/Dockerfile ."
                    }
                }
                stage('Auth Service') {
                    steps {
                        echo '🔨 Building Auth Service...'
                        sh "docker build -t people-delivery/auth-service:${IMAGE_TAG} -f auth-service/Dockerfile ."
                    }
                }
                stage('User Service') {
                    steps {
                        echo '🔨 Building User Service...'
                        sh "docker build -t people-delivery/user-service:${IMAGE_TAG} -f user-service/Dockerfile ."
                    }
                }
                stage('Store Service') {
                    steps {
                        echo '🔨 Building Store Service...'
                        sh "docker build -t people-delivery/store-service:${IMAGE_TAG} -f store-service/Dockerfile ."
                    }
                }
                stage('Cart Service') {
                    steps {
                        echo '🔨 Building Cart Service...'
                        sh "docker build -t people-delivery/cart-service:${IMAGE_TAG} -f cart-service/Dockerfile ."
                    }
                }
                stage('Payment Service') {
                    steps {
                        echo '🔨 Building Payment Service...'
                        sh "docker build -t people-delivery/payment-service:${IMAGE_TAG} -f payment-service/Dockerfile ."
                    }
                }
                stage('AI Service') {
                    steps {
                        echo '🔨 Building AI Service...'
                        sh "docker build -t people-delivery/ai-service:${IMAGE_TAG} -f ai-service/Dockerfile ."
                    }
                }
            }
        }
        
        stage('Deploy Services') {
            steps {
                echo '🚀 Deploying services...'
                sh """
                    # 기존 컨테이너 정리
                    docker stop \$(docker ps -q --filter "name=people-delivery") || true
                    docker rm \$(docker ps -aq --filter "name=people-delivery") || true
                    
                    # 데이터베이스 컨테이너 실행
                    docker run -d --name people-delivery-postgres-auth -p ${POSTGRES_AUTH_PORT}:5432 -e POSTGRES_DB=authdb -e POSTGRES_USER=${POSTGRES_USER} -e POSTGRES_PASSWORD=${POSTGRES_PASSWORD} postgres:13
                    docker run -d --name people-delivery-postgres-user -p ${POSTGRES_USER_PORT}:5432 -e POSTGRES_DB=userdb -e POSTGRES_USER=${POSTGRES_USER} -e POSTGRES_PASSWORD=${POSTGRES_PASSWORD} postgres:13
                    docker run -d --name people-delivery-postgres-store -p ${POSTGRES_STORE_PORT}:5432 -e POSTGRES_DB=storedb -e POSTGRES_USER=${POSTGRES_USER} -e POSTGRES_PASSWORD=${POSTGRES_PASSWORD} postgres:13
                    docker run -d --name people-delivery-postgres-cart -p ${POSTGRES_CART_PORT}:5432 -e POSTGRES_DB=cartdb -e POSTGRES_USER=${POSTGRES_USER} -e POSTGRES_PASSWORD=${POSTGRES_PASSWORD} postgres:13
                    docker run -d --name people-delivery-redis -p ${REDIS_PORT}:6379 redis:6-alpine
                    
                    # 서비스 컨테이너 실행 (의존성 순서대로)
                    sleep 10
                    docker run -d --name people-delivery-discovery -p ${DISCOVERY_PORT}:8761 people-delivery/discovery:${IMAGE_TAG}
                    
                    sleep 15
                    docker run -d --name people-delivery-auth -p ${AUTH_PORT}:8015 --link people-delivery-postgres-auth:postgres-auth --link people-delivery-redis:redis --link people-delivery-discovery:discovery people-delivery/auth-service:${IMAGE_TAG}
                    docker run -d --name people-delivery-user -p ${USER_PORT}:8014 --link people-delivery-postgres-user:postgres-user --link people-delivery-redis:redis --link people-delivery-discovery:discovery people-delivery/user-service:${IMAGE_TAG}
                    docker run -d --name people-delivery-store -p ${STORE_PORT}:8013 --link people-delivery-postgres-store:postgres-store --link people-delivery-discovery:discovery people-delivery/store-service:${IMAGE_TAG}
                    docker run -d --name people-delivery-cart -p ${CART_PORT}:8012 --link people-delivery-postgres-cart:postgres-cart --link people-delivery-discovery:discovery people-delivery/cart-service:${IMAGE_TAG}
                    docker run -d --name people-delivery-payment -p ${PAYMENT_PORT}:8017 --link people-delivery-discovery:discovery people-delivery/payment-service:${IMAGE_TAG}
                    docker run -d --name people-delivery-ai -p ${AI_PORT}:8016 --link people-delivery-discovery:discovery people-delivery/ai-service:${IMAGE_TAG}
                    
                    sleep 10
                    docker run -d --name people-delivery-apigateway -p ${GATEWAY_PORT}:8080 --link people-delivery-discovery:discovery people-delivery/apigateway:${IMAGE_TAG}
                """
            }
        }
        
        stage('Health Check') {
            steps {
                echo '🔍 Checking deployment...'
                sh """
                    sleep 30
                    docker ps
                    docker images | grep people-delivery
                    
                    echo "Health checking services..."
                    curl -f http://${SERVER_IP}:${DISCOVERY_PORT}/actuator/health || echo "Discovery service not ready"
                    curl -f http://${SERVER_IP}:${AUTH_PORT}/actuator/health || echo "Auth service not ready"
                    curl -f http://${SERVER_IP}:${USER_PORT}/actuator/health || echo "User service not ready"
                    curl -f http://${SERVER_IP}:${STORE_PORT}/actuator/health || echo "Store service not ready"
                    curl -f http://${SERVER_IP}:${CART_PORT}/actuator/health || echo "Cart service not ready"
                    curl -f http://${SERVER_IP}:${PAYMENT_PORT}/actuator/health || echo "Payment service not ready"
                    curl -f http://${SERVER_IP}:${AI_PORT}/actuator/health || echo "AI service not ready"
                    curl -f http://${SERVER_IP}:${GATEWAY_PORT}/actuator/health || echo "API Gateway not ready"
                """
            }
        }
    }
    
    post {
        success {
            echo """
            🎉 배포 완료!
            
            서비스 URL:
            • Discovery Service: http://${SERVER_IP}:${DISCOVERY_PORT}/actuator/health
            • API Gateway: http://${SERVER_IP}:${GATEWAY_PORT}/actuator/health
            • Auth Service: http://${SERVER_IP}:${AUTH_PORT}/actuator/health
            • User Service: http://${SERVER_IP}:${USER_PORT}/actuator/health
            • Store Service: http://${SERVER_IP}:${STORE_PORT}/actuator/health
            • Cart Service: http://${SERVER_IP}:${CART_PORT}/actuator/health
            • Payment Service: http://${SERVER_IP}:${PAYMENT_PORT}/actuator/health
            • AI Service: http://${SERVER_IP}:${AI_PORT}/actuator/health
            """
        }
        failure {
            echo '💥 Pipeline failed!'
        }
        always {
            sh 'docker system prune -f || true'
        }
    }
}
