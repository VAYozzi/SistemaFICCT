# ENVIRONMENT_AUDIT.md
# Informe de Auditoría del Entorno de Desarrollo y Hardware

**Proyecto:** Sistema Web de Gestión Administrativa con Alertas Predictivas y OCR — Dirección de Carrera de Ingeniería en Sistemas (FICCT–UAGRM)  
**Fecha de Auditoría:** 19 de Noviembre de 2026  
**Auditor:** Equipo de Ingeniería de Software (Antigravity Senior Full Stack & DevOps)

---

## 1. Hardware Detectado

| Parámetro | Especificación Detectada | Estado / Evaluación |
| :--- | :--- | :--- |
| **Sistema Operativo** | Windows 10 Pro (Build 19045, 64-bit) | ✅ Compatible |
| **Procesador (CPU)** | Intel(R) Core(TM) i5-6200U @ 2.30GHz (2 núcleos físicos, 4 lógicos) | ⚠️ Restricción de rendimiento. Requiere compilaciones secuenciales. |
| **Memoria RAM Total** | 12.18 GB (~12,474 MB físicos visibles) | ℹ️ Superior a los 8 GB nominales esperados. Mayor margen operativo. |
| **Memoria RAM Disponible**| ~4.70 GB libres al momento de la auditoría | ⚠️ Moderado. Debe limitarse el heap de Java (`-Xmx512m` o `-Xmx768m`). |
| **Almacenamiento (Disco C:)**| 111.07 GB libres de 222.91 GB totales (~50% libre) | ✅ Espacio suficiente para librerías, dependencias y base de datos local. |
| **Virtualización / Hypervisor**| `HypervisorPresent: False` / WSL base no inicializado con distro activa | ⚠️ Sin WSL2 ni distribución Linux configurada. |

---

## 2. Software Instalado y Estado de Herramientas

| Herramienta | Requerido por Proyecto | Estado en el Host | Versión / Detalle |
| :--- | :--- | :--- | :--- |
| **Git** | Control de versiones | ✅ **Instalado** | `git version 2.55.0.windows.5` |
| **Node.js** | Frontend Vite/React | ✅ **Instalado** | `v22.13.0` |
| **npm** | Gestor de paquetes Web | ✅ **Operativo** | `10.9.2` (Corregido shim mediante `nvm reshim`) |
| **PostgreSQL** | Base de datos relacional | ✅ **Instalado y Corriendo** | `PostgreSQL 16.15` (Servicio Windows `postgresql-x64-16` en ejecución) |
| **Windows Package Manager**| Instalación desatendida | ✅ **Instalado** | `winget v1.29.380` |
| **Java JDK** | Backend `core-api` | ⚠️ **Versión incompatible** | Detectado `Temurin OpenJDK 11.0.32.1`. Proyecto exige **Java 17 (LTS)**. |
| **Maven (`mvn`)** | Build Spring Boot | ❌ **No disponible en PATH** | Se resolverá con **Maven Wrapper (`mvnw`)** en el proyecto. |
| **Python** | Microservicio `ai-worker-api` | ❌ **No instalado** | Alias de Windows activo apuntando a Store (sin runtime). Requiere Python 3.10 o 3.11. |
| **Tesseract OCR** | Motor OCR | ❌ **No instalado en PATH** | Requiere binario Tesseract 5.x para Windows con datos de idioma español (`spa.traineddata`). |
| **Docker Desktop / WSL2** | Contenedores | ❌ **No instalado / Inactivo** | Docker no está en PATH ni servicio activo. |

---

## 3. Análisis de Compatibilidad y Estrategia de Ejecución (Windows 10 / Hardware)

