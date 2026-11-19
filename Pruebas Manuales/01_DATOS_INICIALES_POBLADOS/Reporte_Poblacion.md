# Reporte de Población Inicial — Base de Datos ficct_sistemas_db

- **Fecha de Ejecución:** 19 de noviembre de 2026
- **Base de Datos:** `ficct_sistemas_db` (PostgreSQL 16 Localhost)
- **Script de Población:** `C:\Users\vajho\Desktop\SistemaFICCT\storage\backups\poblacion_inicial_grupo_a.sql`

---

## 1. Inventario Cuantitativo por Tabla

| Tabla | Registros Previos | Registros Poblados (Grupo A) | Total Posterior | Observaciones |
| :--- | :---: | :---: | :---: | :--- |
| `docentes` | 0 | +10 | **10** | Docentes ordinarios y extraordinarios para asignación |
| `materias` | 2 | +16 | **18** | Asignaturas completas del Plan 187-3 (Cálculo, Física, BD, Software...) |
| `expedientes_estudiantiles`| 1 | +9 | **10** | Expedientes con R.U., C.I., nombres, carrera y estado académico |
| `titulados` | 1 | +2 | **3** | Titulados históricos en modalidades Tesis, Excelencia y Examen de Grado |
| `poa_gestiones` | 0 | +1 | **1** | Gestión 2026 aperturada con presupuesto BOB 185,000.00 |
| `poa_actividades` | 0 | +4 | **4** | 4 actividades estratégicas con responsables y % avance |
| `correspondencia` | 1 | +12 | **13** | CITEs CITE-FICCT-SIS-001 al 012 (Recibidas, Emitidas e Internas) |
| `tramites_estudiantiles` | 1 | +7 | **8** | Trámites en estados INGRESADO, EN_VERIFICACION, CON_DICTAMEN, APROBADO |
| `historial_estados_tramite` | 4 | +4 | **8** | Trazabilidad inmutable de transiciones de los trámites |
| `alertas` | 1 | +5 | **6** | Alertas semafóricas activas (ROJO: Cupos, AMARILLO: POA, VERDE: Metas) |
| `oferta_grupos_materia` | 1 | +2 | **3** | Grupos paralelos SA y SB para cálculo de demanda y cupos |
| `cobertura_carga_docente` | 0 | +1 | **1** | Designación docente activa para verificación |
| `usuarios` | 3 | 0 | **3** | **Preservados intactos:** Director, Coordinador y Auxiliar |
| `roles` | 3 | 0 | **3** | Roles RBAC del sistema |
| `flyway_schema_history` | 5 | 0 | **5** | Migraciones V1 a V5 preservadas íntegras |

---

## 2. Validación de Indicadores del Dashboard

A través de la API `/institucional/dashboard/kpis`, el sistema ahora reporta en tiempo real:
- **Total Estudiantes:** 10
- **Trámites en Curso:** 8
- **Alertas Pendientes:** 5 (2 Rojas, 2 Amarillas, 1 Verde)
- **Titulados Históricos:** 3
- **Presupuesto POA Gestionado:** BOB 150,000.00
- **Correspondencia Paginada:** 13 registros

