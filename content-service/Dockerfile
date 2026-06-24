FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY common/pom.xml common/
COPY auth-service/pom.xml auth-service/
COPY content-service/pom.xml content-service/
COPY list-service/pom.xml list-service/
COPY gateway/pom.xml gateway/
COPY eureka-server/pom.xml eureka-server/

# Instala el POM raiz en el cache local de Maven dentro de Docker
RUN --mount=type=cache,target=/root/.m2 mvn -N install -q

RUN --mount=type=cache,target=/root/.m2 mvn -pl content-service -am dependency:go-offline -q

COPY common/src common/src
COPY content-service/src content-service/src

RUN --mount=type=cache,target=/root/.m2 mvn -pl content-service -am package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/content-service/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
