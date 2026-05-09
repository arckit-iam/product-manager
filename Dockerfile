# ── Build stage ─────────────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jdk-noble AS build

WORKDIR /build
COPY pom.xml .
COPY src ./src

RUN apt-get update -qq && apt-get install -y -qq maven \
 && mvn package -q -DskipTests \
 && mv target/product-manager-*.jar target/product-manager.jar

# ── Runtime stage ────────────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jre-noble

RUN useradd -r appuser
USER appuser

WORKDIR /app
COPY --from=build /build/target/product-manager.jar product-manager.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "product-manager.jar"]