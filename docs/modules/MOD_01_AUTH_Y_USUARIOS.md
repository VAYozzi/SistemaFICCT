# MOD_01_AUTH_Y_USUARIOS.md
# Especificación Funcional: Seguridad, Usuarios, Roles y Bitácora de Auditoría

## 1. Alcance y Casos de Uso Vinculados
Este módulo cubre la gestión de acceso, control de permisos institucionales basados en roles (RBAC), invitaciones para nuevo personal, el proceso crítico de transferencia de mando de Dirección y la bitácora inmutable de auditoría.

* **Requisitos del Proyecto:** RF-01 al RF-05 y RF-21.
* **Casos de Uso IEEE 830 / RUP:**
  * `CU-01`: Gestionar cuenta y acceso (Login y Perfil).
  * `CU-02`: Gestionar roles y permisos institucionales.
  * `CU-03`: Consultar bitácora de auditoría inmutable.
  * `CU-04`: Transferir autoridad de Dirección de Carrera.
  * `CU-05`: Gestionar invitaciones institucionales de acceso.

---

## 2. Reglas Inmutables de Negocio (Guardarraíles)

1. **Invariante Absoluto de Dirección:** Solo puede existir **un único usuario activo** con `ROLE_DIRECTOR` (`rol_id = 1`) en toda la base de datos a la vez. Se protege mediante índice parcial único en PostgreSQL (`idx_unico_director_activo`).
2. **Caso Crítico: Transferencia de Autoridad de Dirección:**
   * Debe ejecutarse bajo una **única transacción ACID** (`@Transactional(isolation = Isolation.SERIALIZABLE)`).
   * Exige la verificación estricta de la contraseña actual del Director saliente.
   * En el mismo instante transaccional:
     1. El Director entrante adquiere `ROLE_DIRECTOR`.
     2. El Director saliente es degradado/desactivado de la Dirección.
     3. Se inserta el registro histórico en `historial_transferencia_direccion` con el código de resolución facultativa de respaldo.
     4. Se registra el evento `TRANSFERENCIA` en `bitacora_auditoria`.
3. **Invitaciones Temporales:**
   * Solo el Director puede emitir invitaciones para nuevo personal administrativo.
   * Cada invitación genera un token UUID único con expiración automática a las 48 horas.
   * Una vez aceptada o revocada, no puede reutilizarse.
4. **Inmutabilidad de la Auditoría:**
   * Ningún usuario (ni siquiera el Director) puede modificar o eliminar registros de `bitacora_auditoria`.
   * Prohibido crear endpoints o métodos de repositorio para `UPDATE` o `DELETE` sobre esta tabla.

---

## 3. Especificación Técnica Backend (`core-api`)

### 3.1. Paquete y Clases Principales
* **Paquete:** `bo.edu.uagrm.ficct.sistemas.gestion.modules.auth`
* **Controladores:**
  * `AuthController` (`/api/v1/auth`): Login, perfil, aceptar invitación.
  * `UsuarioController` (`/api/v1/usuarios`): CRUD de usuarios, invitaciones y transferencia.
  * `AuditoriaController` (`/api/v1/auditoria`): Consulta paginada y filtrada.
* **Servicios:**
  * `AuthService`, `UsuarioService`, `TransferenciaDireccionService`, `AuditoriaService`.
* **Aspecto AOP:**
  * `AuditoriaAspect` en `common.aop`: Intercepta llamadas anotadas con `@Auditable` y escribe en `bitacora_auditoria` de forma asíncrona o en transacción separada (`Propagation.REQUIRES_NEW`).

### 3.2. Endpoints Clave
* `POST /api/v1/auth/login`: Autentica con email y contraseña, devuelve JWT con claims de rol y expiración (8 horas).
* `POST /api/v1/usuarios/transferir-direccion`: **Exclusivo `ROLE_DIRECTOR`**.
* `POST /api/v1/usuarios/invitaciones`: Crea token UUID para invitar funcionarios.
* `GET /api/v1/auditoria`: Consulta con filtros por `fechaInicio`, `fechaFin`, `usuarioId`, `modulo` y `accion`.

---

## 4. Especificación Técnica Frontend (`web-client`)

### 4.1. Módulo `features/auth/`
* **Páginas:**
  * `LoginPage.tsx`: Formulario centrado con identidad institucional UAGRM, inputs para correo y contraseña, mensajes de error amigables.
  * `AceptarInvitacionPage.tsx`: Vista accesible mediante enlace temporal `/invitacion?token={uuid}` donde el nuevo funcionario define su contraseña y completa sus datos.
* **Componentes:**
  * `LoginForm.tsx` con validación Zod.
  * `TransferirDireccionModal.tsx`: Modal con advertencia visual roja, campo para seleccionar nuevo director, resolución de respaldo y re-ingreso obligatorio de la contraseña actual del Director saliente.
  * `UsuariosTable.tsx`: Tabla con filtros y botón para enviar invitación.
  * `AuditoriaViewer.tsx`: Tabla de bitácora con badges para acciones (`CREATE`, `UPDATE`, `DELETE`, `LOGIN_OK`, `LOGIN_FAIL`, `STATE_CHANGE`, `TRANSFERENCIA`) y visor de cambios JSON antes/después en modal.

---

## 5. Pruebas y Criterios de Aceptación
1. Intentar crear un segundo usuario activo con `ROLE_DIRECTOR` debe fallar con error de integridad de BD.
2. Transferir dirección con contraseña incorrecta debe devolver `HTTP 401 Unauthorized` sin alterar ningún rol.
3. El login con credenciales erróneas debe registrar `LOGIN_FAIL` en la bitácora con la IP origen.
4. El rol `ROLE_AUXILIAR` al intentar consultar `/api/v1/auditoria` debe recibir `HTTP 403 Forbidden`.
