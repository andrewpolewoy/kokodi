# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace/app

COPY gradle gradle
COPY build.gradle settings.gradle gradlew gradlew.bat ./
COPY src src

RUN chmod +x gradlew
RUN ./gradlew build -x test
RUN ls -la build/libs/  # Для отладки

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/app/build/libs/kokodi-*.jar /app/kokodi.jar
ENTRYPOINT ["java", "-jar", "/app/kokodi.jar"]