# 1단계: 빌드
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

# Gradle Wrapper 및 설정 파일 복사
COPY gradlew ./gradlew
COPY gradle ./gradle
COPY settings.gradle build.gradle ./
COPY */build.gradle ./

# 전체 소스 복사
COPY . .

# QueryDSL 빌드 + 해당 모듈 JAR 빌드
RUN ./gradlew clean compileQuerydsl :store-service:bootJar --no-daemon --build-cache

# 2단계: 실행
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/store-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
