# USER_GUIDE_SPEC.md
# Especificación del Manual de Usuario por Roles Institucionales

> **Propósito:**  
> Este documento define la **experiencia de usuario operativa y el manual de procedimientos** del *Sistema Web de Gestión Administrativa con Alertas Predictivas y OCR para la Dirección de Carrera de Ingeniería en Sistemas (FICCT - UAGRM)*. Sirve como guía de diseño UX para los desarrolladores y agentes de IA, y como base formal para el documento final de defensa de grado.

---

## 1. Introducción y Estructura de Roles del Sistema

El sistema es una intranet directiva de uso interno exclusiva para tres roles:
1. **Auxiliar de Ventanilla / Secretaría (`ROLE_AUXILIAR`):** Operaciones diarias de recepción, digitalización y validación.
2. **Coordinador Académico (`ROLE_COORDINADOR`):** Planificación curricular, oferta de materias, carga docente y dictamen técnico.
3. **Director de Carrera (`ROLE_DIRECTOR`):** Máxima autoridad, resolución de alertas predictivas, aprobación final y auditoría.

---

## 2. Guía de Operaciones: Auxiliar de Ventanilla (`ROLE_AUXILIAR`)

### 2.1. Recepción y Registro de Correspondencia
1. Iniciar sesión con sus credenciales institucionales.
2. Ir a **Correspondencia** $\rightarrow$ **Nueva Correspondencia**.
3. Seleccionar el tipo (`RECIBIDA` o `EMITIDA`), ingresar remitente, destinatario y asunto.
4. Adjuntar el archivo digitalizado (PDF o imagen).
5. Hacer clic en **Guardar**. El sistema asigna automáticamente el código correlativo oficial (ej. `DIR-SIS-2026-0045`).

### 2.2. Recepción de Trámites Estudiantiles en Ventanilla
1. Navegar a **Trámites Estudiantiles** $\rightarrow$ **Nuevo Expediente**.
2. Buscar al estudiante por su Cédula de Identidad o Registro Universitario.
3. Seleccionar el tipo de trámite (ej. `Convalidación de Materias`).
4. Adjuntar los archivos escaneados correspondientes a cada requisito obligatorio.
5. Presionar **Crear Expediente**. El trámite nace en estado inicial `INGRESADO`.

### 2.3. Uso del Visor Comparativo Dual OCR
1. En el detalle del expediente o en el módulo de documentos, hacer clic en **"Procesar con OCR"**.
2. Una vez completada la extracción automática, se abre la pantalla dividida (**Visor Dual**):
   * **Lado Izquierdo:** Utilizar los controles de zoom (`+`/`-`), ajuste a ancho y rotación para examinar el documento escaneado original.
   * **Lado Derecho:** Revisar el texto extraído automáticamente. Si existen errores ortográficos o caracteres mal leídos por escaneo borroso, corregirlos directamente en el editor de texto.
3. Observar la barra de nivel de confianza devuelta por el sistema.
4. Presionar el botón verde **"Confirmar y Validar Texto"**.
5. *Resultado:* El documento pasa a estado `VALIDADO` y queda instantáneamente disponible para búsquedas de texto completo.

---

## 3. Guía de Operaciones: Coordinador Académico (`ROLE_COORDINADOR`)

### 3.1. Revisión de Requisitos y Emisión de Dictamen Técnico
1. Ir a **Trámites Estudiantiles** y filtrar expedientes en estado `EN_VERIFICACION`.
2. Abrir el expediente deseado. En la lista de requisitos:
   * Si un documento es correcto, marcar **Conforme**.
   * Si falta información o es ilegible, marcar **Observado** e ingresar el motivo (el trámite pasará a `OBSERVADO` hasta que el estudiante subsane).
3. Una vez que **todos** los requisitos obligatorios estén en `Conforme`, se habilitará el botón **"Emitir Dictamen Técnico"**.
4. Redactar el informe de convalidación o recomendación técnica y presionar **Enviar a Dirección**.
5. *Resultado:* El trámite avanza al estado `CON_DICTAMEN`.

