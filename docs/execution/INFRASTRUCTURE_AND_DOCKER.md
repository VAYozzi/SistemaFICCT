# INFRASTRUCTURE_AND_DOCKER.md
# Especificación de Infraestructura, Contenedores y Entorno (`Docker`)

## 1. Propósito y Topología de Red
El sistema se despliega íntegramente mediante **Docker** y **Docker Compose**, garantizando paridad absoluta entre entornos de desarrollo y producción.

### Topología de Servicios:
```text
                          [ CLIENTE WEB / NAVEGADOR ]
                                      │
                         HTTP :80 / :3000 (Público)
                                      ▼
                        ┌───────────────────────────┐
                        │   web-client (Nginx/Vite) │
                        └─────────────┬─────────────┘
                                      │ HTTP REST + JWT (:8080)
                                      ▼
                        ┌───────────────────────────┐
                        │  core-api (Spring Boot)   │◄────┐
                        └──────┬─────────────┬──────┘     │
            PostgreSQL (:5432) │             │            │ Volumen compartido
                               ▼             │            │ /app/storage
    ┌──────────────────────────────┐         │            │ (PDFs e imágenes)
    │     db (PostgreSQL 15+)      │         │            │
    └──────────────────────────────┘         ▼            │
                                ┌─────────────────────────┴─┐
                                │ ai-worker-api (FastAPI)   │
                                │ (Red interna :8000 + Key) │
                                └───────────────────────────┘
```

---

## 2. Archivo Maestro `docker-compose.yml`

El archivo `docker-compose.yml` en la raíz del proyecto debe estructurarse de la siguiente manera:

```yaml
version: '3.8'

networks:
  ficct_network:
    driver: bridge

volumes:
  pgdata:
    driver: local
  shared_storage:
    driver: local

services:
  # -------------------------------------------------------------
  # 1. Base de Datos Relacional
  # -------------------------------------------------------------
  db:
    image: postgres:15-alpine
    container_name: ficct-sistemas-db
    restart: always
    environment:
      POSTGRES_DB: ${POSTGRES_DB:-ficct_sistemas_db}
      POSTGRES_USER: ${POSTGRES_USER:-postgres}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-postgres123}
      TZ: America/La_Paz
    ports:
      - "${POSTGRES_PORT:-5432}:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
    networks:
      - ficct_network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres -d ficct_sistemas_db"]
      interval: 5s
      timeout: 5s
      retries: 5

  # -------------------------------------------------------------
  # 2. Microservicio Especializado de IA, OCR y RPA
  # -------------------------------------------------------------
  ai-worker-api:
    build:
      context: ./ai-worker-api
      dockerfile: Dockerfile
    container_name: ficct-sistemas-ai-worker
    restart: always
    environment:
      INTERNAL_SHARED_SECRET: ${INTERNAL_SHARED_SECRET:-ficct_super_secret_internal_key_2026}
      STORAGE_BASE_PATH: /app/storage
      PYTHONUNBUFFERED: 1
    volumes:
      - shared_storage:/app/storage
      - ./ai-worker-api/app/models:/app/models
    networks:
      - ficct_network
    expose:
      - "8000" # Solo accesible dentro de la red Docker, no se mapea al host por seguridad
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8000/health"]
      interval: 10s
      timeout: 5s
      retries: 3

  # -------------------------------------------------------------
  # 3. Backend Transaccional y Orquestador
  # -------------------------------------------------------------
  core-api:
    build:
      context: ./core-api
      dockerfile: Dockerfile
    container_name: ficct-sistemas-core-api
    restart: always
    depends_on:
      db:
        condition: service_healthy
      ai-worker-api:
        condition: service_started
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/${POSTGRES_DB:-ficct_sistemas_db}
      SPRING_DATASOURCE_USERNAME: ${POSTGRES_USER:-postgres}
      SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD:-postgres123}
      JWT_SECRET: ${JWT_SECRET:-9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1a0b9c8d7e6f5a4b3c2d1e0f}
      JWT_EXPIRATION_MS: ${JWT_EXPIRATION_MS:-28800000} # 8 horas
      AI_WORKER_BASE_URL: http://ai-worker-api:8000/internal/v1
      AI_WORKER_API_KEY: ${INTERNAL_SHARED_SECRET:-ficct_super_secret_internal_key_2026}
      STORAGE_LOCAL_ROOT: /app/storage
    ports:
      - "${CORE_API_PORT:-8080}:8080"
    volumes:
      - shared_storage:/app/storage
    networks:
      - ficct_network

  # -------------------------------------------------------------
  # 4. Frontend Web (React + Vite + Nginx)
  # -------------------------------------------------------------
  web-client:
    build:
      context: ./web-client
      dockerfile: Dockerfile
      args:
        VITE_API_BASE_URL: /api/v1
    container_name: ficct-sistemas-web-client
    restart: always
    depends_on:
      - core-api
    ports:
      - "${WEB_PORT:-3000}:80"
    networks:
      - ficct_network
```

