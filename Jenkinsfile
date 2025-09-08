pipeline {
    agent any
    
    options {
        timeout(time: 60, unit: 'MINUTES')
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '5'))
        skipStagesAfterUnstable()
    }
    
    environment {
        AWS_REGION = 'ap-northeast-2'
        AWS_ACCOUNT_ID = '061039771693'
        ECR_REGISTRY = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
        SHORT_SHA = "${env.GIT_COMMIT?.take(7) ?: 'latest'}"
        IMAGE_TAG = "jenkins-${SHORT_SHA}"
        ECR_PREFIX = "peopleofdelivery"
        DOCKER_BUILDKIT = "1"
    }
    
    stages {
        stage('Preparation') {
            steps {
                echo 'Preparing environment...'
                sh '''
                    echo "=== System Resources ==="
                    free -h
                    df -h
                    echo "=== Docker Cleanup ==="
                    docker system prune -f
                '''
            }
        }
        
        stage('AWS & ECR Login') {
            steps {
                sh 'aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}'
            }
        }
        
        stage('Gradle Build') {
            steps {
                sh '''
                    chmod +x ./gradlew
                    # 메모리 사용량을 극도로 제한
                    ./gradlew clean build \
                        -x test \
                        --no-daemon \
                        --max-workers=1 \
                        -Dorg.gradle.jvmargs="-Xmx512m -XX:MaxMetaspaceSize=128m"
                    
                    echo "=== Memory after build ==="
                    free -h
                '''
            }
        }
        
        // 한 번에 하나씩만 빌드 (순차 실행)
        stage('Build Auth Service') {
            steps {
                script {
                    if (fileExists('auth-service/Dockerfile')) {
                        buildSingleService('auth-service')
                    }
                }
            }
        }
        
        stage('Build User Service') {
            steps {
                script {
                    if (fileExists('user-service/Dockerfile')) {
                        buildSingleService('user-service')
                    }
                }
            }
        }
        
        stage('Build Store Service') {
            steps {
                script {
                    if (fileExists('store-service/Dockerfile')) {
                        buildSingleService('store-service')
                    }
                }
            }
        }
        
        stage('Build Cart Service') {
            steps {
                script {
                    if (fileExists('cart-service/Dockerfile')) {
                        buildSingleService('cart-service')
                    }
                }
            }
        }
        
        stage('Build AI Service') {
            steps {
                script {
                    if (fileExists('ai-service/Dockerfile')) {
                        buildSingleService('ai-service')
                    }
                }
            }
        }
        
        stage('Build Payment Service') {
            steps {
                script {
                    if (fileExists('payment-service/Dockerfile')) {
                        buildSingleService('payment-service')
                    }
                }
            }
        }
    }
    
    post {
        always {
            sh '''
                echo "Cleaning up..."
                docker system prune -f
                echo "=== Final Memory Status ==="
                free -h
            '''
        }
    }
}

def buildSingleService(String serviceName) {
    echo "Building ${serviceName}..."
    sh """
        echo "=== Memory before ${serviceName} ==="
        free -h
        
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
        
        # 메모리 제한을 두고 빌드
        docker build \
            --memory=1g \
            --memory-swap=1g \
            -t ${ECR_REGISTRY}/${ECR_PREFIX}/${serviceName}:${IMAGE_TAG} \
            -t ${ECR_REGISTRY}/${ECR_PREFIX}/${serviceName}:latest \
            -f ${serviceName}/Dockerfile \
            .
        
        # ECR 푸시
        echo "Pushing ${serviceName} to ECR..."
        docker push ${ECR_REGISTRY}/${ECR_PREFIX}/${serviceName}:${IMAGE_TAG}
        docker push ${ECR_REGISTRY}/${ECR_PREFIX}/${serviceName}:latest
        
        # 즉시 정리
        docker rmi ${ECR_REGISTRY}/${ECR_PREFIX}/${serviceName}:${IMAGE_TAG} ${ECR_REGISTRY}/${ECR_PREFIX}/${serviceName}:latest || true
        
        echo "Successfully built and pushed ${serviceName}"
        echo "=== Memory after ${serviceName} ==="
        free -h
    """
}