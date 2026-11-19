# MOD_03_PLANIFICACION_ACADEMICA.md
# Especificación Funcional: Planificación Académica, Cupos y Carga Docente

## 1. Alcance y Casos de Uso Vinculados
Este módulo administra la estructura curricular de la carrera de Ingeniería en Sistemas, la apertura de periodos semestrales, la oferta de materias, grupos y cupos, el registro de migraciones entre planes de estudio y la cobertura de la carga horaria docente.

* **Requisitos del Proyecto:** RF-10, RF-11, RF-12.
* **Casos de Uso IEEE 830 / RUP:**
  * `CU-13`: Gestionar planificación académica semestral.
  * `CU-14`: Gestionar materias, grupos y cupos planificados.
  * `CU-15`: Gestionar registros de migración entre planes de estudio.
  * `CU-16`: Gestionar carga docente y antecedentes de cobertura.

---

## 2. Reglas Inmutables de Negocio (Guardarraíles)

1. **Integridad Curricular y Prerrequisitos:**
   * La malla curricular modela dependencias directas (`prerrequisitos_materia`).
   * No se permite circularidad en los prerrequisitos (ej. A exige B y B exige A).
   * La red de prerrequisitos es un insumo primario para el cálculo de flujo de estudiantes en el módulo de Machine Learning.
2. **Cierre Estadístico Semestral:**
   * Al concluir un periodo académico, el Coordinador o Director ejecuta el cierre estadístico en `oferta_grupos_materia`, consolidando:
     * `inscritos_finales`
     * `aprobados`, `reprobados`, `abandonos`
     * `demanda_insatisfecha_estimada` (solicitudes de caso especial o adición que quedaron fuera por falta de cupo).
   * Este cierre es el que alimenta de forma continua el dataset histórico del modelo de Machine Learning.
3. **Control de Acefalías Docentes:**
   * Todo grupo ofertado en estado activo debe tener una asignación docente en `cobertura_carga_docente`.
   * Si un grupo queda sin docente asignado al iniciar el semestre, el sistema dispara automáticamente una **Alerta Operativa de Acefalía**.

---

## 3. Especificación Técnica Backend (`core-api`)

### 3.1. Paquete y Clases Principales
* **Paquete:** `bo.edu.uagrm.ficct.sistemas.gestion.modules.academico`
* **Controladores:**
  * `PlanEstudioController` (`/api/v1/planes-estudio`)
  * `PeriodoAcademicoController` (`/api/v1/periodos`)
  * `OfertaGruposController` (`/api/v1/oferta-grupos`)
  * `MigracionPlanController` (`/api/v1/migraciones`)
  * `CargaDocenteController` (`/api/v1/carga-docente`)
* **Entidades Clave:**
  * `PlanEstudio`, `Materia`, `PrerrequisitoMateria`, `PeriodoAcademico`, `OfertaGruposMateria`, `MigracionPlanEstudio`, `Docente`, `CoberturaCargaDocente`.

---

## 4. Especificación Técnica Frontend (`web-client`)

### 4.1. Módulo `features/academico/`
* **Vistas Principales:**
  * `OfertaAcademicaPage.tsx`: Matriz interactiva de asignaturas del semestre filtrable por nivel (semestres 1 al 10), mostrando grupos paralelos (SA, SB, SC, etc.), cupos ofertados y docentes asignados.
  * `MallaCurricularViewer.tsx`: Diagrama interactivo de la malla del Plan 187-4 que resalta visualmente las cadenas de prerrequisitos de cada materia.
  * `CargaDocentePage.tsx`: Vista de seguimiento de docentes titulares, interinos y alertas visuales de grupos acéfalos.

---

## 5. Pruebas y Criterios de Aceptación
1. No se puede crear un grupo de materia sin vincularlo a un periodo académico válido.
2. El registro de cierre estadístico debe validar que: `aprobados + reprobados + abandonos <= inscritos_finales`.
3. Detectar grupos sin designación docente al cambiar el estado del periodo a `EN_CURSO` y verificar la creación de alertas de acefalía.
