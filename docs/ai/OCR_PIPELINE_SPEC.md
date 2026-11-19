# OCR_PIPELINE_SPEC.md
# Especificación Técnica del Pipeline de Visión Artificial y OCR

## 1. Propósito y Tecnologías
Este documento detalla el pipeline matemático y de procesamiento de imágenes implementado en **`ai-worker-api`** para digitalizar y extraer texto de documentos universitarios escaneados (cartas de convalidación, certificados de notas, cédulas de identidad y resoluciones rectorales).

### Stack de Visión Artificial:
* **Librerías Core:** `opencv-python-headless` (4.9+), `pytesseract` (0.3.10+), `PyMuPDF` (`fitz`) / `pdf2image`, `numpy`.
* **Motor OCR Base:** Tesseract OCR 5.x con diccionario en español (`tesseract-ocr-spa`).

---

## 2. Pipeline de Preprocesamiento de Imágenes en OpenCV

Para maximizar la precisión de Tesseract en escaneos con ruido, sombras o inclinaciones, la imagen atraviesa estrictamente las siguientes etapas antes de la extracción de texto:

```text
[ Documento Original (PDF/JPG) ]
               │
               ▼
   [ 1. Renderizado a 300 DPI (PyMuPDF) ]
               │
               ▼
   [ 2. Conversión a Escala de Grises (cv2.cvtColor) ]
               │
               ▼
   [ 3. Filtro Gaussiano 3x3 (Reducción de ruido) ]
               │
               ▼
   [ 4. Binarización Adaptativa u Otsu (cv2.threshold) ]
               │
               ▼
   [ 5. Detección y Corrección de Inclinación (Deskewing) ]
               │
               ▼
   [ 6. Extracción con PyTesseract (-l spa --psm 3) ]
               │
               ▼
[ Texto Extraído Crudo + Nivel de Confianza ]
```

### 2.1. Detalle Matemático del Pipeline

#### Paso 1: Conversión de PDF a Imagen de Alta Resolución
Si el archivo de entrada es un PDF, cada página se rasteriza a una resolución de **300 DPI** utilizando `fitz.Matrix(300/72, 300/72)`, generando matrices de píxeles BGR.

#### Paso 2: Escala de Grises
```python
gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
```

#### Paso 3: Filtro Gaussiano de Reducción de Ruido
Se aplica un suavizado espacial con un kernel de $3 \times 3$ y desviación estándar calculada automáticamente ($\sigma = 0$) para eliminar granulado de fotocopiadora sin desenfocar los bordes de los caracteres:
```python
blurred = cv2.GaussianBlur(gray, (3, 3), 0)
```

#### Paso 4: Binarización Adaptativa / Umbral de Otsu
El algoritmo de Otsu calcula el umbral óptimo $\tau$ que minimiza la varianza intra-clase de los niveles de gris, separando limpiamente la tinta del papel:
```python
_, binary = cv2.threshold(blurred, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)
```

#### Paso 5: Corrección de Inclinación (Deskewing)
Si un documento fue colocado con un ángulo de inclinación en el escáner:
1. Se calculan las coordenadas de los píxeles de texto ($v > 0$).
2. Se calcula el rectángulo delimitador de área mínima con `cv2.minAreaRect(pts)`.
3. Se extrae el ángulo $\theta$. Si $|\theta| > 0.5^\circ$, se genera la matriz afín de rotación centrada y se endereza el documento:
```python
def deskew_image(binary_img: np.ndarray) -> np.ndarray:
    coords = np.column_stack(np.where(binary_img == 0)) # Píxeles de texto (negros)
    angle = cv2.minAreaRect(coords)[-1]
    if angle < -45:
        angle = -(90 + angle)
    else:
        angle = -angle
    
    if abs(angle) > 0.5:
        (h, w) = binary_img.shape[:2]
        center = (w // 2, h // 2)
        M = cv2.getRotationMatrix2D(center, angle, 1.0)
        return cv2.warpAffine(binary_img, M, (w, h), flags=cv2.INTER_CUBIC, borderMode=cv2.BORDER_REPLICATE)
    return binary_img
```

---

## 3. Invocación y Configuración de Tesseract OCR

* **Parámetros de Ejecución:**
  * Idioma: `-l spa`.
  * Page Segmentation Mode: `--psm 3` (Segmentación completamente automática de páginas) o `--psm 6` para bloques uniformes.
  * OCR Engine Mode: `--oem 1` (Redes neuronales LSTM de Tesseract 5).
* **Cálculo de Métrica de Confianza:**
  Mediante `pytesseract.image_to_data(..., output_type=Output.DICT)`, se extrae la confianza individual ($0$ a $100$) de cada palabra identificada, calculando el promedio ponderado:
  $$\text{NivelConfianza} = \frac{\sum_{i=1}^{N} \text{conf}_i}{N} \quad (\text{donde } \text{conf}_i \ge 0)$$

---

## 4. Regla de Concurrencia y Soberanía Humana

* **Concurrencia:** La llamada a este pipeline en FastAPI se ejecuta obligatoriamente en un hilo de fondo:
  ```python
  result = await asyncio.to_thread(ocr_engine.extract_text, input_file)
  ```
* **Soberanía Humana:** El texto extraído nace con `estado_validacion = 'PENDIENTE_VALIDACION'`. Se presenta en el **Visor Dual** de React para que el funcionario humano confirme su validez antes de activar el trigger de indexación en PostgreSQL.
