FROM maven:3.8.7-amazoncorretto-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:resolve

COPY src ./src
RUN mvn clean package -DskipTests

FROM amazoncorretto:17-alpine-jdk
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]