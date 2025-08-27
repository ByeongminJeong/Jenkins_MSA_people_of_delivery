FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

COPY gradlew ./gradlew
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
COPY module-common/ ./module-common
COPY store-service/ ./store-service

# 이제 compileJava만으로 QueryDSL까지 처리됨
RUN ./gradlew clean --no-daemon
RUN ./gradlew :store-service:bootJar --no-daemon --build-cache

# 실행 단계
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/store-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
