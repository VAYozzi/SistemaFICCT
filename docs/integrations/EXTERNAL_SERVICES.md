# EXTERNAL_SERVICES.md
# Integración con Servicios Externos e Infraestructura Institucional

## 1. Alcance de las Integraciones Externas
Este documento especifica los puntos de conexión del sistema con plataformas externas a la red de contenedores: **Azure Blob Storage**, el **Servidor de Correo Institucional (SMTP)** y los puntos de enlace con los sistemas de la **UAGRM (DPA / CPD)**.

---

## 2. Integración con Azure Blob Storage

### 2.1. Propósito y Contenedor
* Repositorio en la nube para el almacenamiento de archivos digitales pesados (PDFs, imágenes escaneadas de requisitos y reportes oficiales generados por RPA).
* **Nombre del Contenedor Oficial:** `ficct-sistemas-docs`
* **Estructura Jerárquica de Blobs:**
  ```text
  /tramites/{anio}/{mes}/{tramite_id}/{uuid}_{nombre_original}.pdf
  /correspondencia/{anio}/{mes}/{correlativo}/{uuid}_{nombre_original}.pdf
  /titulados/{anio}/{estudiante_id}/acta_{uuid}.pdf
  /rpa/{anio}/{mes}/reporte_consolidado_{ejecucion_id}.pdf
  ```

### 2.2. Gestión de Credenciales y Generación de SAS URLs
* **Dueño de las Credenciales:** Exclusivamente `core-api` (Spring Boot) posee la clave de cuenta o *Connection String*.
* **Microservicio FastAPI:** No posee credenciales maestras de Azure; recibe una **SAS URL temporal** generada por Spring Boot:
  * SAS URL de lectura (`sp=r`): Válida por 15 minutos para que FastAPI lea una imagen/PDF al ejecutar OCR.
  * SAS URL de escritura (`sp=cw`): Válida por 15 minutos para que el bot RPA deposite directamente el PDF consolidado generado.
* **Emulador Local para Desarrollo:** Se soporta **Azurite** o volumen POSIX local (`/app/storage`) configurado transparentemente mediante la propiedad:
  ```properties
  azure.storage.enabled=false
  storage.local.root=/app/storage
  ```

---

## 3. Integración con Servidor de Correo Institucional (SMTP)

### 3.1. Propósito
* Envío de enlaces de invitación a nuevo personal administrativo con el token UUID temporal.
* Notificación por correo a estudiantes cuando su trámite pasa a estado `OBSERVADO` (con el detalle de los requisitos a corregir) o `CONCLUIDO` (para recoger su resolución sellada).

### 3.2. Parámetros de Configuración
```properties
spring.mail.host=${SMTP_HOST:smtp.uagrm.edu.bo}
spring.mail.port=${SMTP_PORT:587}
spring.mail.username=${SMTP_USER:direccion.sistemas@uagrm.edu.bo}
spring.mail.password=${SMTP_PASS:secret}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

---

## 4. Enlaces Futuros con Sistemas Centrales UAGRM (DPA / CPD)

* **Padrón de Estudiantes y Notas (DPA):** El sistema modela la tabla `estudiantes` de forma desacoplada. Se contemplan interfaces de importación mediante archivos de consolidación o conexión REST con los servicios del Centro de Procesamiento de Datos (CPD) de la UAGRM para validación de inscritos y boletas de notas.
