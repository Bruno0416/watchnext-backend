FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/*.jar app.jar

# Exponemos el puerto
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
