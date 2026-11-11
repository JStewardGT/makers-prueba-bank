# Etapa 1: Compilación del artefacto
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copia de descriptor y descarga de dependencias
COPY pom.xml ./
RUN mvn dependency:go-offline -B || true

# Copia de código fuente y compilación
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Entorno de ejecución ligero
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Crear usuario sin privilegios por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build --chown=appuser:appgroup /app/target/loan-system-1.0.0.jar app.jar

ENV PORT=8081
EXPOSE 8081

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