---

## 3. Dockerfiles Oficiales por Componente

### 3.1. `core-api/Dockerfile` (Multi-stage Java 17)
```dockerfile
# Etapa 1: Compilación con Maven
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Etapa 2: Imagen de Ejecución Ligera
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN apk add --no-cache tzdata curl
ENV TZ=America/La_Paz
COPY --from=builder /build/target/*.jar app.jar
RUN mkdir -p /app/storage && chown -R 1000:1000 /app
USER 1000:1000
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
```

---

### 3.2. `ai-worker-api/Dockerfile` (Python 3.10 con Dependencias C)
```dockerfile
FROM python:3.10-slim

WORKDIR /app

# Instalación de librerías nativas del sistema necesarias para OpenCV y Tesseract
RUN apt-get update && apt-get install -y --no-install-recommends \
    tesseract-ocr \
    tesseract-ocr-spa \
    libgl1 \
    libglib2.0-0 \
    libgomp1 \
    curl \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Instalar dependencias de Playwright headless
RUN playwright install --with-deps chromium

COPY app ./app
RUN mkdir -p /app/storage /app/models

ENV PYTHONUNBUFFERED=1
EXPOSE 8000

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000", "--workers", "2"]
```

---

### 3.3. `web-client/Dockerfile` (Multi-stage Vite + Nginx)
```dockerfile
# Etapa 1: Compilación de Assets
FROM node:18-alpine AS builder
WORKDIR /app
COPY package.xml* package.json package-lock.json* ./
RUN npm ci
COPY . .
ARG VITE_API_BASE_URL=/api/v1
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
RUN npm run build

# Etapa 2: Servidor Web Nginx
FROM nginx:1.25-alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

#### Archivo de Configuración `web-client/nginx.conf`:
```nginx
server {
    listen 80;
    server_name localhost;

    location / {
        root /usr/share/nginx/html;
        index index.html index.htm;
        try_files $uri $uri/ /index.html;
    }

    # Proxy inverso hacia el backend para evitar problemas de CORS
    location /api/ {
        proxy_pass http://core-api:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

---

## 4. Archivo de Variables de Entorno Central (`.env.example`)

Colocar en la raíz del proyecto como `.env`:

```env
# Configuración General del Sistema
PROJECT_NAME=ficct-sistemas-gestion
ENVIRONMENT=development

# Base de Datos PostgreSQL
POSTGRES_DB=ficct_sistemas_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres123
POSTGRES_PORT=5432

# Seguridad y Tokens
JWT_SECRET=4f7b2c8a9e0d1f3b5a7c9e1d3f5a7b9c1d3e5f7a9b1c3d5e7f9a1b3c5d7e9f1a
JWT_EXPIRATION_MS=28800000
INTERNAL_SHARED_SECRET=ficct_super_secret_internal_key_2026

# Puertos Expuestos en Host
CORE_API_PORT=8080
WEB_PORT=3000

# Azure Blob Storage (Opcional, en local se utiliza volumen compartido)
AZURE_STORAGE_CONTAINER=ficct-sistemas-docs
AZURE_STORAGE_CONNECTION_STRING=DefaultEndpointsProtocol=https;AccountName=...;AccountKey=...;EndpointSuffix=core.windows.net
```
