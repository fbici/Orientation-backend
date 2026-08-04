# Stage 1: Build Angular frontend
FROM node:20-alpine AS frontend
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
WORKDIR /app
COPY --from=backend /app/target/*.jar app.jar
EXPOSE 8080
CMD java -jar app.jar --spring.profiles.active=prod --server.port=${PORT:-8080}
