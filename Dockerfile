# Arun AI - Multi-stage Production Container Image
# Stage 1: Build Application
FROM maven:3.9.6-eclipse-temurin-21-jammy AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Production JRE 21 Runtime (Optimized for Render Free Tier)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=build /app/target/arun-ai-0.0.1-SNAPSHOT.jar app.jar

# Expose default HTTP port
EXPOSE 8080

# Configure memory limits optimized for Render Free Tier (512MB limit)
ENV PORT=8080
ENV JAVA_OPTS="-Xms128m -Xmx384m -XX:+UseSerialGC"

# Launch Arun AI
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT} -jar app.jar"]