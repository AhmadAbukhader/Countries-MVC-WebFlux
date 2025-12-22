# Multi-stage build for Spring Boot application
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml and download dependencies (this layer will be cached if pom.xml doesn't change)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage - Using JDK instead of JRE for better native library support
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Create a non-root user for security (Debian uses different commands)
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the application port
EXPOSE 8089

# Run the application with native library path
ENTRYPOINT ["java", "-jar", "app.jar"]