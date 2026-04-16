# ── Stage 1: Build ────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build

COPY pom.xml .
COPY commons/pom.xml commons/
COPY action-engine/pom.xml action-engine/
RUN mvn dependency:go-offline -pl action-engine -am -q

COPY commons/src commons/src
COPY action-engine/src action-engine/src
RUN mvn clean package -pl action-engine -am -DskipTests -q

# ── Stage 2: Runtime ──────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
COPY --from=builder /build/action-engine/target/*.jar app.jar
EXPOSE 8092
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
