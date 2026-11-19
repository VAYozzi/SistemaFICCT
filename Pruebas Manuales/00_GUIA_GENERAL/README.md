# GUÍA DE EJECUCIÓN MANUAL — SISTEMAFICCT (FICCT–UAGRM)

Bienvenido a la carpeta de **Pruebas Manuales** de SistemaFICCT. Este directorio contiene todos los conjuntos de datos, archivos de soporte y documentos administrativos listos para ejecutar la auditoría de funcionamiento manual del sistema desde el navegador web en `http://localhost:5173`.

---

## 1. ESTRUCTURA Y CONTENIDO DE LA CARPETA

```text
Pruebas Manuales/
├── 00_GUIA_GENERAL/
│   ├── README.md                          <- Este instructivo de operación
│   └── Matriz_de_Seguimiento.csv          <- Cuadro de mando de las 25 pruebas manuales
├── 01_Usuarios_y_Roles/
│   ├── Usuarios_Prueba.csv                <- 10 perfiles administrativos simulados
│   └── Invitaciones_Prueba.csv            <- 10 escenarios de invitación con token UUID
├── 02_Correspondencia/
│   ├── Correspondencia_Prueba.csv         <- 30 registros con CITE, remitente y referencia
│   ├── Cartas/ (10 archivos PDF)          <- Cartas oficiales CITE-FICCT-SIS-001 al 010
│   ├── Solicitudes/ (10 archivos PDF)     <- Solicitudes estudiantiles CITE-011 al 020
│   └── Circulares/ (10 archivos PDF)      <- Circulares de dirección CITE-021 al 030
├── 03_Tramites_Academicos/
│   ├── Tramites_Prueba.csv                <- 20 trámites con máquina de estados finitos
│   └── Solicitudes_Estudiantiles/ (10 PDF)<- Expedientes PDF para adjuntar a trámites
├── 04_Documentos_OCR/
│   ├── Resultados_Esperados.csv           <- 15 casos con dificultad y palabras clave
│   ├── Documentos_Legibles/ (5 PDF)       <- Documentos digitales de alta nitidez
│   ├── Documentos_Escaneados/ (5 PNG/JPG) <- Documentos escaneados con tablas y sellos
│   └── Documentos_Con_Errores/ (5 casos)  <- Documentos con eñes, tildes y tipeo
├── 05_Planificacion_Academica/
│   ├── Planes_de_Estudio.csv              <- Mallas 187-3 (Vigente) y 187-4
│   ├── Materias.csv                       <- 25 asignaturas oficiales de Sistemas
│   ├── Periodos_Academicos.csv            <- Semestres 2026-1, 2026-2 y 2025-2
│   ├── Oferta_de_Grupos.csv               <- 20 paralelos SA, SB, SC con cupos
│   └── Cobertura_Docente.csv              <- 15 designaciones horarias docentes
├── 06_Titulados_y_POA/
│   ├── Titulados_Prueba.csv               <- 20 titulados por modalidad (Tesis, Excelencia...)
│   └── Metas_POA.csv                      <- 8 metas POA institucionales con avance
├── 07_Alertas_y_Predicciones/
│   ├── Escenarios_Alertas.csv             <- 12 alertas semafóricas (Rojo, Amarillo, Verde)
│   └── Escenarios_Prediccion.csv          <- 8 casos de estimación de cupos (Random Forest)
├── 08_Reportes_y_Dossier/
│   └── Verificacion_Reportes.csv          <- Casos de generación de dossier institucional
└── 09_Casos_Negativos/
    └── Casos_de_Error.csv                 <- 25 pruebas negativas (RBAC 403, 400, 409)
```

---

## 2. CREDENCIALES DE PRUEBA EXISTENTES EN LOCALHOST

