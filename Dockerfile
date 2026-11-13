# ====================================================================
# DOCKERFILE MULTI-STAGE: SPRING BOOT 3 (JAVA 17) + MAVEN
# Optimizado para Render, Railway, AWS y TiDB Cloud con SSL
# ====================================================================

# Etapa 1: Compilación del artefacto JAR
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build

# Copiar pom.xml y código fuente
COPY pom.xml .
COPY src ./src

# Compilar paquete JAR ejecutable sin tests
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final de ejecución ultraligera (Alpine Linux JRE 17)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Certificados SSL para validar conexión a TiDB Cloud
RUN apk --no-cache add ca-certificates curl

# Copiar el JAR generado por Maven
COPY --from=builder /build/target/proyecto-integrador-backend-1.0.0.jar app.jar

# Variables de entorno y puerto de exposición
ENV PORT=3000
EXPOSE 3000

# Parámetros optimizados para contenedores en nube con aceleración de entropía urandom y enlace de puerto dinámico
ENTRYPOINT ["sh", "-c", "java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Dserver.port=${PORT:-3000} -Dserver.address=0.0.0.0 -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
