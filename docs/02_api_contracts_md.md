# 02_API_CONTRACTS.md
# Contratos de API RESTful y Comunicación Inter-Servicios (SDD)

## 1. Propósito y Reglas de Comunicación

Este documento establece el contrato inmutable de comunicación entre los bloques arquitectónicos del sistema:
1. **Frontera Externa (`web-client` $\leftrightarrow$ `core-api`):** API RESTful pública consumida exclusivamente por la aplicación React 18. Protegida mediante tokens JWT y validación estricta de roles (RBAC).
2. **Frontera Interna (`core-api` $\leftrightarrow$ `ai-worker-api`):** API RESTful privada dentro de la red de contenedores Docker (`http://ai-worker-api:8000`). Protegida mediante cabecera secreta compartida `X-Internal-Api-Key`.
3. **Estrategia con Azure Blob Storage:** `core-api` (Spring Boot) es el único propietario de las credenciales maestras de escritura en Azure Blob Storage. Cuando `ai-worker-api` (FastAPI) necesita leer un PDF/imagen para OCR, o subir un reporte PDF generado por RPA, `core-api` genera y le envía una **SAS URL (Shared Access Signature)** temporal (con permisos de lectura `r` o escritura `w` válidos por 15 minutos).

---

## 2. Estándares Globales de Payloads (`core-api`)

### 2.1. Cabeceras Obligatorias (Requests desde React)
```http
Authorization: Bearer <jwt_access_token>
Content-Type: application/json
Accept: application/json
```
*(Para subida de archivos se emplea `Content-Type: multipart/form-data`).*

### 2.2. Envoltorio Estándar de Respuesta (`ApiResponse<T>`)
Toda respuesta HTTP (`200`, `201`, `400`, `401`, `403`, `404`, `409`, `500`) devuelta por `core-api` debe respetar esta estructura JSON:

```json
{
  "success": true,
  "message": "Operación completada exitosamente",
  "data": {},
  "timestamp": "2026-11-18T17:00:00Z",
  "errors": null
}
```

En caso de error de validación (`HTTP 400`) o regla de negocio (`HTTP 409`):
```json
{
  "success": false,
  "message": "Error de validación en los datos enviados",
  "data": null,
  "timestamp": "2026-11-18T17:00:00Z",
  "errors": [
    {
      "field": "estadoNuevo",
      "issue": "Transición inválida: No se puede pasar de INGRESADO directamente a APROBADO"
    }
  ]
}
```

### 2.3. Estructura Estándar de Paginación (`PageResponse<T>`)
Para cualquier endpoint listado con parámetros `?page=0&size=10&sort=id,desc`:
```json
{
  "content": [ ... ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 145,
  "totalPages": 15,
  "last": false
}
```

---

## 3. Catálogo de Endpoints Externos (`web-client` $\rightarrow$ `core-api`)

Prefijo base global: `/api/v1`

### 3.1. Paquete 1: Autenticación, Usuarios y Auditoría (`/auth`, `/usuarios`, `/auditoria`)

| Método | Endpoint | Roles Permitidos | Descripción y Reglas |
| :--- | :--- | :--- | :--- |
| `POST` | `/auth/login` | Público | Autentica credenciales y devuelve JWT + datos del usuario y rol. Registra `LOGIN_OK` o `LOGIN_FAIL` en bitácora. |
| `GET` | `/auth/perfil` | Todos | Obtiene los datos de sesión del usuario autenticado. |
| `GET` | `/usuarios` | `DIRECTOR` | Lista paginada del personal administrativo registrado. |
| `PATCH` | `/usuarios/{id}/estado` | `DIRECTOR` | Activa o desactiva la cuenta de un funcionario (`activo: true/false`). |
| `POST` | `/usuarios/invitaciones` | `DIRECTOR` | Crea invitación con token UUID temporal para nuevo personal (RF-05). |
| `POST` | `/auth/invitaciones/aceptar`| Público | Completa el registro de un usuario invitado validando el token UUID. |
| `POST` | `/usuarios/transferir-direccion` | `DIRECTOR` | **Crítico (CU-04):** Transfiere `ROLE_DIRECTOR` a otro usuario activo y degrada/desactiva al director saliente en una sola transacción ACID. |
| `GET` | `/auditoria` | `DIRECTOR` | Consulta paginada y filtrada de `bitacora_auditoria` (por fechas, usuario, módulo, acción). **Solo lectura.** |

