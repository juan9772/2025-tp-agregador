# Stage 1: Build the application with JDK 21
# Importing JDK and copying required files
FROM maven:3.9.4-eclipse-temurin-21 AS build
# Updated to use an available Maven/OpenJDK 21 image
WORKDIR /app
# Set working directory inside the container
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Create the final Docker image using OpenJDK 21
FROM openjdk:21-jdk
VOLUME /tmp

# Copy the JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Debug: Print environment variables and then run the application
ENTRYPOINT ["java","-jar","/app.jar"]

EXPOSE 8083

