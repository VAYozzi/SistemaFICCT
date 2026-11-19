# IMPLEMENTATION_TRACKER.md
# Bitácora de Implementación y Seguimiento del Agente de IA

> **Instrucción para el Agente de IA:**  
> Este archivo es el **tablero de control y memoria persistente** del proyecto. Antes de iniciar cualquier tarea, debes revisar este archivo para saber el estado actual. Al finalizar cada sesión o sprint, **debes actualizar obligatoriamente** los estados de las tareas (`[ ]` $\rightarrow$ `[/]` $\rightarrow$ `[X]`), registrar los archivos creados/modificados en la bitácora y documentar cualquier decisión técnica en la sección de ADRs.

---

## 1. Estado Global del Proyecto

* **Fase Actual:** `FASE 0: Planificación y Especificación de Requisitos (SDD) COMPLETADA ✅`
* **Progreso Estimado:** `100% de Especificaciones Técnicas Listas para Ejecución`
* **Última Actualización:** 2026-11-18
* **Próxima Acción:** Iniciar Fase 1 (Infraestructura y Docker Compose) siguiendo `execution/AGENT_ORCHESTRATION_GUIDE.md`.

---

## 2. Checklist Maestro de Implementación por Fases

### Fase 0: Arquitectura y Especificaciones (SDD)
- [X] `00_PROJECT_CONSTITUTION.md` (Reglas globales y guardarraíles)
- [X] `01_DOMAIN_AND_DATABASE.md` (Modelo relacional DDL PostgreSQL 15+)
- [X] `02_API_CONTRACTS.md` (Contratos RESTful externos e internos)
- [X] `03_REQUIREMENTS_SPECIFICATION.md` (Catálogo IEEE 830 RF-01 al RF-26 y RNF)
- [X] `04_BUSINESS_RULES.md` (Reglas inmutables de negocio RN-01 al RN-16)
- [X] `05_USER_ROLES_AND_PERMISSIONS.md` (Matriz RBAC por endpoints y vistas)
- [X] `06_USE_CASES_AND_WORKFLOWS.md` (Mapeo CU-01 al CU-37 y diagramas de secuencia)
- [X] `USER_GUIDE_SPEC.md` (Especificación del Manual de Usuario por Roles)
- [X] `architecture/` (`ARCHITECTURE_SPRING_BOOT.md`, `ARCHITECTURE_FASTAPI.md`, `ARCHITECTURE_REACT_TS.md`, `ARCHITECTURE_OVERVIEW.md`)
- [X] `design/` (`UI_UX_GUIDELINES.md`, `NAVIGATION_AND_LAYOUT.md`, `COMPONENTS_AND_DESIGN_SYSTEM.md`)
- [X] `security/` (`SECURITY_POLICIES.md`, `AUDIT_AND_LOGGING.md`, `DATA_PROTECTION.md`)
- [X] `integrations/` (`EXTERNAL_SERVICES.md`, `INTERNAL_COMMUNICATION.md`)
- [X] `ai/` (`OCR_PIPELINE_SPEC.md`, `ML_MODEL_SPECIFICATION.md`, `DATASET_AND_TRAINING.md`, `MODEL_EVALUATION.md`)
- [X] `execution/` (`INFRASTRUCTURE_AND_DOCKER.md`, `SEED_DATA_AND_FIXTURES.md`, `IMPLEMENTATION_TRACKER.md`, `IMPLEMENTATION_ROADMAP.md`, `DEPENDENCY_MATRIX.md`, `CODING_STANDARDS.md`, `VERSIONING_AND_MIGRATIONS.md`, `ACCEPTANCE_CRITERIA.md`, `TESTING_STRATEGY.md`, `TESTING_AND_QA_LOG.md`, `AGENT_ORCHESTRATION_GUIDE.md`)
- [X] `modules/` (`MOD_01` al `MOD_07`)
- [X] `decisions/` (`ARCHITECTURE_DECISION_RECORDS.md`)

---

### Fase 1: Infraestructura Base y Entorno de Desarrollo
- [ ] Configurar repositorio y estructura de carpetas (`/core-api`, `/ai-worker-api`, `/web-client`).
- [ ] Crear `docker-compose.yml` con servicios `db`, `core-api`, `ai-worker-api` y `web-client`.
- [ ] Configurar `Dockerfile` multietapa para `core-api` (Java 17).
- [ ] Configurar `Dockerfile` para `ai-worker-api` (Python 3.10 + Tesseract spa + OpenCV headless).
- [ ] Configurar `Dockerfile` para `web-client` (Vite + Nginx).
- [ ] Crear archivos `.env.example` para cada servicio.
- [ ] Levantar entorno completo con `docker compose up --build` y verificar conectividad.

