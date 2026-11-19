# 00_PROJECT_CONSTITUTION.md
# Constitución Técnica y Reglas Globales del Proyecto (SDD)

## 1. Propósito del Documento
Este archivo constituye la ley suprema de arquitectura, estándares de código y reglas de negocio para el desarrollo del **Sistema Web de Gestión Administrativa con Alertas Predictivas y Reconocimiento Óptico de Caracteres (OCR) para la Dirección de Carrera de Ingeniería en Sistemas (FICCT - UAGRM)**.
Cualquier agente de IA o desarrollador debe cumplir estrictamente estas directrices antes de generar o modificar código.

---

## 2. Arquitectura del Sistema y Patrón de Comunicación

El sistema se despliega mediante contenedores (`Docker` y `Docker Compose`) bajo una arquitectura desacoplada de 4 bloques:

1. **`web-client` (Frontend Web):** React 18 + TypeScript + Vite.
2. **`core-api` (Backend Transaccional y Orquestador):** Java 17 + Spring Boot 3.x.
3. **`ai-worker-api` (Microservicio Especializado IA/OCR/RPA):** Python 3.10+ + FastAPI.
4. **`db` (Persistencia Relacional):** PostgreSQL 15+ con soporte de búsqueda de texto completo (`tsvector`) y almacenamiento en volumen compartido (`/app/storage`) para archivos físicos (PDF, JPG, PNG).

### Regla Estricta de Comunicación (Topología en Estrella)
* **`web-client` SOLO se comunica con `core-api`** a través de peticiones HTTP RESTful aseguradas con tokens JWT.
* **`web-client` NUNCA llama directamente a `ai-worker-api`**.
* **`core-api` actúa como único orquestador:** recibe la solicitud del cliente, valida permisos (RBAC), registra la auditoría y, si requiere procesamiento de OCR, inferencia de Machine Learning, ejecución de RPA o procesamiento de lenguaje natural (NLP), realiza una petición HTTP interna hacia `ai-worker-api`.
* Tanto `core-api` como `ai-worker-api` comparten acceso al volumen de archivos `/app/storage` para evitar transferir binarios pesados por red interna; se comunican pasando la ruta relativa del archivo y el ID de documento.

---

## 3. Stack Tecnológico Oficial y Librerías

### 3.1. Frontend (`web-client`)
* **Core:** React 18, TypeScript 5.x, Vite.
* **Estilos y Componentes UI:** Tailwind CSS + `shadcn/ui` (Radix UI) + `lucide-react` (iconografía limpia e institucional).
* **Gestión de Estado y Caché:**
  * Estado del servidor (fetching, caché, mutaciones): `@tanstack/react-query`.
  * Estado global del cliente (sesión JWT, rol activo, preferencias UI): `zustand`.
* **Formularios y Validación:** `react-hook-form` + `zod`.
* **Tablas, Gráficos y Visor OCR:**
  * Tablas avanzadas y filtros: `@tanstack/react-table`.
  * Dashboards y semaforización predictiva: `recharts`.
  * Visor comparativo dual OCR (PDF/Imágenes con zoom): `react-pdf` + `react-zoom-pan-pinch`.
* **Cliente HTTP:** `axios` configurado con interceptores automáticos para inyectar `Authorization: Bearer <token>` y manejar expiración de sesión (HTTP 401/403).

### 3.2. Backend Transaccional (`core-api`)
* **Core:** Java 17 (JDK), Spring Boot 3.2+.
* **Dependencias Oficiales:**
  * `Spring Web` (REST Controllers).
  * `Spring Security` + `jjwt` (Autenticación Stateless con JWT y RBAC).
  * `Spring Data JPA` / `Hibernate` (ORM).
  * `Flyway` (Migraciones versionadas de base de datos SQL).
  * `Bean Validation` (`jakarta.validation` para DTOs).
  * `Lombok` y `MapStruct` (Reducción de boilerplate y mapeo Entidad <-> DTO).
  * `Spring WebFlux` (`WebClient` exclusivamente para llamadas HTTP síncronas/asíncronas hacia `ai-worker-api`).
* **Estructura de Paquetes (Modular por Dominio):**
  ```text
  bo.edu.uagrm.ficct.sistemas.gestion
  ├── config/          (Seguridad, CORS, WebClient, Swagger/OpenAPI)
  ├── common/          (ApiResponse, PageResponse, Excepciones Globales, Auditoría AOP)
  └── modules/
      ├── auth/        (Usuarios, Roles, Invitaciones, Transferencia Dirección, Bitácora)
      ├── documental/  (Correspondencia, Documentos Digitales, Expedientes, Trámites/StateMachine)
      ├── academico/   (Periodos, Materias, Grupos, Cupos, Migración Planes, Carga Docente)
      ├── institucional/(Titulados, Investigaciones, POA, Eventos)
      ├── inteligencia/(Orquestación OCR, Búsqueda Avanzada/Semántica, Asistente)
      ├── analitica/   (Dashboard, Reportes, Alertas Administrativas y Predictivas)
      └── rpa/         (Orquestación de tareas RPA, validación e incidencias)
  ```

### 3.3. Microservicio Especializado (`ai-worker-api`)
* **Core:** Python 3.10+, FastAPI, Uvicorn, Pydantic v2.
* **Visión Artificial y OCR:** `opencv-python-headless` (escala de grises, filtro Gaussiano, binarización Otsu, deskewing/corrección de inclinación), `pytesseract` (Tesseract OCR configurado en idioma español `spa`), `pdf2image` / `PyMuPDF` (`fitz`).
* **Machine Learning y Datos:** `scikit-learn` (Random Forest Classifier + Logistic Regression), `pandas`, `numpy`, `joblib` (persistencia de modelos entrenados).
* **Automatización RPA y PDF:** `playwright` (ejecución headless desatendida), `WeasyPrint` o `ReportLab` (generación de informes PDF institucionales).
* **Búsqueda Semántica y Asistente NLP:** `sentence-transformers` o pipeline NLP ligero conectado a consultas parametrizadas seguras.

