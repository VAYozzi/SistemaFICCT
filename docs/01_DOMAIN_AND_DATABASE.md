# 01_DOMAIN_AND_DATABASE.md
# Modelo de Dominio y Esquema de Base de Datos (PostgreSQL 15+)

## 1. Propósito y Convenciones Generales
Este documento define la estructura física y lógica de la base de datos relacional en **PostgreSQL 15+**. Sirve como fuente única de verdad para generar las migraciones SQL de **Flyway** en `core-api` (Spring Boot) y los modelos de lectura/analítica en `ai-worker-api` (FastAPI).

### Reglas de Diseño
* **Nomenclatura:** `snake_case` en plural para tablas, `snake_case` en singular para columnas.
* **Tipos Estándar:**
  * Identificadores principales: `BIGSERIAL PRIMARY KEY`.
  * Fechas y horas: `TIMESTAMPTZ` (siempre con zona horaria UTC/local).
  * Metadatos dinámicos y auditoría: `JSONB`.
  * Búsqueda de texto completo OCR: `TSVECTOR` indexado con `GIN`.
* **Almacenamiento de Archivos:** Los binarios (PDF, JPG, PNG) **no** se almacenan en PostgreSQL. Se alojan en **Azure Blob Storage** (o contenedor local equivalente en desarrollo) bajo la estructura jerárquica: `/{modulo}/{anio}/{mes}/{entidad_id}/{uuid}_{nombre_archivo}`. La base de datos almacena únicamente la ruta/URL (`url_blob`), el hash de integridad (`hash_sha256`) y los metadatos.

---

## 2. Paquete 1: Seguridad, Usuarios, Roles y Auditoría

```sql
-- Catálogo de roles institucionales (RBAC)
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE, -- 'ROLE_DIRECTOR', 'ROLE_COORDINADOR', 'ROLE_AUXILIAR'
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Usuarios internos de la Dirección de Carrera
CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    rol_id BIGINT NOT NULL REFERENCES roles(id),
    nombre_completo VARCHAR(150) NOT NULL,
    correo_institucional VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    cargo VARCHAR(100) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    ultimo_acceso TIMESTAMPTZ,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Invariante: Solo puede existir UN usuario activo con el rol de Director (ej. rol_id = 1)
-- Se garantiza mediante índice parcial único:
CREATE UNIQUE INDEX idx_unico_director_activo 
ON usuarios (rol_id) 
WHERE activo = TRUE AND rol_id = 1;

-- Invitaciones de acceso temporal (RF-05)
CREATE TABLE invitaciones_acceso (
    id BIGSERIAL PRIMARY KEY,
    correo_destino VARCHAR(150) NOT NULL,
    rol_asignado_id BIGINT NOT NULL REFERENCES roles(id),
    token_invitacion UUID NOT NULL UNIQUE,
    creado_por_usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    fecha_expiracion TIMESTAMPTZ NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE', -- 'PENDIENTE', 'ACEPTADA', 'EXPIRADA', 'REVOCADA'
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Historial de transferencia de autoridad de Dirección (CU-04)
CREATE TABLE historial_transferencia_direccion (
    id BIGSERIAL PRIMARY KEY,
    director_saliente_id BIGINT NOT NULL REFERENCES usuarios(id),
    director_entrante_id BIGINT NOT NULL REFERENCES usuarios(id),
    resolucion_respaldo VARCHAR(150) NOT NULL,
    observaciones TEXT,
    fecha_transferencia TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Bitácora de auditoría inmutable (RF-21 / CU-03)
-- PROHIBIDO realizar UPDATE o DELETE sobre esta tabla a nivel de aplicación.
CREATE TABLE bitacora_auditoria (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT REFERENCES usuarios(id), -- Puede ser NULL en intentos fallidos de login
    correo_actor VARCHAR(150),
    direccion_ip VARCHAR(45) NOT NULL,
    modulo VARCHAR(80) NOT NULL,
    accion VARCHAR(50) NOT NULL, -- 'CREATE', 'UPDATE', 'DELETE', 'LOGIN_OK', 'LOGIN_FAIL', 'STATE_CHANGE', 'TRANSFERENCIA'
    entidad_afectada VARCHAR(100) NOT NULL,
    entidad_id VARCHAR(50),
    datos_anteriores JSONB,
    datos_nuevos JSONB,
    fecha_hora TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_auditoria_fecha ON bitacora_auditoria (fecha_hora DESC);
CREATE INDEX idx_auditoria_usuario ON bitacora_auditoria (usuario_id);
```

