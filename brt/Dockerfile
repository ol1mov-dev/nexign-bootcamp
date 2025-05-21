FROM eclipse-temurin:21-jre-alpine

ARG JAR_FILE=brt-0.0.1-SNAPSHOT.jar

WORKDIR /app

COPY build/libs/${JAR_FILE} /app/app.jar

RUN apk add --no-cache curl \
 && addgroup -S spring && adduser -S spring -G spring \
 && chown -R spring:spring /app

USER spring

EXPOSE 8764

#HEALTHCHECK --interval=10s --timeout=5s --retries=5 \
#  CMD curl -f http://localhost:8764/actuator/health || exit 1

ENTRYPOINT ["java","-jar","app.jar"]