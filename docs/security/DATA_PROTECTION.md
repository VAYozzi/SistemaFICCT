# DATA_PROTECTION.md
# Protección de Datos, Privacidad e Integridad Documental

## 1. Marco Normativo y Principios de Privacidad
El sistema gestiona información académica confidencial de estudiantes y funcionarios de la **Universidad Autónoma Gabriel René Moreno (UAGRM)**. Su diseño cumple con la **Ley N° 164 de Telecomunicaciones y Tecnologías de Información y Comunicación del Estado Plurinacional de Bolivia** y el Estatuto Orgánico de la UAGRM.

### Principios Rectores:
1. **Minimización de Datos:** Solo se capturan los datos estrictamente necesarios para la tramitación académica (Registro Universitario, Cédula de Identidad, Nombre Completo, Correo y Carrera).
2. **Confidencialidad Exclusiva:** Los expedientes académicos, notas y resoluciones son de uso interno de la Dirección de Carrera; no existen portales públicos ni indexación por motores de búsqueda comerciales (Google, Bing).
3. **Integridad Criptográfica:** Garantía matemática de que ningún archivo digitalizado ha sido modificado o reemplazado ilícitamente desde su recepción en ventanilla.

---

## 2. Seguridad en el Almacenamiento de Archivos Digitales

Los archivos binarios (cartas de convalidación, certificados de notas, cédulas escaneadas y resoluciones) reciben el siguiente tratamiento:

### 2.1. Cero Almacenamiento Directo en PostgreSQL
* Los binarios no se guardan como tipos `BLOB` o `BYTEA` en la base de datos relacional.
* Se almacenan en **Azure Blob Storage** (o en el volumen aislado `/app/storage` en despliegues locales).
* La base de datos almacena únicamente la ruta lógica relativa, el tipo MIME y el hash `SHA-256`.

### 2.2. Cálculo y Verificación del Hash SHA-256
* Al subir cualquier archivo (`POST /api/v1/documentos/upload`), el servidor calcula su digestión `SHA-256` en memoria:
  ```java
  String hashSha256 = DigestUtils.sha256Hex(file.getInputStream());
  ```
* Este hash se almacena en `documentos_digitales.hash_sha256`.
* Al consultar o procesar el archivo, se vuelve a calcular el hash; si existe discrepancia, el sistema bloquea el archivo y emite una alerta de seguridad por corrupción de datos.

### 2.3. Acceso Mediante SAS URLs Temporales (Shared Access Signatures)
* Para renderizar un documento en el navegador web del funcionario, `core-api` genera una **SAS URL de lectura temporal** con vigencia de **15 minutos**.
* **Beneficios:**
  * No existen enlaces públicos ni estáticos a los documentos.
  * Si un usuario copia la URL del archivo, el enlace expira y queda inutilizable inmediatamente después de transcurridos los 15 minutos.

---

## 3. Protección de Datos en Tránsito y en Reposo

* **En Tránsito:**
  * Todas las comunicaciones entre el cliente web y `core-api` operan bajo **HTTPS / TLS 1.3**.
  * Las comunicaciones internas entre `core-api` y `ai-worker-api` están aisladas dentro de la red privada de Docker (`bridge`), requiriendo la cabecera `X-Internal-Api-Key`.
* **En Reposo:**
  * El volumen de PostgreSQL (`pgdata`) y el volumen de archivos (`shared_storage`) se configuran sobre sistemas de archivos con cifrado a nivel de disco en los servidores del Centro de Cómputo de la FICCT.

---

## 4. Política de Respaldos (Backups) y Retención

* **Copias de Seguridad Automatizadas:**
  * Script nocturno de `pg_dump` para la base de datos relacional (incluyendo la bitácora inmutable).
  * Sincronización diferencial del volumen de almacenamiento de archivos.
* **Retención Institucional:**
  * Los registros de expedientes concluidos y actas de titulación tienen un periodo de retención permanente por constituir la memoria histórica de la carrera.
