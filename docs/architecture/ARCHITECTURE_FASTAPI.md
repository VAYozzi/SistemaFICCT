# ARCHITECTURE_FASTAPI.md
# Arquitectura y Estándares Técnicos del Microservicio de IA (`ai-worker-api`)

## 1. Propósito y Filosofía del Componente
El componente `ai-worker-api` es el **microservicio especializado de cómputo intensivo e inteligencia artificial** del *Sistema Web de Gestión Administrativa para la Dirección de Carrera de Ingeniería en Sistemas (FICCT - UAGRM)*.

### Responsabilidades Exclusivas:
1. **Visión Artificial y OCR:** Preprocesamiento de imágenes de documentos institucionales mediante OpenCV y extracción de texto con Tesseract OCR en español (`spa`).
2. **Machine Learning Predictivo:** Entrenamiento y ejecución de inferencia con `scikit-learn` (Random Forest Classifier) para predecir sobredemanda y déficit de cupos semestrales.
3. **Automatización Robótica de Procesos (RPA):** Tareas desatendidas de consolidación de correspondencia y generación masiva de reportes PDF oficiales mediante `playwright` y `WeasyPrint` / `ReportLab`.
4. **Asistente Inteligente NLP:** Procesamiento de consultas en lenguaje natural restringido a fuentes estructuradas institucionales.

### Principio de Aislamiento:
* `ai-worker-api` es un servicio **estrictamente interno**. Nunca expone puertos a internet ni atiende peticiones directas del frontend (`web-client`).
* Solo responde a peticiones HTTP autenticadas provenientes de `core-api` mediante la cabecera `X-Internal-Api-Key`.
* No transfiere archivos binarios pesados por red: lee y escribe directamente en el volumen compartido `/app/storage` o mediante **SAS URLs temporales de Azure Blob Storage** provistas por `core-api`.

---

## 2. Stack Tecnológico Oficial

* **Lenguaje:** Python 3.10+ (Tipado estricto con `typing`).
* **Framework:** FastAPI 0.110+ con servidor ASGI Uvicorn.
* **Validación y Configuración:** Pydantic v2 (`pydantic-settings`).
* **Visión Artificial y OCR:**
  * `opencv-python-headless` (filtros, binarización y corrección de rotación).
  * `pytesseract` (Tesseract OCR con paquete de idioma español `tesseract-ocr-spa`).
  * `PyMuPDF` (`fitz`) / `pdf2image` (renderizado de PDFs a imágenes procesables).
* **Machine Learning y Analítica:** `scikit-learn`, `pandas`, `numpy`, `joblib`.
* **Automatización RPA y PDFs:** `playwright` (navegación headless), `WeasyPrint` o `ReportLab`.
* **Cliente HTTP y Pruebas:** `httpx`, `pytest`, `pytest-asyncio`.

---

## 3. Estructura de Directorios Estricta

