# AGENT_HANDOFF.md
# Protocolo de Traspaso y Continuidad de Desarrollo

**Proyecto:** Sistema Web de Gestión Administrativa con Alertas Predictivas y OCR (FICCT–UAGRM)  
**Fecha de Cierre:** 19 de Noviembre de 2026  
**Avance Global:** 100% (Todos los Bloques A, B, C, D, E, F y G completados y validados localmente)

---

## 1. Estado Final del Sistema
* **Entorno:** Exclusivamente local en Windows 10 Pro (PostgreSQL 16 en puerto 5432). Sin despliegues externos ni exposición a Internet.
* **Microservicios y Puertos Locales:**
  - `web-client`: React 19 + TypeScript + Vite en `http://localhost:5173` (compilación verificada en `dist/`).
  - `core-api`: Spring Boot 3 + Java 17 LTS en `http://localhost:8080`.
  - `ai-worker-api`: FastAPI + Python 3.11 en `http://127.0.0.1:8000`.
  - `ficct_sistemas_db`: PostgreSQL 16 con 28 tablas normalizadas y 5 migraciones Flyway versionadas (`V1` a `V5`) validadas.

---

## 2. Scripts Operativos Entregados
* `iniciar_sistema.bat`: Inicia PostgreSQL (si no corre), levanta FastAPI y Spring Boot en consolas dedicadas.
* `detener_sistema.bat`: Cierra ordenadamente todos los procesos Java, Python y Node.js, liberando la memoria RAM.
* `run_full_regression.ps1`: Suite automatizada que ejecuta en 4 segundos las 7 pruebas de extremo a extremo.
* `GUIA_DEMOSTRACION_DEFENSA.md`: Guión estructurado paso a paso para la defensa ante el tribunal universitario.

---

## 3. Estado de Acreditación y Defensa
El sistema satisface todos los requisitos funcionales (RF-01 a RF-26) y criterios de aceptación (CA-01 a CA-06). Se encuentra **listo para la demostración local y defensa de grado**.