| Rol Institucional | Correo Institucional | Contraseña | Perfil en Base de Datos |
| :--- | :--- | :--- | :--- |
| **Director de Carrera** | `nuevo.coordinador@uagrm.edu.bo` | `Coord2026!` | Director Activo (ROLE_DIRECTOR) |
| **Coordinador Académico** | `director.sistemas@uagrm.edu.bo` | `Director2026!` | Coordinador Activo (ROLE_COORDINADOR) |
| **Auxiliar de Dirección** | `secretaria.auxiliar@uagrm.edu.bo` | `Auxiliar2026!` | Auxiliar de Ventanilla (ROLE_AUXILIAR) |

---

## 3. PROCEDIMIENTO PARA EJECUTAR LAS PRUEBAS

Todas las pruebas deben ser ejecutadas desde la interfaz web en:
👉 **`http://localhost:5173`**

### Ejemplo 1: Prueba de Registro de Correspondencia con Carga de Archivo (PM-011)
1. Iniciar sesión como **Auxiliar de Dirección** (`secretaria.auxiliar@uagrm.edu.bo`).
2. Abrir el menú **Correspondencia** en el Sidebar.
3. En el formulario *Registrar Nueva Correspondencia*, copiar los datos de la fila `ESC-COR-01` de `02_Correspondencia/Correspondencia_Prueba.csv`:
   - Código CITE: `CITE-FICCT-SIS-001-2026`
   - Tipo: `RECIBIDA`
   - Remitente Institución: `Decanato FICCT`
   - Remitente Persona: `Lic. Remitente 1`
   - Destinatario: `Dirección de Carrera de Ingeniería en Sistemas`
   - Referencia: `Coordinación interinstitucional de actividades académicas e informe técnico #01`
4. En el campo de archivo, seleccionar desde el explorador:  
   `Pruebas Manuales/02_Correspondencia/Cartas/Carta_001.pdf`
5. Presionar **Registrar Correspondencia**.
6. **Verificación:** Comprobar que el archivo se sube calculando su hash SHA-256 y que el nuevo CITE aparece inmediatamente en la tabla inferior.

---

### Ejemplo 2: Prueba de OCR con Soberanía Humana y Búsqueda Lingüística (PM-018 a PM-020)
1. En el módulo **Visor Dual OCR**, introducir el ID del documento recién subido (o seleccionar uno existente).
2. Presionar **Procesar con OCR (PyTesseract)**.
3. Observar la extracción de texto crudo y la confianza en pantalla.
4. En el editor de texto derecho, realizar correcciones o añadir la palabra clave `convalidación`.
5. Presionar **Guardar y Validar Texto Humano**.
6. En la barra de búsqueda documental superior, escribir `convalidación`.
7. **Verificación:** El documento debe aparecer en los resultados resaltando la coincidencia entre etiquetas `<mark>`.

---

### Ejemplo 3: Prueba de Control de Acceso Estricto (RBAC Negativa - PM-008)
1. Manteniendo la sesión como **Auxiliar de Dirección**, escribir directamente en la barra del navegador:  
   `http://localhost:5173/usuarios`
2. **Verificación:** El sistema debe redirigir a una ruta permitida o presentar el menú oculto sin permitir ver la lista de personal.
3. Al intentar consumir `/api/v1/usuarios` mediante el cliente de red, el servidor Spring Boot rechaza la petición con código **403 Forbidden**.

---

## 4. REGISTRO EN LA MATRIZ DE SEGUIMIENTO

A medida que completes cada prueba manual en pantalla:
1. Abre el archivo `Pruebas Manuales/00_GUIA_GENERAL/Matriz_de_Seguimiento.csv`.
2. Actualiza la columna **Resultado Obtenido** con tu observación.
3. Cambia la columna **Estado** de `NO EJECUTADA` a `APROBADA` o `FALLIDA`.

> [!NOTE]
> Todos los documentos incluidos en esta carpeta son simulados con fines de auditoría y pruebas del software, no contienen datos personales reales ni firmas comprometedoras.

