# MOD_07_AUTOMATIZACION_RPA.md
# Especificación Funcional: Automatización Robótica de Procesos (RPA) y Generación de Reportes

## 1. Alcance y Casos de Uso Vinculados
Este módulo implementa tareas automatizadas desatendidas mediante robots de software basados en **Playwright** y **WeasyPrint** / **ReportLab**: consolidación periódica de correspondencia, generación masiva de reportes oficiales en PDF con formato institucional y gestión de incidencias y reejecuciones.

* **Casos de Uso IEEE 830 / RUP:**
  * `CU-31`: Ejecutar consolidación de correspondencia mediante RPA.
  * `CU-32`: Revisar y validar resultados de ejecución RPA.
  * `CU-33`: Consultar registros consolidados mediante filtros.
  * `CU-34`: Generar informes y reportes PDF masivos mediante RPA.
  * `CU-35`: Preparar borradores de correo con documentos adjuntos.
  * `CU-36`: Consultar resultados e incidencias de ejecuciones RPA.
  * `CU-37`: Gestionar incidencias y reintentos de flujos RPA.

---

## 2. Reglas Inmutables de Negocio (Guardarraíles)

1. **Soberanía y Revisión Humana:**
   * Los informes PDF consolidados generados por los bots RPA nacen en estado de **revisión previa**. No se despachan automáticamente a las autoridades facultativas hasta que un usuario (`ROLE_COORDINADOR` o `ROLE_DIRECTOR`) revise el PDF resultante.
2. **Resiliencia y Reintentos Automáticos:**
   * Cada flujo de automatización cuenta con un mecanismo de reintento automático (máximo 3 intentos con espera de 5 segundos) ante errores de timeout o renderizado de red.
   * Si tras los 3 reintentos el proceso falla, se registra con `estado = 'FALLIDO'` y se guarda el detalle del error en la columna `log_incidencias` (JSONB).
3. **Almacenamiento Directo:**
   * Los PDFs generados por el bot se depositan directamente en el almacenamiento oficial (`/app/storage` o mediante SAS URL de escritura de Azure Blob) y se registra su metadato y hash SHA-256 en `documentos_digitales`.

---

## 3. Especificación Técnica Microservicio (`ai-worker-api`)

### 3.1. Flujos de Automatización (`engines/rpa/`)
* **Librerías:** `playwright` (Chromium headless), `jinja2`, `weasyprint` o `reportlab`.
* **Tipos de Flujo Disponibles:**
  1. `CONSOLIDACION_CORRESPONDENCIA`: Agrupa la correspondencia del periodo, extrae estados de trámite y genera el índice institucional consolidado.
  2. `REPORTE_MASIVO_PDF`: Compila los expedientes concluidos y genera un dossier en PDF con membrete oficial de la UAGRM, numeración correlativa y marcas de agua.
  3. `BORRADORES_CORREO`: Ensambla cartas modelo con los datos del estudiante para trámites observados o aprobados.
* **Endpoint Interno:** `POST /internal/v1/rpa/execute`.

---

## 4. Especificación Técnica Frontend (`web-client`)

### 4.1. Módulo `features/rpa/`
* **Vista `RpaMonitorPage.tsx`:**
  * Tabla con historial de ejecuciones: Tipo de flujo, funcionario que lo ejecutó, fecha/hora, registros procesados, registros con error y badge de estado (`EN_PROCESO`, `COMPLETADO`, `FALLIDO`).
  * Botón **"Lanzar Consolidación RPA"** con modal de parámetros (rango de fechas, incluir concluidos).
  * Enlace directo para previsualizar y descargar el PDF oficial generado.
  * Botón **"Reintentar Flujo"** en caso de ejecuciones fallidas para relanzar el proceso tras corregir datos (CU-37).

---

## 5. Pruebas y Criterios de Aceptación
1. Ejecutar el flujo de consolidación y verificar que genere un PDF válido en `/app/storage` con su hash SHA-256.
2. Simular un fallo forzado y comprobar que el sistema registre los 3 reintentos y guarde el error en `log_incidencias`.
3. Validar que el botón de reejecución en el frontend reactive el flujo y actualice el registro correspondiente.
