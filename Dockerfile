# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Copiar arquivos de configuração do Maven
COPY pom.xml .

# Copiar código-fonte
COPY src/ src/

# Build da aplicação
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Instalar curl para health checks
RUN apk add --no-cache curl

# Copiar JAR da aplicação do stage anterior
COPY --from=builder /build/target/todolist-0.0.1-SNAPSHOT.jar app.jar

# Metadados
LABEL maintainer="aghiot321" \
      description="Aplicação ToDoList - Gerenciador de tarefas com Spring Boot" \
      version="1.0.0"

# Expor porta
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Comando de inicialização
ENTRYPOINT ["java", "-jar", "app.jar"]
