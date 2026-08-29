# Use lightweight JRE base image
FROM eclipse-temurin:21-jre-alpine
# Set working directory inside container
WORKDIR /app
# Copy the built JAR into the container
COPY target/user-service-*.jar app.jar
# Expose the application port
EXPOSE 8081
# Run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]