---

### Fase 2: Módulo 1 — Seguridad, Autenticación y Bitácora (`MOD_01`)
- [ ] **Backend (`core-api`):**
  - [ ] Migración Flyway `V1__init_security_and_audit.sql` (tablas `roles`, `usuarios`, `invitaciones`, `bitacora_auditoria`).
  - [ ] Implementar Spring Security 6 + Filtro JWT stateless.
  - [ ] Endpoints `/api/v1/auth/login` y `/api/v1/auth/perfil`.
  - [ ] CRUD de usuarios e invitaciones con tokens temporales UUID (`ROLE_DIRECTOR`).
  - [ ] Caso Crítico: Endpoint `@Transactional` de Transferencia de Autoridad de Dirección.
  - [ ] Aspecto Spring AOP `@Auditable` para alimentar `bitacora_auditoria` inmutable.
  - [ ] Endpoint de consulta filtrada de bitácora (`ROLE_DIRECTOR`).
- [ ] **Frontend (`web-client`):**
  - [ ] Pantalla de Login institucional con formulario Zod y mutación React Query.
  - [ ] `useAuthStore` con Zustand para persistir sesión y rol activo.
  - [ ] `RoleGuard` y `DashboardLayout` con menú lateral dinámico según rol.
  - [ ] Vista de gestión de usuarios y modal para invitar nuevo funcionario.
  - [ ] Vista de Bitácora de Auditoría con filtros avanzados por fecha y actor.

---

### Fase 3: Módulo 2 — Gestión Documental y Trámites (`MOD_02`)
- [ ] **Backend (`core-api`):**
  - [ ] Migración Flyway de `documentos_digitales`, `correspondencia` y `tramites_estudiantiles`.
  - [ ] Servicio de carga de archivos (Azure Blob Storage / almacenamiento local) y cálculo SHA-256.
  - [ ] Gestión y derivación de correspondencia institucional.
  - [ ] Máquina de Estados finitos de trámites (`EstadoTramite` con transiciones y validaciones por rol).
  - [ ] Registro inmutable de transiciones en `historial_estados_tramite`.
- [ ] **Frontend (`web-client`):**
  - [ ] Bandeja de correspondencia recibida y emitida con estados.
  - [ ] Lista y detalle de expedientes estudiantiles.
  - [ ] Componente visual `TramiteTimeline` (Stepper de estados) y modal de transiciones según rol.
  - [ ] Carga y visualización de requisitos adjuntos.

---

### Fase 4: Módulo 5 — Servicios Inteligentes y Visor Dual OCR (`MOD_05`)
- [ ] **Microservicio (`ai-worker-api`):**
  - [ ] Endpoint interno `POST /internal/v1/ocr/extract` con autenticación `X-Internal-Api-Key`.
  - [ ] Pipeline OpenCV: escala de grises, filtro Gaussiano, binarización Otsu y deskewing.
  - [ ] Extracción de texto con PyTesseract (`-l spa`) y cálculo de métricas de confianza.
  - [ ] Ejecución en hilo separado con `asyncio.to_thread` para no congelar el servidor.
- [ ] **Backend (`core-api`):**
  - [ ] Orquestador `WebClient` para invocar a `ai-worker-api` al subir un documento.
  - [ ] Trigger PostgreSQL nativo `fn_actualizar_vector_ocr` para generar `tsvector`.
  - [ ] Endpoint `PUT /api/v1/ocr/{id}/validar` para confirmar el texto corregido.
  - [ ] Endpoint `GET /api/v1/busqueda/documental` con búsqueda full-text sobre `tsvector`.
- [ ] **Frontend (`web-client`):**
  - [ ] Componente **Visor Comparativo Dual OCR**:
    - Panel izquierdo: PDF con `react-pdf` + zoom/pan con `react-zoom-pan-pinch`.
    - Panel derecho: editor de texto extraído con badge de confianza y botón "Confirmar y Validar".
  - [ ] Barra de búsqueda documental con resaltado de coincidencias.

---

