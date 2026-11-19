# IMPLEMENTATION_ROADMAP.md
# Hoja de Ruta de Implementación por Sprints y Cronograma de Entregables

## 1. Visión General del Cronograma
La construcción del **Sistema Web de Gestión Administrativa con Alertas Predictivas y OCR para la Dirección de Carrera de Ingeniería en Sistemas (FICCT - UAGRM)** se organiza en **7 Sprints de desarrollo secuencial**.

```mermaid
gantt
    title Cronograma de Implementación por Sprints (SDD)
    dateFormat  YYYY-MM-DD
    section Infraestructura
    Sprint 0: Setup Docker y Repositorio     :done,    des0, 2026-11-01, 4d
    section Backend y Frontend Core
    Sprint 1: Seguridad, Auth y Bitácora    :active,  des1, after des0, 7d
    Sprint 2: Gestión Documental y Trámites :         des2, after des1, 8d
    section Inteligencia Artificial
    Sprint 3: Motor OCR y Visor Dual        :         des3, after des2, 8d
    Sprint 4: Planificación y Machine Learning:       des4, after des3, 9d
    section Automatización y Cierre
    Sprint 5: Extensión Institucional y RPA :         des5, after des4, 7d
    Sprint 6: Pruebas E2E y Manual de Usuario:        des6, after des5, 5d
```

---

## 2. Detalle de Entregables por Sprint

### Sprint 0: Infraestructura y Entorno Base
* **Objetivo:** Disponer del entorno Dockerizado multicontenedor levantado y funcionando con un solo comando.
* **Entregables:**
  * Estructura base de repositorios (`/core-api`, `/ai-worker-api`, `/web-client`).
  * `docker-compose.yml` con servicios `db`, `core-api`, `ai-worker-api`, `web-client`.
  * `Dockerfile` multi-etapa optimizados para Java 17, Python 3.10 y Vite/Nginx.
  * Verificación de conectividad en red interna `ficct_network`.

### Sprint 1: Seguridad, Autenticación y Bitácora (`MOD_01`)
* **Objetivo:** Núcleo de control de acceso institucional y auditoría forense inmutable.
* **Entregables:**
  * Migraciones Flyway `V1__init_security_and_audit.sql` y `V2__seed_data.sql`.
  * Autenticación JWT stateless y filtros RBAC (`ROLE_DIRECTOR`, `ROLE_COORDINADOR`, `ROLE_AUXILIAR`).
  * Endpoint `@Transactional` de Transferencia de Autoridad de Dirección.
  * Aspecto Spring AOP `@Auditable` persistiendo en `bitacora_auditoria`.
  * Pantalla de Login institucional y Layout con `RoleGuard` en React.

### Sprint 2: Gestión Documental y Máquina de Estados (`MOD_02`)
* **Objetivo:** Digitalización de correspondencia y motor estricto de expedientes.
* **Entregables:**
  * Subida de archivos a almacenamiento con cálculo SHA-256 y SAS URLs temporales.
  * Bandeja de correspondencia recibida y emitida con correlativo oficial.
  * Máquina de Estados finitos de trámites estudiantiles gobernada por `EstadoTramite`.
  * Componente `TramiteTimeline` (Stepper) y modal de actuaciones en React.

### Sprint 3: Motor OCR y Visor Comparativo Dual (`MOD_05`)
* **Objetivo:** Visión artificial con soberanía humana obligatoria.
* **Entregables:**
  * Pipeline OpenCV (deskewing, binarización Otsu) y PyTesseract (`-l spa`).
  * Endpoint de extracción en hilo separado `asyncio.to_thread` en FastAPI.
  * Trigger PostgreSQL nativo `fn_actualizar_vector_ocr` para generar `tsvector`.
  * Componente **Visor Comparativo Dual** (PDF con zoom/pan + editor corregible con confianza).
  * Búsqueda avanzada de texto completo sobre documentos validados.

### Sprint 4: Planificación Académica y Machine Learning (`MOD_03` y `MOD_06`)
* **Objetivo:** Soporte directivo y alertas predictivas de cupos.
* **Entregables:**
  * Malla curricular con grafo de prerrequisitos y oferta semestral de grupos.
  * Modelo `RandomForestClassifier` en Scikit-Learn entrenado sobre histórico ($F1 \ge 0.80$).
  * Inferencia de demanda semestral con semáforos (Rojo si $P \ge 0.75$).
  * Dashboard directivo con Recharts y matriz de alertas con modal de resolución humana.

### Sprint 5: Extensión Institucional, RPA y Asistente (`MOD_04` y `MOD_07`)
* **Objetivo:** Tareas desatendidas y seguimiento de grado.
* **Entregables:**
  * Módulos de titulados, investigaciones, POA anual y eventos.
  * Bots Playwright para consolidación de correspondencia y PDFs oficiales con WeasyPrint.
  * Asistente NLP de consultas institucionales de solo lectura.

### Sprint 6: Pruebas Integrales, Documentación y Cierre
* **Objetivo:** Acreditación de calidad y manuales finales para defensa.
* **Entregables:**
  * Ejecución de suite completa de pruebas unitarias y de integración.
  * Validación de flujos de usuario contra `USER_GUIDE_SPEC.md`.
  * Manual de Usuario final exportado en Markdown/PDF para anexo de tesis.