```text
ai-worker-api/
│
├── app/
│   ├── core/                              # Configuraciones y utilidades de infraestructura
│   │   ├── config.py                      # Variables de entorno con Pydantic BaseSettings
│   │   ├── security.py                    # Validador de cabecera X-Internal-Api-Key
│   │   ├── logging.py                     # Configuración de logs estructurados (Loguru o logging)
│   │   └── lifespan.py                    # Gestión del ciclo de vida de la aplicación (arranque/apagado)
│   │
│   ├── api/                               # Controladores y rutas HTTP internas
│   │   └── v1/
│   │       ├── router.py                  # Agregador principal de rutas (/internal/v1)
│   │       ├── ocr.py                     # Endpoints de extracción OCR
│   │       ├── ml.py                      # Endpoints de entrenamiento e inferencia ML
│   │       ├── assistant.py               # Endpoints del asistente inteligente
│   │       └── rpa.py                     # Endpoints de ejecución y monitor de bots RPA
│   │
│   ├── schemas/                           # Modelos Pydantic v2 (Contratos de Request y Response)
│   │   ├── ocr_schema.py                  # OcrExtractRequest, OcrExtractResponse
│   │   ├── ml_schema.py                   # TrainModelResponse, PredictQuotaRequest, PredictQuotaResponse
│   │   ├── assistant_schema.py            # AssistantQueryRequest, AssistantQueryResponse
│   │   └── rpa_schema.py                  # RpaExecuteRequest, RpaExecuteResponse
│   │
│   ├── services/                          # Orquestadores de casos de uso (descarga SAS, coordinación)
│   │   ├── ocr_service.py                 # Descarga/lectura de imagen -> invoca motor -> formatea
│   │   ├── ml_service.py                  # Lectura de dataset -> invoca Random Forest -> persiste modelo
│   │   ├── assistant_service.py           # Valida contexto del rol -> consulta motor -> genera respuesta
│   │   └── rpa_service.py                 # Lanza tarea Playwright -> sube PDF resultante
│   │
│   ├── engines/                           # Motores especializados de cómputo puro
│   │   ├── ocr/                           # Pipeline OpenCV + PyTesseract
│   │   │   ├── preprocessor.py            # Escala de grises, Otsu, Gaussian blur, deskew
│   │   │   └── extractor.py               # Extracción por página y cálculo de confianza
│   │   ├── ml/                            # Pipeline Scikit-Learn
│   │   │   ├── trainer.py                 # Entrenamiento, validación cruzada y cálculo F1-Score
│   │   │   └── predictor.py               # Carga de .joblib e inferencia de demanda
│   │   ├── nlp/                           # Pipeline de procesamiento de lenguaje natural
│   │   │   └── query_matcher.py           # Extracción de entidades y clasificación de intención
│   │   └── rpa/                           # Automatización desatendida
│   │       ├── report_builder.py          # Renderizado HTML/CSS a PDF oficial
│   │       └── browser_worker.py          # Scripts headless de Playwright
│   │
│   ├── models/                            # Repositorio de modelos entrenados persistidos (.joblib)
│   └── utils/                             # Helpers (descargas con httpx, limpieza de archivos temporales)
│       ├── azure_storage.py               # Lectura y subida por SAS URL
│       └── file_cleaner.py                # Context managers para borrado seguro de temporales
│
├── tests/                                 # Pruebas automatizadas con pytest
│   ├── test_ocr.py
│   ├── test_ml.py
│   └── test_security.py
│
├── Dockerfile                             # Instalación de Python + dependencias C (tesseract, libgl)
└── requirements.txt                       # Librerías fijadas con versión exacta
```

---

## 4. Regla de Oro de Concurrencia: Async vs. CPU-Bound

FastAPI corre sobre un **bucle de eventos asíncrono (`asyncio event loop`)** de un solo hilo por worker de Uvicorn.  
* Las tareas de I/O de red (descargar un archivo vía SAS URL, esperar una respuesta HTTP) son excelentes en funciones `async def`.
* **PELIGRO CRÍTICO:** Las tareas de **cómputo intensivo de CPU** (filtros de OpenCV, procesamiento de imágenes con Tesseract, entrenamiento de modelos o inferencia matemática) **bloquean por completo el event loop** si se llaman directamente dentro de una función `async def`, congelando todas las demás peticiones que `core-api` envíe a FastAPI.

### Regla Obligatoria:
Toda llamada a un motor de CPU intensivo debe delegarse al hilo de fondo mediante `asyncio.to_thread(...)`:

```python
# FORMA CORRECTA Y OBLIGATORIA
import asyncio
from fastapi import APIRouter, Depends, HTTPException, status
from app.schemas.ocr_schema import OcrExtractRequest, OcrExtractResponse
from app.services.ocr_service import OcrService
from app.core.security import verify_internal_api_key

router = APIRouter(prefix="/ocr", tags=["OCR Engine"])

@router.post("/extract", response_model=OcrExtractResponse)
async def extract_text_from_document(
    request: OcrExtractRequest,
    ocr_service: OcrService = Depends(),
    _: None = Depends(verify_internal_api_key)
) -> OcrExtractResponse:
    try:
        # La llamada pesada de CPU (OpenCV + Tesseract) se corre en un hilo separado
        result = await asyncio.to_thread(ocr_service.process_document, request)
        return result
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error durante el procesamiento OCR: {str(e)}"
        )
```

---

## 5. Especificación de los Motores Especializados (`engines/`)

### 5.1. Pipeline de Visión Artificial y OCR (`engines/ocr/`)
Para garantizar la máxima legibilidad de documentos administrativos escaneados, el pipeline de preprocesamiento debe ejecutar estrictamente los siguientes pasos en OpenCV antes de invocar a Tesseract:

1. **Conversión a Escala de Grises:** `cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)`.
2. **Filtro Gaussiano:** `cv2.GaussianBlur(gray, (3, 3), 0)` para reducir ruido de escaneo.
3. **Binarización Adaptativa u Otsu:** `cv2.threshold(blur, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)`.
4. **Corrección de Inclinación (Deskewing):** Detección del ángulo de las líneas de texto mediante transformada de Hough o `cv2.minAreaRect` y rotación de la matriz si el ángulo supera $\pm 0.5^\circ$.
5. **Invocación a Tesseract:**
   * Idioma configurado: `-l spa`.
   * PSM (Page Segmentation Mode): `PSM 3` (completamente automático) o `PSM 6` (bloque uniforme de texto).
   * Extracción de texto y cálculo de métrica de confianza promedio de los caracteres (`pytesseract.image_to_data`).

