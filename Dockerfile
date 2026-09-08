# Arun AI - Production Container Image
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy the pre-built executable jar
COPY target/arun-ai-0.0.1-SNAPSHOT.jar app.jar

# Expose default HTTP port
EXPOSE 8080

# Configure memory and performance options
ENV PORT=8080
ENV JAVA_OPTS="-Xms256m -Xmx1024m -XX:+UseG1GC"

# Launch Arun AI
ENTRYPOINT ["sh", "-c", "java  -jar app.jar"]