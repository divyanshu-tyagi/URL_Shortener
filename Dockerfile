# ── Stage 1: Build ──────────────────────────────────────────────────
FROM gradle:8.14-jdk21 AS builder
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src
RUN gradle bootJar --no-daemon -x test

# ── Stage 2: Run ────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /app/build/libs/url-shortener.jar app.jar

EXPOSE 8085
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]