---

## 3. Paquete 2: Gestión Documental, OCR, Correspondencia y Trámites

```sql
-- Repositorio de metadatos de archivos alojados en Azure Blob Storage
CREATE TABLE documentos_digitales (
    id BIGSERIAL PRIMARY KEY,
    nombre_original VARCHAR(255) NOT NULL,
    ruta_blob VARCHAR(500) NOT NULL UNIQUE, -- Ej: /tramites/2026/08/104/uuid_carta.pdf
    contenedor_azure VARCHAR(100) NOT NULL DEFAULT 'ficct-sistemas-docs',
    tipo_mime VARCHAR(100) NOT NULL, -- 'application/pdf', 'image/jpeg', 'image/png'
    tamano_bytes BIGINT NOT NULL,
    hash_sha256 VARCHAR(64) NOT NULL,
    subido_por_usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Resultados del motor OpenCV + Tesseract OCR (RF-17, RF-18, RF-22)
CREATE TABLE procesos_ocr (
    id BIGSERIAL PRIMARY KEY,
    documento_id BIGINT NOT NULL UNIQUE REFERENCES documentos_digitales(id) ON DELETE CASCADE,
    texto_extraido_crudo TEXT,
    texto_validado_humano TEXT,
    nivel_confianza NUMERIC(5,2), -- Porcentaje 0.00 a 100.00
    estado_validacion VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE_VALIDACION', -- 'PENDIENTE_VALIDACION', 'VALIDADO', 'ERROR_OCR'
    validado_por_usuario_id BIGINT REFERENCES usuarios(id),
    fecha_validacion TIMESTAMPTZ,
    vector_busqueda TSVECTOR, -- Se puebla ÚNICAMENTE cuando estado_validacion = 'VALIDADO'
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ocr_vector_busqueda ON procesos_ocr USING GIN (vector_busqueda);

-- Correspondencia institucional recibida y emitida (RF-06)
CREATE TABLE correspondencia (
    id BIGSERIAL PRIMARY KEY,
    codigo_correlativo VARCHAR(50) NOT NULL UNIQUE, -- Ej: 'DIR-SIS-2026-0045'
    tipo VARCHAR(20) NOT NULL, -- 'RECIBIDA', 'EMITIDA'
    remitente VARCHAR(200) NOT NULL,
    destinatario VARCHAR(200) NOT NULL,
    asunto VARCHAR(300) NOT NULL,
    resumen TEXT,
    fecha_documento DATE NOT NULL,
    fecha_registro TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    estado VARCHAR(30) NOT NULL DEFAULT 'REGISTRADO', -- 'REGISTRADO', 'DERIVADO', 'RESPONDIDO', 'ARCHIVADO'
    documento_id BIGINT REFERENCES documentos_digitales(id),
    registrado_por_usuario_id BIGINT NOT NULL REFERENCES usuarios(id)
);

-- Derivaciones y seguimiento de correspondencia
CREATE TABLE seguimiento_correspondencia (
    id BIGSERIAL PRIMARY KEY,
    correspondencia_id BIGINT NOT NULL REFERENCES correspondencia(id) ON DELETE CASCADE,
    usuario_origen_id BIGINT NOT NULL REFERENCES usuarios(id),
    usuario_destino_id BIGINT REFERENCES usuarios(id),
    area_destino VARCHAR(150),
    instruccion TEXT NOT NULL,
    fecha_derivacion TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Padrón básico de estudiantes solicitantes
CREATE TABLE estudiantes (
    id BIGSERIAL PRIMARY KEY,
    registro_universitario VARCHAR(20) NOT NULL UNIQUE,
    cedula_identidad VARCHAR(20) NOT NULL UNIQUE,
    nombre_completo VARCHAR(150) NOT NULL,
    correo VARCHAR(150),
    telefono VARCHAR(30),
    codigo_plan_actual VARCHAR(20) NOT NULL, -- Ej: '187-3', '187-4'
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Catálogo de tipos de trámites estudiantiles (RF-07)
CREATE TABLE tipos_tramite (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE, -- 'CAMBIO_CARRERA', 'CARRERA_PARALELA', 'CASO_ESPECIAL', 'CONVALIDACION'
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    plazo_maximo_dias INT NOT NULL DEFAULT 10, -- Base para alertas administrativas por vencimiento
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Requisitos documentales exigidos por tipo de trámite
CREATE TABLE requisitos_tipo_tramite (
    id BIGSERIAL PRIMARY KEY,
    tipo_tramite_id BIGINT NOT NULL REFERENCES tipos_tramite(id) ON DELETE CASCADE,
    nombre_requisito VARCHAR(200) NOT NULL,
    es_obligatorio BOOLEAN NOT NULL DEFAULT TRUE
);

-- Expedientes de trámites estudiantiles gobernados por State Machine (RF-08, RF-09)
CREATE TABLE tramites_estudiantiles (
    id BIGSERIAL PRIMARY KEY,
    codigo_expediente VARCHAR(50) NOT NULL UNIQUE, -- Ej: 'EXP-2026-0012'
    estudiante_id BIGINT NOT NULL REFERENCES estudiantes(id),
    tipo_tramite_id BIGINT NOT NULL REFERENCES tipos_tramite(id),
    periodo_academico_codigo VARCHAR(20) NOT NULL, -- Ej: '2026-2'
    motivo_solicitud TEXT NOT NULL,
    estado_actual VARCHAR(30) NOT NULL DEFAULT 'INGRESADO',
    -- Estados estrictos: 'INGRESADO', 'EN_VERIFICACION', 'OBSERVADO', 'CON_DICTAMEN', 'APROBADO', 'RECHAZADO', 'CONCLUIDO'
    fecha_ingreso TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    fecha_limite_estimada DATE NOT NULL,
    fecha_cierre TIMESTAMPTZ,
    registrado_por_usuario_id BIGINT NOT NULL REFERENCES usuarios(id)
);

CREATE INDEX idx_tramites_estado ON tramites_estudiantiles (estado_actual);
CREATE INDEX idx_tramites_estudiante ON tramites_estudiantiles (estudiante_id);

-- Documentos adjuntos por cada requisito del trámite
CREATE TABLE requisitos_presentados_tramite (
    id BIGSERIAL PRIMARY KEY,
    tramite_id BIGINT NOT NULL REFERENCES tramites_estudiantiles(id) ON DELETE CASCADE,
    requisito_tipo_id BIGINT NOT NULL REFERENCES requisitos_tipo_tramite(id),
    documento_id BIGINT REFERENCES documentos_digitales(id),
    estado_revision VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE', -- 'PENDIENTE', 'CONFORME', 'OBSERVADO'
    observacion VARCHAR(300)
);

-- Historial inmutable de transiciones de la Máquina de Estados
CREATE TABLE historial_estados_tramite (
    id BIGSERIAL PRIMARY KEY,
    tramite_id BIGINT NOT NULL REFERENCES tramites_estudiantiles(id) ON DELETE CASCADE,
    estado_anterior VARCHAR(30),
    estado_nuevo VARCHAR(30) NOT NULL,
    comentario_o_dictamen TEXT NOT NULL,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    rol_en_momento VARCHAR(50) NOT NULL,
    fecha_transicion TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
```

