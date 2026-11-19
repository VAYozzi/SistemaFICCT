# ARCHITECTURE_OVERVIEW.md
# Visión General de la Arquitectura del Sistema (Modelo C4)

## 1. Propósito y Principios de Diseño
Este documento describe la arquitectura global del **Sistema Web de Gestión Administrativa con Alertas Predictivas y OCR para la Dirección de Carrera de Ingeniería en Sistemas (FICCT - UAGRM)** mediante el enfoque del **Modelo C4** (Contexto, Contenedores, Componentes).

---

## 2. Nivel 1: Diagrama de Contexto del Sistema

```mermaid
C4Context
    title Diagrama de Contexto del Sistema (Nivel 1 C4)

    Person(director, "Director de Carrera", "Máxima autoridad ejecutiva y decisoria.")
    Person(coord, "Coordinador Académico", "Supervisión de oferta, materias y dictamen de trámites.")
    Person(auxiliar, "Auxiliar de Ventanilla", "Atención en ventanilla, correspondencia y validación OCR.")

    System(sistema_ficct, "Sistema de Gestión Administrativa FICCT", "Gestiona expedientes, correspondencia, planificación semestral, alertas predictivas y OCR.")

    System_Ext(azure_blob, "Azure Blob Storage", "Repositorio en la nube para almacenamiento seguro de documentos PDF e imágenes.")
    System_Ext(correo_uagrm, "Servidor SMTP Institucional", "Envío de notificaciones y confirmación de resoluciones.")

    Rel(director, sistema_ficct, "Consulta KPIs, aprueba trámites, gestiona alertas", "HTTPS/Web")
    Rel(coord, sistema_ficct, "Planifica semestres, emite dictámenes técnicos", "HTTPS/Web")
    Rel(auxiliar, sistema_ficct, "Registra correspondencia, digitaliza y valida OCR", "HTTPS/Web")

    Rel(sistema_ficct, azure_blob, "Almacena y genera SAS URLs temporales de lectura", "HTTPS/REST")
    Rel(sistema_ficct, correo_uagrm, "Envía notificaciones de estado", "SMTP/TLS")
```

---

## 3. Nivel 2: Diagrama de Contenedores

```mermaid
C4Container
    title Diagrama de Contenedores (Nivel 2 C4)

    Container(web_client, "web-client", "React 18, TypeScript, Vite, Nginx", "Interfaz de usuario institucional SPA servida mediante Nginx alpine.")
    Container(core_api, "core-api", "Java 17, Spring Boot 3.2", "Orquestador central, lógica de negocio transaccional, seguridad JWT, RBAC y auditoría.")
    Container(ai_worker, "ai-worker-api", "Python 3.10, FastAPI, Uvicorn", "Microservicio interno de cómputo intensivo: OpenCV, Tesseract OCR, Scikit-Learn y Playwright.")
    ContainerDb(database, "db", "PostgreSQL 15+", "Persistencia relacional, índices GIN, búsqueda full-text tsvector y bitácora inmutable.")
    ContainerDb(storage, "shared_storage", "Volumen Docker / Azure Blob", "Repositorio de binarios físicos (/app/storage).")

    Rel(web_client, core_api, "Peticiones RESTful con token JWT", "JSON / HTTPS :8080")
    Rel(core_api, database, "Lectura y escritura transaccional con JDBC / Flyway", "TCP :5432")
    Rel(core_api, ai_worker, "Llamadas HTTP internas con X-Internal-Api-Key", "JSON / HTTP :8000")
    Rel(core_api, storage, "Lectura y escritura de metadatos de archivos", "POSIX / FS")
    Rel(ai_worker, storage, "Lectura de imágenes para OCR y escritura de PDFs RPA", "POSIX / FS")
```

---

## 4. Topología en Estrella y Reglas de Flujo de Datos

1. **Unicidad de la Frontera Externa:**
   * El navegador web (`web-client`) **únicamente se comunica con `core-api`**.
   * `ai-worker-api` reside en una red interna privada de Docker (`ficct_network`) y no expone ningún puerto al host público.
2. **Orquestación Transaccional:**
   * Cuando el usuario solicita un proceso de OCR, la petición llega a `core-api`. Este valida los permisos del funcionario, registra la solicitud en base de datos y llama a `ai-worker-api` mediante `WebClient`.
   * Al recibir la respuesta con el texto extraído y el nivel de confianza, `core-api` consolida los datos y responde al frontend.
3. **Manejo Eficiente de Archivos Binarios:**
   * Para evitar transferir archivos binarios pesados (PDFs de 20 MB o imágenes escaneadas) por las llamadas HTTP entre Java y Python, ambos servicios comparten el volumen físico `/app/storage` (o utilizan SAS URLs temporales de Azure Blob Storage). Solo intercambian rutas relativas o identificadores numéricos.

---

## 5. Estrategia de Resiliencia y Fallos

* **Aislamiento de Cómputo:** Si `ai-worker-api` experimenta sobrecarga de CPU durante un entrenamiento de Machine Learning, `core-api` sigue atendiendo con total normalidad las peticiones transaccionales de ventanilla.
* **Manejo de Timeouts:** Las llamadas de `core-api` a `ai-worker-api` están configuradas con un Read Timeout de 120 segundos y reintentos automáticos para mitigar latencias en documentos de gran volumen.
* **Transacciones de Auditoría Independientes:** La escritura en `bitacora_auditoria` se ejecuta con `Propagation.REQUIRES_NEW`, garantizando que incluso si una transacción de negocio falla y hace rollback, el intento o la excepción quede registrada para trazabilidad forense.
