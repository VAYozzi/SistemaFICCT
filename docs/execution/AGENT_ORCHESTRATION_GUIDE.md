# AGENT_ORCHESTRATION_GUIDE.md
# Guía Maestra de Orquestación y Prompts para Agentes de IA

> **Propósito:**  
> Este documento contiene los **prompts maestros exactos** que el usuario debe copiar y pegar al interactuar con cualquier agente de IA (Antigravity, Claude Code, Cursor, ChatGPT, etc.) para implementar el proyecto fase por fase sin perder el rumbo ni violar la arquitectura.

---

## 1. Reglas de Oro de Despacho para el Agente

Cada vez que inicies un nuevo sprint con una IA, debes asegurarte de que cumpla estas 4 reglas:
1. **Lectura Previa Obligatoria:** La IA debe leer siempre `00_PROJECT_CONSTITUTION.md` y el módulo específico correspondiente antes de escribir una sola línea de código.
2. **Respeto de Contratos:** La IA jamás debe cambiar los nombres de endpoints ni los esquemas de base de datos definidos en `01_DOMAIN_AND_DATABASE.md` y `02_API_CONTRACTS.md`.
3. **Control de Calidad (DoD):** No puede dar por terminada una tarea sin compilar y ejecutar los comandos de prueba de `TESTING_AND_QA_LOG.md`.
4. **Registro Continuo:** Debe actualizar los checkboxes y la bitácora de `IMPLEMENTATION_TRACKER.md` al finalizar su intervención.

---

## 2. Prompts Maestros Listos para Copiar y Pegar

### 🚀 Sprint 0: Configuración Inicial de Repositorio y Docker
```text
Actúa como Arquitecto de Software y Desarrollador DevOps Senior.
Vamos a inicializar el proyecto "Sistema Web de Gestión Administrativa FICCT - UAGRM" bajo el enfoque Spec-Driven Development (SDD).

Por favor, lee detalladamente los siguientes archivos:
- docs/00_project_constitution_md.md
- docs/execution/INFRASTRUCTURE_AND_DOCKER.md
- docs/execution/IMPLEMENTATION_TRACKER.md

Tu objetivo en esta sesión es:
1. Crear la estructura base de directorios (/core-api, /ai-worker-api, /web-client).
2. Generar el archivo docker-compose.yml y los Dockerfiles multi-etapa oficiales para cada servicio.
3. Crear el archivo .env.example centralizado.
4. Levantar el ecosistema con `docker compose up --build -d` y verificar que todos los contenedores respondan correctamente en sus respectivos puertos.
5. Al terminar, actualiza el checklist de la Fase 1 en docs/execution/IMPLEMENTATION_TRACKER.md.
```

---

### 🛡️ Sprint 1: Módulo 1 — Seguridad, Autenticación y Bitácora
```text
Actúa como Ingeniero Full Stack Senior (Java 17 / React 18).
Vamos a implementar el Módulo 1: Seguridad, Autenticación JWT, RBAC y Bitácora de Auditoría.

Lee atentamente:
- docs/00_project_constitution_md.md
- docs/01_domain_and_database_md.md (Paquete 1)
- docs/02_api_contracts_md.md (Sección 3.1)
- docs/architecture/ARCHITECTURE_SPRING_BOOT.md
- docs/architecture/ARCHITECTURE_REACT_TS.md
- docs/modules/MOD_01_AUTH_Y_USUARIOS.md
- docs/execution/SEED_DATA_AND_FIXTURES.md

Tu objetivo en esta sesión es:
1. En core-api: Crear las migraciones Flyway V1 (tablas roles, usuarios, invitaciones, bitacora) y V2 (seed data). Implementar Spring Security 6 con JWT stateless, el aspecto @Auditable AOP y el endpoint crítico @Transactional de Transferencia de Dirección.
2. En web-client: Implementar la pantalla de Login institucional con React Hook Form + Zod, el store Zustand de sesión, el RoleGuard para rutas privadas y el layout del Dashboard.
3. Ejecutar las pruebas unitarias y de integración según docs/execution/TESTING_AND_QA_LOG.md.
4. Registrar los avances en docs/execution/IMPLEMENTATION_TRACKER.md.
```

---

### 📂 Sprint 2: Módulo 2 — Gestión Documental, Trámites y State Machine
```text
Actúa como Desarrollador Backend y Frontend Senior.
Vamos a implementar el Módulo 2: Correspondencia Institucional y Máquina de Estados de Trámites.

Lee atentamente:
- docs/00_project_constitution_md.md (Sección 4.3 - State Machine)
- docs/01_domain_and_database_md.md (Paquete 2)
- docs/02_api_contracts_md.md (Sección 3.2)
- docs/modules/MOD_02_DOCUMENTAL_Y_TRAMITES.md

Tu objetivo en esta sesión es:
1. En core-api: Implementar la subida y almacenamiento de archivos (/app/storage), el servicio de correspondencia y el motor estricto de la Máquina de Estados finitos para expedientes estudiantiles con validaciones por rol.
2. En web-client: Construir la bandeja de correspondencia y el visor del expediente con el componente visual TramiteTimeline (Stepper) y el modal de actuaciones según permisos RBAC.
3. Verificar que las transiciones ilegales sean rechazadas con HTTP 409 Conflict.
4. Actualizar docs/execution/TESTING_AND_QA_LOG.md y docs/execution/IMPLEMENTATION_TRACKER.md.
```

