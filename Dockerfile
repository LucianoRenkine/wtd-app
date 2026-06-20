FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY backend/wtd-app/gradle gradle
COPY backend/wtd-app/gradlew .
COPY backend/wtd-app/build.gradle .
COPY backend/wtd-app/settings.gradle .
COPY backend/wtd-app/src src
RUN chmod +x ./gradlew && ./gradlew build -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/wtd-app-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
