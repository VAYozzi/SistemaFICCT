# 03_REQUIREMENTS_SPECIFICATION.md
# Especificación Formal de Requisitos de Software (IEEE 830-1998)

## 1. Introducción
Este documento consolida la especificación formal de requisitos de software (SRS) para el **Sistema Web de Gestión Administrativa con Alertas Predictivas y Reconocimiento Óptico de Caracteres (OCR) para la Dirección de Carrera de Ingeniería en Sistemas (FICCT - UAGRM)**, estructurado conforme al estándar internacional **IEEE 830-1998**.

---

## 2. Catálogo Oficial de Requisitos Funcionales (RF)

| ID | Nombre del Requisito | Módulo | Prioridad | Descripción Resumida |
| :--- | :--- | :--- | :--- | :--- |
| **RF-01** | Autenticación y Gestión de Sesión | Seguridad | Alta | Inicio de sesión mediante credenciales institucionales, generación de tokens JWT stateless y expiración controlada. |
| **RF-02** | Control de Acceso Basado en Roles (RBAC) | Seguridad | Alta | Restricción estricta de vistas y endpoints para `ROLE_DIRECTOR`, `ROLE_COORDINADOR` y `ROLE_AUXILIAR`. |
| **RF-03** | Perfil de Funcionario | Seguridad | Media | Consulta y actualización de datos personales del funcionario autenticado. |
| **RF-04** | Transferencia de Autoridad de Dirección | Seguridad | Crítica | Proceso transaccional ACID para transferir el rol de Director garantizando la existencia de un único director activo. |
| **RF-05** | Invitaciones Institucionales de Acceso | Seguridad | Media | Generación de enlaces con tokens temporales UUID (48h) para alta de nuevo personal. |
| **RF-06** | Registro de Correspondencia Oficial | Documental | Alta | Registro de correspondencia recibida y emitida con generación de correlativo institucional `DIR-SIS-YYYY-NNNN`. |
| **RF-07** | Catálogo de Trámites y Requisitos | Documental | Alta | Configuración de tipos de trámites estudiantiles (Convalidación, Cambio de Carrera, etc.) y sus requisitos documentales. |
| **RF-08** | Apertura de Expedientes Estudiantiles | Trámites | Alta | Creación de expedientes en estado `INGRESADO` vinculados al registro del estudiante y requisitos adjuntos. |
| **RF-09** | Máquina de Estados de Trámites | Trámites | Crítica | Control estricto de transiciones: `INGRESADO` $\rightarrow$ `EN_VERIFICACION` $\rightarrow$ `OBSERVADO` / `CON_DICTAMEN` $\rightarrow$ `APROBADO` / `RECHAZADO` $\rightarrow$ `CONCLUIDO`. |
| **RF-10** | Planificación Semestral de Oferta y Cupos | Académico | Alta | Gestión de materias, grupos paralelos (SA, SB, etc.) y cupos planificados por periodo académico. |
| **RF-11** | Registro de Migraciones de Plan | Académico | Media | Trazabilidad de traspasos entre el plan antiguo (187-3) y el vigente (187-4). |
| **RF-12** | Cobertura de Carga Horaria Docente | Académico | Alta | Asignación de docentes titulares, interinos y suplentes a grupos ofertados con control de acefalías. |
| **RF-13** | Memoria Histórica de Titulados | Institucional | Media | Registro de graduados por modalidad (Tesis, Proyecto, Excelencia, etc.) con digitalización de actas. |
| **RF-14** | Seguimiento a Investigaciones y Perfiles | Institucional | Media | Registro de perfiles de grado y líneas de investigación docente. |
| **RF-15** | Seguimiento del Plan Operativo Anual (POA) | Institucional | Alta | Control de actividades del POA, presupuesto asignado, ejecución financiera y porcentaje de avance físico. |
| **RF-16** | Dashboard Directivo con KPIs | Analítica | Alta | Visualización ejecutiva de trámites, alertas semafóricas, correspondencia pendiente y balance del POA. |
| **RF-17** | Preprocesamiento y Extracción OCR | IA / OCR | Alta | Pipeline OpenCV (deskewing, binarización Otsu) y extracción con Tesseract OCR en español (`spa`). |
| **RF-18** | Visor Comparativo Dual y Validación Humana | IA / OCR | Crítica | Interfaz dividida (PDF vs. editor) donde el funcionario revisa y valida el texto para activar el índice `tsvector`. |
| **RF-19** | Búsqueda Avanzada y Semántica | IA / OCR | Alta | Motor de búsqueda de texto completo sobre documentos validados combinable con filtros estructurados. |
| **RF-20** | Gestión de Eventos y Talleres | Institucional | Baja | Registro y seguimiento de seminarios y talleres organizados por la carrera. |
| **RF-21** | Bitácora Inmutable de Auditoría | Seguridad | Crítica | Registro automático vía AOP de toda operación de escritura sin endpoints de modificación o borrado. |
| **RF-22** | Almacenamiento Seguro de Documentos | Documental | Alta | Alojamiento en almacenamiento físico con cálculo SHA-256 y lectura vía SAS URLs temporales (15 min). |
| **RF-23** | Asistente Inteligente de Consultas NLP | Asistente | Media | Agente de solo consulta en lenguaje natural que responde preguntas sobre datos institucionales según el rol. |
| **RF-24** | Entrenamiento de Modelo Predictivo | Machine Learning | Crítica | Entrenamiento de `RandomForestClassifier` sobre histórico de cupos exigiendo $F1\text{-Score} \ge 0.80$. |
| **RF-25** | Inferencia y Simulación de Demanda | Machine Learning | Crítica | Proyección de demanda y cálculo de probabilidad de sobredemanda $P$ con semaforización (Rojo si $P \ge 0.75$). |
| **RF-26** | Atención y Resolución de Alertas | Analítica | Alta | Registro formal de la medida administrativa tomada por la autoridad ante una alerta predictiva u operativa. |

