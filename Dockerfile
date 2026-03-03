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

# Etapa de ejecución - USANDO ECLIPSE-TEMURIN (CORREGIDO)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copiar el JAR
COPY --from=build /app/target/*.jar app.jar

# Exponer puerto
EXPOSE 8080

# Ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]