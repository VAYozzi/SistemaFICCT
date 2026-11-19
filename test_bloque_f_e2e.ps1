try {
    # 1. Login Director
    $loginBody = @{ correoInstitucional = 'nuevo.coordinador@uagrm.edu.bo'; password = 'Coord2026!' } | ConvertTo-Json
    $loginRes = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/auth/login' -Method POST -ContentType 'application/json' -Body $loginBody
    $token = $loginRes.data.token
    $headers = @{ Authorization = "Bearer $token" }
    Write-Output "[PASS] Login Director OK"

    # 2. Registrar Titulado (Estudiante ID 1 - Modalidad TESIS con acta adjunta ID 1)
    $tituladoBody = @{
        expedienteId = 1
        modalidadGraduacion = 'TESIS'
        fechaDefensa = '2026-11-15'
        notaFinal = 95.50
        numeroTituloProvision = 'TP-FICCT-2026-089'
        documentoActaId = 1
    } | ConvertTo-Json
    $titBytes = [System.Text.Encoding]::UTF8.GetBytes($tituladoBody)
    $titRes = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/institucional/titulados' -Method POST -ContentType 'application/json; charset=utf-8' -Body $titBytes -Headers $headers
    Write-Output "[PASS] Titulado registrado ID: $($titRes.data.id) Modalidad: $($titRes.data.modalidadGraduacion) Nota: $($titRes.data.notaFinal)"

    # 3. Listar Titulados
    $listTit = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/institucional/titulados' -Method GET -Headers $headers
    Write-Output "[PASS] Total Titulados listados: $($listTit.data.Count)"

    # 4. Registrar Alerta Predictiva de Demanda de Cupos (Semáforo ROJO)
    $alertaBody = @{
        categoria = 'PREDICTIVA_CUPOS'
        nivelCriticidad = 'ROJO'
        titulo = 'Alerta Predictiva de Sobredemanda Crítica: INF110'
        mensajeDetalle = 'Probabilidad de saturación de cupos calculada en 98.7% mediante Random Forest. Déficit proyectado: 133 estudiantes.'
        entidadOrigen = 'materias'
        entidadOrigenId = 1
        estado = 'PENDIENTE'
    } | ConvertTo-Json
    $alertaBytes = [System.Text.Encoding]::UTF8.GetBytes($alertaBody)
    $alertaRes = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/institucional/alertas' -Method POST -ContentType 'application/json; charset=utf-8' -Body $alertaBytes -Headers $headers
    $alertaId = $alertaRes.data.id
    Write-Output "[PASS] Alerta creada ID: $alertaId Criticidad: $($alertaRes.data.nivelCriticidad) Titulo: $($alertaRes.data.titulo)"

    # 5. Atender Alerta con Soberanía Humana (Registrar Resolución de Autoridad - RF-26 / CU-30)
    $atenderBody = @{
        accionTomada = 'Resolución Directiva N° 14/2026: Se autoriza la habilitación de 2 grupos paralelos adicionales (SB y SC) con 45 cupos cada uno.'
    } | ConvertTo-Json
    $atenderBytes = [System.Text.Encoding]::UTF8.GetBytes($atenderBody)
    $atenderRes = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/institucional/alertas/$alertaId/atender" -Method POST -ContentType 'application/json; charset=utf-8' -Body $atenderBytes -Headers $headers
    Write-Output "[PASS] Alerta atendida exitosamente. Estado actual: $($atenderRes.data.estado)"

    # 6. Consultar KPIs Consolidados del Dashboard Directivo (CU-26)
    $kpisRes = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/institucional/dashboard/kpis' -Method GET -Headers $headers
    $kpis = $kpisRes.data
    Write-Output "=========================================================="
    Write-Output "KPIS CONSOLIDADOS DEL DASHBOARD DIRECTIVO (FICCT - UAGRM)"
    Write-Output "Total Estudiantes Registrados: $($kpis.totalEstudiantes)"
    Write-Output "Total Tramites en Curso: $($kpis.totalTramitesEnCurso)"
    Write-Output "Total Titulados Historico: $($kpis.totalTituladosHistorico)"
    Write-Output "Alertas Pendientes: $($kpis.totalAlertasPendientes) (Rojas: $($kpis.alertasRojasPendientes))"
    Write-Output "Presupuesto POA Total: BOB $($kpis.presupuestoPoaTotal) (Avance: $($kpis.porcentajeAvancePoaPromedio)%)"
    Write-Output "=========================================================="

    # 7. Generar Dossier Institucional Oficial en PDF con SHA-256 (CU-34 / RPA)
    $dossierReq = @{
        periodoCodigo = '2026-1'
        fechaDesde = '2026-01-01'
        fechaHasta = '2026-06-30'
        incluirModulos = @('CORRESPONDENCIA', 'TRAMITES', 'ALERTAS', 'POA')
    } | ConvertTo-Json
    $dossierBytes = [System.Text.Encoding]::UTF8.GetBytes($dossierReq)
    $dossierRes = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/institucional/dossier/generar' -Method POST -ContentType 'application/json; charset=utf-8' -Body $dossierBytes -Headers $headers
    $dossier = $dossierRes.data
    Write-Output "[PASS] Dossier generado: $($dossier.nombreArchivo)"
    Write-Output "  -> Almacenamiento: $($dossier.rutaAlmacenamiento)"
    Write-Output "  -> Firma Criptografica SHA-256: $($dossier.hashSha256)"
    Write-Output "  -> Mensaje: $($dossier.mensaje)"

} catch {
    Write-Error $_
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        Write-Output $reader.ReadToEnd()
    }
}

