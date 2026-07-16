FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# POMs de todos los modulos (Maven los necesita para resolver el padre)
COPY pom.xml .
COPY common/pom.xml common/
COPY auth-service/pom.xml auth-service/
COPY content-service/pom.xml content-service/
COPY list-service/pom.xml list-service/
COPY feedback-service/pom.xml feedback-service/
COPY user-service/pom.xml user-service/
COPY email-service/pom.xml email-service/
COPY gateway/pom.xml gateway/
COPY eureka-server/pom.xml eureka-server/
COPY search-service/pom.xml search-service/

COPY settings.xml /root/.m2/settings.xml

# cachea dependencias de list-service y sus deps (common)
RUN mvn -pl list-service -am dependency:go-offline -q

# codigo fuente de los modulos necesarios
COPY common/src common/src
COPY list-service/src list-service/src

RUN mvn -pl list-service -am package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/list-service/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