---

### 👁️ Sprint 3: Módulo 5 — Servicios Inteligentes y Visor Dual OCR
```text
Actúa como Especialista en Visión Artificial (Python) e Ingeniero Frontend (React).
Vamos a implementar el Módulo 5: Motor OpenCV + Tesseract OCR y el Visor Comparativo Dual.

Lee atentamente:
- docs/architecture/ARCHITECTURE_FASTAPI.md
- docs/architecture/ARCHITECTURE_REACT_TS.md (Sección 5.1)
- docs/modules/MOD_05_SERVICIOS_INTELIGENTES_OCR.md

Tu objetivo en esta sesión es:
1. En ai-worker-api: Implementar el pipeline de visión artificial en Python (escala de grises, filtro Gaussiano, binarización Otsu, deskewing) y extracción de texto con PyTesseract spa. Encapsular la ejecución pesada con `asyncio.to_thread` para no bloquear FastAPI.
2. En core-api: Configurar WebClient para llamar a ai-worker-api con X-Internal-Api-Key, el trigger PostgreSQL tsvector y el endpoint PUT /api/v1/ocr/{id}/validar para la validación humana.
3. En web-client: Construir el Visor Comparativo Dual (panel izquierdo con react-pdf + zoom-pan-pinch; panel derecho con editor de texto corregible y botón de confirmación).
4. Comprobar que el texto no se indexe en la búsqueda hasta que un humano presione "Confirmar y Validar".
5. Registrar evidencias en docs/execution/TESTING_AND_QA_LOG.md.
```

---

### 📊 Sprint 4: Módulos 3 y 6 — Planificación Académica y Machine Learning
```text
Actúa como Científico de Datos y Desarrollador Full Stack.
Vamos a implementar la Planificación Académica y el Modelo Predictivo Random Forest de Cupos.

Lee atentamente:
- docs/01_domain_and_database_md.md (Paquetes 3 y 5)
- docs/02_api_contracts_md.md (Secciones 3.3, 3.6 y 4.2)
- docs/modules/MOD_03_PLANIFICACION_ACADEMICA.md
- docs/modules/MOD_06_ANALITICA_Y_ALERTAS.md

Tu objetivo en esta sesión es:
1. En ai-worker-api: Entrenar el modelo RandomForestClassifier con el histórico de cupos y prerrequisitos. Validar obligatoriamente que F1-Score >= 0.80 para persistirlo en .joblib. Implementar la inferencia con semáforos (P >= 0.75 -> Rojo).
2. En core-api: Exponer endpoints de oferta académica, simulación predictiva y gestión de alertas operativas/predictivas.
3. En web-client: Crear el Dashboard Directivo con Recharts y la matriz de alertas semafóricas con modal para registrar la decisión humana.
4. Actualizar docs/execution/TESTING_AND_QA_LOG.md e IMPLEMENTATION_TRACKER.md.
```

---

### 🤖 Sprint 5: Módulos 4 y 7 — Extensión Institucional, RPA y Asistente
```text
Actúa como Ingeniero de Automatización y Full Stack.
Vamos a implementar el Seguimiento Institucional (Titulados, POA), los bots RPA y el Asistente NLP.

Lee atentamente:
- docs/01_domain_and_database_md.md (Paquete 4)
- docs/02_api_contracts_md.md (Secciones 3.4, 3.7 y 4.4)
- docs/modules/MOD_04_SEGUIMIENTO_INSTITUCIONAL.md
- docs/modules/MOD_07_AUTOMATIZACION_RPA.md

Tu objetivo en esta sesión es:
1. En ai-worker-api: Implementar bots Playwright headless para consolidar correspondencia y generar reportes oficiales en PDF con WeasyPrint. Implementar el motor NLP de consulta para el asistente.
2. En core-api: Exponer endpoints para titulados, investigaciones, POA, ejecución de RPA y asistente.
3. En web-client: Construir pantallas de POA y titulados, monitor de tareas RPA y panel lateral del Asistente Inteligente.
4. Registrar avances en docs/execution/IMPLEMENTATION_TRACKER.md.
```

---

### 🏁 Sprint 6: Auditoría Final, E2E y Manual de Usuario
```text
Actúa como QA Lead y Documentador Técnico.
Vamos a cerrar el proyecto ejecutando la verificación integral de extremo a extremo.

Lee atentamente:
- docs/execution/TESTING_AND_QA_LOG.md
- docs/USER_GUIDE_SPEC.md

Tu objetivo en esta sesión es:
1. Ejecutar las pruebas completas de integración en core-api, ai-worker-api y web-client.
2. Validar cada flujo del sistema simulando las acciones de los 3 roles (Director, Coordinador, Auxiliar).
3. Generar el Manual de Usuario institucional final en formato Markdown listo para su presentación y anexo de grado.
4. Marcar el 100% de las tareas completadas en docs/execution/IMPLEMENTATION_TRACKER.md.
```