#### Payload Crítico: `POST /api/v1/usuarios/transferir-direccion`
```json
// Request
{
  "nuevoDirectorUsuarioId": 4,
  "resolucionRespaldo": "RES-FICCT-2026-089",
  "observaciones": "Cambio de gestión conforme a resolución decanal",
  "passwordConfirmacionActual": "MiClaveActualSegura123!"
}
```

---

### 3.2. Paquete 2: Gestión Documental, Correspondencia y Trámites (`/documentos`, `/correspondencia`, `/tramites`)

| Método | Endpoint | Roles Permitidos | Descripción y Reglas |
| :--- | :--- | :--- | :--- |
| `POST` | `/documentos/upload` | Todos | Sube archivo (`multipart/form-data`) a Azure Blob Storage, calcula SHA-256 y crea registro en `documentos_digitales`. Opcionalmente dispara OCR (`ejecutarOcr: true`). |
| `GET` | `/documentos/{id}/url` | Todos | Genera y devuelve una SAS URL temporal de lectura (15 min) de Azure Blob Storage para renderizar el archivo en el navegador. |
| `GET` | `/correspondencia` | Todos | Lista paginada con filtros por `tipo` (`RECIBIDA`/`EMITIDA`), `estado` y rango de fechas. |
| `POST` | `/correspondencia` | `AUXILIAR`, `COORDINADOR`, `DIRECTOR` | Registra nueva correspondencia vinculada a un `documentoId`. |
| `POST` | `/correspondencia/{id}/derivar` | `AUXILIAR`, `COORDINADOR`, `DIRECTOR` | Registra pase/derivación en `seguimiento_correspondencia`. |
| `GET` | `/tramites` | Todos | Lista paginada de expedientes estudiantiles filtrable por `estadoActual`, `tipoTramiteId`, `registroUniversitario` y `periodo`. |
| `POST` | `/tramites` | `AUXILIAR`, `COORDINADOR` | Crea expediente de trámite en estado inicial `INGRESADO` y adjunta sus requisitos iniciales. |
| `GET` | `/tramites/{id}` | Todos | Obtiene detalle completo del expediente, requisitos adjuntos (con sus documentos y OCR) e historial de estados. |
| `PATCH`| `/tramites/{id}/requisitos/{reqId}` | `AUXILIAR`, `COORDINADOR` | Marca un requisito individual como `CONFORME` u `OBSERVADO`. |
| `POST` | `/tramites/{id}/transicion` | Según Matriz State Machine | **Crítico (RF-08/RF-09):** Ejecuta una transición válida en la Máquina de Estados del trámite. |

#### Matriz de Permisos de la Máquina de Estados (`POST /api/v1/tramites/{id}/transicion`)
* `INGRESADO` $\rightarrow$ `EN_VERIFICACION`: `ROLE_AUXILIAR`, `ROLE_COORDINADOR`
* `EN_VERIFICACION` $\rightarrow$ `OBSERVADO`: `ROLE_AUXILIAR`, `ROLE_COORDINADOR`
* `OBSERVADO` $\rightarrow$ `EN_VERIFICACION`: `ROLE_AUXILIAR`, `ROLE_COORDINADOR` (tras subsanar documento)
* `EN_VERIFICACION` $\rightarrow$ `CON_DICTAMEN`: `ROLE_COORDINADOR` (exige que todos los requisitos obligatorios estén en `CONFORME`)
* `CON_DICTAMEN` $\rightarrow$ `APROBADO` | `RECHAZADO`: **Exclusivo `ROLE_DIRECTOR`**
* (`APROBADO` | `RECHAZADO`) $\rightarrow$ `CONCLUIDO`: `ROLE_AUXILIAR`, `ROLE_COORDINADOR`, `ROLE_DIRECTOR`

```json
// Request: POST /api/v1/tramites/104/transicion
{
  "estadoDestino": "APROBADO",
  "comentarioODictamen": "Cumple con el promedio mínimo y convalidación de 6 materias según informe de coordinación.",
  "documentoRespaldoId": 215
}
```

---

### 3.3. Paquete 3: Planificación Académica y Carga Docente (`/periodos`, `/materias`, `/oferta-grupos`, `/migraciones`, `/carga-docente`)

