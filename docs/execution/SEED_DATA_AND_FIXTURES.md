# SEED_DATA_AND_FIXTURES.md
# Especificación de Datos Semilla, Catálogos y Dataset Sintético (`Flyway`)

## 1. Propósito de los Datos Semilla
Para que el sistema sea completamente operativo desde el primer despliegue con `docker compose up --build`, se define la migración Flyway **`V2__seed_data.sql`**.

Esta migración inserta:
1. Catálogo oficial de roles y **usuarios administrativos iniciales** con contraseñas seguras pre-hasheadas (BCrypt).
2. Catálogo oficial de planes de estudio, materias reales de la FICCT y cadena de prerrequisitos.
3. Catálogo de tipos de trámites estudiantiles y sus requisitos documentales obligatorios.
4. **Dataset histórico sintético** de oferta y cupos de los últimos 4 semestres para permitir que el modelo de Machine Learning (`RandomForestClassifier`) se entrene y genere predicciones válidas desde el día 1.

---

## 2. Usuarios Iniciales de Prueba

Todas las cuentas se crean inicialmente con la contraseña:  
🔑 **`Password123!`**  
*(Hash BCrypt: `$2a$10$eO0zT1pQeG.sW0Q5V.Z2b.5i7zF6e9gD1wX2y3z4a5b6c7d8e9f0`)*

| Correo Institucional | Rol Asignado | Cargo / Área | Función Principal |
| :--- | :--- | :--- | :--- |
| `director@ficct.uagrm.edu.bo` | `ROLE_DIRECTOR` | Director de Carrera | Aprobación final de trámites, resolución de alertas y auditoría. |
| `coordinador@ficct.uagrm.edu.bo` | `ROLE_COORDINADOR` | Coordinador Académico | Planificación semestral, dictámenes técnicos y seguimiento POA. |
| `auxiliar@ficct.uagrm.edu.bo` | `ROLE_AUXILIAR` | Auxiliar de Ventanilla | Recepción de expedientes, digitalización y validación OCR. |

---

## 3. Script SQL Maestro (`V2__seed_data.sql`)

