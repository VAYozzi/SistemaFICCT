# Multistage build for Spring Boot 3
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Copiar Maven wrapper y pom.xml
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

# Copiar fuentes y compilar
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Etapa final ligera
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Crear usuario sin privilegios
RUN addgroup -S ficct && adduser -S ficct -G ficct

# Copiar artefacto compilado
COPY --from=build /app/target/*.jar app.jar

# Permisos para directorio de almacenamiento
RUN mkdir -p /app/storage && chown -R ficct:ficct /app

USER ficct

EXPOSE 8080

# Control estricto de memoria JVM para hardware limitado
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Xms256m", "-Xmx512m", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]

