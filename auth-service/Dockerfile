# 1단계: 빌드
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

# 루트 gradle 환경만 복사
COPY gradlew ./
COPY gradle ./gradle
COPY settings.gradle build.gradle ./

# auth-service 및 공통 모듈만 복사
COPY module-common ./module-common
COPY auth-service ./auth-service

RUN ./gradlew :auth-service:bootJar --no-daemon --build-cache

# 2단계: 실행
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/auth-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
