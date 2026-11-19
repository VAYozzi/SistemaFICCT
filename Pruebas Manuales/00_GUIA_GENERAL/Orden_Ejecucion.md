# GUÍA Y ORDEN DE EJECUCIÓN MANUAL — SISTEMAFICCT (42 PRUEBAS)

Esta guía establece el recorrido óptimo para comprobar el 100% de las funcionalidades del sistema mediante las **42 pruebas manuales preparadas** en `Matriz_42_Pruebas.csv`.

---

## 1. REGLA DE ORO DE LOS DATOS

- **Datos Poblados (Grupo A):** Ya están dentro de la base de datos `ficct_sistemas_db`. Te permitirán ver estadísticas, tablas pobladas, materias, alertas y trámites en diferentes estados nada más iniciar sesión.
- **Datos Nuevos Reservados (Grupo B):** Se encuentran en `Pruebas Manuales/02_DATOS_NUEVOS_PARA_PRUEBAS`. Son los que introducirás manualmente a través de los formularios para validar la creación de nuevos registros.

---

## 2. ORDEN DE EJECUCIÓN RECOMENDADO

### ETAPA 1: VENTANILLA DE ATENCIÓN Y MESA DE ENTRADA (Rol Auxiliar)
> **Credenciales:** `secretaria.auxiliar@uagrm.edu.bo` / `Auxiliar2026!`

1. **PM-003:** Iniciar sesión y comprobar que únicamente se muestran 3 módulos de ventanilla: *Correspondencia*, *Trámites Académicos* y *Visor Dual OCR*.
2. **PM-010 / PM-016 / PM-037 (Pruebas Negativas RBAC):** Escribir directamente en el navegador `/usuarios`, `/auditoria` o `/academico` y comprobar que el sistema impide el acceso o redirige a una ruta autorizada.
3. **PM-017:** Abrir **Correspondencia** y comprobar los 13 registros iniciales poblados.
4. **PM-018:** Registrar correspondencia nueva con los datos de `CITE-FICCT-SIS-013-2026` y adjuntar `Solicitud_013.pdf` desde `02_DATOS_NUEVOS_PARA_PRUEBAS/Correspondencia`.
5. **PM-019 (Prueba Negativa):** Intentar registrar un CITE duplicado (`CITE-FICCT-SIS-001-2026`) y verificar el error 409 Conflict.
6. **PM-029 a PM-032:** Ir a **Visor Dual OCR**, procesar `DOC-OCR-001.pdf`, realizar corrección humana, presionar *Guardar Validación* y comprobar la búsqueda lingüística con `<mark>`.
7. **PM-021 y PM-022:** Ir a **Trámites**, revisar el expediente inicial `TR-2026-0002` (en estado `INGRESADO`) y pasarlo a `EN_VERIFICACION`.
8. **PM-024 (Prueba Negativa):** Intentar emitir dictamen técnico o aprobar un trámite siendo Auxiliar y verificar el bloqueo HTTP 403 Forbidden.
9. **PM-006:** Cerrar sesión.

---

### ETAPA 2: GESTIÓN CURRICULAR Y COORDINACIÓN (Rol Coordinador)
> **Credenciales:** `director.sistemas@uagrm.edu.bo` / `Director2026!`

10. **PM-002:** Iniciar sesión y verificar los 5 módulos visibles en el Sidebar.
11. **PM-009 / PM-015 (Pruebas Negativas RBAC):** Verificar que no puede ingresar a `/usuarios` ni `/auditoria`.
12. **PM-023:** En **Trámites**, seleccionar el trámite en verificación (`TR-2026-0003`), redactar el dictamen técnico académico y transicionar a `CON_DICTAMEN`.
13. **PM-026 (Prueba Negativa):** Intentar transicionar un trámite a `APROBADO` y verificar el bloqueo por regla de soberanía directiva (403 Forbidden).
14. **PM-034 a PM-036:** Entrar a **Malla y Cupos**, explorar las 18 asignaturas del Plan 187-3, consultar la predicción de cupos *Random Forest* para *Programación I* y constatar el aviso consultivo no vinculante.
15. **PM-041 (Prueba Negativa):** Ir a **Panel y Alertas**, intentar atender una alerta sin escribir la resolución y verificar que el formulario lo exige obligatoriamente.
16. **PM-006:** Cerrar sesión.

---

### ETAPA 3: AUTORIDAD EJECUTIVA Y GOBIERNO INSTITUCIONAL (Rol Director)
> **Credenciales:** `nuevo.coordinador@uagrm.edu.bo` / `Coord2026!`

17. **PM-001:** Iniciar sesión y comprobar el acceso integral a los 7 módulos del sistema.
18. **PM-038 y PM-039:** En el **Dashboard**, constatar las métricas consolidadas (10 estudiantes, 8 trámites, 5 alertas, 3 titulados, BOB 150,000 POA) y las alertas semafóricas.
19. **PM-040:** Atender la alerta roja de sobredemanda escribiendo la resolución humana: *"Apertura autorizada del grupo paralelo SC con 45 cupos adicionales"*.
20. **PM-042:** Presionar el botón **Generar Dossier Oficial (PDF)** y comprobar la generación del informe con hash criptográfico SHA-256.
21. **PM-025:** En **Trámites**, seleccionar el trámite con dictamen (`TR-2026-0004`) y aprobarlo formalmente mediante resolución directiva.
22. **PM-027 (Prueba Negativa):** Intentar pasar un trámite en estado `INGRESADO` directamente a `APROBADO` y comprobar el rechazo por máquina de estados finitos (409 Conflict).
23. **PM-008:** Ir a **Personal y Roles** y revisar la tabla de usuarios institucionales.
24. **PM-011:** Emitir una invitación formal para `nuevo.auxiliar2026@uagrm.edu.bo` con rol Auxiliar y verificar la generación del token UUID.
25. **PM-013:** Desactivar temporalmente al usuario Auxiliar (ID 3) y reactivarlo para validar el control de estado.
26. **PM-014:** Ir a **Bitácora Inmutable** y constatar que cada una de las operaciones ejecutadas quedó registrada con su timestamp, IP, actor y módulo.
27. **PM-004 a PM-007:** Realizar las pruebas negativas de autenticación (contraseña incorrecta, validación de correo y navegación directa sin token).

