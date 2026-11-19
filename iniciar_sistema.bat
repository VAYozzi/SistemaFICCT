@echo off
chcp 65001 > nul
echo =========================================================================
echo   INICIANDO SISTEMAFICCT (FICCT - UAGRM) - ENTORNO LOCAL
echo =========================================================================

REM 1. Verificar servicio de PostgreSQL 16
echo [1/4] Verificando servicio de PostgreSQL 16 local...
sc query postgresql-x64-16 | find "RUNNING" > nul
if %errorlevel% neq 0 (
    echo [INFO] Iniciando servicio de PostgreSQL...
    net start postgresql-x64-16
) else (
    echo [OK] PostgreSQL 16 se encuentra en ejecucion.
)

REM 2. Iniciar FastAPI ai-worker-api (Puerto 8000)
echo [2/4] Levantando FastAPI ai-worker-api en http://127.0.0.1:8000...
start "FICCT - ai-worker-api (FastAPI)" cmd /k "cd /d C:\Users\vajho\Desktop\SistemaFICCT\ai-worker-api && .\venv\Scripts\uvicorn.exe app.main:app --host 127.0.0.1 --port 8000"

REM 3. Iniciar Spring Boot core-api (Puerto 8080)
echo [3/4] Levantando Spring Boot core-api en http://localhost:8080...
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.21.9-hotspot"
set "Path=%JAVA_HOME%\bin;%Path%"
start "FICCT - core-api (Spring Boot)" cmd /k "cd /d C:\Users\vajho\Desktop\SistemaFICCT\core-api && .\mvnw.cmd spring-boot:run"

REM 4. Esperar a que el backend Spring Boot este listo antes de levantar el frontend
echo [INFO] Esperando a que el backend Spring Boot (Puerto 8080) este listo...
:wait_core
timeout /t 2 /nobreak > nul
powershell -NoProfile -Command "try { $res = Invoke-WebRequest -Uri 'http://localhost:8080/api/v1/auth/login' -Method Options -UseBasicParsing -TimeoutSec 2; if ($res.StatusCode -eq 200) { exit 0 } else { exit 1 } } catch { exit 1 }"
if %errorlevel% neq 0 (
    echo ... esperando inicializacion de core-api en puerto 8080 ...
    goto wait_core
)
echo [OK] Spring Boot core-api esta listo y respondiendo en el puerto 8080.

REM 5. Iniciar Frontend React web-client (Puerto 5173)
echo [4/4] Levantando Frontend React web-client en http://localhost:5173...
start "FICCT - web-client (React Vite)" cmd /k "cd /d C:\Users\vajho\Desktop\SistemaFICCT\web-client && npm run dev"

echo [INFO] Esperando disponibilidad del Frontend React...
:wait_web
timeout /t 2 /nobreak > nul
powershell -NoProfile -Command "try { $res = Invoke-WebRequest -Uri 'http://localhost:5173' -Method Get -UseBasicParsing -TimeoutSec 2; if ($res.StatusCode -eq 200) { exit 0 } else { exit 1 } } catch { exit 1 }"
if %errorlevel% neq 0 (
    echo ... esperando inicializacion de Vite dev server ...
    goto wait_web
)
echo [OK] Frontend React web-client esta listo y respondiendo en el puerto 5173.

echo.
echo =========================================================================
echo   SISTEMAFICCT INICIADO EXITOSAMENTE
echo =========================================================================
echo   - Frontend:    http://localhost:5173
echo   - Backend:     http://localhost:8080/api/v1
echo   - AI Worker:   http://127.0.0.1:8000/docs
echo =========================================================================
echo.
pause
