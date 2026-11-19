-- ==============================================================================
-- V1__init_security_and_audit.sql
-- Módulo 01: Seguridad, Usuarios, Roles, Invitaciones y Bitácora de Auditoría
-- ==============================================================================

-- 1. Catálogo de roles institucionales (RBAC)
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE, -- 'ROLE_DIRECTOR', 'ROLE_COORDINADOR', 'ROLE_AUXILIAR'
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2. Usuarios internos de la Dirección de Carrera
CREATE TABLE IF NOT EXISTS usuarios (
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

-- Invariante de Negocio (RN-02): Solo puede existir UN usuario activo con el rol de Director
CREATE UNIQUE INDEX IF NOT EXISTS idx_unico_director_activo 
ON usuarios (rol_id) 
WHERE activo = TRUE AND rol_id = 1;

-- 3. Invitaciones de acceso temporal (RF-05)
CREATE TABLE IF NOT EXISTS invitaciones_acceso (
    id BIGSERIAL PRIMARY KEY,
    correo_destino VARCHAR(150) NOT NULL,
    rol_asignado_id BIGINT NOT NULL REFERENCES roles(id),
    token_invitacion UUID NOT NULL UNIQUE,
    creado_por_usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    fecha_expiracion TIMESTAMPTZ NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE', -- 'PENDIENTE', 'ACEPTADA', 'EXPIRADA', 'REVOCADA'
    creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 4. Historial de transferencia de autoridad de Dirección (CU-04 / RN-02)
CREATE TABLE IF NOT EXISTS historial_transferencia_direccion (
    id BIGSERIAL PRIMARY KEY,
    director_saliente_id BIGINT NOT NULL REFERENCES usuarios(id),
    director_entrante_id BIGINT NOT NULL REFERENCES usuarios(id),
    resolucion_respaldo VARCHAR(150) NOT NULL,
    observaciones TEXT,
    fecha_transferencia TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 5. Bitácora de auditoría inmutable (RF-21 / CU-03 / RN-03)
-- PROHIBIDO realizar UPDATE o DELETE sobre esta tabla a nivel de aplicación
CREATE TABLE IF NOT EXISTS bitacora_auditoria (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT REFERENCES usuarios(id),
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

CREATE INDEX IF NOT EXISTS idx_auditoria_fecha ON bitacora_auditoria (fecha_hora DESC);
CREATE INDEX IF NOT EXISTS idx_auditoria_usuario ON bitacora_auditoria (usuario_id);

-- Semillas iniciales obligatorias de Roles (ID 1 = ROLE_DIRECTOR garantizado)
INSERT INTO roles (id, codigo, nombre, descripcion) VALUES
(1, 'ROLE_DIRECTOR', 'Director de Carrera', 'Máxima autoridad ejecutiva y académica de la carrera'),
(2, 'ROLE_COORDINADOR', 'Coordinador Académico', 'Supervisión académica, grupos, cupos y revisión preliminar de trámites'),
(3, 'ROLE_AUXILIAR', 'Auxiliar de Oficina / Secretaría', 'Recepción de correspondencia, ventanilla de trámites y validación OCR')
ON CONFLICT (id) DO NOTHING;

-- Usuario semilla inicial: Director de Carrera provisional para primer acceso
-- Contraseña en texto plano para desarrollo: Director2026! (BCrypt $2a$10$wT0lVl8J5jH4L9n3nK2Wf.k7z5S4q9r2Y2u1e7V8x3s4t5r6q7u8)
INSERT INTO usuarios (id, rol_id, nombre_completo, correo_institucional, password_hash, cargo, activo) VALUES
(1, 1, 'Ing. Director de Sistemas', 'director.sistemas@uagrm.edu.bo', '$2a$10$wT0lVl8J5jH4L9n3nK2Wf.k7z5S4q9r2Y2u1e7V8x3s4t5r6q7u8', 'Director de Carrera', TRUE)
ON CONFLICT (id) DO NOTHING;

-- Resetear secuencia para IDs automáticos posteriores
SELECT setval('roles_id_seq', (SELECT MAX(id) FROM roles));
SELECT setval('usuarios_id_seq', (SELECT MAX(id) FROM usuarios));