| Método | Endpoint | Roles Permitidos | Descripción y Reglas |
| :--- | :--- | :--- | :--- |
| `GET` | `/planes-estudio` | Todos | Lista planes de estudio y sus mallas de materias + prerrequisitos. |
| `GET` | `/periodos` | Todos | Lista periodos académicos (`2026-1`, `2026-2`, etc.). |
| `POST` | `/periodos` | `COORDINADOR`, `DIRECTOR` | Apertura un nuevo periodo académico semestral. |
| `GET` | `/oferta-grupos` | Todos | Lista grupos ofertados filtrados por `periodoId`, `semestreNivel` o `siglaMateria`. |
| `POST` | `/oferta-grupos` | `COORDINADOR`, `DIRECTOR` | Registra o actualiza oferta de grupo y cupos planificados (RF-10). |
| `PATCH`| `/oferta-grupos/{id}/cierre-estadistico` | `COORDINADOR`, `DIRECTOR` | Registra `inscritosFinales`, `aprobados`, `reprobados`, `abandonos` y `demandaInsatisfecha` al cerrar el semestre (alimenta el dataset ML). |
| `GET` | `/migraciones` | Todos | Lista historial de migraciones de estudiantes entre planes (RF-11). |
| `POST` | `/migraciones` | `COORDINADOR`, `DIRECTOR` | Registra migración de plan de estudio de un estudiante. |
| `GET` | `/carga-docente` | Todos | Lista asignaciones docentes, interinatos, suplencias y acefalías por periodo (RF-12). |
| `POST` | `/carga-docente` | `COORDINADOR`, `DIRECTOR` | Asigna docente a un grupo ofertado con su resolución de respaldo. |

---

### 3.4. Paquete 4: Seguimiento Institucional (`/titulados`, `/investigaciones`, `/poa`, `/eventos`)

| Método | Endpoint | Roles Permitidos | Descripción y Reglas |
| :--- | :--- | :--- | :--- |
| `GET` | `/titulados` | Todos | Consulta memoria histórica de titulados filtrable por `modalidadGraduacion` y año (RF-13). |
| `POST` | `/titulados` | `AUXILIAR`, `COORDINADOR`, `DIRECTOR` | Registra nuevo titulado con su acta digitalizada. |
| `GET` | `/investigaciones` | Todos | Lista trabajos de grado e investigaciones por línea de investigación y tutor (RF-14). |
| `POST` | `/investigaciones` | `COORDINADOR`, `DIRECTOR` | Registra perfil o proyecto de investigación/grado. |
| `GET` | `/poa/{anioGestion}` | Todos | Obtiene el POA del año, actividades, presupuesto y porcentaje de avance acumulado (RF-15). |
| `POST` | `/poa/actividades/{id}/avance` | `COORDINADOR`, `DIRECTOR` | Registra un reporte de ejecución presupuestaria y física de una actividad POA. |
| `GET` | `/eventos` | Todos | Lista seminarios, talleres y congresos registrados (CU-20). |
| `POST` | `/eventos` | `AUXILIAR`, `COORDINADOR`, `DIRECTOR` | Crea un nuevo evento o taller académico. |

---

### 3.5. Paquete 5: Servicios Inteligentes — OCR, Búsqueda y Asistente (`/ocr`, `/busqueda`, `/asistente`)

| Método | Endpoint | Roles Permitidos | Descripción y Reglas |
| :--- | :--- | :--- | :--- |
| `POST` | `/ocr/procesar/{documentoId}` | `AUXILIAR`, `COORDINADOR`, `DIRECTOR` | Solicita a `ai-worker-api` el preprocesamiento OpenCV + extracción Tesseract del documento. Guarda estado `PENDIENTE_VALIDACION`. |
| `GET` | `/ocr/documento/{documentoId}` | Todos | Devuelve la SAS URL de la imagen/PDF junto con `textoExtraidoCrudo`, `textoValidadoHumano`, `nivelConfianza` y `estadoValidacion` para el **Visor Comparativo Dual**. |
| `PUT` | `/ocr/{procesoOcrId}/validar` | `AUXILIAR`, `COORDINADOR`, `DIRECTOR` | **Soberanía Humana (RF-18):** Guarda el texto corregido por el funcionario, cambia estado a `VALIDADO` y activa el trigger `tsvector` en PostgreSQL. |
| `GET` | `/busqueda/documental` | Todos | Búsqueda full-text sobre `vector_busqueda` (`tsvector`) combinada con filtros estructurados (tipo documento, fechas, estudiante). Solo devuelve documentos con OCR `VALIDADO`. |
| `POST` | `/asistente/consultar` | Todos | Envía consulta en lenguaje natural (texto o transcripción de voz) al asistente inteligente. Solo lectura y filtrada por el rol del usuario en sesión (RF-19, RF-23). |