---

## 3. Catálogo de Requisitos No Funcionales (RNF)

| ID | Categoría | Requisito | Criterio de Medición |
| :--- | :--- | :--- | :--- |
| **RNF-01** | Rendimiento | Tiempo de respuesta de endpoints REST transaccionales | Menor a 500 ms en el percentil 95 ($P95$). |
| **RNF-02** | Rendimiento | Procesamiento OCR de documentos (1 página) | Menor a 3.5 segundos por página en OpenCV + Tesseract. |
| **RNF-03** | Rendimiento | Inferencia de demanda de cupos para todo el semestre | Menor a 2.0 segundos para las ~60 asignaturas de la malla. |
| **RNF-04** | Seguridad | Cifrado de credenciales | Hashing de contraseñas con BCrypt (cost factor = 10). |
| **RNF-05** | Seguridad | Seguridad en el transporte | HTTPS obligatorio en producción y JWT firmado con HMAC-SHA256 (clave $\ge$ 256 bits). |
| **RNF-06** | Seguridad | Integridad documental | Hashing SHA-256 generado al subir cada archivo y verificado al consultar. |
| **RNF-07** | Disponibilidad | Arquitectura contenerizada | Despliegue en contenedores Docker con reinicio automático (`restart: always`). |
| **RNF-08** | Usabilidad | Diseño institucional y responsivo | Compatibilidad con resoluciones desktop (1366x768 en adelante) y paleta formal UAGRM. |
| **RNF-09** | Confiabilidad | Cero pérdida de bitácora | Transacciones independientes `REQUIRES_NEW` para auditoría que aseguran persistencia ante fallos. |
| **RNF-10** | Mantenibilidad | Desacoplamiento arquitectónico | Topología en estrella: backend Java desacoplado de microservicio Python y frontend React. |
| **RNF-11** | Calidad ML | Precisión del modelo predictivo | Métrica $F1\text{-Score} \ge 0.80$ evaluada mediante validación cruzada ($K=5$). |
| **RNF-12** | Portabilidad | Persistencia estándar SQL | Base de datos PostgreSQL 15+ sin dependencias propietarias no migrables. |
