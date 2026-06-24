FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# POMs de todos los modulos (Maven los necesita para resolver el padre)
COPY pom.xml .
COPY common/pom.xml common/
COPY auth-service/pom.xml auth-service/
COPY content-service/pom.xml content-service/
COPY list-service/pom.xml list-service/
COPY gateway/pom.xml gateway/
COPY eureka-server/pom.xml eureka-server/

# Instala el POM raiz en el cache local de Maven dentro de Docker
RUN --mount=type=cache,target=/root/.m2 mvn -N install -q

# cachea dependencias de eureka-server y sus deps
RUN --mount=type=cache,target=/root/.m2 mvn -pl eureka-server -am dependency:go-offline -q

# codigo fuente del modulo
COPY eureka-server/src eureka-server/src

RUN --mount=type=cache,target=/root/.m2 mvn -pl eureka-server -am package

FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache wget
WORKDIR /app
COPY --from=build /app/eureka-server/target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
