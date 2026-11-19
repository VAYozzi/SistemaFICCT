# GUIA DE OPERACION Y DEMOSTRACION LOCAL — SISTEMA FICCT
## Proyecto de Grado / Dirección de Carrera de Ingeniería en Sistemas (FICCT–UAGRM)

---

## 1. Credenciales Institucionales Activas para Demostración

| Rol Institucional | Correo Institucional | Contraseña | Alcance y Permisos |
|---|---|---|---|
| **Director de Carrera** | `nuevo.coordinador@uagrm.edu.bo` | `Coord2026!` | Acceso pleno: Dashboard, Alertas, Aprobación/Rechazo de Trámites, Gestión de Personal, Auditoría y Transferencia de Autoridad. |
| **Coordinador Académico** | `director.sistemas@uagrm.edu.bo` | `Director2026!` | Emisión de Dictámenes de Trámites, Planificación Académica, Inferencia de Cupos y Monitoreo del POA. Bloqueado de administración de usuarios. |
| **Auxiliar de Dirección** | `secretaria.auxiliar@uagrm.edu.bo` | `Auxiliar2026!` | Mesa de Entrada: Registro de Correspondencia, Carga y Visor Dual OCR, Seguimiento de Expedientes. Bloqueado de auditoría y dictámenes. |

---

## 2. Instrucciones para Iniciar y Detener el Sistema

### Inicio Rápido Automatizado
1. Haz doble clic en el archivo [iniciar_sistema.bat](file:///C:/Users/vajho/Desktop/SistemaFICCT/iniciar_sistema.bat).
2. Se abrirán dos consolas independientes para **FastAPI** (`http://127.0.0.1:8000`) y **Spring Boot** (`http://localhost:8080`).
3. En una terminal PowerShell abre el frontend React:
   ```powershell
   cd C:\Users\vajho\Desktop\SistemaFICCT\web-client
   npm run dev
   ```
4. Abre tu navegador web en: `http://localhost:5173`.

### Detención y Liberación de Memoria
* Haz doble clic en el archivo [detener_sistema.bat](file:///C:/Users/vajho/Desktop/SistemaFICCT/detener_sistema.bat). Todos los procesos de Java, Python y Node.js se cerrarán liberando la memoria RAM.

---

## 3. Guión Paso a Paso para la Defensa Universitaria

### Paso 1: Control de Acceso y Matriz RBAC
1. Inicia sesión con la cuenta de **Auxiliar** (`secretaria.auxiliar@uagrm.edu.bo` / `Auxiliar2026!`).
2. Observa el menú lateral: las opciones directivas de *Personal y Roles* y *Bitácora de Auditoría* no son visibles.
3. Intenta navegar manualmente a `/usuarios` o `/auditoria`: el sistema bloquea el acceso con código `403 Prohibido`.
4. Cierra sesión e ingresa como **Director de Carrera** (`nuevo.coordinador@uagrm.edu.bo` / `Coord2026!`). Ahora todos los módulos institucionales se encuentran plenamente accesibles.

### Paso 2: Gestión Documental con Integridad Criptográfica (CU-05, CU-06)
1. Navega a **Correspondencia**.
2. Haz clic en **+ Nueva Correspondencia**.
3. Ingresa un CITE oficial (ej. `CITE-FICCT-2026-005`), remitente, destinatario y selecciona un documento PDF o imagen.
4. Presiona **Guardar Correspondencia**.
5. Muestra en la base de datos o en el explorador cómo el archivo fue depositado en `storage/documentos/` y sellado con su huella digital criptográfica SHA-256 única.

### Paso 3: Visión Artificial, Visor Dual y Soberanía Humana (CU-21 a CU-24, CA-03)
1. Navega al **Visor Dual OCR**.
2. Ingresa el ID de documento generado o presiona **Ejecutar OCR**.
3. Demuestra las capacidades del **Panel Izquierdo**: Zoom In (`+`), Zoom Out (`-`) y rotación de 90° en caso de escaneos torcidos.
4. En el **Panel Derecho**, resalta el cálculo de confianza ponderado y el área de edición del funcionario.
5. **Comprobación de Soberanía Humana ante el Tribunal:**
   - Realiza una búsqueda de texto en la barra inferior antes de validar: el documento **no aparece** (`vector_busqueda = NULL`).
   - Edita el texto, corrige una palabra institucional y presiona **Confirmar y Validar Texto**.
   - El trigger nativo de PostgreSQL `trg_documento_paginas_ocr_vector` genera el índice lingüístico `to_tsvector('spanish', ...)`.
   - Vuelve a buscar: el documento aparece inmediatamente en primer lugar con las palabras buscadas resaltadas con `<mark>`.

### Paso 4: Ciclo de Vida y Máquina de Estados de Trámites (CU-07 a CU-09, CA-02)
1. Navega a **Trámites Académicos**.
2. Selecciona el trámite del estudiante Juan Carlos Perez (`TR-2026-0001`).
3. Muestra el historial inmutable de transiciones sellado en la base de datos (`INGRESADO` $\rightarrow$ `EN_VERIFICACION` $\rightarrow$ `CON_DICTAMEN` $\rightarrow$ `APROBADO` $\rightarrow$ `CONCLUIDO`).
4. Explica cómo la máquina de estados rechaza saltos ilegales con `HTTP 409 Conflict` si se intenta saltar etapas obligatorias.

### Paso 5: Inteligencia Predictiva de Cupos con Machine Learning (CU-14, CU-29, CA-04)
1. Navega a **Planificación Académica**.
2. Selecciona la materia de alta demanda `INF110` (Introducción a la Informática) y presiona **Predecir Cupos (ML)**.
3. Observa cómo `core-api` invoca al microservicio de Python (`ai-worker-api`), ejecutando el ensamble `RandomForestClassifier` validado con $K=5$ ($F1 \ge 0.80$).
4. Muestra la tarjeta semafórica en color **ROJO** (probabilidad $> 75\%$), el cálculo del déficit proyectado y el aviso institucional obligatorio que aclara el carácter estrictamente consultivo del modelo.

### Paso 6: Dashboard Centralizado, Alertas y Dossier Oficial (CU-26, CU-30, CU-34)
1. Navega a **Panel Directivo y Alertas**.
2. Revisa los KPIs ejecutivos en tiempo real (Estudiantes, Trámites, Titulados, Alertas Críticas).
3. Muestra el centro de alertas semafórico y selecciona una alerta pendiente para demostrar la **Soberanía Humana**: el sistema exige ingresar la resolución administrativa oficial adoptada por la autoridad para poder atenderla.
4. Presiona el botón **Generar Dossier Oficial (PDF)**: el sistema compilará de forma automatizada (RPA) el reporte consolidado del semestre, calculando su firma SHA-256 e integrándolo al archivo digital.