---

## 4. Reglas Inmutables de Negocio (Guardarraíles)

### 4.1. Control de Acceso Basado en Roles (RBAC)
El sistema es de **uso interno exclusivo** para la Dirección de Carrera (no existe portal público de estudiantes). Solo existen tres roles principales:
1. **`ROLE_DIRECTOR` (Director de Carrera):** Autoridad máxima. Único rol autorizado para aprobar/rechazar dictámenes finales de trámites, transferir la autoridad de Dirección, administrar roles/invitaciones, gestionar alertas predictivas y auditar la bitácora completa.
   * *Invariante de Dirección:* Solo puede existir **1 usuario activo** con `ROLE_DIRECTOR` a la vez. Al ejecutar el caso de uso de Transferencia de Autoridad, el Director saliente pierde el rol activo de Dirección dentro de la misma transacción ACID.
2. **`ROLE_COORDINADOR` (Coordinador Académico):** Supervisa y registra planificación académica semestral, materias, grupos, cupos, carga docente, seguimiento del POA y realiza la revisión preliminar y emisión de dictamen técnico en trámites estudiantiles.
3. **`ROLE_AUXILIAR` (Auxiliar Profesional / Secretaría):** Registra correspondencia, recepciona trámites en ventanilla, verifica requisitos documentales, digitaliza archivos, ejecuta y valida el texto extraído por OCR, y opera las tareas de consolidación RPA.

### 4.2. Soberanía Humana Obligatoria
Ningún componente inteligente o automatizado puede alterar estados críticos de negocio por cuenta propia:
* **Regla OCR:** Todo documento procesado por Tesseract nace en estado `PENDIENTE_VALIDACION`. El texto extraído NO se indexa en el motor de búsqueda oficial hasta que un usuario (`ROLE_AUXILIAR` o superior) revise el documento en el **Visor Comparativo Dual**, corrija posibles errores y confirme la validación (`VALIDADO`).
* **Regla Machine Learning:** Las alertas predictivas de déficit de cupos (umbral crítico $P \ge 0.75$, modelo aceptado con $F1\text{-Score} \ge 0.80$) son estrictamente consultivas. No abren grupos ni modifican cupos de forma automática.
* **Regla RPA y Asistente:** El asistente inteligente (texto/voz) es de **solo consulta** y filtra respuestas según el rol del usuario en sesión. Los flujos RPA generan consolidados, informes PDF y borradores de correo sujetos a revisión humana.

### 4.3. Máquina de Estados Finitos de Trámites (State Machine)
Los trámites estudiantiles deben gobernarse por transiciones estrictas. No se permite saltar estados mediante `UPDATE` directo:
* **Estados válidos:** `INGRESADO` $\rightarrow$ `EN_VERIFICACION` $\rightarrow$ (`OBSERVADO` $\leftrightarrow$ `EN_VERIFICACION`) $\rightarrow$ `CON_DICTAMEN` $\rightarrow$ (`APROBADO` | `RECHAZADO`) $\rightarrow$ `CONCLUIDO`.
* Cada transición de estado exige registrar: `usuario_id`, `rol_en_momento`, `estado_anterior`, `estado_nuevo`, `observacion_o_dictamen` y `fecha_hora`.

### 4.4. Bitácora de Auditoría Inmutable
* Toda operación de escritura (`POST`, `PUT`, `PATCH`, `DELETE`), inicio de sesión, fallo de autenticación o transferencia de mando debe registrarse automáticamente en la tabla `bitacora_auditoria` (implementado mediante Spring AOP / Listeners en `core-api`).
* **Prohibición estricta:** No debe existir ningún endpoint, método de repositorio ni servicio que permita hacer `UPDATE` o `DELETE` sobre `bitacora_auditoria`.

---

## 5. Estándares de Código y Contratos API

1. **Prohibido exponer Entidades JPA:** Todo controlador en Spring Boot debe recibir y devolver objetos `DTO` (`record` de Java 17).
2. **Envoltorio Estándar de Respuesta (`ApiResponse<T>`):**
   ```json
   {
     "success": true,
     "message": "Operación ejecutada correctamente",
     "data": { ... },
     "timestamp": "2026-08-15T10:30:00Z",
     "errors": null
   }
   ```
3. **Convenciones de Nomenclatura:**
   * **Base de datos (PostgreSQL):** `snake_case` en plural para tablas (`tramites_estudiantiles`), `snake_case` en singular para columnas (`fecha_registro`). Llaves primarias `id` (BIGSERIAL o UUID según entidad).
   * **Backend (Java):** `CamelCase` para clases, `camelCase` para métodos y propiedades JSON.
   * **Microservicio (Python):** `snake_case` siguiendo PEP-8.
   * **Frontend (React/TS):** `PascalCase` para componentes (`.tsx`), `camelCase` para hooks (`useTramites.ts`) y utilidades.
4. **Idioma del Dominio:** Todo el código de dominio (nombres de tablas, entidades, variables de negocio, endpoints y mensajes de error) debe escribirse en **español técnico claro** (ej. `/api/v1/tramites`, `TramiteEstudiantil`, `AlertaPredictiva`) para mantener trazabilidad 1:1 con la documentación IEEE 830 y los diagramas UML del proyecto de grado.