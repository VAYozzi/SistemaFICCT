# ARCHITECTURE_DECISION_RECORDS.md
# Registro de Decisiones de Arquitectura (ADRs)

Este documento registra formalmente las decisiones de arquitectura de software adoptadas para el **Sistema Web de Gestión Administrativa con Alertas Predictivas y OCR para la Dirección de Carrera de Ingeniería en Sistemas (FICCT - UAGRM)**.

---

## Índice de Decisiones (ADRs)

* [ADR-001: Topología en Estrella y Desacoplamiento de Servicios](#adr-001-topología-en-estrella-y-desacoplamiento-de-servicios)
* [ADR-002: Ejecución de Tareas de CPU en Hilos de Fondo en FastAPI](#adr-002-ejecución-de-tareas-de-cpu-en-hilos-de-fondo-en-fastapi)
* [ADR-003: Inmutabilidad de la Bitácora de Auditoría vía Spring AOP](#adr-003-inmutabilidad-de-la-bitácora-de-auditoría-vía-spring-aop)
* [ADR-004: Adopción de Java 17 Records para DTOs y Cero Exposición de Entidades](#adr-004-adopción-de-java-17-records-para-dtos-y-cero-exposición-de-entidades)
* [ADR-005: Búsqueda Full-Text Nativa con tsvector en PostgreSQL vs. Elasticsearch](#adr-005-búsqueda-full-text-nativa-con-tsvector-en-postgresql-vs-elasticsearch)
* [ADR-006: Estrategia de Almacenamiento Binario: SAS URLs de Azure Blob y Volumen Docker](#adr-006-estrategia-de-almacenamiento-binario-sas-urls-de-azure-blob-y-volumen-docker)
* [ADR-007: Algoritmo Random Forest Classifier para Alertas de Cupos con Umbral F1 >= 0.80](#adr-007-algoritmo-random-forest-classifier-para-alertas-de-cupos-con-umbral-f1--080)
* [ADR-008: Gestión de Estado Dual en Frontend: TanStack Query + Zustand](#adr-008-gestión-de-estado-dual-en-frontend-tanstack-query--zustand)
* [ADR-009: Automatización RPA Desatendida con Playwright Headless](#adr-009-automatización-rpa-desatendida-con-playwright-headless)

---

### ADR-001: Topología en Estrella y Desacoplamiento de Servicios
* **Estado:** Aceptado.
* **Contexto:** Se requería definir el esquema de comunicación entre el frontend web, el backend transaccional y el microservicio de IA/OCR.
* **Decisión:** Implementar una topología en estrella donde `web-client` **solo se comunica con `core-api`**. `ai-worker-api` se aísla dentro de la red interna de Docker y solo responde llamadas de Spring Boot aseguradas con `X-Internal-Api-Key`.
* **Consecuencias:** Máxima seguridad y trazabilidad. Se evita exponer endpoints de cómputo intensivo al exterior y se centraliza la auditoría en Spring Boot.

---

### ADR-002: Ejecución de Tareas de CPU en Hilos de Fondo en FastAPI
* **Estado:** Aceptado.
* **Contexto:** OpenCV, PyTesseract y Scikit-Learn consumen intensamente la CPU. En Python, si estas librerías corren dentro del `event loop` asíncrono de FastAPI, congelan todas las peticiones entrantes.
* **Decisión:** Todas las llamadas a motores de visión artificial o ML deben delegarse obligatoriamente al threadpool mediante `asyncio.to_thread(...)`.
* **Consecuencias:** Uvicorn mantiene su bucle de eventos siempre disponible para recibir peticiones concurrentes sin bloqueos.

---

### ADR-003: Inmutabilidad de la Bitácora de Auditoría vía Spring AOP
* **Estado:** Aceptado.
* **Contexto:** Las normativas institucionales y forenses exigen que toda acción administrativa sea auditable e inalterable.
* **Decisión:** Implementar una anotación `@Auditable` procesada por Spring AOP (`@Aspect`). Se prohíbe explícitamente cualquier método de `UPDATE` o `DELETE` sobre la tabla `bitacora_auditoria`. La persistencia se ejecuta con `Propagation.REQUIRES_NEW`.
* **Consecuencias:** Trazabilidad inmutable garantizada. Ni siquiera un usuario con rol Director puede alterar el registro histórico.

---

### ADR-004: Adopción de Java 17 Records para DTOs y Cero Exposición de Entidades
* **Estado:** Aceptado.
* **Contexto:** Exponer entidades JPA directamente en controladores REST acopla la base de datos con la API y genera problemas de serialización recursiva con Jackson.
* **Decisión:** Los controladores solo reciben y devuelven DTOs modelados exclusivamente con `record` de Java 17, empleando MapStruct para el mapeo.
* **Consecuencias:** Inmutabilidad estricta, código limpio sin boilerplate y blindaje de la capa de persistencia.

---

### ADR-005: Búsqueda Full-Text Nativa con tsvector en PostgreSQL vs. Elasticsearch
* **Estado:** Aceptado.
* **Contexto:** El sistema requiere búsqueda avanzada de texto completo sobre los documentos validados por OCR.
* **Decisión:** Utilizar el motor nativo de búsqueda de texto completo de PostgreSQL (`tsvector`, `to_tsquery('spanish', ...)` con índices `GIN`) en lugar de introducir un cluster de Elasticsearch.
* **Consecuencias:** Simplificación drástica de la infraestructura Docker, menor consumo de memoria RAM y consistencia transaccional inmediata con las tablas de trámites.

---

### ADR-006: Estrategia de Almacenamiento Binario: SAS URLs de Azure Blob y Volumen Docker
* **Estado:** Aceptado.
* **Contexto:** La base de datos relacional no debe saturarse almacenando archivos binarios pesados (PDFs e imágenes escaneadas).
* **Decisión:** Almacenar binarios en Azure Blob Storage (o volumen compartido `/app/storage` en desarrollo local). La base de datos almacena únicamente la ruta, metadatos y el hash `SHA-256`. La visualización en frontend utiliza SAS URLs temporales de lectura (15 minutos).
* **Consecuencias:** Alta eficiencia, escalabilidad de almacenamiento y reducción del tráfico de red interna.

---

### ADR-007: Algoritmo Random Forest Classifier para Alertas de Cupos con Umbral F1 >= 0.80
* **Estado:** Aceptado.
* **Contexto:** La predicción de sobredemanda de cupos combina múltiples variables históricas no lineales (demanda insatisfecha, reprobación y prerrequisitos).
* **Decisión:** Emplear `RandomForestClassifier` de Scikit-Learn como modelo oficial. El modelo solo puede marcarse como activo para producción si alcanza un **$F1\text{-Score} \ge 0.80$**.
* **Consecuencias:** Se evitan falsos positivos en alertas de déficit de cupos y se garantiza que solo modelos matemáticamente rigurosos entren en producción.

---

### ADR-008: Gestión de Estado Dual en Frontend: TanStack Query + Zustand
* **Estado:** Aceptado.
* **Contexto:** Redux genera boilerplate excesivo y mezclar caché del servidor con estado de la interfaz provoca inconsistencias de datos.
* **Decisión:** Separar responsabilidades:
  * `@tanstack/react-query` para fetching, caché reactiva e invalidación de mutaciones.
  * `zustand` para el estado global del cliente (sesión JWT, rol activo y controles del Visor Dual).
* **Consecuencias:** Código frontend ligero, mantenible y reactivo sin estados desincronizados.

---

### ADR-009: Automatización RPA Desatendida con Playwright Headless
* **Estado:** Aceptado.
* **Contexto:** Se requiere consolidar correspondencia y generar reportes oficiales en PDF con formato facultativo idéntico al emitido manualmente.
* **Decisión:** Emplear `playwright` (Chromium headless) junto con `WeasyPrint` / `ReportLab`.
* **Consecuencias:** Automatización desatendida robusta con soporte para reintentos y generación de PDFs con fidelidad visual exacta.
