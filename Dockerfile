# --- Stage 1: Build ---
# Use a Maven and JDK 21 image as the builder
FROM maven:3.9-eclipse-temurin-21 AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven wrapper and pom.xml to download dependencies first
# This leverages Docker's layer caching.
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Download all dependencies
RUN ./mvnw dependency:go-offline

# Copy the rest of your application's source code
COPY src/ ./src

# Build the application. We skip tests here for faster builds.
# For production, you might want to run tests here as well.
RUN ./mvnw package -DskipTests

# --- Stage 2: Run ---
# Use a lightweight JRE image for the final container
FROM eclipse-temurin:21-jre-jammy

# Set the working directory
WORKDIR /app

# Copy the built .jar file from the 'builder' stage
# The jar file will be in the 'target' directory
COPY --from=builder /app/target/*.jar app.jar

# Expose the port your application runs on
EXPOSE 8080

# The command to run your application
# The application.properties file will automatically
# pick up the environment variables passed to the container.
ENTRYPOINT ["java", "-jar", "app.jar"]