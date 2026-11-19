try {
    # 1. Login Director
    $loginBody = @{ correoInstitucional = 'nuevo.coordinador@uagrm.edu.bo'; password = 'Coord2026!' } | ConvertTo-Json
    $loginRes = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/auth/login' -Method POST -ContentType 'application/json' -Body $loginBody
    $token = $loginRes.data.token
    $headers = @{ Authorization = "Bearer $token" }
    Write-Output "[PASS] Login Director OK"

    # 2. Consultar Trámite creado
    $tramiteRes = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/tramites/1' -Method GET -Headers $headers
    $tramite = $tramiteRes.data
    Write-Output "[PASS] Tramite obtenido ID: $($tramite.id) Codigo: $($tramite.codigoTramite) Estado Inicial: $($tramite.estadoActual)"

    # 3. Transición 1: INGRESADO -> EN_VERIFICACION (Válido)
    $t1Body = @{ estadoNuevo = 'EN_VERIFICACION'; observacionODictamen = 'Iniciando verificacion de materias' } | ConvertTo-Json
    $t1Bytes = [System.Text.Encoding]::UTF8.GetBytes($t1Body)
    $t1Res = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/tramites/1/transicion' -Method POST -ContentType 'application/json; charset=utf-8' -Body $t1Bytes -Headers $headers
    Write-Output "[PASS] Transicion 1 exitosa: $($t1Res.data.estadoActual)"

    # 4. Transición Inválida: EN_VERIFICACION -> CONCLUIDO (Debe ser rechazada con 400)
    try {
        $invBody = @{ estadoNuevo = 'CONCLUIDO'; observacionODictamen = 'Intento invalido de concluir' } | ConvertTo-Json
        $invBytes = [System.Text.Encoding]::UTF8.GetBytes($invBody)
        $invRes = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/tramites/1/transicion' -Method POST -ContentType 'application/json; charset=utf-8' -Body $invBytes -Headers $headers
        Write-Error "[FAIL] Se permitio una transicion invalida que debio ser rechazada"
    } catch {
        Write-Output "[PASS] Transicion invalida rechazada correctamente: $($_.Exception.Message)"
    }

    # 5. Transición 2: EN_VERIFICACION -> CON_DICTAMEN (Válido)
    $t2Body = @{ estadoNuevo = 'CON_DICTAMEN'; observacionODictamen = 'Dictamen tecnico favorable de convalidacion' } | ConvertTo-Json
    $t2Bytes = [System.Text.Encoding]::UTF8.GetBytes($t2Body)
    $t2Res = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/tramites/1/transicion' -Method POST -ContentType 'application/json; charset=utf-8' -Body $t2Bytes -Headers $headers
    Write-Output "[PASS] Transicion 2 exitosa: $($t2Res.data.estadoActual)"

    # 6. Transición 3: CON_DICTAMEN -> APROBADO (Válido para Director)
    $t3Body = @{ estadoNuevo = 'APROBADO'; observacionODictamen = 'Aprobado por Direccion de Carrera' } | ConvertTo-Json
    $t3Bytes = [System.Text.Encoding]::UTF8.GetBytes($t3Body)
    $t3Res = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/tramites/1/transicion' -Method POST -ContentType 'application/json; charset=utf-8' -Body $t3Bytes -Headers $headers
    Write-Output "[PASS] Transicion 3 exitosa: $($t3Res.data.estadoActual)"

    # 7. Transición 4: APROBADO -> CONCLUIDO (Válido)
    $t4Body = @{ estadoNuevo = 'CONCLUIDO'; observacionODictamen = 'Entrega de resolucion y finalizacion' } | ConvertTo-Json
    $t4Bytes = [System.Text.Encoding]::UTF8.GetBytes($t4Body)
    $t4Res = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/tramites/1/transicion' -Method POST -ContentType 'application/json; charset=utf-8' -Body $t4Bytes -Headers $headers
    Write-Output "[PASS] Transicion 4 exitosa: $($t4Res.data.estadoActual) Fecha conclusion: $($t4Res.data.fechaConclusion)"

} catch {
    Write-Error $_
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        Write-Output $reader.ReadToEnd()
    }
}

