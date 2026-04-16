# ── Stage 1: Build ────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build

COPY pom.xml .
COPY commons/pom.xml commons/
COPY ai-service/pom.xml ai-service/
RUN mvn dependency:go-offline -pl ai-service -am -q

COPY commons/src commons/src
COPY ai-service/src ai-service/src
RUN mvn clean package -pl ai-service -am -DskipTests -q

# ── Stage 2: Runtime ──────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
COPY --from=builder /build/ai-service/target/*.jar app.jar
EXPOSE 8091
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
