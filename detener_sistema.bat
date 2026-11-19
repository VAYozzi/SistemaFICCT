@echo off
chcp 65001 > nul
echo =========================================================================
echo   DETENIENDO SERVICIOS DE SISTEMAFICCT (FICCT - UAGRM)
echo =========================================================================

echo Deteniendo procesos Java (Spring Boot) y Python (FastAPI)...
taskkill /F /IM java.exe /T 2>nul
taskkill /F /IM python.exe /T 2>nul
taskkill /F /IM uvicorn.exe /T 2>nul
taskkill /F /IM node.exe /T 2>nul

echo [OK] Todos los servicios de SistemaFICCT han sido detenidos y la memoria RAM fue liberada.
pause

