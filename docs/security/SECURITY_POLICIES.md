# SECURITY_POLICIES.md
# Políticas de Seguridad de la Información y Gestión de Acceso

## 1. Propósito y Marco Normativo
Este documento define las políticas técnicas y operativas de seguridad para el **Sistema Web de Gestión Administrativa de la Dirección de Carrera de Ingeniería en Sistemas (FICCT - UAGRM)**, alineado con los estándares internacionales de la industria y las recomendaciones del **OWASP Top 10**.

---

## 2. Política de Autenticación y Tokens JWT

### 2.1. Arquitectura Stateless con JWT
* **Algoritmo de Firma:** HMAC-SHA256 (`HS256`) con una clave secreta (`JWT_SECRET`) de entropía mínima de **256 bits** (64 caracteres hexadecimales).
* **Tiempo de Vida del Token:** **8 horas** (`28,800,000 ms`), correspondiente a una jornada laboral administrativa completa.
* **Payload Mínimo Requerido en el Token:**
  ```json
  {
    "sub": "director@ficct.uagrm.edu.bo",
    "usuarioId": 1,
    "rol": "ROLE_DIRECTOR",
    "nombreCompleto": "MSc. Ing. Carlos Alberto Mendoza",
    "iat": 1795020000,
    "exp": 1795048800
  }
  ```
* **Manejo en Cliente:** El token se almacena en memoria de la sesión cliente (Zustand con persistencia en `sessionStorage` o `localStorage`) y se inyecta en cada petición mediante la cabecera estándar:
  ```http
  Authorization: Bearer <jwt_token>
  ```

---

## 3. Política de Contraseñas y Credenciales

1. **Complejidad Obligatoria:**
   * Longitud mínima: **8 caracteres**.
   * Debe contener al menos: 1 letra mayúscula, 1 letra minúscula, 1 número y 1 carácter especial (`!@#$%^&*()_+-=`).
2. **Algoritmo de Almacenamiento:**
   * Se prohíbe terminantemente almacenar contraseñas en texto plano o con algoritmos reversibles (como MD5 o SHA-1).
   * Se utiliza **BCrypt** con un factor de costo (*work factor*) de **10** (`$2a$10$...`).
3. **Verificación en Acciones Críticas:**
   * La reautenticación mediante reingreso de contraseña es obligatoria para:
     * Transferencia de autoridad de Dirección (`POST /api/v1/usuarios/transferir-direccion`).
     * Cambio de contraseña del perfil propio.

---

## 4. Política de Invitaciones y Altas de Personal

* Para incorporar nuevo personal a la intranet, no existe registro público abierto.
* El Director genera una invitación institucional que emite un **token UUID v4** criptográficamente seguro.
* El token tiene una validez estricta de **48 horas**. Cumplido ese lapso, el sistema marca el token como `EXPIRADA` e impide el registro.

---

## 5. Mitigación contra OWASP Top 10

| Vulnerabilidad OWASP | Mecanismo de Defensa Implementado |
| :--- | :--- |
| **A01: Broken Access Control** | Control estricto en controladores con `@PreAuthorize("hasRole('...')")` y navegación protegida en cliente con `RoleGuard`. |
| **A02: Cryptographic Failures** | Contraseñas con BCrypt (cost=10), JWT firmado con HS256 $\ge$ 256 bits y HTTPS forzado en transporte. |
| **A03: Injection (SQL / NoSQL)** | Uso exclusivo de Spring Data JPA con consultas parametrizadas en Hibernate. Cero concatenación de cadenas en SQL. |
| **A04: Insecure Design** | Máquina de Estados finitos que impide transiciones ilegales. Soberanía humana obligatoria en OCR y predicciones ML. |
| **A05: Security Misconfiguration** | Cabeceras de seguridad estrictas en Nginx (`X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `CSP`). |
| **A07: Identification and Auth Failures** | Registro de intentos fallidos en `bitacora_auditoria` (`LOGIN_FAIL`) capturando la IP origen. |
| **A08: Software and Data Integrity** | Integridad documental garantizada calculando el hash `SHA-256` de todo archivo subido y validándolo antes de servirlo. |
| **A09: Security Logging and Monitoring** | Bitácora inmutable mediante Spring AOP con persistencia `REQUIRES_NEW` independiente. |
