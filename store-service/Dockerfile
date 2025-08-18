# 1단계: 빌드
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

# Gradle wrapper & 설정 먼저 복사 (캐시 최적화)
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY store-service/build.gradle store-service/build.gradle
COPY module-common/build.gradle module-common/build.gradle

RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon || return 0

# 소스 복사
COPY . .

# QueryDSL 생성 + JAR 빌드
RUN ./gradlew :store-service:clean :store-service:compileQuerydsl :store-service:bootJar --no-daemon --build-cache

# 2단계: 런타임
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/store-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
