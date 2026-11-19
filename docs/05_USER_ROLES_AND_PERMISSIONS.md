# 05_USER_ROLES_AND_PERMISSIONS.md
# Control de Acceso Basado en Roles (RBAC) y Matriz de Permisos

## 1. Definición de Roles Institucionales
El sistema es una intranet directiva de uso interno para la Dirección de Carrera de Ingeniería en Sistemas (FICCT - UAGRM). Se definen exactamente tres roles:

1. **`ROLE_DIRECTOR` (Director de Carrera):** Máxima autoridad ejecutiva. Posee control total del sistema, exclusividad en la aprobación/rechazo de resoluciones finales de trámites, gestión de usuarios, resolución de alertas predictivas, auditoría forense y transferencia de autoridad.
2. **`ROLE_COORDINADOR` (Coordinador Académico):** Responsable de la planificación curricular, apertura de grupos y cupos, designación de carga docente, seguimiento del POA y emisión del dictamen técnico preliminar en trámites estudiantiles.
3. **`ROLE_AUXILIAR` (Auxiliar de Secretaría / Ventanilla):** Responsable de la atención en ventanilla, recepción de correspondencia, creación de expedientes, digitalización de documentos, validación en el Visor Dual OCR y ejecución de tareas RPA.

---

## 2. Matriz Global de Permisos por Módulo y Capacidad

| Módulo / Capacidad | Operación / Vista | `ROLE_AUXILIAR` | `ROLE_COORDINADOR` | `ROLE_DIRECTOR` |
| :--- | :--- | :---: | :---: | :---: |
| **Autenticación** | Iniciar sesión y ver perfil propio | ✅ | ✅ | ✅ |
| **Usuarios** | Listar personal administrativo | ❌ | ❌ | ✅ |
| **Usuarios** | Activar/desactivar funcionario | ❌ | ❌ | ✅ |
| **Usuarios** | Enviar invitaciones con token UUID | ❌ | ❌ | ✅ |
| **Usuarios** | Transferir autoridad de Dirección | ❌ | ❌ | ✅ |
| **Auditoría** | Consultar bitácora inmutable de auditoría | ❌ | ❌ | ✅ |
| **Correspondencia** | Registrar correspondencia recibida/emitida | ✅ | ✅ | ✅ |
| **Correspondencia** | Derivar correspondencia entre áreas | ✅ | ✅ | ✅ |
| **Documentos** | Subir archivos a almacenamiento | ✅ | ✅ | ✅ |
| **Documentos** | Generar SAS URL de lectura temporal | ✅ | ✅ | ✅ |
| **Trámites** | Crear expediente en estado `INGRESADO` | ✅ | ✅ | ❌ |
| **Trámites** | Revisar requisitos (`CONFORME` / `OBSERVADO`) | ✅ | ✅ | ✅ |
| **Trámites** | Transición a `EN_VERIFICACION` | ✅ | ✅ | ❌ |
| **Trámites** | Transición a `CON_DICTAMEN` (Emitir dictamen) | ❌ | ✅ | ❌ |
| **Trámites** | Transición a `APROBADO` o `RECHAZADO` | ❌ | ❌ | ✅ |
| **Trámites** | Transición a `CONCLUIDO` (Entrega final) | ✅ | ✅ | ✅ |
| **Académico** | Consultar mallas y prerrequisitos | ✅ | ✅ | ✅ |
| **Académico** | Abrir periodos académicos | ❌ | ✅ | ✅ |
| **Académico** | Ofertar grupos, cupos y cierre estadístico | ❌ | ✅ | ✅ |
| **Académico** | Asignar docentes a grupos (Carga horaria) | ❌ | ✅ | ✅ |
| **Institucional** | Registrar titulados y actas | ✅ | ✅ | ✅ |
| **Institucional** | Registrar avances físicos y de presupuesto POA | ❌ | ✅ | ✅ |
| **Institucional** | Registrar eventos y conferencias | ✅ | ✅ | ✅ |
| **OCR** | Procesar documento con OpenCV + Tesseract | ✅ | ✅ | ✅ |
| **OCR** | Validar texto en el Visor Comparativo Dual | ✅ | ✅ | ✅ |
| **Búsqueda** | Búsqueda documental full-text y semántica | ✅ | ✅ | ✅ |
| **Analítica** | Ver Dashboard ejecutivo y KPIs | ✅ | ✅ | ✅ |
| **Machine Learning** | Reentrenar modelo Random Forest | ❌ | ✅ | ✅ |
| **Machine Learning** | Ejecutar simulación predictiva de cupos | ❌ | ✅ | ✅ |
| **Alertas** | Consultar matriz de alertas semafóricas | ✅ | ✅ | ✅ |
| **Alertas** | Registrar medida humana y atender alerta | ❌ | ✅ | ✅ |
| **RPA** | Lanzar consolidación de correspondencia | ✅ | ✅ | ✅ |
| **RPA** | Reintentar ejecución fallida de bot RPA | ✅ | ✅ | ✅ |

---

## 3. Matriz de Endpoints y Códigos HTTP de Seguridad

Toda petición realizada a la API REST de `core-api` valida el rol mediante anotaciones `@PreAuthorize("hasRole('...')")`:

| Endpoint REST | Método | Roles Permitidos | Respuesta si No Autorizado |
| :--- | :--- | :--- | :--- |
| `/api/v1/auth/login` | `POST` | Público | - |
| `/api/v1/auth/perfil` | `GET` | Autenticado | `401 Unauthorized` |
| `/api/v1/usuarios/**` | `GET`, `POST`, `PATCH`| `ROLE_DIRECTOR` | `403 Forbidden` |
| `/api/v1/auditoria/**` | `GET` | `ROLE_DIRECTOR` | `403 Forbidden` |
| `/api/v1/tramites/{id}/transicion` | `POST` | Según Matriz State Machine | `403 Forbidden` o `409 Conflict` |
| `/api/v1/predicciones/entrenar` | `POST` | `ROLE_DIRECTOR`, `ROLE_COORDINADOR` | `403 Forbidden` |
| `/api/v1/alertas/{id}/atender` | `POST` | `ROLE_DIRECTOR`, `ROLE_COORDINADOR` | `403 Forbidden` |

---

## 4. Adaptación Dinámica de la Interfaz (Frontend)

El componente `Sidebar.tsx` en `web-client` filtra las opciones del menú lateral leyendo el rol del store de Zustand:

* **Menú Auxiliar:** Correspondencia, Trámites (Ventanilla), Visor OCR, Titulados, Eventos, Monitor RPA.
* **Menú Coordinador:** Todo lo anterior más: Planificación Académica, Malla Curricular, Carga Docente, Dictamen de Trámites, POA, Simulación Predictiva.
* **Menú Director:** Todo lo anterior más: Dashboard Ejecutivo con Alertas Rojas, Aprobación de Resoluciones, Gestión de Personal / Invitaciones, Transferencia de Dirección y Bitácora de Auditoría.
