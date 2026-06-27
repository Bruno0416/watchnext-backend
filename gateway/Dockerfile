FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# POMs de todos los modulos (Maven los necesita para resolver el padre y el reactor)
COPY pom.xml .
COPY common/pom.xml common/
COPY auth-service/pom.xml auth-service/
COPY content-service/pom.xml content-service/
COPY list-service/pom.xml list-service/
COPY feedback-service/pom.xml feedback-service/
COPY gateway/pom.xml gateway/
COPY eureka-server/pom.xml eureka-server/

# cachea dependencias de gateway-service y sus deps (common)
RUN mvn -pl gateway -am dependency:go-offline -q

# Copia el codigo fuente (esta capa invalida cache cuando cambias .java o resources)
COPY common/src common/src
COPY gateway/src gateway/src

RUN mvn -pl gateway -am package

# --- Stage 2: imagen final liviana (solo JRE, sin Maven ni fuentes) ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/gateway/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
