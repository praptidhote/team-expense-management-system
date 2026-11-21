# ====================================================================
# Stage 1: Build Frontend (React 18 + Vite + Tailwind CSS)
# ====================================================================
FROM node:20-alpine AS frontend-builder
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# ====================================================================
# Stage 2: Build Backend (Java 17 + Spring Boot 3 + Embedded UI)
# ====================================================================
FROM maven:3.9.6-eclipse-temurin-17 AS backend-builder
WORKDIR /app/backend
COPY backend/pom.xml ./
RUN mvn dependency:go-offline -B
COPY backend/src ./src
# Copy compiled frontend assets into Spring Boot's static resources
COPY --from=frontend-builder /app/frontend/dist ./src/main/resources/static
RUN mvn clean package -DskipTests

# ====================================================================
# Stage 3: Lightweight Production Runtime
# ====================================================================
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Create persistent storage directories
RUN mkdir -p /app/data /app/uploads/receipts

# Copy executable jar from build stage
COPY --from=backend-builder /app/backend/target/expense-management-backend-1.0.0.jar app.jar

# Dynamic port binding (supports Render, Railway, Heroku, AWS ECS)
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
