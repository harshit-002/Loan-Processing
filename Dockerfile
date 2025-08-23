# Use a base image with Java 21
FROM eclipse-temurin:21-jdk-alpine
# Set working directory in container
WORKDIR /app

# Copy the Maven build output (jar file) into container
COPY target/*.jar app.jar

# Expose the port your Spring Boot app runs on
EXPOSE 8080

# Run the Spring Boot app
ENTRYPOINT ["java","-jar","app.jar"]
