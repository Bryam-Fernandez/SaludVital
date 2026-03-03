# Etapa de construcción
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app

# Copiar solo el pom.xml primero (para aprovechar caché de Docker)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiar el código fuente
COPY src ./src

# Construir la aplicación
RUN mvn clean package -DskipTests

# Etapa de ejecución - CORREGIDO usando eclipse-temurin
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Crear usuario no root para seguridad
RUN addgroup --system --gid 1001 spring && \
    adduser --system --uid 1001 --gid 1001 spring

# Copiar el JAR
COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

# Cambiar al usuario no root
USER spring:spring

# Exponer puerto
EXPOSE 8080

# Health check (requiere instalar curl)
RUN apt-get update && apt-get install -y curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]