### Fase 5: Módulo 3 y 6 — Planificación Académica y Machine Learning (`MOD_03` y `MOD_06`)
- [ ] **Backend (`core-api`):**
  - [ ] Migración de planes de estudio, mallas, prerrequisitos y oferta de grupos/cupos.
  - [ ] Registro de métricas semestrales (inscritos, aprobados, reprobados, demanda insatisfecha).
  - [ ] Servicio de generación de alertas predictivas y operativas.
  - [ ] Endpoint `POST /api/v1/alertas/{id}/atender` para registrar la decisión humana.
- [ ] **Microservicio (`ai-worker-api`):**
  - [ ] Pipeline Scikit-Learn: extracción de features curriculares y entrenamiento de `RandomForestClassifier`.
  - [ ] Verificación inmutable: validar que $F1\text{-Score} \ge 0.80$ antes de activar el modelo.
  - [ ] Endpoint interno de inferencia para proyectar demanda de cupos y semaforización ($P \ge 0.75 \rightarrow$ Rojo).
- [ ] **Frontend (`web-client`):**
  - [ ] Gestión de oferta académica semestral y asignación de carga docente.
  - [ ] Dashboard Directivo con gráficos Recharts y KPIs de cupos vs. demanda.
  - [ ] Matriz de Alertas con semaforización y modal para registrar acciones tomadas.

---

### Fase 6: Módulo 4 y 7 — Extensión Institucional, RPA y Asistente (`MOD_04` y `MOD_07`)
- [ ] **Backend (`core-api`):**
  - [ ] CRUD y seguimiento de titulados, investigaciones, actividades del POA y eventos.
  - [ ] Endpoints para disparar flujos RPA y consultar el asistente NLP.
- [ ] **Microservicio (`ai-worker-api`):**
  - [ ] Bots Playwright para consolidación de correspondencia y generación masiva de reportes PDF oficiales.
  - [ ] Pipeline del Asistente NLP de solo consulta filtrado por rol.
- [ ] **Frontend (`web-client`):**
  - [ ] Módulos de titulados, POA y eventos.
  - [ ] Interfaz de ejecución y monitor de resultados de bots RPA.
  - [ ] Chatbot / Asistente inteligente lateral.

---

### Fase 7: Pruebas Integrales, Documentación y Cierre
- [ ] Ejecutar suite completa de pruebas unitarias y de integración en los 3 componentes.
- [ ] Validar flujos de usuario contra `USER_GUIDE_SPEC.md`.
- [ ] Verificación de despliegue productivo en Docker Compose.
- [ ] Generación del Manual de Usuario final para anexo de grado.

---

## 3. Bitácora de Sesiones de la IA (Session Log)

| Fecha | Agente / Rol | Módulo / Tarea | Resumen de Cambios Realizados | Estado |
| :--- | :--- | :--- | :--- | :--- |
| 2026-11-18 | Arquitecto IA | Documentación SDD | Creación de arquitecturas Spring Boot, FastAPI y React TS. | Exitoso |

---

## 4. Registro de Decisiones de Arquitectura (ADR)

### ADR-001: Topología en Estrella y Cero Comunicación Directa Frontend $\rightarrow$ FastAPI
* **Contexto:** Se requería definir cómo se comunica el cliente web con los motores de IA y OCR.
* **Decisión:** `web-client` SOLO se comunica con `core-api`. Toda llamada a `ai-worker-api` es orquestada por Spring Boot mediante `WebClient` y autenticada con `X-Internal-Api-Key`.
* **Consecuencias:** Máxima seguridad y centralización de la auditoría; FastAPI no expone puertos públicos.

### ADR-002: Ejecución de Tareas de CPU en Hilos de Fondo en FastAPI
* **Contexto:** Las operaciones de OpenCV, Tesseract y Scikit-Learn consumen intensivamente la CPU.
* **Decisión:** Se prohíbe invocar motores de visión artificial o ML dentro del hilo principal asíncrono. Deben ejecutarse obligatoriamente mediante `asyncio.to_thread(...)`.
* **Consecuencias:** FastAPI mantiene su event loop siempre libre para recibir peticiones sin bloqueos.

### ADR-003: Inmutabilidad de la Bitácora de Auditoría
* **Contexto:** Se requiere cumplir con los requisitos no funcionales de auditoría institucional y forense.
* **Decisión:** La tabla `bitacora_auditoria` no posee métodos de `UPDATE` ni `DELETE` en ningún repositorio, y se alimenta automáticamente vía Spring AOP (`@Auditable`).
* **Consecuencias:** Garantía de integridad histórica inmutable ante cualquier inspección directiva.
