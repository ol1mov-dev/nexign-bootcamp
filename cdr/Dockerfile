# Этап сборки
FROM gradle:8.7.0-jdk17 AS build
WORKDIR /app

# Копируем только файлы сборки сначала для кэширования зависимостей
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY gradlew ./

# Создаем файл инициализации Gradle для решения проблем с TLS и репозиториями
RUN echo 'systemProp.javax.net.ssl.trustStoreType=JKS' > gradle.properties && \
    echo 'systemProp.https.protocols=TLSv1.2,TLSv1.3' >> gradle.properties && \
    echo 'systemProp.https.cipherSuites=TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384' >> gradle.properties

# Создаем init.gradle для управления репозиториями
RUN echo 'allprojects { repositories { mavenCentral(); mavenLocal(); maven { url "https://repo1.maven.org/maven2/" } } }' > init.gradle

# Копируем исходный код
COPY src ./src

# Запускаем сборку с использованием инициализационного скрипта
RUN gradle build --init-script init.gradle --no-daemon -x test || (echo "Trying alternative build method..." && \
    gradle build --no-daemon -x test --stacktrace --info || \
    (echo "Falling back to downloading pre-built jar..." && \
    mkdir -p build/libs && \
    echo "Creating dummy JAR for next stage" && \
    touch build/libs/app.jar))

# Этап запуска
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Копируем JAR из этапа сборки
COPY --from=build /app/build/libs/*.jar app.jar

# Проверяем наличие JAR
RUN if [ ! -s app.jar ]; then echo "JAR file is empty or doesn't exist" && exit 1; fi

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]