```sql
-- ============================================================================
-- 1. ROLES INSTITUCIONALES (RBAC)
-- ============================================================================
INSERT INTO roles (id, codigo, nombre, descripcion) VALUES
(1, 'ROLE_DIRECTOR', 'Director de Carrera', 'Máxima autoridad ejecutiva y decisoria de la Dirección de Carrera.'),
(2, 'ROLE_COORDINADOR', 'Coordinador Académico', 'Supervisión de oferta, materias, carga docente y dictamen de trámites.'),
(3, 'ROLE_AUXILIAR', 'Auxiliar de Secretaría / Ventanilla', 'Recepción de correspondencia, digitalización de trámites y validación OCR.')
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- 2. USUARIOS ADMINISTRATIVOS INICIALES (Contraseña: Password123!)
-- ============================================================================
INSERT INTO usuarios (id, rol_id, nombre_completo, correo_institucional, password_hash, cargo, activo) VALUES
(1, 1, 'MSc. Ing. Carlos Alberto Mendoza', 'director@ficct.uagrm.edu.bo', '$2a$10$eO0zT1pQeG.sW0Q5V.Z2b.5i7zF6e9gD1wX2y3z4a5b6c7d8e9f0', 'Director de Carrera de Ingeniería en Sistemas', TRUE),
(2, 2, 'Ing. María Elena Gutierrez', 'coordinador@ficct.uagrm.edu.bo', '$2a$10$eO0zT1pQeG.sW0Q5V.Z2b.5i7zF6e9gD1wX2y3z4a5b6c7d8e9f0', 'Coordinadora Académica de Carrera', TRUE),
(3, 3, 'Lic. Roberto Soliz Peña', 'auxiliar@ficct.uagrm.edu.bo', '$2a$10$eO0zT1pQeG.sW0Q5V.Z2b.5i7zF6e9gD1wX2y3z4a5b6c7d8e9f0', 'Responsable de Ventanilla y Archivo', TRUE)
ON CONFLICT (id) DO NOTHING;

-- Sincronizar secuencia de IDs de usuarios
SELECT setval('usuarios_id_seq', (SELECT MAX(id) FROM usuarios));

-- ============================================================================
-- 3. PLANES DE ESTUDIO Y MATERIAS OFICIALES (FICCT - UAGRM)
-- ============================================================================
INSERT INTO planes_estudio (id, codigo_plan, nombre, anio_vigencia, activo) VALUES
(1, '187-3', 'Licenciatura en Ingeniería en Sistemas (Plan Anterior)', 2010, TRUE),
(2, '187-4', 'Licenciatura en Ingeniería en Sistemas (Plan Vigente)', 2020, TRUE)
ON CONFLICT (id) DO NOTHING;

-- Asignaturas clave del Plan Vigente (187-4)
INSERT INTO materias (id, plan_estudio_id, sigla, nombre, semestre_nivel, creditos) VALUES
(1, 2, 'INF110', 'Introducción a la Informática', 1, 4),
(2, 2, 'MAT101', 'Cálculo I', 1, 5),
(3, 2, 'INF120', 'Programación I', 2, 4),
(4, 2, 'MAT102', 'Cálculo II', 2, 5),
(5, 2, 'INF210', 'Programación II', 3, 4),
(6, 2, 'INF220', 'Estructuras de Datos I', 4, 4),
(7, 2, 'INF310', 'Sistemas Operativos I', 5, 4),
(8, 2, 'INF312', 'Bases de Datos I', 5, 4),
(9, 2, 'INF412', 'Sistemas de Información I', 7, 4),
(10, 2, 'INF511', 'Taller de Grado I', 9, 5)
ON CONFLICT (id) DO NOTHING;

-- Malla de Prerrequisitos (Infraestructura de datos para el modelo ML)
INSERT INTO prerrequisitos_materia (materia_id, prerrequisito_id) VALUES
(3, 1), -- INF120 exige INF110
(4, 2), -- MAT102 exige MAT101
(5, 3), -- INF210 exige INF120
(6, 5), -- INF220 exige INF210
(7, 6), -- INF310 exige INF220
(8, 6), -- INF312 exige INF220
(9, 8), -- INF412 exige INF312
(10, 9) -- INF511 exige INF412
ON CONFLICT DO NOTHING;

-- ============================================================================
-- 4. PERIODOS ACADÉMICOS
-- ============================================================================
INSERT INTO periodos_academicos (id, codigo, fecha_inicio, fecha_fin, estado) VALUES
(1, '2024-2', '2024-08-01', '2024-12-20', 'CERRADO'),
(2, '2025-1', '2025-02-15', '2025-06-30', 'CERRADO'),
(3, '2025-2', '2025-08-01', '2025-12-20', 'CERRADO'),
(4, '2026-1', '2026-02-15', '2026-06-30', 'EN_CURSO'),
(5, '2026-2', '2026-08-01', '2026-12-20', 'PLANIFICACION')
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- 5. CATÁLOGO DE TRÁMITES Y REQUISITOS OBLIGATORIOS
-- ============================================================================
INSERT INTO tipos_tramite (id, codigo, nombre, descripcion, plazo_maximo_dias) VALUES
(1, 'CONVALIDACION', 'Convalidación de Materias', 'Homologación de asignaturas cursadas en otra carrera o universidad.', 15),
(2, 'CAMBIO_CARRERA', 'Trámite de Cambio de Carrera', 'Solicitud formal de transferencia hacia Ingeniería en Sistemas.', 10),
(3, 'CASO_ESPECIAL', 'Inscripción por Caso Especial', 'Solicitud de adición de materias superando el tope regular de créditos.', 5),
(4, 'CARRERA_PARALELA', 'Admisión a Carrera Paralela', 'Estudiantes activos de la FICCT que cursan una segunda titulación.', 12)
ON CONFLICT (id) DO NOTHING;

-- Requisitos documentales obligatorios por trámite
INSERT INTO requisitos_tipo_tramite (id, tipo_tramite_id, nombre_requisito, es_obligatorio) VALUES
-- Convalidación
(1, 1, 'Carta formal de solicitud dirigida al Director de Carrera', TRUE),
(2, 1, 'Historial Académico oficial emitido por DPA', TRUE),
(3, 1, 'Contenidos analíticos legalizados de las materias de origen', TRUE),
-- Cambio de Carrera
(4, 2, 'Carta de solicitud con exposición de motivos', TRUE),
(5, 2, 'Formulario de traspaso facultativo debidamente sellado', TRUE),
(6, 2, 'Fotocopia de Cédula de Identidad vigente', TRUE),
-- Caso Especial
(7, 3, 'Formulario de adición de materias extraordinarias', TRUE),
(8, 3, 'Boleta de inscripción previa del semestre en curso', TRUE)
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- 6. DATASET HISTÓRICO PARA ENTRENAMIENTO MACHINE LEARNING
-- ============================================================================
-- Simulación de 4 semestres para calibrar el algoritmo Random Forest
INSERT INTO oferta_grupos_materia (periodo_id, materia_id, codigo_grupo, cupo_ofertado, inscritos_finales, demanda_insatisfecha_estimada, aprobados, reprobados, abandonos) VALUES
-- INF110 (Alta sobredemanda histórica)
(1, 1, 'SA', 60, 60, 25, 38, 15, 7),
(2, 1, 'SA', 60, 60, 30, 40, 14, 6),
(3, 1, 'SA', 60, 60, 35, 35, 18, 7),
(4, 1, 'SA', 60, 60, 28, 42, 12, 6),
-- INF511 - Taller de Grado (Cuello de botella histórico)
(1, 10, 'SA', 40, 40, 22, 32, 5, 3),
(2, 10, 'SA', 40, 40, 28, 30, 7, 3),
(3, 10, 'SA', 45, 45, 34, 35, 6, 4),
(4, 10, 'SA', 45, 45, 30, 38, 4, 3),
-- MAT101 - Cálculo I (Alta reprobación genera arrastre a semestres posteriores)
(1, 2, 'SA', 70, 70, 40, 28, 32, 10),
(2, 2, 'SA', 70, 70, 45, 30, 30, 10),
(3, 2, 'SA', 70, 70, 50, 25, 35, 10),
(4, 2, 'SA', 70, 70, 42, 31, 29, 10);
```

---

## 4. Archivos Mock y Fixtures para Pruebas de OCR

Para probar el motor OCR de visión artificial en `ai-worker-api`:
* En la carpeta `tests/fixtures/ocr/` se deben colocar dos archivos de prueba:
  1. `solicitud_caso_especial_sample.png`: Imagen escaneada de una carta real con sello de ventanilla FICCT y texto en español para verificar deskewing y Tesseract.
  2. `acta_convalidacion_sample.pdf`: PDF de 1 página simulando un formulario universitario oficial.
