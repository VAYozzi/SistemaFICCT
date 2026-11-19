from fastapi import FastAPI, Depends, Header, HTTPException, status
from pydantic import BaseModel, Field
from pydantic_settings import BaseSettings
import uvicorn
import asyncio
import os
from typing import List, Optional
from app.engines.ocr.extractor import extract_text_from_file
from app.engines.ml.cupos_predictor import predecir_demanda_cupos

class Settings(BaseSettings):
    internal_shared_secret: str = "ficct_super_secret_internal_key_2026"
    storage_base_path: str = "C:/Users/vajho/Desktop/SistemaFICCT/storage"
    tesseract_cmd: str = "C:/Program Files/Tesseract-OCR/tesseract.exe"

    class Config:
        env_file = "../.env"
        extra = "ignore"

settings = Settings()

app = FastAPI(
    title="FICCT AI Worker API",
    description="Microservicio de OCR, Analítica Predictiva y RPA para Dirección de Carrera FICCT-UAGRM",
    version="1.0.0"
)

def verify_internal_key(x_internal_api_key: str = Header(...)):
    if x_internal_api_key != settings.internal_shared_secret:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Acceso no autorizado: Cabecera X-Internal-Api-Key inválida o ausente"
        )
    return x_internal_api_key

class OcrRequest(BaseModel):
    ruta_relativa: str = Field(..., description="Ruta relativa del archivo dentro de /storage")
    idioma: Optional[str] = Field("spa", description="Idioma de OCR (spa, eng)")

class OcrPageResponse(BaseModel):
    numero_pagina: int
    texto_extraido: str
    confianza: float

class OcrResponse(BaseModel):
    documento: str
    total_paginas: int
    paginas: List[OcrPageResponse]

class PrediccionCuposRequest(BaseModel):
    materia_sigla: str
    demanda_insatisfecha_prom: float
    tasa_reprobacion_historica: float
    tasa_abandono_historica: float
    estudiantes_habilitados_prerreq: int
    cupos_ofertados_planificados: int

class PrediccionCuposResponse(BaseModel):
    materia_sigla: str
    probabilidad_sobredemanda: float
    nivel_alerta: str
    cupos_ofertados: int
    demanda_proyectada: int
    deficit_estimado: int
    es_datos_prueba: bool
    aviso_institucional: str

@app.get("/health")
def health_check():
    return {
        "status": "UP",
        "service": "ai-worker-api",
        "storage_path": settings.storage_base_path
    }

@app.get("/internal/v1/status", dependencies=[Depends(verify_internal_key)])
def internal_status():
    return {
        "status": "OPERATIONAL",
        "security": "VERIFIED",
        "modules": ["OCR", "ML_PREDICTOR", "RPA", "ASSISTANT"]
    }

@app.post("/internal/v1/ocr/extract", response_model=OcrResponse, dependencies=[Depends(verify_internal_key)])
async def extract_ocr(request: OcrRequest):
    normalized_rel = os.path.normpath(request.ruta_relativa).lstrip("/\\")
    full_path = os.path.abspath(os.path.join(settings.storage_base_path, normalized_rel))

    if not full_path.startswith(os.path.abspath(settings.storage_base_path)):
        raise HTTPException(status_code=400, detail="Violación de seguridad: Ruta fuera de los límites de almacenamiento")

    if not os.path.exists(full_path):
        raise HTTPException(status_code=404, detail=f"Archivo no encontrado en el sistema: {request.ruta_relativa}")

    try:
        paginas = await asyncio.to_thread(extract_text_from_file, full_path, request.idioma)
        return OcrResponse(
            documento=request.ruta_relativa,
            total_paginas=len(paginas),
            paginas=paginas
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error durante el procesamiento OCR: {str(e)}")

@app.post("/internal/v1/ml/predict-cupos", response_model=PrediccionCuposResponse, dependencies=[Depends(verify_internal_key)])
async def predict_cupos(request: PrediccionCuposRequest):
    try:
        pred_dict = await asyncio.to_thread(
            predecir_demanda_cupos,
            request.demanda_insatisfecha_prom,
            request.tasa_reprobacion_historica,
            request.tasa_abandono_historica,
            request.estudiantes_habilitados_prerreq,
            request.cupos_ofertados_planificados
        )
        return PrediccionCuposResponse(
            materia_sigla=request.materia_sigla,
            **pred_dict
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error al ejecutar modelo predictivo: {str(e)}")

if __name__ == "__main__":
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)