---

## 4. Paquete 3: Planificación Académica, Cupos y Carga Docente

```sql
-- Planes de estudio (Mallas curriculares)
CREATE TABLE planes_estudio (
    id BIGSERIAL PRIMARY KEY,
    codigo_plan VARCHAR(20) NOT NULL UNIQUE, -- Ej: '187-3', '187-4'
    nombre VARCHAR(150) NOT NULL,
    anio_vigencia INT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Asignaturas de la carrera
CREATE TABLE materias (
    id BIGSERIAL PRIMARY KEY,
    plan_estudio_id BIGINT NOT NULL REFERENCES planes_estudio(id),
    sigla VARCHAR(20) NOT NULL, -- Ej: 'INF511', 'MAT101'
    nombre VARCHAR(150) NOT NULL,
    semestre_nivel INT NOT NULL, -- 1 al 10
    creditos INT NOT NULL,
    UNIQUE (plan_estudio_id, sigla)
);

-- Malla de prerrequisitos (Feature crítico para el flujo curricular en Machine Learning)
CREATE TABLE prerrequisitos_materia (
    materia_id BIGINT NOT NULL REFERENCES materias(id) ON DELETE CASCADE,
    prerrequisito_id BIGINT NOT NULL REFERENCES materias(id) ON DELETE CASCADE,
    PRIMARY KEY (materia_id, prerrequisito_id)
);

-- Periodos académicos semestrales
CREATE TABLE periodos_academicos (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE, -- Ej: '2026-1', '2026-2'
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PLANIFICACION' -- 'PLANIFICACION', 'EN_CURSO', 'CERRADO'
);

-- Oferta de grupos, cupos y métricas históricas (RF-10 + Dataset de entrenamiento Random Forest)
CREATE TABLE oferta_grupos_materia (
    id BIGSERIAL PRIMARY KEY,
    periodo_id BIGINT NOT NULL REFERENCES periodos_academicos(id),
    materia_id BIGINT NOT NULL REFERENCES materias(id),
    codigo_grupo VARCHAR(10) NOT NULL, -- Ej: 'SA', 'SB', 'SC', 'Z1'
    cupo_ofertado INT NOT NULL,
    inscritos_finales INT NOT NULL DEFAULT 0,
    demanda_insatisfecha_estimada INT NOT NULL DEFAULT 0, -- Solicitudes de adición/casos especiales sin cupo
    aprobados INT NOT NULL DEFAULT 0,
    reprobados INT NOT NULL DEFAULT 0,
    abandonos INT NOT NULL DEFAULT 0,
    UNIQUE (periodo_id, materia_id, codigo_grupo)
);

-- Registro de migración de estudiantes entre planes de estudio (RF-11)
CREATE TABLE migraciones_plan_estudio (
    id BIGSERIAL PRIMARY KEY,
    estudiante_id BIGINT NOT NULL REFERENCES estudiantes(id),
    plan_origen_id BIGINT NOT NULL REFERENCES planes_estudio(id),
    plan_destino_id BIGINT NOT NULL REFERENCES planes_estudio(id),
    periodo_id BIGINT NOT NULL REFERENCES periodos_academicos(id),
    materias_homologadas INT NOT NULL DEFAULT 0,
    resolucion_respaldo VARCHAR(150),
    registrado_por_usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    fecha_migracion TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Plantel docente
CREATE TABLE docentes (
    id BIGSERIAL PRIMARY KEY,
    codigo_docente VARCHAR(30) NOT NULL UNIQUE,
    nombre_completo VARCHAR(150) NOT NULL,
    correo_institucional VARCHAR(150),
    categoria VARCHAR(50), -- 'TITULAR', 'INTERINO', 'INVITADO'
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Cobertura de carga docente, interinatos y suplencias (RF-12)
CREATE TABLE cobertura_carga_docente (
    id BIGSERIAL PRIMARY KEY,
    oferta_grupo_id BIGINT NOT NULL REFERENCES oferta_grupos_materia(id),
    docente_id BIGINT NOT NULL REFERENCES docentes(id),
    tipo_designacion VARCHAR(30) NOT NULL, -- 'TITULAR', 'INTERINATO', 'SUPLENCIA'
    horas_semanales INT NOT NULL,
    resolucion_facultativa VARCHAR(150),
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    estado VARCHAR(30) NOT NULL DEFAULT 'VIGENTE' -- 'VIGENTE', 'CONCLUIDA', 'ACEFALIA'
);
```

