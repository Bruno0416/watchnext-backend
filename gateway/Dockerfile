FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# POMs de todos los modulos (Maven los necesita para resolver el padre y el reactor)
COPY pom.xml .
COPY common/pom.xml common/
COPY auth-service/pom.xml auth-service/
COPY content-service/pom.xml content-service/
COPY gateway/pom.xml gateway/

# Instala el POM raiz en el cache local de Maven dentro de Docker
RUN mvn -N install -q

# Descarga dependencias de gateway y sus modulos requeridos (common)
# Esta capa se cachea mientras los pom.xml no cambien
RUN mvn -pl gateway -am dependency:go-offline -q

# Copia el codigo fuente (esta capa invalida cache cuando cambias .java o resources)
COPY common/src common/src
COPY gateway/src gateway/src

# Compila y empaqueta solo gateway y sus dependencias locales
RUN mvn -pl gateway -am package -DskipTests

# --- Stage 2: imagen final liviana (solo JRE, sin Maven ni fuentes) ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/gateway/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
