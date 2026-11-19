# AUDIT_AND_LOGGING.md
# Sistema de Auditoría Inmutable y Gestión de Logs Técnicos

## 1. Propósito y Principio de Inmutabilidad
El sistema de auditoría es el componente encargado de garantizar la **trazabilidad forense e inalterable** de todas las operaciones realizadas dentro del *Sistema Web de Gestión Administrativa (FICCT - UAGRM)*.

### Regla Suprema de Auditoría:
> **La tabla `bitacora_auditoria` es estrictamente de solo inserción (Append-Only).**  
> Ningún usuario, proceso ni administrador de base de datos puede modificar (`UPDATE`) o eliminar (`DELETE`) registros de esta tabla. No existen endpoints ni métodos de repositorio que expongan operaciones destructivas sobre la bitácora.

---

## 2. Arquitectura de Intercepción AOP (`@Auditable`)

Para garantizar que ningún cambio de datos omita la auditoría, se utiliza **Programación Orientada a Aspectos (Spring AOP)**.

### 2.1. Anotación Personalizada:
```java
package bo.edu.uagrm.ficct.sistemas.gestion.common.aop;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {
    String modulo();
    String accion(); // 'CREATE', 'UPDATE', 'DELETE', 'STATE_CHANGE', 'TRANSFERENCIA'
    String entidad();
}
```

### 2.2. Funcionamiento del Aspecto (`AuditoriaAspect`):
1. **Punto de Intercepción (`@Around`):** El aspecto intercepta los métodos de servicio anotados con `@Auditable`.
2. **Captura del Contexto:**
   * Extrae la IP de origen desde `HttpServletRequest` (respetando la cabecera `X-Forwarded-For` de Nginx).
   * Obtiene el usuario autenticado desde el `SecurityContextHolder`.
   * Si es una modificación (`UPDATE`), serializa el estado anterior de la entidad a `JSONB`.
3. **Ejecución del Método de Negocio:** Se ejecuta la operación transaccional.
4. **Captura del Estado Nuevo:** Se serializa la entidad resultante a `JSONB`.
5. **Persistencia Autónoma (`REQUIRES_NEW`):** Se inserta el registro en `bitacora_auditoria` dentro de una transacción independiente para asegurar que quede grabado incluso si la transacción de negocio se revierte.

---

## 3. Esquema Físico de la Tabla de Auditoría

```sql
CREATE TABLE bitacora_auditoria (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT REFERENCES usuarios(id), -- NULL en intentos fallidos de login
    correo_actor VARCHAR(150),
    direccion_ip VARCHAR(45) NOT NULL,
    modulo VARCHAR(80) NOT NULL,
    accion VARCHAR(50) NOT NULL,
    entidad_afectada VARCHAR(100) NOT NULL,
    entidad_id VARCHAR(50),
    datos_anteriores JSONB,
    datos_nuevos JSONB,
    fecha_hora TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Índices de alto rendimiento para búsquedas forenses
CREATE INDEX idx_auditoria_fecha ON bitacora_auditoria (fecha_hora DESC);
CREATE INDEX idx_auditoria_usuario ON bitacora_auditoria (usuario_id);
CREATE INDEX idx_auditoria_modulo_accion ON bitacora_auditoria (modulo, accion);
```

---

## 4. Estándares de Logging Técnico de Aplicación

Además de la auditoría de negocio en base de datos, los contenedores generan logs estructurados para observabilidad:

### 4.1. Configuración en Spring Boot (`core-api`)
* **Librería:** SLF4J con Logback.
* **Formato de Salida:**
  ```text
  [2026-11-18 10:30:15.120] [INFO ] [ficct-sistemas-core] [http-nio-8080-exec-1] [c.u.f.s.m.t.TramiteServiceImpl] - Transición ejecutada para trámite EXP-2026-0012: INGRESADO -> EN_VERIFICACION por usuario 3
  ```

### 4.2. Configuración en FastAPI (`ai-worker-api`)
* **Librería:** Python `logging` estructurado con timestamps UTC.
* **Niveles de Log:**
  * `INFO`: Inicio y finalización de procesamiento OCR o entrenamiento ML con métricas devueltas.
  * `WARN`: Intentos de reintento en Playwright o documentos con confianza OCR inferior al 65%.
  * `ERROR`: Excepciones no capturadas con traceback completo.

### 4.3. Regla de Enmascaramiento de Datos Sensibles
* **PROHIBICIÓN ESTRICTA:** Ningún log técnico debe imprimir contraseñas, hashes, tokens JWT completos ni datos personales protegidos (como cédulas de identidad o números de teléfono).
