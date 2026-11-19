# ML_MODEL_SPECIFICATION.md
# Especificación del Modelo de Machine Learning para Predicción de Cupos

## 1. Definición Formal del Problema
La planificación semestral de la Dirección de Carrera de Ingeniería en Sistemas enfrenta recurrentemente problemas de **sobredemanda y déficit de cupos** en materias críticas (como INF110, MAT101 e INF511).

El problema se formula como una **clasificación binaria supervisada**:
* Dado un vector de características $\vec{x}$ de una materia en un periodo académico determinado, predecir si la materia experimentará sobredemanda crítica ($y = 1$) o si los cupos planificados serán suficientes ($y = 0$).
* Adicionalmente, el modelo debe estimar la **probabilidad calibrada de sobredemanda $P(y = 1 \mid \vec{x})$**, la cual se utiliza directamente para gobernar el sistema de semaforización de alertas (Rojo si $P \ge 0.75$).

---

## 2. Selección del Algoritmo Oficial: `RandomForestClassifier`

Se selecciona el algoritmo **Random Forest Classifier** de `scikit-learn` por las siguientes justificaciones técnicas:
1. **Modelado de Relaciones No Lineales:** Capta con precisión la interacción compleja entre tasas de reprobación y el embudo de estudiantes habilitados por la cadena de prerrequisitos.
2. **Resistencia al Sobreajuste (Overfitting):** El ensamble de árboles mediante *bagging* y selección aleatoria de características ofrece una alta estabilidad en datasets académicos de tamaño medio.
3. **Calibración de Probabilidades:** Permite extraer estimaciones de probabilidad suaves (`predict_proba`) ideales para la toma de decisiones semafóricas.
4. **Interpretabilidad Institucional:** Facilita la extracción de la importancia relativa de cada característica (*Feature Importance*), permitiendo justificar al Director qué factores provocan la saturación de una materia.

---

## 3. Ingeniería de Características (Feature Engineering)

Para cada materia $m$ evaluada en el periodo semestral $t$, se construye el siguiente vector de características $\vec{x}$:

| Característica | Nombre Técnico | Descripción Matemática |
| :--- | :--- | :--- |
| $X_1$ | `demanda_insatisfecha_prom` | Promedio móvil de solicitudes de adición/casos especiales sin cupo en los últimos 3 semestres: $\frac{1}{3}\sum_{k=1}^{3} D_{t-k}$. |
| $X_2$ | `tasa_reprobacion_historica` | Proporción histórica de estudiantes que reprobaron la materia: $\frac{\text{reprobados}_{t-1}}{\text{inscritos}_{t-1}}$. |
| $X_3$ | `tasa_abandono_historica` | Proporción histórica de estudiantes que abandonaron: $\frac{\text{abandonos}_{t-1}}{\text{inscritos}_{t-1}}$. |
| $X_4$ | `estudiantes_habilitados_prerreq` | Total de estudiantes que aprobaron todos los prerrequisitos inmediatos de la materia en el semestre $t-1$ (demanda potencial entrante). |
| $X_5$ | `cupos_ofertados_planificados` | Número total de cupos abiertos en todos los grupos paralelos planificados para el semestre $t$. |

---

## 4. Configuración de Hiperparámetros y Entrenamiento

```python
from sklearn.ensemble import RandomForestClassifier

def create_model() -> RandomForestClassifier:
    return RandomForestClassifier(
        n_estimators=100,          # 100 árboles de decisión en el ensamble
        max_depth=6,               # Profundidad máxima para prevenir sobreajuste
        min_samples_split=4,       # Mínimo de muestras para dividir un nodo
        min_samples_leaf=2,        # Mínimo de muestras en hojas terminales
        class_weight="balanced",   # Balanceo automático ante clases desbalanceadas
        random_state=42,           # Semilla reproducible
        n_jobs=-1                  # Paralelización sobre todos los cores de CPU
    )
```

---

## 5. Inferencia y Cálculo del Déficit Proyectado

Al ejecutar `POST /internal/v1/ml/predict`, el microservicio evalúa cada asignatura:
1. Calcula la probabilidad $P = \text{predict\_proba}(\vec{x})[1]$.
2. Si $P \ge 0.50$, estima la demanda proyectada mediante una regresión auxiliar ponderada y calcula el déficit:
   $$\text{DeficitEstimado} = \max(0, \text{DemandaProyectada} - \text{CuposOfertados})$$
3. Devuelve la recomendación semafórica:
   * $P \ge 0.75 \rightarrow$ Nivel sugerido: **ROJO**.
   * $0.50 \le P < 0.75 \rightarrow$ Nivel sugerido: **AMARILLO**.
   * $P < 0.50 \rightarrow$ Nivel sugerido: **VERDE**.
