# MOD_04_SEGUIMIENTO_INSTITUCIONAL.md
# Especificación Funcional: Seguimiento Institucional (Titulados, POA y Eventos)

## 1. Alcance y Casos de Uso Vinculados
Este módulo gestiona la memoria histórica y la extensión académica de la carrera: registro y estadísticas de titulados por modalidad, seguimiento a trabajos de investigación y proyectos de grado, ejecución física y financiera del Plan Operativo Anual (POA) y registro de eventos y talleres.

* **Requisitos del Proyecto:** RF-13, RF-14, RF-15.
* **Casos de Uso IEEE 830 / RUP:**
  * `CU-17`: Gestionar registros de titulados por modalidad.
  * `CU-18`: Gestionar investigaciones y trabajos de grado.
  * `CU-19`: Gestionar seguimiento de actividades y presupuesto del POA.
  * `CU-20`: Gestionar eventos y talleres institucionales.

---

## 2. Reglas Inmutables de Negocio (Guardarraíles)

1. **Memoria de Titulados:**
   * Cada titulado debe vincularse obligatoriamente a una modalidad de graduación válida: `TESIS`, `PROYECTO_GRADO`, `EXAMEN_GRADO`, `EXCELENCIA` o `DIRIGIDO`.
   * El registro exige fecha de defensa, nota final y el archivo digitalizado del acta de graduación.
2. **Ejecución Presupuestaria del POA:**
   * Las actividades del POA se agrupan por gestión anual (`poa_gestiones`).
   * Ningún reporte de avance puede registrar un monto ejecutado acumulado que supere el presupuesto total asignado a la actividad, a menos que exista un reajuste formal presupuestario.
   * El avance físico se calcula como el promedio ponderado de los porcentajes reportados en `poa_seguimiento_avance`.

---

## 3. Especificación Técnica Backend (`core-api`)

### 3.1. Paquete y Clases Principales
* **Paquete:** `bo.edu.uagrm.ficct.sistemas.gestion.modules.institucional`
* **Controladores:**
  * `TituladosController` (`/api/v1/titulados`)
  * `InvestigacionesController` (`/api/v1/investigaciones`)
  * `PoaController` (`/api/v1/poa`)
  * `EventosController` (`/api/v1/eventos`)
* **Entidades Clave:**
  * `Titulado`, `TrabajoGradoInvestigacion`, `PoaGestion`, `PoaActividad`, `PoaSeguimientoAvance`, `EventoTaller`.

---

## 4. Especificación Técnica Frontend (`web-client`)

### 4.1. Módulo `features/institucional/`
* **Vistas Principales:**
  * `TituladosPage.tsx`: Directorio de profesionales graduados con filtros por año, modalidad y visor del acta escaneada. Gráficos de pastel con distribución por modalidad.
  * `PoaDashboardPage.tsx`: Vista de control presupuestario con barras de progreso de avance físico (%) vs. financiero ($) por cada actividad del POA de la gestión activa.
  * `EventosPage.tsx`: Calendario y listado de seminarios, talleres y conferencias organizadas por la carrera.

---

## 5. Pruebas y Criterios de Aceptación
1. No se puede registrar un titulado con una nota final fuera del rango institucional boliviano $[51.0, 100.0]$.
2. El registro de un reporte de avance del POA debe actualizar automáticamente el `porcentaje_avance` consolidado en `poa_actividades`.
3. Validar que los gráficos de titulados reflejen exactamente el conteo por modalidad almacenado en la base de datos.
