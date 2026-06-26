FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Просто копируем уже собранный локально JAR-файл
COPY target/*.jar app.jar

ENV USE_METADATA=true
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]