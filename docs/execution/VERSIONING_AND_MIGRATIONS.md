# VERSIONING_AND_MIGRATIONS.md
# Versionado Semántico y Gestión de Migraciones de Base de Datos (`Flyway`)

## 1. Versionado Semántico del Proyecto (SemVer 2.0)
El sistema adopta el estándar **Semantic Versioning 2.0.0** (`MAJOR.MINOR.PATCH`):

* **MAJOR (Versión Mayor):** Cambios que rompen compatibilidad en la API o reestructuraciones profundas de la arquitectura.
* **MINOR (Versión Menor):** Nuevas funcionalidades compatibles con versiones anteriores (ej. adición de un nuevo módulo funcional).
* **PATCH (Parche):** Corrección de errores y optimizaciones internas sin alterar los contratos de API.

### Hitos de Versión Planificados:
* `v0.1.0`: Infraestructura Docker, Seguridad, JWT, RBAC y Bitácora (`MOD_01`).
* `v0.2.0`: Núcleo Documental, Azure Blob y Máquina de Estados de Trámites (`MOD_02`).
* `v0.3.0`: Motor OpenCV + Tesseract OCR y Visor Comparativo Dual (`MOD_05`).
* `v0.4.0`: Planificación Académica y Modelo Predictivo Random Forest (`MOD_03` y `MOD_06`).
* `v0.5.0`: Extensión Institucional, Bots RPA y Asistente NLP (`MOD_04` y `MOD_07`).
* `v1.0.0`: Versión final validada y lista para la defensa de grado.

---

## 2. Gestión de Migraciones de Base de Datos con Flyway

Todas las modificaciones al esquema relacional de PostgreSQL se gestionan mediante **Flyway** en `core-api`, ubicadas en:  
`core-api/src/main/resources/db/migration/`

### 2.1. Convención de Nomenclatura Estricta
```text
V<NúmeroVersion>__<descripcion_corta_en_snake_case>.sql
```
*(Nota obligatoria: Se utilizan **dos guiones bajos consecutivos `__`** entre el número y la descripción).*

### 2.2. Catálogo Oficial de Migraciones Versionadas:
1. `V1__init_security_and_audit.sql`:
   * Creación de `roles`, `usuarios`, `invitaciones_acceso`, `historial_transferencia_direccion` y `bitacora_auditoria`.
   * Creación del índice único parcial `idx_unico_director_activo`.
2. `V2__seed_data.sql`:
   * Inserción de roles, 3 usuarios de prueba con BCrypt, catálogo de planes y dataset sintético ML.
3. `V3__documental_and_tramites.sql`:
   * Creación de `documentos_digitales`, `correspondencia`, `seguimiento_correspondencia`, `estudiantes`, `tipos_tramite`, `requisitos_tipo_tramite`, `tramites_estudiantiles`, `requisitos_presentados_tramite`, `historial_estados_tramite`.
4. `V4__ocr_and_fulltext.sql`:
   * Creación de `procesos_ocr` con columna `tsvector` e índice GIN.
   * Creación del trigger nativo `trg_procesos_ocr_vector` y la función `fn_actualizar_vector_ocr()`.
5. `V5__planificacion_academica.sql`:
   * Creación de `planes_estudio`, `materias`, `prerrequisitos_materia`, `periodos_academicos`, `oferta_grupos_materia`, `migraciones_plan_estudio`, `docentes`, `cobertura_carga_docente`.
6. `V6__institucional_and_extension.sql`:
   * Creación de `titulados`, `trabajos_grado_investigacion`, `poa_gestiones`, `poa_actividades`, `poa_seguimiento_avance`, `eventos_talleres`.
7. `V7__ml_alertas_and_rpa.sql`:
   * Creación de `modelos_predictivos_ml`, `predicciones_demanda_cupos`, `alertas`, `atencion_alertas`, `consultas_asistente` y `ejecuciones_rpa`.

---

## 3. Reglas Inmutables de Migración

1. **PROHIBICIÓN DE MODIFICAR SCRIPTS APLICADOS:**
   * Una vez que un archivo `V<N>__*.sql` ha sido ejecutado en cualquier entorno, su suma de verificación (*checksum*) queda registrada en la tabla `flyway_schema_history`.
   * **Nunca se edita un archivo de migración ya aplicado.** Cualquier ajuste o corrección debe realizarse en un nuevo archivo incremental (`V<N+1>__*.sql`).
2. **Scripts Idempotentes y DDL Transaccional:**
   * Las migraciones deben escribirse de forma transaccional; si ocurre un error sintáctico, PostgreSQL revierte todo el script sin dejar la base de datos en estado inconsistente.
