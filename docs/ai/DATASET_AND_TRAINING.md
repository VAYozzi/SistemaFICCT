# DATASET_AND_TRAINING.md
# Extracción de Datos, Preparación de Datasets y Pipeline de Entrenamiento

## 1. Fuentes de Datos y Extracción SQL
El modelo predictivo de sobredemanda de cupos se entrena a partir de los datos históricos consolidados en **PostgreSQL 15+**.

### Consulta SQL Base de Extracción:
```sql
SELECT 
    og.periodo_id,
    og.materia_id,
    m.semestre_nivel,
    SUM(og.cupo_ofertado) AS cupos_totales_ofertados,
    SUM(og.inscritos_finales) AS inscritos_totales,
    SUM(og.demanda_insatisfecha_estimada) AS demanda_insatisfecha,
    SUM(og.aprobados) AS aprobados_totales,
    SUM(og.reprobados) AS reprobados_totales,
    SUM(og.abandonos) AS abandonos_totales,
    CASE 
        WHEN SUM(og.demanda_insatisfecha_estimada) > 5 THEN 1 
        ELSE 0 
    END AS etiqueta_sobredemanda
FROM oferta_grupos_materia og
JOIN materias m ON og.materia_id = m.id
JOIN periodos_academicos p ON og.periodo_id = p.id
WHERE p.estado = 'CERRADO'
GROUP BY og.periodo_id, og.materia_id, m.semestre_nivel
ORDER BY og.periodo_id, og.materia_id;
```

---

## 2. Preparación y Limpieza de Datos (Pandas & NumPy)

El microservicio `ai-worker-api` implementa en `app/engines/ml/trainer.py` la siguiente secuencia de transformación:

1. **Cálculo de Demanda Histórica Móvil:**
   Se calcula la media móvil de la demanda insatisfecha de los últimos 3 periodos para cada asignatura utilizando ventanas deslizantes agrupadas por `materia_id`.
2. **Cálculo de Población Habilitada por Prerrequisitos:**
   Se consulta la tabla `prerrequisitos_materia`; para cada materia dependiente, se suma la cantidad de aprobados de sus asignaturas prerrequisito en el periodo inmediato anterior.
3. **Manejo de Valores Nulos:**
   En materias de primer semestre (que no poseen prerrequisitos universitarios previos), la característica `estudiantes_habilitados_prerreq` se imputa con la matrícula proyectada de nuevos ingresos.
4. **Normalización de Tasas:**
   Las tasas de reprobación y abandono se calculan como cocientes acotados en el rango $[0.0, 1.0]$.

---

## 3. Estrategia de Partición y Validación Cruzada

Para garantizar que el modelo no sufra sobreajuste y que las métricas reflejen su capacidad de generalización sobre futuros semestres:

* **Partición de Datos:**
  * **80% Conjunto de Entrenamiento (*Train Set*):** Utilizado para ajustar los árboles de decisión.
  * **20% Conjunto de Prueba (*Test Set*):** Mantenido aislado para la evaluación final no sesgada.
* **Validación Cruzada Estratificada ($K=5$):**
  Se aplica `StratifiedKFold(n_splits=5, shuffle=True, random_state=42)` durante el entrenamiento para conservar la proporción de clases (materias con sobredemanda vs. materias con cupo suficiente) en cada pliegue.

---

## 4. Persistencia y Ciclo de Vida del Modelo

Al completarse el entrenamiento mediante `POST /internal/v1/ml/train`:
1. Se evalúa el $F1\text{-Score}$ promedio en el conjunto de prueba.
2. Si $F1\text{-Score} \ge 0.80$:
   * Se serializa el modelo en disco con compresión zlib:
     ```python
     import joblib
     model_filename = f"/app/models/rf_cupos_v{datetime.now().strftime('%Y_%m')}.joblib"
     joblib.dump(model, model_filename, compress=3)
     ```
   * Se actualiza el puntero de modelo activo en la tabla `modelos_predictivos_ml` (`activo = TRUE`).
3. Si $F1\text{-Score} < 0.80$:
   * Se rechaza la persistencia y se registra la anomalía con código informativo en la respuesta JSON.
