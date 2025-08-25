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

# 이제 compileJava만으로 QueryDSL까지 처리됨
RUN ./gradlew clean --no-daemon
RUN ./gradlew :store-service:bootJar --no-daemon --build-cache

# 2단계: 실행
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/store-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
