# syntax=docker/dockerfile:1.7
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /workspace

COPY pom.xml ./
COPY platform-spring-boot-starter/pom.xml platform-spring-boot-starter/pom.xml
COPY security-service/pom.xml security-service/pom.xml
COPY backend/pom.xml backend/pom.xml
RUN mvn -B -ntp -pl security-service -am dependency:go-offline

COPY platform-spring-boot-starter/src platform-spring-boot-starter/src
COPY security-service/src security-service/src
RUN mvn -B -ntp -pl security-service -am clean package -DskipTests

FROM eclipse-temurin:25-jre-alpine

RUN apk add --no-cache curl \
    && addgroup -S app \
    && adduser -S app -G app
WORKDIR /app
COPY --from=build /workspace/security-service/target/*.jar app.jar

USER app
EXPOSE 8081

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC"
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=5 \
    CMD curl -fsS http://localhost:8081/api/v1/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