---

## 5. Paquete 4: Seguimiento Institucional (Titulados, POA, Eventos)

```sql
-- Memoria histórica de titulados por modalidad (RF-13)
CREATE TABLE titulados (
    id BIGSERIAL PRIMARY KEY,
    estudiante_id BIGINT NOT NULL UNIQUE REFERENCES estudiantes(id),
    modalidad_graduacion VARCHAR(50) NOT NULL, -- 'TESIS', 'PROYECTO_GRADO', 'EXAMEN_GRADO', 'EXCELENCIA', 'DIRIGIDO'
    fecha_defensa DATE NOT NULL,
    nota_final NUMERIC(5,2),
    numero_titulo_provision VARCHAR(100),
    documento_acta_id BIGINT REFERENCES documentos_digitales(id)
);

-- Trabajos de grado e investigaciones registradas (RF-14)
CREATE TABLE trabajos_grado_investigacion (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(300) NOT NULL,
    tipo VARCHAR(40) NOT NULL, -- 'TESIS', 'PROYECTO_GRADO', 'INVESTIGACION_DOCENTE'
    estudiante_id BIGINT REFERENCES estudiantes(id),
    docente_tutor_id BIGINT NOT NULL REFERENCES docentes(id),
    linea_investigacion VARCHAR(150),
    estado VARCHAR(30) NOT NULL DEFAULT 'EN_DESARROLLO', -- 'PERFIL_APROBADO', 'EN_DESARROLLO', 'DEFENDIDO', 'CANCELADO'
    fecha_registro DATE NOT NULL DEFAULT CURRENT_DATE
);

-- Plan Operativo Anual (POA) - Cabecera por gestión (RF-15)
CREATE TABLE poa_gestiones (
    id BIGSERIAL PRIMARY KEY,
    anio_gestion INT NOT NULL UNIQUE,
    presupuesto_total NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    estado VARCHAR(30) NOT NULL DEFAULT 'EN_EJECUCION' -- 'FORMULADO', 'EN_EJECUCION', 'CERRADO'
);

-- Actividades y metas del POA
CREATE TABLE poa_actividades (
    id BIGSERIAL PRIMARY KEY,
    poa_gestion_id BIGINT NOT NULL REFERENCES poa_gestiones(id) ON DELETE CASCADE,
    codigo_actividad VARCHAR(30) NOT NULL,
    nombre_actividad VARCHAR(250) NOT NULL,
    indicador_meta VARCHAR(200) NOT NULL,
    presupuesto_asignado NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    porcentaje_avance NUMERIC(5,2) NOT NULL DEFAULT 0.00,
    responsable_usuario_id BIGINT REFERENCES usuarios(id)
);

-- Seguimiento periódico de avances del POA
CREATE TABLE poa_seguimiento_avance (
    id BIGSERIAL PRIMARY KEY,
    poa_actividad_id BIGINT NOT NULL REFERENCES poa_actividades(id) ON DELETE CASCADE,
    porcentaje_reportado NUMERIC(5,2) NOT NULL,
    monto_ejecutado NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    descripcion_logros TEXT NOT NULL,
    documento_respaldo_id BIGINT REFERENCES documentos_digitales(id),
    fecha_reporte TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Eventos académicos, seminarios y talleres (CU-20)
CREATE TABLE eventos_talleres (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    tipo VARCHAR(50) NOT NULL, -- 'SEMINARIO', 'TALLER', 'CONGRESO', 'CHARLA'
    fecha_inicio TIMESTAMPTZ NOT NULL,
    fecha_fin TIMESTAMPTZ NOT NULL,
    lugar_o_enlace VARCHAR(200),
    participantes_estimados INT,
    organizador_usuario_id BIGINT NOT NULL REFERENCES usuarios(id)
);
```

