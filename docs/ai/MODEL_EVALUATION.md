# MODEL_EVALUATION.md
# Evaluación de Rendimiento, Métricas y Calibración de Umbrales del Modelo ML

## 1. Métricas de Evaluación Matemática
Dado que el déficit de cupos afecta directamente el avance estudiantil y la asignación presupuestaria de la facultad, la evaluación del modelo no puede basarse únicamente en la Exactitud (*Accuracy*), debido al desbalance natural de las clases.

Se evalúa formalmente la **Matriz de Confusión**:
* **Verdadero Positivo (TP):** El modelo predijo sobredemanda y la materia colapsó en la realidad.
* **Falso Positivo (FP):** El modelo predijo sobredemanda pero los cupos alcanzaron (riesgo: gasto innecesario en contratar un docente interino).
* **Falso Negativo (FN):** El modelo predijo cupos suficientes pero la materia colapsó (riesgo: estudiantes rezagados y quejas en ventanilla).
* **Verdadero Negativo (TN):** El modelo predijo cupos suficientes y se operó con normalidad.

---

## 2. Fórmulas de las Métricas Oficiales

### 2.1. Precisión (*Precision*)
$$\text{Precision} = \frac{\text{TP}}{\text{TP} + \text{FP}}$$
*Mide qué porcentaje de las materias alertadas con sobredemanda realmente requerían apertura de grupos adicionales.*

### 2.2. Sensibilidad / Exhaustividad (*Recall*)
$$\text{Recall} = \frac{\text{TP}}{\text{TP} + \text{FN}}$$
*Mide qué porcentaje de los cuellos de botella reales fueron anticipados con éxito por el modelo.*

### 2.3. Puntuación F1 (*F1-Score*)
$$\text{F1-Score} = 2 \cdot \frac{\text{Precision} \cdot \text{Recall}}{\text{Precision} + \text{Recall}}$$
*Media armónica que penaliza desequilibrios extremos entre precisión y sensibilidad.*

---

## 3. Justificación del Umbral Crítico $F1 \ge 0.80$

* **Decisión Institucional:** Un modelo predictivo que toma decisiones administrativas debe tener un balance riguroso.
  * Si la precisión es muy baja ($< 0.70$), la Dirección de Carrera autorizaría grupos paralelos innecesarios, desperdiciando horas académicas.
  * Si la sensibilidad es muy baja ($< 0.70$), materias críticas como *Taller de Grado* dejarían a decenas de estudiantes sin cupo sin aviso previo.
* Por este motivo, el sistema exige de forma inmutable:
  $$\mathbf{F1\text{-Score} \ge 0.80}$$
  Cualquier modelo que alcance un valor inferior en el conjunto de prueba es **automáticamente bloqueado** y no reemplaza al modelo en producción.

---

## 4. Calibración de Umbrales de Probabilidad Semafórica

La salida de `predict_proba` entrega una probabilidad $P \in [0.0, 1.0]$. Se calibran los umbrales operativos para la toma de decisiones:

```text
  0.0                                0.50                 0.75               1.0
   ├───────────────────────────────────┼────────────────────┼─────────────────┤
   │           🟢 VERDE                │    🟡 AMARILLO     │    🔴 ROJO      │
   │      Capacidad Adecuada           │ Capacidad en Límite│ Déficit Crítico │
   │    (No requiere medidas)          │   (Monitorear)     │ (Abrir Grupo)   │
```

* **🔴 Nivel ROJO ($P \ge 0.75$):** Con una certeza matemática superior al 75%, se dispara la alerta máxima que requiere que el Director evalúe de inmediato la asignación de un docente para un grupo paralelo.
* **🟡 Nivel AMARILLO ($0.50 \le P < 0.75$):** La demanda estimada está cerca del 100% de la capacidad de aula; el Coordinador supervisa las adiciones diarias.
* **🟢 Nivel VERDE ($P < 0.50$):** La oferta actual cubre holgadamente la demanda proyectada.