#### Payload Crítico: `PUT /api/v1/ocr/{procesoOcrId}/validar`
```json
// Request desde el Visor Comparativo Dual en React
{
  "textoValidadoHumano": "UNIVERSIDAD AUTÓNOMA GABRIEL RENÉ MORENO\nFACULTAD DE INGENIERÍA EN CIENCIAS DE LA COMPUTACIÓN Y TELECOMUNICACIONES\nSOLICITUD DE CONVALIDACIÓN DE MATERIAS..."
}
```

---

### 3.6. Paquete 6: Analítica, Dashboard y Alertas Predictivas (`/dashboard`, `/predicciones`, `/alertas`)

| Método | Endpoint | Roles Permitidos | Descripción y Reglas |
| :--- | :--- | :--- | :--- |
| `GET` | `/dashboard/resumen` | Todos | Devuelve KPIs institucionales: trámites por estado, correspondencia pendiente, avance POA, titulados por modalidad y conteo de alertas activas por color de semáforo (RF-16). |
| `POST` | `/predicciones/entrenar` | `DIRECTOR`, `COORDINADOR` | Ordena a `ai-worker-api` reentrenar el modelo Random Forest con el histórico de `oferta_grupos_materia`. Verifica que $F1\text{-Score} \ge 0.80$. |
| `POST` | `/predicciones/simular/{periodoId}` | `DIRECTOR`, `COORDINADOR` | Ejecuta la inferencia de demanda de cupos para todas las materias del periodo objetivo y genera alertas automáticas si $P \ge 0.75$ (RF-25). |
| `GET` | `/predicciones/periodo/{periodoId}` | Todos | Lista la tabla de proyección de cupos, probabilidad de sobredemanda $P$, déficit estimado y semáforo por materia. |
| `GET` | `/alertas` | Todos | Lista alertas operativas (plazos vencidos) y predictivas (déficit de cupos) filtradas por `categoria`, `nivelCriticidad` (`ROJO`, `AMARILLO`, `VERDE`) y `estado`. |
| `POST` | `/alertas/{id}/atender` | `DIRECTOR`, `COORDINADOR` | **Soberanía Humana (RF-26):** Registra la decisión administrativa tomada ante una alerta y cambia su estado a `ATENDIDA` o `DESCARTADA`. |

---

### 3.7. Paquete 7: Automatización RPA (`/rpa`)

| Método | Endpoint | Roles Permitidos | Descripción y Reglas |
| :--- | :--- | :--- | :--- |
| `POST` | `/rpa/ejecutar` | `AUXILIAR`, `COORDINADOR`, `DIRECTOR` | Inicia una tarea RPA en `ai-worker-api` (`CONSOLIDACION_CORRESPONDENCIA`, `REPORTE_MASIVO_PDF`, `BORRADORES_CORREO`). |
| `GET` | `/rpa/ejecuciones` | Todos | Lista el historial de ejecuciones RPA, estados, métricas de registros procesados y enlaces al PDF resultante. |
| `POST` | `/rpa/ejecuciones/{id}/reintentar` | `AUXILIAR`, `COORDINADOR`, `DIRECTOR` | Reejecuta un flujo RPA fallido o con observaciones tras corregir los datos de entrada (CU-37). |

---

## 4. Contratos Internos (`core-api` $\rightarrow$ `ai-worker-api`)

Prefijo base interno: `http://ai-worker-api:8000/internal/v1`  
Cabecera de seguridad requerida en todas las llamadas:
```http
X-Internal-Api-Key: <INTERNAL_SHARED_SECRET>
Content-Type: application/json
```

### 4.1. Motor OCR (`POST /internal/v1/ocr/extract`)
`core-api` genera una SAS URL de lectura de Azure Blob Storage y solicita la extracción:
```json
// Request (Spring Boot -> FastAPI)
{
  "documento_id": 105,
  "azure_read_sas_url": "https://ficctstorage.blob.core.windows.net/ficct-sistemas-docs/tramites/2026/11/104/carta.pdf?sv=2022-11-02&sr=b&sp=r&sig=...",
  "tipo_mime": "application/pdf",
  "idioma": "spa"
}

// Response 200 OK (FastAPI -> Spring Boot)
{
  "documento_id": 105,
  "exito": true,
  "texto_extraido_crudo": "SOLICITUD DE CASO ESPECIAL...",
  "nivel_confianza": 92.45,
  "paginas_procesadas": 1,
  "pipeline_aplicado": ["grayscale", "gaussian_blur_3x3", "otsu_binarization", "deskew"],
  "error_detalle": null
}
```

