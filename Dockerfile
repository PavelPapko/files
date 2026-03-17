# Этап 1: Сборка
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Этап 2: Запуск с Eclipse Temurin (рекомендуемый образ для Java 17)
FROM eclipse-temurin:17-jre-alpine
# Альтернатива: FROM openjdk:17-jdk-slim

WORKDIR /app

# Создание пользователя (для Alpine)
RUN addgroup -g 1001 appuser && \
    adduser -u 1001 -G appuser -s /bin/sh -D appuser

# Копирование jar
COPY --from=build /app/target/files-0.0.1-SNAPSHOT.jar app.jar

# Создание директории для загрузок
RUN mkdir -p /app/uploads && \
    chown -R appuser:appuser /app

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]