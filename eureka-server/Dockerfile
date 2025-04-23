FROM eclipse-temurin:17.0.8_7-jre
EXPOSE 8081
WORKDIR /opt/app
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]