### 3.2. Planificación Semestral de Cupos y Materias
1. Ir a **Planificación Académica** $\rightarrow$ **Oferta de Grupos**.
2. Seleccionar el periodo en planificación (ej. `2026-2`).
3. Para cada asignatura, revisar los grupos paralelos (SA, SB, SC) y los cupos asignados.
4. Asignar el docente responsable en la pestaña **Carga Docente**. Si un grupo queda sin profesor, el sistema advertirá con una alerta de acefalía.

---

## 4. Guía de Operaciones: Director de Carrera (`ROLE_DIRECTOR`)

### 4.1. Monitoreo del Dashboard Directivo y Alertas Predictivas
1. Al ingresar al sistema, el Director visualiza inmediatamente el **Dashboard Ejecutivo**:
   * Gráficos comparativos de demanda de estudiantes vs. cupos ofertados.
   * Indicador del semáforo de cupos:
     * 🔴 **Rojo ($P \ge 0.75$):** Sobredemanda crítica inminente.
     * 🟡 **Amarillo:** Riesgo moderado.
     * 🟢 **Verde:** Capacidad adecuada.
2. Hacer clic en una alerta roja para ver el detalle de la materia (ej. *INF511 - Taller de Grado con déficit estimado de 35 cupos*).
3. Presionar **"Registrar Medida Administrativa"**, ingresar la resolución adoptada (ej. *"Se autoriza apertura de grupo paralelo SC con 45 cupos"*) y marcar como **Atendida**.

### 4.2. Aprobación Final de Trámites Estudiantiles
1. Ingresar a **Trámites** y filtrar por estado `CON_DICTAMEN`.
2. Leer el dictamen técnico elaborado por el Coordinador.
3. En la barra de actuaciones, seleccionar **"Aprobar Trámite"** o **"Rechazar Trámite"**.
4. Ingresar la justificación de la resolución decanal/directiva y presionar **Confirmar Decisión**.
5. *Resultado:* El expediente pasa a `APROBADO` o `RECHAZADO`, habilitando a Ventanilla para emitir la resolución final (`CONCLUIDO`).

### 4.3. Proceso Crítico: Transferencia de Autoridad de Dirección
1. En caso de finalización de gestión o cambio de autoridades, el Director saliente ingresa a **Configuración** $\rightarrow$ **Transferir Dirección**.
2. Leer la advertencia de seguridad.
3. Seleccionar al nuevo Director entrante de la lista de funcionarios activos.
4. Ingresar el número de la **Resolución Facultativa de Respaldo** y las observaciones de entrega de gestión.
5. Ingresar la **contraseña actual** del Director saliente para confirmar su identidad.
6. Presionar **"Ejecutar Transferencia de Autoridad"**.
7. *Resultado:* La transferencia se ejecuta de forma inmediata en una sola transacción atómica: el nuevo usuario asume como Director, el usuario saliente pierde el rol activo de dirección y se genera el registro inmutable en la bitácora de auditoría.

---

## 5. Glosario de Estados y Preguntas Frecuentes (FAQ)

### Estados de un Trámite:
* `INGRESADO`: Documentos recibidos en ventanilla.
* `EN_VERIFICACION`: Coordinación está revisando los requisitos.
* `OBSERVADO`: Falta algún requisito o hay errores que el estudiante debe corregir.
* `CON_DICTAMEN`: Coordinación emitió informe técnico favorable; en espera de firma del Director.
* `APROBADO`: Aprobado formalmente por el Director de Carrera.
* `RECHAZADO`: Denegado por el Director de Carrera con justificación.
* `CONCLUIDO`: Resolución entregada y expediente archivado.

### ¿Por qué no encuentro un documento en la búsqueda avanzada?
* El documento aún se encuentra en estado `PENDIENTE_VALIDACION`. El personal de secretaría debe revisarlo en el **Visor Dual** y presionar **"Confirmar y Validar"** para que el texto sea indexado en el motor de búsqueda.
