# TESTING_STRATEGY.md
# Estrategia Integral de Pruebas de Software (Testing Strategy)

## 1. Pirámide de Pruebas del Sistema

La estrategia de aseguramiento de calidad (QA) implementa una **Pirámide de Pruebas Automatizadas**:

```text
               / \
              /   \
             / E2E \           <-- Playwright (Flujos completos de usuario)
            /───────\
           / Integr. \         <-- MockMvc + Testcontainers PostgreSQL / AsyncClient
          /───────────\
         /  Unitarias  \       <-- JUnit 5 + Mockito / Pytest / Vitest
        /───────────────\
```

---

## 2. Niveles de Prueba y Tecnologías Oficiales

### 2.1. Nivel 1: Pruebas Unitarias
* **Backend Java (`core-api`):**
  * **Frameworks:** JUnit 5 + Mockito + AssertJ.
  * **Alcance:** Validación de lógica de negocio en la capa `service`, cálculo de plazos de trámites, transiciones de la State Machine y formateo de DTOs.
* **Microservicio Python (`ai-worker-api`):**
  * **Frameworks:** `pytest` + `pytest-asyncio`.
  * **Alcance:** Funciones de OpenCV (deskewing, umbrales de Otsu), preprocesamiento de características para ML y cálculo del F1-Score.
* **Frontend React (`web-client`):**
  * **Frameworks:** `vitest` + `@testing-library/react`.
  * **Alcance:** Renderizado de componentes UI, validación de esquemas Zod en formularios y stores de Zustand.

---

### 2.2. Nivel 2: Pruebas de Integración y Persistencia Real
* **Testcontainers con PostgreSQL:**
  * Para evitar falsos positivos con bases de datos en memoria (como H2), las pruebas de integración en Spring Boot levantan un contenedor Docker real de PostgreSQL 15 mediante **Testcontainers**:
  ```java
  @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
  @Testcontainers
  public abstract class BaseIntegrationTest {
      @Container
      static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
          .withDatabaseName("test_db")
          .withUsername("test")
          .withPassword("test");
  }
  ```
  * **Alcance:** Validación de migraciones Flyway, triggers de texto completo `tsvector`, índices únicos parciales e intercepción AOP de auditoría.
* **Pruebas de API REST (MockMvc):**
  * Pruebas de endpoints verificando inyección de tokens JWT, códigos de respuesta HTTP (`200`, `201`, `400`, `401`, `403`, `409`) y envoltura estándar `ApiResponse<T>`.

---

### 2.3. Nivel 3: Pruebas End-to-End (E2E) con Playwright
* **Framework:** `playwright` (Chromium).
* **Flujos Críticos Probados de Extremo a Extremo:**
  1. Login de Auxiliar $\rightarrow$ Creación de expediente en ventanilla $\rightarrow$ Carga de PDF $\rightarrow$ Procesamiento y validación en Visor Dual $\rightarrow$ Pase a Coordinación.
  2. Login de Coordinador $\rightarrow$ Marcado de requisitos conformes $\rightarrow$ Emisión de dictamen técnico.
  3. Login de Director $\rightarrow$ Aprobación de expediente con dictamen $\rightarrow$ Verificación de resolución final en la interfaz.
  4. Simulación de Machine Learning $\rightarrow$ Detección de alerta roja $\rightarrow$ Registro de medida directiva.

---

## 3. Matriz de Comandos de Ejecución

```bash
# 1. Pruebas Backend Java
cd core-api
./mvnw clean test

# 2. Pruebas Microservicio Python
cd ai-worker-api
pytest -v

# 3. Pruebas Unitarias Frontend React
cd web-client
npm run test

# 4. Pruebas E2E completas
npm run test:e2e
```
