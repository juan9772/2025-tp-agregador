# Importing JDK and copying required files
FROM maven:3.8.6-openjdk-18 AS build
COPY . .
run mvn clean package -DskipTests

FROM openjdk:17-jdk-slim
copy --from=build /target/my-app-name-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]

