-- V5: Módulo de Seguimiento Institucional, POA, Alertas Predictivas y RPA (RF-13, RF-14, RF-15, RF-24, RF-25, RF-26)

-- 1. Titulados por Modalidad de Graduación (RF-13)
CREATE TABLE IF NOT EXISTS titulados (
    id BIGSERIAL PRIMARY KEY,
    expediente_id BIGINT NOT NULL UNIQUE REFERENCES expedientes_estudiantiles(id),
    modalidad_graduacion VARCHAR(50) NOT NULL, -- 'TESIS', 'PROYECTO_GRADO', 'EXAMEN_GRADO', 'EXCELENCIA', 'DIRIGIDO'
    fecha_defensa DATE NOT NULL,
    nota_final NUMERIC(5,2) NOT NULL,
    numero_titulo_provision VARCHAR(100),
    documento_acta_id BIGINT REFERENCES documentos_digitales(id),
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2. Trabajos de Grado e Investigaciones (RF-14)
CREATE TABLE IF NOT EXISTS trabajos_grado_investigacion (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(300) NOT NULL,
    tipo VARCHAR(40) NOT NULL, -- 'TESIS', 'PROYECTO_GRADO', 'INVESTIGACION_DOCENTE'
    expediente_id BIGINT REFERENCES expedientes_estudiantiles(id),
    docente_tutor_id BIGINT NOT NULL REFERENCES docentes(id),
    linea_investigacion VARCHAR(150),
    estado VARCHAR(30) NOT NULL DEFAULT 'EN_DESARROLLO', -- 'PERFIL_APROBADO', 'EN_DESARROLLO', 'DEFENDIDO', 'CANCELADO'
    fecha_registro DATE NOT NULL DEFAULT CURRENT_DATE
);

-- 3. Plan Operativo Anual (POA) - Gestiones Anuales (RF-15)
CREATE TABLE IF NOT EXISTS poa_gestiones (
    id BIGSERIAL PRIMARY KEY,
    anio_gestion INT NOT NULL UNIQUE,
    presupuesto_total NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    estado VARCHAR(30) NOT NULL DEFAULT 'EN_EJECUCION', -- 'FORMULADO', 'EN_EJECUCION', 'CERRADO'
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 4. Actividades y Metas POA
CREATE TABLE IF NOT EXISTS poa_actividades (
    id BIGSERIAL PRIMARY KEY,
    poa_gestion_id BIGINT NOT NULL REFERENCES poa_gestiones(id) ON DELETE CASCADE,
    codigo_actividad VARCHAR(30) NOT NULL,
    nombre_actividad VARCHAR(250) NOT NULL,
    indicador_meta VARCHAR(200) NOT NULL,
    presupuesto_asignado NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    porcentaje_avance NUMERIC(5,2) NOT NULL DEFAULT 0.00,
    responsable_usuario_id BIGINT REFERENCES usuarios(id)
);

-- 5. Seguimiento de Avances del POA
CREATE TABLE IF NOT EXISTS poa_seguimiento_avance (
    id BIGSERIAL PRIMARY KEY,
    poa_actividad_id BIGINT NOT NULL REFERENCES poa_actividades(id) ON DELETE CASCADE,
    porcentaje_reportado NUMERIC(5,2) NOT NULL,
    monto_ejecutado NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    descripcion_logros TEXT NOT NULL,
    documento_respaldo_id BIGINT REFERENCES documentos_digitales(id),
    fecha_reporte TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 6. Sistema Unificado de Alertas Operativas y Predictivas (RF-24, RF-25, RF-26)
CREATE TABLE IF NOT EXISTS alertas (
    id BIGSERIAL PRIMARY KEY,
    categoria VARCHAR(40) NOT NULL, -- 'PREDICTIVA_CUPOS', 'OPERATIVA_TRAMITE_VENCIDO', 'OPERATIVA_ACEFALIA'
    nivel_criticidad VARCHAR(20) NOT NULL, -- 'VERDE', 'AMARILLO', 'ROJO'
    titulo VARCHAR(200) NOT NULL,
    mensaje_detalle TEXT NOT NULL,
    entidad_origen VARCHAR(80),
    entidad_origen_id BIGINT,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE', -- 'PENDIENTE', 'EN_REVISION', 'ATENDIDA', 'DESCARTADA'
    fecha_generacion TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_alertas_estado_criticidad ON alertas (estado, nivel_criticidad);

-- 7. Atención y Soberanía Humana sobre Alertas (RF-26)
CREATE TABLE IF NOT EXISTS atencion_alertas (
    id BIGSERIAL PRIMARY KEY,
    alerta_id BIGINT NOT NULL REFERENCES alertas(id) ON DELETE CASCADE,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    accion_tomada TEXT NOT NULL,
    fecha_atencion TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 8. Control de Ejecuciones Automatizadas RPA (CU-31 a CU-37)
CREATE TABLE IF NOT EXISTS ejecuciones_rpa (
    id BIGSERIAL PRIMARY KEY,
    tipo_flujo VARCHAR(60) NOT NULL, -- 'CONSOLIDACION_CORRESPONDENCIA', 'REPORTE_MASIVO_PDF', 'BORRADORES_CORREO'
    ejecutado_por_usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    estado VARCHAR(30) NOT NULL DEFAULT 'EN_PROCESO', -- 'EN_PROCESO', 'COMPLETADO', 'FALLIDO'
    registros_procesados INT NOT NULL DEFAULT 0,
    registros_con_error INT NOT NULL DEFAULT 0,
    documento_resultado_id BIGINT REFERENCES documentos_digitales(id),
    log_incidencias JSONB,
    fecha_inicio TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    fecha_fin TIMESTAMPTZ
);

