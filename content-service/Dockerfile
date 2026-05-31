FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY common/pom.xml common/
COPY auth-service/pom.xml auth-service/
COPY content-service/pom.xml content-service/
COPY gateway/pom.xml gateway/

# Instala el POM raiz en el cache local de Maven dentro de Docker
RUN mvn -N install -q

RUN mvn -pl content-service -am dependency:go-offline -q

COPY common/src common/src
COPY content-service/src content-service/src

RUN mvn -pl content-service -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/content-service/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
