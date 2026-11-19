try {
    # 1. Login Director
    $loginBody = @{ correoInstitucional = 'nuevo.coordinador@uagrm.edu.bo'; password = 'Coord2026!' } | ConvertTo-Json
    $loginRes = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/auth/login' -Method POST -ContentType 'application/json' -Body $loginBody
    $token = $loginRes.data.token
    $headers = @{ Authorization = "Bearer $token" }
    Write-Output "[PASS] Login Director exitoso"

    # 2. Crear Plan de Estudios 187-3
    $planBody = @{
        codigoPlan = '187-3'
        nombre = 'Plan Oficial de Ingenieria en Sistemas'
        anioVigencia = 2026
    } | ConvertTo-Json
    $planBytes = [System.Text.Encoding]::UTF8.GetBytes($planBody)
    $planRes = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/academico/planes' -Method POST -ContentType 'application/json; charset=utf-8' -Body $planBytes -Headers $headers
    $planId = $planRes.data.id
    Write-Output "[PASS] Plan de estudios creado ID: $planId Codigo: $($planRes.data.codigoPlan)"

    # 3. Agregar Materias (INF110 - Introducción a la Informática, Semestre 1)
    $materiaBody1 = @{
        sigla = 'INF110'
        nombre = 'Introduccion a la Informatica'
        semestreNivel = 1
        creditos = 4
        prerrequisitosIds = @()
    } | ConvertTo-Json
    $materiaBytes1 = [System.Text.Encoding]::UTF8.GetBytes($materiaBody1)
    $materiaRes1 = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/academico/planes/$planId/materias" -Method POST -ContentType 'application/json; charset=utf-8' -Body $materiaBytes1 -Headers $headers
    $materiaId1 = $materiaRes1.data.id
    Write-Output "[PASS] Materia 1 creada ID: $materiaId1 Sigla: $($materiaRes1.data.sigla)"

    # Materia 2 (INF120 - Programación I, Semestre 2 con prerrequisito INF110)
    $materiaBody2 = @{
        sigla = 'INF120'
        nombre = 'Programacion I'
        semestreNivel = 2
        creditos = 4
        prerrequisitosIds = @($materiaId1)
    } | ConvertTo-Json
    $materiaBytes2 = [System.Text.Encoding]::UTF8.GetBytes($materiaBody2)
    $materiaRes2 = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/academico/planes/$planId/materias" -Method POST -ContentType 'application/json; charset=utf-8' -Body $materiaBytes2 -Headers $headers
    $materiaId2 = $materiaRes2.data.id
    Write-Output "[PASS] Materia 2 creada ID: $materiaId2 Sigla: $($materiaRes2.data.sigla) con prerrequisito INF110"

    # 4. Aperturar Periodo Académico 2026-1
    $periodoBody = @{
        codigo = '2026-1'
        fechaInicio = '2026-03-01'
        fechaFin = '2026-07-31'
    } | ConvertTo-Json
    $periodoBytes = [System.Text.Encoding]::UTF8.GetBytes($periodoBody)
    $periodoRes = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/academico/periodos' -Method POST -ContentType 'application/json; charset=utf-8' -Body $periodoBytes -Headers $headers
    $periodoId = $periodoRes.data.id
    Write-Output "[PASS] Periodo semestral creado ID: $periodoId Codigo: $($periodoRes.data.codigo)"

    # 5. Planificar Oferta de Grupos y Cupos (Grupo SA con 80 cupos)
    $ofertaBody = @{
        periodoId = $periodoId
        materiaId = $materiaId1
        codigoGrupo = 'SA'
        cupoOfertado = 80
        inscritosFinales = 80
        demandaInsatisfechaEstimada = 35
        aprobados = 45
        reprobados = 25
        abandonos = 10
    } | ConvertTo-Json
    $ofertaBytes = [System.Text.Encoding]::UTF8.GetBytes($ofertaBody)
    $ofertaRes = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/academico/oferta-grupos' -Method POST -ContentType 'application/json; charset=utf-8' -Body $ofertaBytes -Headers $headers
    $ofertaId = $ofertaRes.data.id
    Write-Output "[PASS] Oferta de grupo planificada ID: $ofertaId Materia: INF110 Grupo: SA Cupos: $($ofertaRes.data.cupoOfertado)"

    # 6. Inferencia Supervisada con Random Forest: Spring Boot -> FastAPI -> Inferencia ML
    $predRes = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/academico/predicciones/materias/$materiaId1/periodos/$periodoId" -Method GET -Headers $headers
    $pred = $predRes.data
    Write-Output "=========================================================="
    Write-Output "RESULTADO PREDICCION MACHINE LEARNING (RANDOM FOREST)"
    Write-Output "Materia: $($pred.materiaSigla)"
    Write-Output "Nivel de Alerta: $($pred.nivelAlerta)"
    Write-Output "Probabilidad de Sobredemanda: $($pred.probabilidadSobredemanda)"
    Write-Output "Cupos Ofertados: $($pred.cuposOfertados)"
    Write-Output "Demanda Proyectada: $($pred.demandaProyectada)"
    Write-Output "Deficit Estimado: $($pred.deficitEstimado) estudiantes"
    Write-Output "Identificacion de Datos: Es datos de prueba = $($pred.esDatosPrueba)"
    Write-Output "Aviso Institucional: $($pred.avisoInstitucional)"
    Write-Output "=========================================================="

} catch {
    Write-Error $_
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        Write-Output $reader.ReadToEnd()
    }
}