---

### 5.2. Pipeline de Machine Learning Predictivo (`engines/ml/`)
* **Algoritmo Base:** `RandomForestClassifier` de Scikit-Learn (comparado contra `LogisticRegression`).
* **Variables de Entrada (Features):**
  * Histórico de demanda insatisfecha por materia en los últimos 3 semestres.
  * Tasa de reprobación y abandono del semestre inmediato anterior.
  * Número de estudiantes habilitados por la cadena de prerrequisitos de la materia.
  * Cupos originalmente ofertados.
* **Criterio de Validación para Pase a Producción:**
  * El modelo debe entrenarse mediante validación cruzada ($K\text{-Fold} = 5$).
  * **Regla Inmutable:** El modelo solo se marca como `activo = true` si alcanza un **$F1\text{-Score} \ge 0.80$**.
* **Persistencia:** Guardado seguro en disco mediante `joblib.dump(modelo, path, compress=3)`.
* **Inferencia de Alertas:** Calcula la probabilidad de sobredemanda $P$:
  * $P \ge 0.75 \rightarrow$ Nivel sugerido: **ROJO**.
  * $0.50 \le P < 0.75 \rightarrow$ Nivel sugerido: **AMARILLO**.
  * $P < 0.50 \rightarrow$ Nivel sugerido: **VERDE**.

---

### 5.3. Pipeline de Automatización RPA (`engines/rpa/`)
* **Librería Core:** `playwright` en modo headless (`chromium`).
* **Casos de Uso:**
  * **Consolidación de Correspondencia:** Navegación, agrupamiento de resoluciones y generación de listas consolidadas.
  * **Generación de Reportes PDF:** Uso de plantillas HTML institucionales procesadas mediante Jinja2 y renderizadas a PDF oficial con numeración correlativa y marcas de agua UAGRM.
* **Resiliencia:** Máximo 3 reintentos en caso de fallas transitorias de renderizado, reportando detalladamente cualquier error en el payload de respuesta JSON.

---

## 6. Seguridad y Validación de Entrada

### 6.1. Validación de Cabecera Interna (`X-Internal-Api-Key`)
Ningún endpoint procesará una solicitud si la cabecera no coincide exactamente con el secreto configurado en el archivo de entorno:

```python
from fastapi import Header, HTTPException, status
from app.core.config import settings

async def verify_internal_api_key(x_internal_api_key: str = Header(..., alias="X-Internal-Api-Key")):
    if x_internal_api_key != settings.INTERNAL_SHARED_SECRET:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Acceso denegado: Cabecera X-Internal-Api-Key inválida o ausente"
        )
```

### 6.2. Esquemas Pydantic v2
Todo DTO de entrada y salida debe definirse con tipado estricto, sin permitir campos extraños (`extra = "forbid"`):

```python
from pydantic import BaseModel, Field, HttpUrl
from typing import List, Optional

class OcrExtractRequest(BaseModel):
    documento_id: int = Field(..., gt=0, description="ID del documento en base de datos")
    azure_read_sas_url: Optional[str] = Field(None, description="URL temporal SAS para lectura en Azure")
    ruta_archivo_local: Optional[str] = Field(None, description="Ruta física en volumen /app/storage")
    tipo_mime: str = Field(..., pattern="^(application/pdf|image/jpeg|image/png)$")
    idioma: str = Field(default="spa", pattern="^(spa|eng)$")

    class Config:
        extra = "forbid"
```

---

## 7. Pruebas y Control de Calidad
Antes de dar por completado cualquier cambio en `ai-worker-api`, la IA debe verificar:
1. **Pruebas de OCR:** Procesamiento exitoso de una imagen de prueba simulada verificando que el texto devuelto contenga las palabras clave esperadas.
2. **Pruebas de ML:** Entrenamiento sintético verificando que el archivo `.joblib` se genere y que la inferencia devuelva probabilidades normalizadas entre $[0.0, 1.0]$.
3. **Pruebas de Seguridad:** Validación de rechazo HTTP 403 cuando no se envíe `X-Internal-Api-Key`.
4. **Comando de Verificación:**  
   `pytest -v` (todos los tests deben pasar exitosamente).
