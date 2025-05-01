# Build stage
FROM gradle:8.7.0-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle build --no-daemon -x test

# Production stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]