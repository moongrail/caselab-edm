# syntax=docker/dockerfile:1.7
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /workspace

RUN apk add --no-cache unzip

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -ntp dependency:go-offline

COPY src ./src
RUN ./mvnw -B -ntp clean package -DskipTests

FROM eclipse-temurin:25-jre-alpine

RUN apk add --no-cache curl \
    && addgroup -S app \
    && adduser -S app -G app
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar

USER app
EXPOSE 8080

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC"
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=5 \
    CMD curl -fsS http://localhost:8080/api/v1/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
