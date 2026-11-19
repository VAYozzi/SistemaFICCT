# TESTING_AND_QA_LOG.md
# Registro Formal de Pruebas y Aseguramiento de Calidad

**Proyecto:** Sistema Web de Gestión Administrativa con Alertas Predictivas y OCR (FICCT–UAGRM)  
**Última ejecución de pruebas:** 19 de Noviembre de 2026  
**Resultado de Auditoría Final (Bloque G):** 100% SATISFACTORIO  

---

## 1. Pruebas de Seguridad y Control de Acceso (Bloque B)
* **TC-SEC-01 (Login JWT):** `POST /api/v1/auth/login` emite tokens firmados con claims válidos. Resultado: **APROBADO**.
* **TC-SEC-02 (Aislamiento RBAC):** Verificado rechazo `HTTP 403 Forbidden` a Coordinador en `/usuarios` y Auxiliar en `/auditoria`. Resultado: **APROBADO**.
* **TC-SEC-03 (Transferencia de Dirección CU-04):** Degradación atómica de Director saliente y ascenso de nuevo titular respetando índice `idx_unico_director_activo`. Resultado: **APROBADO**.

---

## 2. Pruebas de Gestión Documental y Trámites (Bloque C)
* **TC-DOC-01 (Carga SHA-256):** `POST /api/v1/documentos/upload` almacena binario en `storage/documentos/` y genera hash SHA-256 coincidente bit a bit con el cálculo del SO. Resultado: **APROBADO**.
* **TC-DOC-02 (CITE y Correspondencia):** `POST /api/v1/correspondencia` crea código oficial y genera registro en `bitacora_auditoria`. Resultado: **APROBADO**.
* **TC-DOC-03 (Máquina de Estados de Trámites):**
  - Transición válida: `INGRESADO` $\rightarrow$ `EN_VERIFICACION` $\rightarrow$ `CON_DICTAMEN` $\rightarrow$ `APROBADO` $\rightarrow$ `CONCLUIDO`. Resultado: **APROBADO**.
  - Transición ilegal: Salto directo de `EN_VERIFICACION` a `CONCLUIDO` rechazado con `HTTP 409 Conflict`. Resultado: **APROBADO**.
  - Historial inmutable: 4 registros creados en `historial_estados_tramite`. Resultado: **APROBADO**.

---

## 3. Pruebas de OCR y Búsqueda Documental (Bloque D)
* **TC-OCR-01 (Pipeline Visión Artificial):** FastAPI procesa imágenes y PDFs mediante PyMuPDF, OpenCV (filtro Gaussiano, Otsu, deskewing) y PyTesseract con diccionario en español (`spa`). Resultado: **APROBADO**.
* **TC-OCR-02 (Integración Spring Boot $\rightarrow$ FastAPI):** `core-api` invoca a `ai-worker-api` mediante cabecera interna `X-Internal-Api-Key`. React nunca accede a FastAPI directamente. Resultado: **APROBADO**.
* **TC-OCR-03 (Soberanía Humana y No Indexación Inicial):** Documento nace con `vector_busqueda = NULL`. Búsqueda previa retorna 0 resultados. Resultado: **APROBADO**.
* **TC-OCR-04 (Validación y Trigger tsvector PostgreSQL):** Al validar con `PUT /api/v1/ocr/validar`, el trigger nativo puebla `vector_busqueda` y la búsqueda devuelve fragmentos resaltados con `<mark>`. Resultado: **APROBADO**.
* **TC-OCR-05 (Compilación Frontend React):** Componente `OcrVisorPage.tsx` compilado sin errores con Vite (`built in 1.22s`). Resultado: **APROBADO**.

---

## 4. Pruebas de Planificación Académica y Predicción de Cupos (Bloque E)
* **TC-ACA-01 (Validación Flyway V1 a V4):** Comando `mvnw flyway:validate` ejecutado exitosamente (`Successfully validated 4 migrations`). Resultado: **APROBADO**.
* **TC-ACA-02 (Malla Curricular y Periodos):** Creación de plan `187-3`, materias con prerrequisitos y oferta de grupos semestrales. Resultado: **APROBADO**.
* **TC-ACA-03 (Modelo Random Forest en FastAPI):** Validación cruzada $K=5$ con métrica $F1 \ge 0.80$ (CA-04.1). Inferencia semafórica ROJO ($P = 0.9874$, déficit 133 cupos). Resultado: **APROBADO**.
* **TC-ACA-04 (Compilación Frontend React):** Vista `PlanificacionAcademicaPage.tsx` validada con Vite (`built in 1.23s`). Resultado: **APROBADO**.

---

## 5. Pruebas de Seguimiento Institucional, Alertas y RPA (Bloque F)
* **TC-INS-01 (Migración Flyway V5):** Aplicada formalmente con `mvnw flyway:migrate` creando tablas de titulados, POA, alertas y ejecuciones RPA. Resultado: **APROBADO**.
* **TC-INS-02 (Directorio de Titulados CU-17):** Registro de graduado por modalidad `TESIS` con acta adjunta ID 1 y nota final 95.50. Resultado: **APROBADO**.
* **TC-INS-03 (Alertas y Soberanía Humana CU-28 / CU-30):**
  - Registro de alerta predictiva semáforo `ROJO` por sobredemanda en `INF110`.
  - Atención humana mediante `POST /api/v1/institucional/alertas/1/atender` exigiendo el ingreso formal de la resolución de autoridad (Resolución N° 14/2026).
  - Alerta transiciona a `ATENDIDA` y se registra en `atencion_alertas`. Resultado: **APROBADO**.
* **TC-INS-04 (KPIs Dashboard Directivo CU-26):** Consulta de contadores en tiempo real (estudiantes, trámites en curso, alertas rojas y avance del POA). Resultado: **APROBADO**.
* **TC-INS-05 (Generación de Dossier RPA CU-34):** Compilación automatizada de informe oficial del periodo en PDF, cálculo de hash SHA-256 (`87ac3b119555...`) y persistencia en `documentos_digitales`. Resultado: **APROBADO**.

---

## 6. Pruebas de Regresión y Validación Integral (Bloque G)
* **TC-REG-01 (Auditoría Integral de Regresión):** Ejecutado script `run_full_regression.ps1` con servicios locales en vivo en Windows 10.
  - Flujo 1 (RBAC y Seguridad): Login Director exitoso, Auxiliar bloqueado con 403.
  - Flujo 2 (Gestión Documental): Almacenamiento y hash SHA-256 verificado.
  - Flujo 3 (OCR y Soberanía Humana): Extracción OpenCV/Tesseract y trigger `tsvector` comprobado.
  - Flujo 4 (Máquina de Estados): Estado de trámite `CONCLUIDO` con trazabilidad inmutable.
  - Flujo 5 (Machine Learning): Predicción supervisada de demanda con modelo Random Forest consultivo.
  - Flujo 6 (Dashboard y Alertas): Métricas del POA y alertas atendidas sincronizadas.
  - Flujo 7 (Dossier RPA): Generación y almacenamiento de informe institucional sellado con SHA-256.
* **Resultado Global de Regresión:** **100% SATISFACTORIO**.
