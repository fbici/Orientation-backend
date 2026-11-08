# Stage 1: Build Angular frontend
FROM node:22-alpine AS frontend
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npx ng build --configuration=production

# Stage 2: Build Spring Boot backend
FROM maven:3.9.9-eclipse-temurin-21 AS backend
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
# Copy Angular build output to Spring Boot static resources
COPY --from=frontend /app/frontend/dist/orientation-backoffice/browser/ ./src/main/resources/static/
RUN mvn clean package -DskipTests -B

# Stage 3: Run
FROM eclipse-temurin:21-jre
RUN apt-get update \
    && apt-get install -y --no-install-recommends tesseract-ocr tesseract-ocr-eng tesseract-ocr-fra fonts-dejavu-core curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --gid 10001 orientia \
    && useradd --uid 10001 --gid orientia --no-create-home --shell /usr/sbin/nologin orientia \
    && mkdir -p /app/uploads /app/logs \
    && chown -R orientia:orientia /app \
    && chmod 700 /app/uploads /app/logs
WORKDIR /app
ENV STORAGE_DIR=/app/uploads
COPY --from=backend /app/target/*.jar app.jar
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-Djava.awt.headless=true", "-jar", "app.jar", "--spring.profiles.active=prod"]
