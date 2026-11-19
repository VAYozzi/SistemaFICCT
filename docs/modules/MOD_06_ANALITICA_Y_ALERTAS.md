# MOD_06_ANALITICA_Y_ALERTAS.md
# Especificación Funcional: Analítica, Dashboard y Alertas Predictivas (Random Forest)

## 1. Alcance y Casos de Uso Vinculados
Este módulo dota a la Dirección de Carrera de capacidades de inteligencia predictiva y soporte a la toma de decisiones: calcula proyecciones de sobredemanda de cupos por materia mediante Machine Learning (`RandomForestClassifier`), gestiona el sistema semafórico unificado de alertas y consolida los KPIs ejecutivos en el Dashboard institucional.

* **Requisitos del Proyecto:** RF-16, RF-24, RF-25, RF-26.
* **Casos de Uso IEEE 830 / RUP:**
  * `CU-26`: Consultar Dashboard directivo institucional.
  * `CU-27`: Generar y exportar reportes ejecutivos.
  * `CU-28`: Consultar alertas administrativas y predictivas.
  * `CU-29`: Realizar análisis predictivo y simulación de demanda.
  * `CU-30`: Registrar atención humana y medidas sobre alertas.

---

## 2. Reglas Inmutables de Negocio (Soberanía y ML)

1. **Umbral Mínimo de Aceptación para Producción ($F1 \ge 0.80$):**
   * El algoritmo `RandomForestClassifier` se entrena sobre el histórico consolidado de `oferta_grupos_materia` y la red de prerrequisitos.
   * **REGLA ESTRICTA:** Si el modelo entrenado no alcanza un **$F1\text{-Score} \ge 0.80$** (evaluado con validación cruzada $K\text{-Fold} = 5$), el sistema **rechaza el pase a producción** y mantiene activo el modelo anterior previamente validado.
2. **Alertas Predictivas Consultivas (Soberanía Humana):**
   * Las alertas predictivas **jamás abren grupos ni modifican cupos de manera autónoma**.
   * Su función es estrictamente consultiva para advertir al Director y Coordinador con semanas de anticipación.
3. **Semaforización Estricta de Probabilidad ($P$):**
   * 🔴 **Nivel ROJO ($P \ge 0.75$):** Sobredemanda crítica inminente. Dispara alerta de atención obligatoria.
   * 🟡 **Nivel AMARILLO ($0.50 \le P < 0.75$):** Capacidad en riesgo.
   * 🟢 **Nivel VERDE ($P < 0.50$):** Oferta suficiente para la demanda proyectada.
4. **Trazabilidad de Resoluciones:**
   * Al atender una alerta (`POST /api/v1/alertas/{id}/atender`), se exige registrar la acción administrativa adoptada (ej. *"Se autorizó apertura de grupo paralelo SC con 45 cupos"*).

---

## 3. Especificación Técnica Microservicio (`ai-worker-api`)

### 3.1. Pipeline de Machine Learning (`engines/ml/`)
* **Vector de Características (Features):**
  1. $X_1$: Demanda insatisfecha promedio de la materia en los últimos 3 semestres.
  2. $X_2$: Tasa de reprobación histórica de la materia.
  3. $X_3$: Número de estudiantes que aprobaron los prerrequisitos inmediatos en el semestre anterior (población habilitada).
  4. $X_4$: Cupo actual ofertado en el semestre.
* **Variable Objetivo ($y$):** Indicador binario de sobredemanda ($1$ si demanda $>$ cupo ofertado, $0$ si fue suficiente).
* **Entrenamiento e Inferencia:**
  * Endpoint `POST /internal/v1/ml/train` $\rightarrow$ Genera archivo `.joblib` en `/app/models/`.
  * Endpoint `POST /internal/v1/ml/predict` $\rightarrow$ Devuelve array con probabilidad de sobredemanda $P$, déficit proyectado y criticidad sugerida por cada materia.

---

## 4. Especificación Técnica Frontend (`web-client`)

### 4.1. Módulo `features/analitica/`
* **Vista `DashboardPage.tsx`:**
  * Tarjetas KPI: Trámites en curso, alertas rojas activas, correspondencia pendiente, avance POA.
  * Gráfico de Barras Dobles (Recharts): Cupo planificado vs. Demanda proyectada por materia.
  * Gráfico de Líneas: Evolución histórica de titulados por gestión.
* **Vista `AlertasPage.tsx`:**
  * Matriz unificada de alertas (Predictivas de cupos, Operativas de trámites por vencer y Acefalías docentes).
  * Filtro por pestañas: `Todas`, `Críticas (Rojo)`, `Advertencia (Amarillo)`, `Atendidas`.
  * Botón en cada tarjeta: **"Registrar Medida"** $\rightarrow$ Abre modal para que el Director ingrese la resolución y pase la alerta a estado `ATENDIDA`.

---

## 5. Pruebas y Criterios de Aceptación
1. Probar el endpoint de simulación y verificar que una probabilidad de 0.76 genere automáticamente una alerta con `nivel_criticidad = 'ROJO'`.
2. Verificar que el reentrenamiento con métricas deficientes ($F1 < 0.80$) rechace el modelo y devuelva código informativo.
3. Comprobar que una alerta atendida cambie su estado a `ATENDIDA` y desaparezca del listado de pendientes.
