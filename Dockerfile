# ====================================================================
# DOCKERFILE MULTI-STAGE: SPRING BOOT 3 (JAVA 17) + MAVEN
# Compatible con Railway, Render, Koyeb, AWS y Fly.io
# ====================================================================

# Etapa 1: Compilación del artefacto JAR
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build

# Copiar pom.xml y descargar dependencias (aprovecha cache de capas Docker)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fuente y compilar JAR ejecutable sin ejecutar tests
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final de ejecución ultraligera (Alpine Linux JRE 17)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Crear usuario sin privilegios root por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copiar únicamente el archivo JAR generado desde la etapa de compilación
COPY --from=builder /build/target/*.jar app.jar

# Variables de entorno y puerto de exposición
ENV PORT=3000
EXPOSE 3000

# Parámetros optimizados para contenedores en nube
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]

