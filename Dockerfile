FROM node:24-alpine AS frontend-builder
WORKDIR /app/frontend

COPY frontend/package*.json ./
RUN npm install

COPY frontend/ ./
RUN npm run build

FROM maven:3.9.11-eclipse-temurin-17 AS backend-builder
WORKDIR /app

COPY backend/pom.xml backend/pom.xml
RUN mvn -f backend/pom.xml dependency:go-offline

COPY backend/ backend/
COPY --from=frontend-builder /app/frontend/dist/ backend/src/main/resources/static/
RUN mvn -f backend/pom.xml -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=prod
ENV PORT=8080

COPY --from=backend-builder /app/backend/target/health-balance-backend-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -jar /app/app.jar --server.port=${PORT}"]
