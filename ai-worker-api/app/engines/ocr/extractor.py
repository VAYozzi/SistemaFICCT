import os
import pytesseract
from PIL import Image
import fitz # PyMuPDF
import numpy as np
from app.engines.ocr.preprocessor import preprocess_image

# Configurar ruta oficial de Tesseract en Windows
TESSERACT_EXE = r"C:\Program Files\Tesseract-OCR\tesseract.exe"
if os.path.exists(TESSERACT_EXE):
    pytesseract.pytesseract.tesseract_cmd = TESSERACT_EXE

def extract_text_from_file(file_path: str, lang: str = "spa") -> list[dict]:
    """
    Procesa un archivo PDF o imagen por demanda.
    Retorna lista de páginas con texto extraído, nivel de confianza y número de página.
    """
    if not os.path.exists(file_path):
        raise FileNotFoundError(f"Archivo no encontrado en almacenamiento: {file_path}")

    ext = os.path.splitext(file_path)[1].lower()
    paginas = []

    if ext in [".pdf"]:
        doc = fitz.open(file_path)
        for page_idx in range(len(doc)):
            page = doc.load_page(page_idx)
            pix = page.get_pixmap(dpi=200)
            img_bytes = pix.tobytes("png")
            
            # Preprocesar con OpenCV
            processed_img = preprocess_image(img_bytes)

            # Ejecutar PyTesseract con diccionario en español
            data = pytesseract.image_to_data(processed_img, lang=lang, output_type=pytesseract.Output.DICT)
            
            # Calcular confianza promedio omitiendo textos vacíos
            confidences = [int(c) for c, w in zip(data["conf"], data["text"]) if int(c) >= 0 and w.strip()]
            avg_conf = float(np.mean(confidences)) if confidences else 0.0

            texto = pytesseract.image_to_string(processed_img, lang=lang).strip()

            paginas.append({
                "numero_pagina": page_idx + 1,
                "texto_extraido": texto,
                "confianza": round(avg_conf, 2)
            })
        doc.close()

    elif ext in [".png", ".jpg", ".jpeg", ".tiff", ".bmp"]:
        with open(file_path, "rb") as f:
            img_bytes = f.read()

        processed_img = preprocess_image(img_bytes)
        data = pytesseract.image_to_data(processed_img, lang=lang, output_type=pytesseract.Output.DICT)
        confidences = [int(c) for c, w in zip(data["conf"], data["text"]) if int(c) >= 0 and w.strip()]
        avg_conf = float(np.mean(confidences)) if confidences else 0.0

        texto = pytesseract.image_to_string(processed_img, lang=lang).strip()

        paginas.append({
            "numero_pagina": 1,
            "texto_extraido": texto,
            "confianza": round(avg_conf, 2)
        })
    else:
        raise ValueError(f"Formato no soportado para OCR: {ext}")

    return paginas

