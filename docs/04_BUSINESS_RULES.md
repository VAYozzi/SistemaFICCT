# 04_BUSINESS_RULES.md
# Catálogo de Reglas Inmutables de Negocio (Guardarraíles)

## 1. Propósito del Documento
Este catálogo define las **reglas inmutables de negocio (RN)** que rigen la lógica de la Dirección de Carrera de Ingeniería en Sistemas. Todo desarrollador, agente de IA o componente automatizado debe obedecer estas reglas; ninguna optimización técnica puede violarlas.

---

## 2. Catálogo Oficial de Reglas de Negocio

### RN-01: Invariante del Director de Carrera
* **Descripción:** Solo puede existir **un único usuario activo** con el rol de Director de Carrera (`ROLE_DIRECTOR`, `rol_id = 1`) en todo el sistema.
* **Mecanismo de Control:** Se impone a nivel físico de base de datos mediante el índice parcial único:
  ```sql
  CREATE UNIQUE INDEX idx_unico_director_activo ON usuarios (rol_id) WHERE activo = TRUE AND rol_id = 1;
  ```

### RN-02: Transferencia Atómica de Autoridad de Dirección
* **Descripción:** La transferencia del cargo de Director debe ejecutarse en una **única transacción atómica (ACID)** con aislamiento serializable.
* **Condiciones:**
  1. El Director saliente debe reingresar su contraseña actual para autenticar la operación.
  2. En la misma transacción: el Director entrante adquiere `ROLE_DIRECTOR`, el Director saliente pierde dicho rol (degradado a rol inactivo o auxiliar), se genera el registro en `historial_transferencia_direccion` y se escribe en `bitacora_auditoria`.
  3. Si cualquier paso falla, la transacción realiza `ROLLBACK` completo.

### RN-03: Soberanía Humana sobre el OCR
* **Descripción:** Ningún texto extraído por visión artificial e inteligencia artificial se indexa de forma automática en el motor de búsqueda institucional.
* **Flujo Obligatorio:**
  1. Todo documento procesado nace con `estado_validacion = 'PENDIENTE_VALIDACION'` y `vector_busqueda = NULL`.
  2. Un funcionario humano (`ROLE_AUXILIAR` o superior) debe revisar el documento en el **Visor Dual**, corregir discrepancias y presionar **"Confirmar y Validar Texto"**.
  3. Solo en ese momento el estado cambia a `VALIDADO` y el trigger nativo de PostgreSQL puebla la columna `vector_busqueda` (`tsvector`).

### RN-04: Soberanía Humana sobre Modelos Predictivos
* **Descripción:** Las alertas predictivas generadas por el modelo de Machine Learning (`RandomForestClassifier`) son **estrictamente consultivas**.
* **Prohibición:** El sistema tiene terminantemente prohibido abrir grupos paralelos, modificar cupos o alterar ofertas de forma autónoma. Solo un ser humano autorizado (`ROLE_DIRECTOR`) puede ejecutar tales medidas.

### RN-05: Umbral de Pase a Producción de Machine Learning ($F1 \ge 0.80$)
* **Descripción:** Todo modelo entrenado para predecir déficit de cupos debe evaluarse mediante validación cruzada ($K=5$).
* **Condición:** Si la métrica **$F1\text{-Score}$ ponderada es inferior a $0.80$**, el modelo es automáticamente rechazado y no se guarda como activo, manteniendo en producción el modelo previo.

### RN-06: Semaforización de Alertas Predictivas de Cupos
* **Descripción:** La probabilidad $P$ de sobredemanda calculada por el modelo ML se categoriza según los siguientes rangos matemáticos:
  * 🔴 **Nivel ROJO:** $P \ge 0.75$ (Sobredemanda crítica inminente).
  * 🟡 **Nivel AMARILLO:** $0.50 \le P < 0.75$ (Capacidad en riesgo).
  * 🟢 **Nivel VERDE:** $P < 0.50$ (Oferta adecuada).

### RN-07: Flujo Estricto de la Máquina de Estados de Trámites
* **Descripción:** Un trámite estudiantil solo puede transitar conforme al grafo formal de estados:
  $$\text{INGRESADO} \longrightarrow \text{EN\_VERIFICACION} \rightleftarrows \text{OBSERVADO}$$
  $$\text{EN\_VERIFICACION} \longrightarrow \text{CON\_DICTAMEN} \longrightarrow (\text{APROBADO} \mid \text{RECHAZADO}) \longrightarrow \text{CONCLUIDO}$$
* **Prohibición:** Prohibido alterar el estado de un trámite saltándose pasos intermedios (ej. de `INGRESADO` a `APROBADO`).

### RN-08: Exclusividad de Roles en Transiciones de Trámites
* **Descripción:**
  * La emisión del dictamen técnico (`EN_VERIFICACION` $\rightarrow$ `CON_DICTAMEN`) es potestad **exclusiva del Coordinador Académico**.
  * La aprobación o rechazo final (`CON_DICTAMEN` $\rightarrow$ `APROBADO` o `RECHAZADO`) es potestad **exclusiva del Director de Carrera**.

### RN-09: Conformidad Requerida para Dictamen Técnico
* **Descripción:** Un trámite no puede pasar al estado `CON_DICTAMEN` si existe al menos un requisito documental obligatorio en estado `PENDIENTE` u `OBSERVADO`. Todos deben estar marcados como `CONFORME`.

### RN-10: Inmutabilidad del Historial de Transiciones
* **Descripción:** Cada vez que un trámite cambia de estado, el sistema genera obligatoriamente un registro en `historial_estados_tramite` indicando: `tramite_id`, `estado_anterior`, `estado_nuevo`, `usuario_id`, `rol_en_momento`, `comentario_o_dictamen` y `fecha_transicion`.

### RN-11: Inmutabilidad de la Bitácora de Auditoría
* **Descripción:** Toda operación de escritura (`POST`, `PUT`, `PATCH`, `DELETE`) debe auditarse.
* **Prohibición:** No debe existir ningún endpoint, método de repositorio, ni procedimiento en base de datos que permita ejecutar `UPDATE` o `DELETE` sobre la tabla `bitacora_auditoria`.

### RN-12: Ciclo de Vida de Invitaciones Institucionales
* **Descripción:** Las invitaciones generadas por el Director para registrar nuevo personal poseen un token UUID único con una vida útil máxima de **48 horas**. Tras expirar o ser aceptadas, quedan invalidadas permanentemente.

### RN-13: Acyclicidad de la Malla de Prerrequisitos
* **Descripción:** La relación de prerrequisitos entre asignaturas modela un Grafo Acíclico Dirigido (DAG). No se permiten dependencias circulares (ej. Asignatura A exige B, y B exige A).

### RN-14: Consistencia en Cierre Estadístico Semestral
* **Descripción:** Al cerrar un periodo semestral en `oferta_grupos_materia`, el sistema valida la siguiente invariante matemática:
  $$\text{aprobados} + \text{reprobados} + \text{abandonos} \le \text{inscritos\_finales}$$

### RN-15: Detección Automática de Acefalías Docentes
* **Descripción:** Al pasar un periodo académico a estado `EN_CURSO`, todo grupo ofertado que no cuente con un docente asignado en estado `VIGENTE` dispara de inmediato una **Alerta Operativa de Acefalía Docente**.

### RN-16: Límite Presupuestario en Avance del POA
* **Descripción:** En ninguna actividad del Plan Operativo Anual (`poa_actividades`) el monto acumulado ejecutado puede superar el presupuesto asignado sin que medie un reajuste presupuestario formalmente registrado.
