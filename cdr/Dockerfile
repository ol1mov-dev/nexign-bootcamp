# Используем официальный базовый образ с Java 17 JDK
FROM openjdk:17-jdk-slim

# Устанавливаем рабочую директорию внутри контейнера
WORKDIR /app

# Копируем собранный JAR-файл внутрь контейнера
COPY build/libs/*.jar app.jar

# Указываем команду запуска приложения
ENTRYPOINT ["java", "-jar", "app.jar"]