### 4.2. Entrenamiento e Inferencia Machine Learning
#### A) Entrenar Modelo (`POST /internal/v1/ml/train`)
`ai-worker-api` lee las vistas históricas de `oferta_grupos_materia`, `materias` y `prerrequisitos_materia` desde PostgreSQL, entrena el `RandomForestClassifier` (comparando con `LogisticRegression`) y persiste el archivo `.joblib`:
```json
// Response 200 OK (FastAPI -> Spring Boot)
{
  "nombre_algoritmo": "RandomForestClassifier",
  "version_modelo": "rf_cupos_v2026_11",
  "ruta_archivo_joblib": "/app/models/rf_cupos_v2026_11.joblib",
  "precision_metrica": 0.8620,
  "recall_metrica": 0.8310,
  "f1_score": 0.8462,
  "cumple_umbral_produccion": true
}
```

#### B) Predecir Demanda de Cupos (`POST /internal/v1/ml/predict`)
```json
// Request (Spring Boot -> FastAPI)
{
  "periodo_objetivo_id": 12,
  "codigo_periodo": "2027-1",
  "version_modelo": "rf_cupos_v2026_11"
}

// Response 200 OK (FastAPI -> Spring Boot)
{
  "periodo_objetivo_id": 12,
  "predicciones": [
    {
      "materia_id": 34,
      "sigla": "INF511",
      "demanda_proyectada_estudiantes": 95,
      "cupos_actuales_planificados": 60,
      "deficit_estimado": 35,
      "probabilidad_sobredemanda": 0.8920,
      "nivel_criticidad_sugerido": "ROJO"
    },
    {
      "materia_id": 18,
      "sigla": "INF310",
      "demanda_proyectada_estudiantes": 78,
      "cupos_actuales_planificados": 80,
      "deficit_estimado": 0,
      "probabilidad_sobredemanda": 0.2100,
      "nivel_criticidad_sugerido": "VERDE"
    }
  ]
}
```

### 4.3. Asistente Inteligente NLP (`POST /internal/v1/assistant/query`)
```json
// Request (Spring Boot -> FastAPI)
{
  "usuario_id": 2,
  "rol_usuario": "ROLE_COORDINADOR",
  "canal_entrada": "TEXTO",
  "consulta_usuario": "¿Cuántos trámites de convalidación están observados en el periodo 2026-2 y qué materias tienen alerta roja de cupos?"
}

// Response 200 OK (FastAPI -> Spring Boot)
{
  "respuesta_generada": "Actualmente existen 4 trámites de CONVALIDACIÓN en estado OBSERVADO para el periodo 2026-2. Respecto a la planificación académica, las materias con alerta ROJA (P >= 0.75) son INF511 (déficit estimado: 35 cupos) e INF412 (déficit estimado: 22 cupos).",
  "fuentes_consultadas": ["tramites_estudiantiles", "alertas", "predicciones_demanda_cupos"],
  "tiempo_respuesta_ms": 420
}
```

### 4.4. Ejecución de Trabajadores RPA (`POST /internal/v1/rpa/execute`)
`core-api` genera una SAS URL de escritura en Azure Blob Storage para que Playwright deposite directamente el PDF consolidado generado:
```json
// Request (Spring Boot -> FastAPI)
{
  "ejecucion_id": 19,
  "tipo_flujo": "REPORTE_MASIVO_PDF",
  "parametros": {
    "periodo_codigo": "2026-2",
    "incluir_correspondencia": true,
    "incluir_tramites_concluidos": true
  },
  "azure_write_sas_url": "https://ficctstorage.blob.core.windows.net/ficct-sistemas-docs/rpa/2026/11/reporte_consolidado_19.pdf?sv=2022-11-02&sr=b&sp=cw&sig=..."
}

// Response 200 OK (FastAPI -> Spring Boot)
{
  "ejecucion_id": 19,
  "estado": "COMPLETADO",
  "registros_procesados": 84,
  "registros_con_error": 0,
  "ruta_blob_generado": "/rpa/2026/11/reporte_consolidado_19.pdf",
  "hash_sha256": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
  "tamano_bytes": 458920,
  "log_incidencias": []
}
```