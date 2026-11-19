-- ==============================================================================
-- V2__init_documental_y_tramites.sql
-- Módulo 02: Gestión Documental, OCR, Correspondencia y Trámites Estudiantiles
-- ==============================================================================

-- 1. Repositorio de metadatos de archivos digitales alojados en almacenamiento físico
CREATE TABLE IF NOT EXISTS documentos_digitales (
    id BIGSERIAL PRIMARY KEY,
    nombre_original VARCHAR(255) NOT NULL,
    nombre_almacenamiento VARCHAR(255) NOT NULL UNIQUE,
    ruta_almacenamiento TEXT NOT NULL,
    tipo_mime VARCHAR(100) NOT NULL,
    tamanio_bytes BIGINT NOT NULL,
    hash_sha256 VARCHAR(64) NOT NULL, -- Verificación obligatoria de integridad (RN-04)
    estado_ocr VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE_VALIDACION', -- 'PENDIENTE_VALIDACION', 'VALIDADO', 'CORREGIDO', 'RECHAZADO'
    confianza_promedio_ocr NUMERIC(5, 2), -- 0.00% a 100.00%
    subido_por_usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_documentos_hash ON documentos_digitales (hash_sha256);
CREATE INDEX IF NOT EXISTS idx_documentos_estado_ocr ON documentos_digitales (estado_ocr);

-- 2. Páginas de texto extraídas por OCR asociadas al documento
CREATE TABLE IF NOT EXISTS documento_paginas_ocr (
    id BIGSERIAL PRIMARY KEY,
    documento_id BIGINT NOT NULL REFERENCES documentos_digitales(id) ON DELETE CASCADE,
    numero_pagina INT NOT NULL,
    texto_extraido_bruto TEXT,
    texto_corregido TEXT,
    confianza_pagina NUMERIC(5, 2),
    tiempo_procesamiento_ms INT,
    vector_busqueda TSVECTOR, -- Búsqueda de texto completo PostgreSQL
    CONSTRAINT uq_documento_pagina UNIQUE (documento_id, numero_pagina)
);

CREATE INDEX IF NOT EXISTS idx_paginas_vector ON documento_paginas_ocr USING GIN (vector_busqueda);

-- 3. Módulo de Correspondencia Institucional (Entrante / Saliente)
CREATE TABLE IF NOT EXISTS correspondencia (
    id BIGSERIAL PRIMARY KEY,
    codigo_cite VARCHAR(80) NOT NULL UNIQUE, -- ej. FICCT-SIS-CITE-2026-001
    tipo VARCHAR(20) NOT NULL, -- 'ENTRANTE', 'SALIENTE', 'INTERNO'
    remitente_institucion VARCHAR(150) NOT NULL,
    remitente_persona VARCHAR(150) NOT NULL,
    destinatario VARCHAR(150) NOT NULL,
    referencia TEXT NOT NULL,
    fecha_recepcion_emision TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    estado VARCHAR(30) NOT NULL DEFAULT 'RECIBIDO', -- 'RECIBIDO', 'EN_REVISION', 'DERIVADO', 'ARCHIVADO'
    usuario_receptor_id BIGINT NOT NULL REFERENCES usuarios(id),
    documento_adjunto_id BIGINT REFERENCES documentos_digitales(id),
    observaciones TEXT,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_correspondencia_cite ON correspondencia (codigo_cite);
CREATE INDEX IF NOT EXISTS idx_correspondencia_tipo ON correspondencia (tipo);

-- 4. Expedientes Estudiantiles
CREATE TABLE IF NOT EXISTS expedientes_estudiantiles (
    id BIGSERIAL PRIMARY KEY,
    registro_estudiantil VARCHAR(20) NOT NULL UNIQUE, -- Registro Universitario UAGRM
    cedula_identidad VARCHAR(20) NOT NULL UNIQUE,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    carrera VARCHAR(100) NOT NULL DEFAULT 'Ingeniería en Sistemas',
    correo VARCHAR(150),
    telefono VARCHAR(30),
    estado_academico VARCHAR(30) NOT NULL DEFAULT 'REGULAR', -- 'REGULAR', 'EGRESADO', 'TITULADO', 'SUSPENDIDO'
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_expediente_registro ON expedientes_estudiantiles (registro_estudiantil);
CREATE INDEX IF NOT EXISTS idx_expediente_ci ON expedientes_estudiantiles (cedula_identidad);

-- 5. Catálogo de Tipos de Trámites
CREATE TABLE IF NOT EXISTS tipos_tramites (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE, -- 'CAMBIO_CARRERA', 'HOMOLOGACION', 'SUSPENSION_SEMESTRE', etc.
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    dias_limite_atencion INT NOT NULL DEFAULT 5,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- 6. Trámites Estudiantiles (State Machine)
CREATE TABLE IF NOT EXISTS tramites_estudiantiles (
    id BIGSERIAL PRIMARY KEY,
    codigo_tramite VARCHAR(50) NOT NULL UNIQUE, -- ej. TR-2026-0001
    expediente_id BIGINT NOT NULL REFERENCES expedientes_estudiantiles(id),
    tipo_tramite_id BIGINT NOT NULL REFERENCES tipos_tramites(id),
    estado_actual VARCHAR(40) NOT NULL DEFAULT 'INGRESADO', -- 'INGRESADO', 'EN_VERIFICACION', 'OBSERVADO', 'CON_DICTAMEN', 'APROBADO', 'RECHAZADO', 'CONCLUIDO'
    dictamen_director TEXT,
    dictamen_coordinador TEXT,
    fecha_ingreso TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    fecha_conclusion TIMESTAMPTZ,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    actualizado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_tramites_codigo ON tramites_estudiantiles (codigo_tramite);
CREATE INDEX IF NOT EXISTS idx_tramites_estado ON tramites_estudiantiles (estado_actual);

-- 7. Historial Inmutable de Transiciones de Trámites (Auditoría de State Machine - RN-05)
CREATE TABLE IF NOT EXISTS historial_estados_tramite (
    id BIGSERIAL PRIMARY KEY,
    tramite_id BIGINT NOT NULL REFERENCES tramites_estudiantiles(id) ON DELETE CASCADE,
    estado_anterior VARCHAR(40) NOT NULL,
    estado_nuevo VARCHAR(40) NOT NULL,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    rol_en_momento VARCHAR(50) NOT NULL,
    observacion_o_dictamen TEXT,
    fecha_hora TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_historial_tramite ON historial_estados_tramite (tramite_id, fecha_hora DESC);

-- 8. Requisitos Documentales Adjuntos a los Trámites
CREATE TABLE IF NOT EXISTS requisitos_tramite (
    id BIGSERIAL PRIMARY KEY,
    tramite_id BIGINT NOT NULL REFERENCES tramites_estudiantiles(id) ON DELETE CASCADE,
    documento_id BIGINT NOT NULL REFERENCES documentos_digitales(id),
    nombre_requisito VARCHAR(150) NOT NULL,
    cumplido BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Semillas de Tipos de Trámites Comunes
INSERT INTO tipos_tramites (id, codigo, nombre, descripcion, dias_limite_atencion, activo) VALUES
(1, 'CAMBIO_CARRERA', 'Cambio de Carrera a Ingeniería en Sistemas', 'Solicitud de traspaso interno o de otra facultad', 7, TRUE),
(2, 'HOMOLOGACION', 'Homologación / Convalidación de Materias', 'Convalidación de materias cursadas en planes anteriores o universidades', 10, TRUE),
(3, 'SUSPENSION_TEMPORAL', 'Suspensión Temporal de Semestre', 'Pausa académica justificada por salud o trabajo', 5, TRUE),
(4, 'CERTIFICACION_NOTAS', 'Certificación Oficial de Calificaciones', 'Emisión de certificados y avance curricular', 3, TRUE)
ON CONFLICT (id) DO NOTHING;

SELECT setval('tipos_tramites_id_seq', (SELECT MAX(id) FROM tipos_tramites));

