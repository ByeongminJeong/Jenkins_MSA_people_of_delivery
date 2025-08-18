# 1단계: 빌드
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

# Gradle 캐시 최적화
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
COPY */build.gradle ./

RUN ./gradlew dependencies --no-daemon || return 0

# 소스 복사 후 빌드
COPY . .

# store-service는 Querydsl 필요
RUN ./gradlew clean compileQuerydsl :store-service:bootJar --no-daemon --build-cache

# 2단계: 실행
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/store-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
