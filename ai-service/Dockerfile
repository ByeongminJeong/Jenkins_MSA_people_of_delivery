# 1단계: 빌드
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
COPY */build.gradle ./
COPY . .

RUN ./gradlew :ai-service:bootJar --no-daemon --build-cache

# 2단계: 실행
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/ai-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
