# Use lightweight Java 17 image
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy jar file
COPY target/splito-*.jar app.jar

# Expose port (change if needed)
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]