---

## 6. Paquetes 5, 6 y 7: Machine Learning, Alertas, Asistente y RPA

```sql
-- Registro de versiones y métricas del modelo Random Forest (RF-24)
CREATE TABLE modelos_predictivos_ml (
    id BIGSERIAL PRIMARY KEY,
    nombre_algoritmo VARCHAR(100) NOT NULL, -- 'RandomForestClassifier'
    version_modelo VARCHAR(50) NOT NULL,
    ruta_archivo_joblib VARCHAR(300) NOT NULL,
    precision_metrica NUMERIC(5,4) NOT NULL,
    recall_metrica NUMERIC(5,4) NOT NULL,
    f1_score NUMERIC(5,4) NOT NULL, -- Regla de negocio: debe ser >= 0.80 para pasar a producción
    activo BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_entrenamiento TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Resultados de inferencia de demanda de cupos por materia (RF-25)
CREATE TABLE predicciones_demanda_cupos (
    id BIGSERIAL PRIMARY KEY,
    modelo_id BIGINT NOT NULL REFERENCES modelos_predictivos_ml(id),
    periodo_objetivo_id BIGINT NOT NULL REFERENCES periodos_academicos(id),
    materia_id BIGINT NOT NULL REFERENCES materias(id),
    demanda_proyectada_estudiantes INT NOT NULL,
    cupos_actuales_planificados INT NOT NULL,
    deficit_estimado INT NOT NULL,
    probabilidad_sobredemanda NUMERIC(5,4) NOT NULL, -- Ej: 0.8450 (Si >= 0.75 dispara alerta roja)
    fecha_calculo TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Sistema unificado de Alertas Operativas y Predictivas (RF-24, RF-25, RF-26)
CREATE TABLE alertas (
    id BIGSERIAL PRIMARY KEY,
    categoria VARCHAR(30) NOT NULL, -- 'PREDICTIVA_CUPOS', 'OPERATIVA_TRAMITE_VENCIDO', 'OPERATIVA_ACEFALIA'
    nivel_criticidad VARCHAR(20) NOT NULL, -- 'VERDE', 'AMARILLO', 'ROJO'
    titulo VARCHAR(200) NOT NULL,
    mensaje_detalle TEXT NOT NULL,
    entidad_origen VARCHAR(80), -- 'predicciones_demanda_cupos', 'tramites_estudiantiles'
    entidad_origen_id BIGINT,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE', -- 'PENDIENTE', 'EN_REVISION', 'ATENDIDA', 'DESCARTADA'
    fecha_generacion TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_alertas_estado_criticidad ON alertas (estado, nivel_criticidad);

-- Registro de atención y decisiones humanas sobre alertas (Soberanía Humana - RF-26)
CREATE TABLE atencion_alertas (
    id BIGSERIAL PRIMARY KEY,
    alerta_id BIGINT NOT NULL REFERENCES alertas(id) ON DELETE CASCADE,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    accion_tomada TEXT NOT NULL, -- Ej: 'Se autorizó apertura de grupo paralelo SC con 45 cupos'
    fecha_atencion TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Trazabilidad de consultas al Asistente Inteligente NLP (RF-19, RF-23)
CREATE TABLE consultas_asistente (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    canal_entrada VARCHAR(20) NOT NULL, -- 'TEXTO', 'VOZ'
    consulta_usuario TEXT NOT NULL,
    respuesta_generada TEXT NOT NULL,
    tiempo_respuesta_ms INT,
    fecha_consulta TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Control de ejecuciones de robots RPA con Playwright (CU-31 a CU-37)
CREATE TABLE ejecuciones_rpa (
    id BIGSERIAL PRIMARY KEY,
    tipo_flujo VARCHAR(60) NOT NULL, -- 'CONSOLIDACION_CORRESPONDENCIA', 'REPORTE_MASIVO_PDF', 'BORRADORES_CORREO'
    ejecutado_por_usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    estado VARCHAR(30) NOT NULL DEFAULT 'EN_PROCESO', -- 'EN_PROCESO', 'COMPLETADO', 'COMPLETADO_CON_OBSERVACIONES', 'FALLIDO'
    registros_procesados INT NOT NULL DEFAULT 0,
    registros_con_error INT NOT NULL DEFAULT 0,
    documento_resultado_id BIGINT REFERENCES documentos_digitales(id), -- Ej: PDF consolidado generado
    log_incidencias JSONB,
    fecha_inicio TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    fecha_fin TIMESTAMPTZ
);
```

---

## 7. Triggers y Reglas de Integridad en PostgreSQL

Para garantizar que la búsqueda de texto completo del OCR (`vector_busqueda`) jamás indexe texto sin validación humana, se define el siguiente trigger nativo en PostgreSQL:

```sql
CREATE OR REPLACE FUNCTION fn_actualizar_vector_ocr()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.estado_validacion = 'VALIDADO' AND NEW.texto_validado_humano IS NOT NULL THEN
        NEW.vector_busqueda := to_tsvector('spanish', NEW.texto_validado_humano);
    ELSE
        NEW.vector_busqueda := NULL;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_procesos_ocr_vector
BEFORE INSERT OR UPDATE ON procesos_ocr
FOR EACH ROW
EXECUTE FUNCTION fn_actualizar_vector_ocr();
```