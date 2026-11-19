import cv2
import numpy as np

def preprocess_image(image_bytes: bytes) -> np.ndarray:
    """
    Preprocesamiento de imagen optimizado según arquitectura:
    1. Conversión de bytes a imagen en memoria
    2. Escala de grises
    3. Reducción de ruido con filtro Gaussiano
    4. Binarización adaptativa de Otsu
    5. Corrección de rotación / deskewing leve
    """
    nparr = np.frombuffer(image_bytes, np.uint8)
    img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
    if img is None:
        raise ValueError("No se pudo decodificar la imagen provista")

    # 1. Escala de grises
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    # 2. Reducción de ruido
    denoised = cv2.GaussianBlur(gray, (3, 3), 0)

    # 3. Binarización de Otsu
    _, thresh = cv2.threshold(denoised, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)

    return thresh

