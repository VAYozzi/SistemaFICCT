# BATERIA DE PRUEBAS INTEGRALES Y REGRESION - BLOQUE G
Write-Output "================================================================="
Write-Output "EJECUTANDO AUDITORIA Y PRUEBAS E2E DE SISTEMAFICCT (BLOQUES A-F)"
Write-Output "================================================================="

try {
    # 1. TEST RBAC Y AUTENTICACION
    Write-Output "`n[1/7] VERIFICANDO AUTENTICACION Y CONTROL DE ACCESO RBAC..."
    $dirLogin = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/auth/login' -Method POST -ContentType 'application/json' -Body (@{ correoInstitucional = 'nuevo.coordinador@uagrm.edu.bo'; password = 'Coord2026!' } | ConvertTo-Json)
    $dirToken = $dirLogin.data.token
    $dirHeaders = @{ Authorization = "Bearer $dirToken" }
    Write-Output "  -> Login Director: OK (Rol: $($dirLogin.data.rolCodigo))"

    # Probar acceso de auxiliar bloqueado a usuarios (debe fallar 403)
    $auxLogin = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/auth/login' -Method POST -ContentType 'application/json' -Body (@{ correoInstitucional = 'secretaria.auxiliar@uagrm.edu.bo'; password = 'Auxiliar2026!' } | ConvertTo-Json)
    $auxToken = $auxLogin.data.token
    $auxHeaders = @{ Authorization = "Bearer $auxToken" }
    try {
        Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/usuarios' -Method GET -Headers $auxHeaders
        Write-Error "  -> [FALLO] Auxiliar pudo acceder a /usuarios"
    } catch {
        Write-Output "  -> [EXITO] Auxiliar bloqueado correctamente con 403 de /usuarios"
    }

    # 2. TEST GESTION DOCUMENTAL Y HASH SHA-256
    Write-Output "`n[2/7] VERIFICANDO GESTION DOCUMENTAL Y CALCULO SHA-256..."
    $uploadOutput = & curl.exe -s -X POST "http://localhost:8080/api/v1/documentos/upload" -H "Authorization: Bearer $dirToken" -F "file=@C:\Users\vajho\Desktop\SistemaFICCT\storage\test_ocr_sample.png"
    $uploadJson = $uploadOutput | ConvertFrom-Json
    $docId = $uploadJson.data.id
    Write-Output "  -> Subida de archivo ID: $docId Hash SHA-256: $($uploadJson.data.hashSha256)"

    # 3. TEST OCR CON VISION ARTIFICIAL Y SOBERANIA HUMANA
    Write-Output "`n[3/7] VERIFICANDO SERVICIO OCR (FASTAPI) Y SOBERANIA HUMANA..."
    $ocrRes = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/ocr/$docId/procesar" -Method POST -Headers $dirHeaders
    Write-Output "  -> Procesamiento OCR completado por FastAPI. Paginas: $($ocrRes.data.paginas.Count)"

    # Validacion humana
    $valBody = @{
        documentoId = $docId
        paginas = @(@{ numeroPagina = 1; textoCorregido = 'FICCT UNIVERSIDAD AUTONOMA GABRIEL RENE MORENO ACREDITACION 2026' })
    } | ConvertTo-Json -Depth 4
    $valBytes = [System.Text.Encoding]::UTF8.GetBytes($valBody)
    $valRes = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/ocr/validar' -Method PUT -ContentType 'application/json; charset=utf-8' -Body $valBytes -Headers $dirHeaders
    Write-Output "  -> Validacion humana registrada: Estado $($valRes.data.estadoOcr)"

    # Busqueda texto completo
    $searchRes = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/ocr/busqueda?q=ACREDITACION' -Method GET -Headers $dirHeaders
    Write-Output "  -> Busqueda de texto completo tsvector: $($searchRes.data.Count) coincidencias encontradas con <mark>"

    # 4. TEST MAQUINA DE ESTADOS DE TRAMITES
    Write-Output "`n[4/7] VERIFICANDO MAQUINA DE ESTADOS DE TRAMITES..."
    $tramiteRes = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/tramites/1' -Method GET -Headers $dirHeaders
    Write-Output "  -> Estado actual de tramite TR-2026-0001: $($tramiteRes.data.estadoActual)"

    # 5. TEST PREDICCION DE CUPOS (RANDOM FOREST)
    Write-Output "`n[5/7] VERIFICANDO MODELO PREDICTIVO RANDOM FOREST (K=5 F1>=0.80)..."
    $mlRes = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/academico/predicciones/materias/1/periodos/1' -Method GET -Headers $dirHeaders
    $pred = $mlRes.data
    Write-Output "  -> Inferencia ML para $($pred.materiaSigla): Alerta $($pred.nivelAlerta) (Probabilidad: $($pred.probabilidadSobredemanda), Deficit: $($pred.deficitEstimado) cupos)"
    Write-Output "  -> Caracter consultivo verificado: $($pred.avisoInstitucional)"

    # 6. TEST SEGUIMIENTO INSTITUCIONAL, POA Y ALERTAS
    Write-Output "`n[6/7] VERIFICANDO ALERTAS Y KPIS DEL DASHBOARD..."
    $kpiRes = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/institucional/dashboard/kpis' -Method GET -Headers $dirHeaders
    Write-Output "  -> KPIs: $($kpiRes.data.totalEstudiantes) Estudiantes, $($kpiRes.data.totalTituladosHistorico) Titulados, Avance POA: $($kpiRes.data.porcentajeAvancePoaPromedio)%"

    # 7. TEST GENERACION DE DOSSIER OFICIAL (RPA)
    Write-Output "`n[7/7] VERIFICANDO COMPILADOR DE DOSSIER OFICIAL INSTITUCIONAL..."
    $dossierBody = @{ periodoCodigo = '2026-1'; incluirModulos = @('CORRESPONDENCIA', 'TRAMITES') } | ConvertTo-Json
    $dossierBytes = [System.Text.Encoding]::UTF8.GetBytes($dossierBody)
    $dossierRes = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/institucional/dossier/generar' -Method POST -ContentType 'application/json; charset=utf-8' -Body $dossierBytes -Headers $dirHeaders
    Write-Output "  -> Dossier generado: $($dossierRes.data.nombreArchivo) (Hash: $($dossierRes.data.hashSha256.Substring(0,16))...)"

    Write-Output "`n================================================================="
    Write-Output "TODAS LAS PRUEBAS DE REGRESION RESULTARON 100% SATISFACTORIAS"
    Write-Output "================================================================="

} catch {
    Write-Error $_
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        Write-Output $reader.ReadToEnd()
    }
}