### 3.1. Evaluación de Docker Desktop vs. Ejecución Local Híbrida
* **Condición de la máquina:** Procesador i5 de 6ta generación (2 núcleos / 4 hilos), Hypervisor no habilitado a nivel usuario para WSL2 distro, memoria disponible ~4.7 GB.
* **Impacto de Docker Desktop:** Docker Desktop sobre Windows 10 requiere habilitar WSL2 y asigna por defecto un motor Vmmem que consume 2-4 GB de RAM fijos, compitiendo severamente con el IDE, Vite, Spring Boot y FastAPI.
* **Estrategia Recomendada:**  
  1. Mantener los archivos oficiales de contenedores (`Dockerfile` para cada servicio y `docker-compose.yml`) estrictamente fieles a la documentación para despliegues y entornos productivos/CI-CD.
  2. Para el desarrollo y ejecución local inmediata optimizada al hardware:
     - **Base de Datos:** Aprovechar el servicio nativo **PostgreSQL 16.15** que ya está corriendo en el host en el puerto 5432 (máximo rendimiento sin sobrecarga de virtualización).
     - **Backend (`core-api`):** Requiere instalar **OpenJDK 17 (Temurin o Microsoft JDK 17)** y ejecutar con `-Xms256m -Xmx512m` mediante `./mvnw spring-boot:run`.
     - **Microservicio (`ai-worker-api`):** Instalar **Python 3.10/3.11** nativo con entorno virtual aislado (`venv`), Tesseract OCR nativo Windows con paquete `spa`.
     - **Frontend (`web-client`):** Node.js 22.13.0 + npm 10.9.2 ya están completamente listos y funcionales con Vite.

---

## 4. Software Faltante y Acciones de Preparación

### 4.1. Java Development Kit (JDK) 17
* **Por qué se necesita:** `core-api` está diseñado sobre Spring Boot 3.2+, el cual requiere obligatoriamente Java 17 o superior como baseline de bytecode (Java 11 no puede ejecutar Spring Boot 3).
* **Versión recomendada:** Eclipse Temurin JDK 17 LTS o Microsoft Build of OpenJDK 17.
* **Instalación:** Vía winget: `winget install EclipseAdoptium.Temurin.17.JDK` o instalador manual oficial de [Adoptium](https://adoptium.net/temurin/releases/?version=17).

### 4.2. Python 3.10 o 3.11
* **Por qué se necesita:** Microservicio `ai-worker-api` (FastAPI, Scikit-learn, OpenCV headless, PyTesseract, Playwright).
* **Versión recomendada:** Python 3.11.x (64-bit).
* **Instalación:** Vía winget: `winget install Python.Python.3.11` o instalador manual oficial de [python.org](https://www.python.org/downloads/).

### 4.3. Tesseract OCR (Windows) + `spa.traineddata`
* **Por qué se necesita:** Extracción OCR en español para `MOD_05`.
* **Versión recomendada:** Tesseract 5.3+ (UB-Mannheim build para Windows).
* **Instalación:** `winget install UB-Mannheim.TesseractOCR` o [GitHub UB-Mannheim](https://github.com/UB-Mannheim/tesseract/wiki), asegurando seleccionar el paquete de idioma "Spanish".

---

## 5. Riesgos Técnicos y Mitigaciones

1. **Riesgo de agotamiento de memoria:** Si se ejecutan simultáneamente tests pesados o Playwright con navegadores gráficos.
   * *Mitigación:* Playwright correrá siempre en modo `--headless` sin interfaz gráfica; Spring Boot tendrá tope de memoria `-Xmx512m`.
2. **Conflicto de puertos locales:**
   * PostgreSQL nativo: 5432 (verificar credenciales o crear usuario/base de datos `ficct_db`).
   * Spring Boot `core-api`: puerto 8080.
   * FastAPI `ai-worker-api`: puerto 8000.
   * React Vite `web-client`: puerto 5173.
3. **Persistencia y almacenamiento de archivos:**
   * Al ejecutar localmente sin Azure Blob Storage real, la arquitectura permite almacenamiento en carpeta local estructurada (`/app/storage` o `C:\Users\vajho\Desktop\SistemaFICCT\storage`) manteniendo exactamente la interfaz y hash SHA-256 documentados.

---

## 6. Estado de Preparación del Entorno
* **Entorno Web/Node:** 100% Listo (Node 22, npm 10).
* **Entorno Base de Datos:** 90% Listo (PostgreSQL 16 corriendo en Windows).
* **Entorno Java 17:** Pendiente actualización (actualmente JDK 11).
* **Entorno Python/OCR:** Pendiente instalación.
* **Archivos Docker:** Se generarán en el Bloque A para garantizar la portabilidad exigida.

