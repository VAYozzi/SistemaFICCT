# ACCEPTANCE_CRITERIA.md
# Criterios de Aceptación Formales para Defensa y Acreditación de Grado

## 1. Propósito
Este documento define los **Criterios de Aceptación (CA)** verificables que el tribunal de grado de la FICCT - UAGRM evaluará para la aprobación formal del proyecto de software. Todo módulo debe satisfacer sus criterios al 100%.

---

## 2. Catálogo Oficial de Criterios de Aceptación

### CA-01: Control de Acceso, Invariante de Dirección y Auditoría
* **CA-01.1:** El sistema bloquea a nivel de base de datos cualquier intento de registrar o activar un segundo usuario con `ROLE_DIRECTOR`.
* **CA-01.2:** La transferencia de autoridad degrada al director saliente y promueve al entrante en una única transacción atómica, verificando la contraseña actual.
* **CA-01.3:** Toda operación de mutación (`POST`, `PUT`, `PATCH`, `DELETE`) genera un registro en `bitacora_auditoria` con IP, actor, fecha y datos JSON antes/después.
* **CA-01.4:** El sistema no posee ningún endpoint ni método que permita alterar o borrar registros de la bitácora.

---

### CA-02: Integridad de la Máquina de Estados de Trámites
* **CA-02.1:** Un trámite no puede cambiar de estado saltándose etapas intermedias (ej. pasar directamente de `INGRESADO` a `APROBADO` debe retornar `HTTP 409 Conflict`).
* **CA-02.2:** La transición a `CON_DICTAMEN` solo puede ser ejecutada por el Coordinador Académico y exige que **todos los requisitos obligatorios estén marcados como `CONFORME`**.
* **CA-02.3:** Las transiciones a `APROBADO` o `RECHAZADO` son de potestad **exclusiva del Director de Carrera**.
* **CA-02.4:** Cada cambio de estado genera un registro inmutable en `historial_estados_tramite`.

---

### CA-03: Visión Artificial, OCR y Soberanía Humana
* **CA-03.1:** El documento escaneado procesado por OpenCV y PyTesseract se guarda inicialmente con `estado_validacion = 'PENDIENTE_VALIDACION'` y `vector_busqueda = NULL`.
* **CA-03.2:** El texto extraído **no aparece en búsquedas documentales** mientras esté pendiente de validación.
* **CA-03.3:** El **Visor Comparativo Dual** permite hacer zoom, pan y rotar el PDF original a la izquierda, y editar el texto con cálculo de confianza a la derecha.
* **CA-03.4:** Al presionar *"Confirmar y Validar Texto"*, el trigger nativo de PostgreSQL puebla la columna `vector_busqueda` (`tsvector`), haciendo el documento inmediatamente buscable.

---

### CA-04: Machine Learning y Alertas Predictivas de Cupos
* **CA-04.1:** El modelo `RandomForestClassifier` solo se marca como activo para producción si alcanza un **$F1\text{-Score} \ge 0.80$** mediante validación cruzada ($K=5$).
* **CA-04.2:** La inferencia asigna el semáforo **ROJO** a cualquier materia cuya probabilidad de sobredemanda calculada sea $P \ge 0.75$.
* **CA-04.3:** Las alertas son estrictamente consultivas y no abren grupos de forma desatendida.
* **CA-04.4:** Al atender una alerta en el Dashboard, se exige el registro formal de la resolución administrativa tomada por la autoridad.

---

### CA-05: Automatización Robótica de Procesos (RPA)
* **CA-05.1:** El bot Playwright consolida la correspondencia del periodo y genera un dossier oficial en formato PDF con membrete y numeración institucional.
* **CA-05.2:** Si un flujo falla, el sistema ejecuta hasta 3 reintentos automáticos y registra las fallas en `log_incidencias`.

---

### CA-06: Despliegue e Infraestructura Contenerizada
* **CA-06.1:** El sistema completo (`db`, `core-api`, `ai-worker-api`, `web-client`) se levanta en limpio mediante `docker compose up --build -d` sin errores.
* **CA-06.2:** La migración Flyway `V2__seed_data.sql` inicializa los usuarios y el catálogo académico automáticamente.
