# PRE_IMPLEMENTATION_AUDIT.md
# Auditoría Pre-Implementación y Análisis Integral de Especificaciones

**Proyecto:** Sistema Web de Gestión Administrativa con Alertas Predictivas y OCR (FICCT–UAGRM)  
**Fecha:** 19 de Noviembre de 2026  
**Auditor:** Equipo de Arquitectura e Ingeniería de Software

---

## 1. Resumen Ejecutivo
Se realizó una inspección completa y recursiva de todos los documentos dentro de `docs/` (constitución, arquitectura, contratos API, modelo de datos, reglas de negocio, requerimientos IEEE 830, roles RBAC, casos de uso, módulos `MOD_01` a `MOD_07`, seguridad, IA/OCR/ML, integraciones y plan de pruebas).

La documentación presenta un nivel de madurez, consistencia y detalle técnico sumamente alto (100% de especificaciones completadas). No existen vacíos conceptuales que impidan el diseño o desarrollo del software.

---

## 2. Cobertura de Requisitos y Verificación de Consistencia

| Área Analizada | Resultado de Verificación | Observación Técnica |
| :--- | :--- | :--- |
| **Requerimientos (RF-01 a RF-26)** | ✅ 100% Especificados | Criterios de aceptación IEEE 830 claros y comprobables. |
| **Casos de Uso (CU-01 a CU-37)** | ✅ 100% Especificados | Flujos principales, alternativos y de excepción documentados. |
| **Modelo de Datos DDL** | ✅ 100% Coherente | 7 paquetes de tablas DDL PostgreSQL, claves foráneas, índices GIN e índices parciales definidos. |
| **Contratos de API RESTful** | ✅ 100% Mapeados | Endpoints `/api/v1` con DTOs, códigos HTTP, envoltorio `ApiResponse<T>` y `PageResponse<T>`. |
| **Matriz de Permisos (RBAC)** | ✅ 100% Consistente | Solo 3 roles internos: `ROLE_DIRECTOR`, `ROLE_COORDINADOR`, `ROLE_AUXILIAR`. |
| **Pipeline OCR (OpenCV + Tesseract)** | ✅ 100% Definido | Preprocesamiento, corrección de ángulo, binarización Otsu y estado inicial `PENDIENTE_VALIDACION`. |
| **Machine Learning (Predictivo)** | ✅ 100% Definido | Random Forest + Regresión Logística, umbrales de probabilidad $P \ge 0.75$, métrica meta $F1 \ge 0.80$. |
| **Automatización RPA y NLP** | ✅ 100% Definido | Bots headless Playwright, generación PDF institucional y Asistente interno RBAC de solo lectura. |

---

## 3. Inconsistencias Detectadas y Clasificación de Problemas

### 3.1. Archivos Duplicados en `docs/` (Problema Menor - Resuelto)
* **Hallazgo:** Existen 3 archivos duplicados por nomenclatura:
  - `00_project_constitution_md.md` idéntico bit a bit a `00_PROJECT_CONSTITUTION.md`.
  - `01_domain_and_database_md.md` idéntico bit a bit a `01_DOMAIN_AND_DATABASE.md`.
  - `02_api_contracts_md.md` idéntico bit a bit a `02_API_CONTRACTS.md`.
* **Clasificación:** Menor.
* **Resolución:** Se toma la versión en mayúsculas (`00_PROJECT_CONSTITUTION.md`, etc.) como fuente canónica según la jerarquía establecida. Los archivos originales se conservan intactos.

### 3.2. Dependencia de Azure Blob Storage vs. Almacenamiento Local (Problema Menor - Resuelto)
* **Hallazgo:** La documentación menciona Azure Blob Storage con SAS URLs para producción, pero en desarrollo local y en el contenedor `db` se especifica volumen compartido `/app/storage`.
* **Clasificación:** Menor (Decisión Técnica).
* **Resolución:** Se implementa un `StorageService` desacoplado mediante interfaz en Spring Boot:
  - Implementación de desarrollo: `LocalStorageServiceImpl` que almacena en `/app/storage` (o subcarpeta `storage/`) preservando el cálculo de hash SHA-256, metadatos y generación de URLs temporales.
  - Implementación cloud: compatible con `AzureBlobStorageServiceImpl`.

### 3.3. Datasets Reales para Entrenamiento de ML (Problema Importante - Mitigado)
* **Hallazgo:** No existen registros históricos de matrículas reales provistos en el repositorio en este momento inicial.
* **Clasificación:** Importante.
* **Resolución:** Según la directriz de la Sección 12, se implementará el generador de datos sintéticos estocásticos documentado en `docs/ai/DATASET_AND_TRAINING.md` (`generate_synthetic_academic_dataset.py`) para entrenar el modelo base y validar que los algoritmos, validación cruzada y serialización `.joblib` funcionen técnicamente, dejando constancia de que son datos sintéticos y evaluando honestamente las métricas.

---

## 4. Viabilidad Técnica en el Hardware Host

1. **Memoria RAM (12.18 GB totales, ~4.7 GB libres):**
   - El sistema host cuenta con más memoria física de la esperada (12 GB en lugar de 8 GB).
   - Configuración:
     - Spring Boot JVM: `-Xms256m -Xmx512m`.
     - FastAPI: Uvicorn con 1 worker (consumo estimado ~150-250 MB).
     - PostgreSQL local: ya corriendo en Windows, consumo estimado ~50-100 MB.
     - React Vite: consumo de desarrollo ~100-200 MB.
     - Playwright: ejecución `--headless` por demanda.
   - **Diagnóstico:** Totalmente viable ejecutar los componentes en este equipo.

---

## 5. Recomendaciones de Implementación

1. **Bloque A (Infraestructura):**
   - Configurar la estructura de carpetas estándar:
     - `core-api/` (Spring Boot 3.2+ Maven Wrapper).
     - `ai-worker-api/` (FastAPI Python).
     - `web-client/` (React Vite TypeScript).
     - `storage/` (Volumen de almacenamiento local para documentos).
   - Crear `docker-compose.yml` oficial para producción/portabilidad con los 4 servicios.
2. **Proceder con el Bloque B** (Seguridad, Usuarios, JWT y Auditoría) en cuanto las dependencias base de ejecución estén disponibles.

