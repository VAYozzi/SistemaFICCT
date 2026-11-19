# MOD_05_SERVICIOS_INTELIGENTES_OCR.md
# Especificación Funcional: Reconocimiento Óptico de Caracteres (OCR) y Visor Dual

## 1. Alcance y Casos de Uso Vinculados
Este módulo implementa el subsistema de visión artificial, extracción de texto mediante OCR, el Visor Comparativo Dual con soberanía humana obligatoria, y el motor de búsqueda de texto completo (`tsvector`) con asistente NLP.

* **Requisitos del Proyecto:** RF-17, RF-18, RF-19, RF-22, RF-23.
* **Casos de Uso IEEE 830 / RUP:**
  * `CU-21`: Procesar documentos mediante OCR.
  * `CU-22`: Revisar y validar texto extraído mediante OCR (Visor Comparativo Dual).
  * `CU-23`: Realizar búsqueda avanzada de texto completo.
  * `CU-24`: Realizar búsqueda semántica de documentos.
  * `CU-25`: Consultar información mediante asistente inteligente NLP.

---

## 2. Reglas Inmutables de Negocio (Soberanía Humana)

1. **Estado Inicial No Indexado:**
   * Todo documento procesado por OpenCV + PyTesseract nace con:
     * `estado_validacion = 'PENDIENTE_VALIDACION'`
     * `vector_busqueda = NULL`
   * **PROHIBICIÓN ESTRICTA:** El texto extraído crudo **nunca se indexa de forma automática** en el motor de búsqueda oficial.
2. **Activación de Búsqueda mediante Trigger Nativo:**
   * La búsqueda de texto completo solo indexa contenido cuando un funcionario (`ROLE_AUXILIAR` o superior) valida o corrige el texto extraído en el Visor Dual.
   * Al ejecutarse `PUT /api/v1/ocr/{id}/validar`, el campo `estado_validacion` cambia a `VALIDADO` y el trigger nativo de PostgreSQL `trg_procesos_ocr_vector` puebla automáticamente la columna `vector_busqueda`:
     ```sql
     NEW.vector_busqueda := to_tsvector('spanish', NEW.texto_validado_humano);
     ```
3. **No Bloqueo del Servidor:**
   * Toda operación pesada de visión artificial en `ai-worker-api` debe correr obligatoriamente encapsulada en `asyncio.to_thread(...)`.

---

## 3. Especificación Técnica Microservicio (`ai-worker-api`)

### 3.1. Pipeline OpenCV + PyTesseract (`engines/ocr/`)
* **Entrada:** Archivo en `/app/storage` o leído por SAS URL temporal de Azure.
* **Fases del Preprocesamiento de Imagen:**
  1. Conversión de PDF a imagen a 300 DPI mediante `pdf2image` o `PyMuPDF` (`fitz`).
  2. Conversión a escala de grises: `cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)`.
  3. Filtro Gaussiano de 3x3: `cv2.GaussianBlur(gray, (3, 3), 0)`.
  4. Binarización adaptativa u Otsu: `cv2.threshold(blur, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)`.
  5. Deskewing: Corrección automática de inclinación en caso de escaneos torcidos.
* **Invocación a PyTesseract:**
  * Parámetros: `-l spa --psm 3`.
  * Cálculo de nivel de confianza ponderado de los caracteres devueltos.
* **Payload de Salida:**
  ```json
  {
    "documento_id": 104,
    "exito": true,
    "texto_extraido_crudo": "UNIVERSIDAD AUTÓNOMA GABRIEL RENÉ MORENO...",
    "nivel_confianza": 92.40,
    "paginas_procesadas": 1,
    "pipeline_aplicado": ["grayscale", "gaussian_blur", "otsu", "deskew"]
  }
  ```

---

## 4. Especificación Técnica Frontend (`web-client`)

### 4.1. Componente `DualOcrViewer.tsx` (`features/ocr/`)
El componente se divide en dos columnas ergonómicas de 50% de ancho:
* **Panel Izquierdo (Visor del Documento Original):**
  * Utiliza `react-pdf` y `react-zoom-pan-pinch`.
  * Barra superior de controles:
    * 🔍 Zoom In (`+`), Zoom Out (`-`), Reset (`100%`), Ajustar a Ancho.
    * 🔄 Rotar 90° a la derecha.
    * 📄 Selector de páginas (si es un documento multi-página).
* **Panel Derecho (Editor Humano de Validación):**
  * Encabezado con medidor de confianza:
    * 🟢 Verde: Confianza $\ge 85\%$.
    * 🟡 Ámbar: Confianza entre $65\%$ y $84\%$.
    * 🔴 Rojo: Confianza $< 65\%$.
  * Área de edición `textarea` con el texto extraído editable por el usuario.
  * Botón destacado de acción: **"Confirmar y Validar Texto"** (envía la mutación y actualiza el estado a `VALIDADO`).

---

## 5. Pruebas y Criterios de Aceptación
1. Subir un documento y verificar que antes de validarlo la búsqueda por palabras clave no lo retorne.
2. Tras presionar "Confirmar y Validar" en el visor dual, la búsqueda documental debe encontrar el documento inmediatamente gracias al trigger `tsvector`.
3. Procesar un PDF rotado a 15° y verificar que el algoritmo de deskewing lo corrija antes de enviarlo a Tesseract.
