-- V4: Módulo de Planificación Académica, Cupos y Carga Docente (RF-10, RF-11, RF-12)

-- 1. Planes de Estudio
CREATE TABLE IF NOT EXISTS planes_estudio (
    id BIGSERIAL PRIMARY KEY,
    codigo_plan VARCHAR(20) NOT NULL UNIQUE, -- '187-3', '187-4'
    nombre VARCHAR(150) NOT NULL,
    anio_vigencia INT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2. Materias
CREATE TABLE IF NOT EXISTS materias (
    id BIGSERIAL PRIMARY KEY,
    plan_estudio_id BIGINT NOT NULL REFERENCES planes_estudio(id) ON DELETE CASCADE,
    sigla VARCHAR(20) NOT NULL, -- 'INF511', 'MAT101'
    nombre VARCHAR(150) NOT NULL,
    semestre_nivel INT NOT NULL, -- 1 al 10
    creditos INT NOT NULL,
    CONSTRAINT uq_plan_materia_sigla UNIQUE (plan_estudio_id, sigla)
);

-- 3. Prerrequisitos de Materias (Flujo curricular para ML)
CREATE TABLE IF NOT EXISTS prerrequisitos_materia (
    materia_id BIGINT NOT NULL REFERENCES materias(id) ON DELETE CASCADE,
    prerrequisito_id BIGINT NOT NULL REFERENCES materias(id) ON DELETE CASCADE,
    PRIMARY KEY (materia_id, prerrequisito_id)
);

-- 4. Periodos Académicos
CREATE TABLE IF NOT EXISTS periodos_academicos (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE, -- '2026-1', '2026-2'
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PLANIFICACION', -- 'PLANIFICACION', 'EN_CURSO', 'CERRADO'
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 5. Oferta de Grupos, Cupos y Demanda (Insumo directo de Random Forest)
CREATE TABLE IF NOT EXISTS oferta_grupos_materia (
    id BIGSERIAL PRIMARY KEY,
    periodo_id BIGINT NOT NULL REFERENCES periodos_academicos(id) ON DELETE CASCADE,
    materia_id BIGINT NOT NULL REFERENCES materias(id) ON DELETE CASCADE,
    codigo_grupo VARCHAR(10) NOT NULL, -- 'SA', 'SB', 'SC', 'Z1'
    cupo_ofertado INT NOT NULL,
    inscritos_finales INT NOT NULL DEFAULT 0,
    demanda_insatisfecha_estimada INT NOT NULL DEFAULT 0,
    aprobados INT NOT NULL DEFAULT 0,
    reprobados INT NOT NULL DEFAULT 0,
    abandonos INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_periodo_materia_grupo UNIQUE (periodo_id, materia_id, codigo_grupo)
);

-- 6. Docentes
CREATE TABLE IF NOT EXISTS docentes (
    id BIGSERIAL PRIMARY KEY,
    codigo_docente VARCHAR(30) NOT NULL UNIQUE,
    nombre_completo VARCHAR(150) NOT NULL,
    correo_institucional VARCHAR(150),
    categoria VARCHAR(50) NOT NULL DEFAULT 'TITULAR', -- 'TITULAR', 'INTERINO', 'INVITADO'
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 7. Cobertura de Carga Docente y Detección de Acefalías
CREATE TABLE IF NOT EXISTS cobertura_carga_docente (
    id BIGSERIAL PRIMARY KEY,
    oferta_grupo_id BIGINT NOT NULL REFERENCES oferta_grupos_materia(id) ON DELETE CASCADE,
    docente_id BIGINT NOT NULL REFERENCES docentes(id) ON DELETE CASCADE,
    tipo_designacion VARCHAR(30) NOT NULL DEFAULT 'TITULAR', -- 'TITULAR', 'INTERINATO', 'SUPLENCIA'
    horas_semanales INT NOT NULL DEFAULT 4,
    resolucion_facultativa VARCHAR(150),
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    estado VARCHAR(30) NOT NULL DEFAULT 'VIGENTE' -- 'VIGENTE', 'CONCLUIDA', 'ACEFALIA'
);

-- 8. Migraciones entre Planes de Estudio
CREATE TABLE IF NOT EXISTS migraciones_plan_estudio (
    id BIGSERIAL PRIMARY KEY,
    expediente_id BIGINT NOT NULL REFERENCES expedientes_estudiantiles(id),
    plan_origen_id BIGINT NOT NULL REFERENCES planes_estudio(id),
    plan_destino_id BIGINT NOT NULL REFERENCES planes_estudio(id),
    periodo_id BIGINT NOT NULL REFERENCES periodos_academicos(id),
    materias_homologadas INT NOT NULL DEFAULT 0,
    resolucion_respaldo VARCHAR(150),
    registrado_por_usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    fecha_migracion TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

