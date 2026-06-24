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

# cachea dependencias de auth-service y sus deps (common)
RUN --mount=type=cache,target=/root/.m2 mvn -pl list-service -am dependency:go-offline -q

# codigo fuente de los modulos necesarios
COPY common/src common/src
COPY list-service/src list-service/src

RUN --mount=type=cache,target=/root/.m2 mvn -pl list-service -am package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/list-service/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
