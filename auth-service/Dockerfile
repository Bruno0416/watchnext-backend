FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# POMs de todos los modulos (Maven los necesita para resolver el padre)
COPY pom.xml .
COPY common/pom.xml common/
COPY auth-service/pom.xml auth-service/
COPY content-service/pom.xml content-service/
COPY list-service/pom.xml list-service/
COPY feedback-service/pom.xml feedback-service/
COPY gateway/pom.xml gateway/
COPY eureka-server/pom.xml eureka-server/

# cachea dependencias de auth-service y sus deps (common)
RUN mvn -pl auth-service -am dependency:go-offline -q

# codigo fuente de los modulos necesarios
COPY common/src common/src
COPY auth-service/src auth-service/src

RUN mvn -pl auth-service -am package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/auth-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
