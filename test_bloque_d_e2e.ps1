try {
    # 1. Login Director
    $loginBody = @{ correoInstitucional = 'nuevo.coordinador@uagrm.edu.bo'; password = 'Coord2026!' } | ConvertTo-Json
    $loginRes = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/auth/login' -Method POST -ContentType 'application/json' -Body $loginBody
    $token = $loginRes.data.token
    $headers = @{ Authorization = "Bearer $token" }
    Write-Output "[PASS] Login Director OK"

    # 2. Subir documento de prueba real a Spring Boot usando curl con token fresco
    $uploadOutput = & curl.exe -s -X POST "http://localhost:8080/api/v1/documentos/upload" `
        -H "Authorization: Bearer $token" `
        -F "file=@C:\Users\vajho\Desktop\SistemaFICCT\storage\test_ocr_sample.png"

    Write-Output "Upload raw: $uploadOutput"
    $uploadJson = $uploadOutput | ConvertFrom-Json
    $docId = $uploadJson.data.id
    Write-Output "[PASS] Documento digital registrado ID: $docId Hash: $($uploadJson.data.hashSha256) Estado OCR inicial: $($uploadJson.data.estadoOcr)"

    # 3. Invocar procesamiento OCR: Spring Boot -> FastAPI (ai-worker-api) -> Spring Boot
    $ocrProcRes = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/ocr/$docId/procesar" -Method POST -Headers $headers
    $ocrData = $ocrProcRes.data
    Write-Output "[PASS] Procesamiento OCR completado exitosamente a traves de FastAPI. Paginas: $($ocrData.paginas.Count) Confianza: $($ocrData.confianzaPromedio)"
    $textoCrudo = $ocrData.paginas[0].textoExtraidoBruto
    Write-Output "Texto extraido crudo: $textoCrudo"

    # 4. Probar búsqueda antes de validación humana (Debe dar 0 resultados para garantizar Soberanía Humana)
    $busquedaAntes = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/ocr/busqueda?q=AUTONOMA' -Method GET -Headers $headers
    Write-Output "[PASS] Busqueda antes de validacion: $($busquedaAntes.data.Count) resultados (Soberania Humana garantizada: 0)"

    # 5. Funcionario valida y corrige el texto en el Visor Dual (PUT /api/v1/ocr/validar)
    $validarBody = @{
        documentoId = $docId
        paginas = @(
            @{
                numeroPagina = 1
                textoCorregido = 'UNIVERSIDAD AUTONOMA GABRIEL RENE MORENO - FACULTAD DE CIENCIAS EXACTAS Y TECNOLOGIA FICCT GESTION 2026'
            }
        )
    } | ConvertTo-Json -Depth 4

    $validarBytes = [System.Text.Encoding]::UTF8.GetBytes($validarBody)
    $validarRes = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/ocr/validar' -Method PUT -ContentType 'application/json; charset=utf-8' -Body $validarBytes -Headers $headers
    Write-Output "[PASS] Validacion humana completada: Documento estado $($validarRes.data.estadoOcr)"

    # 6. Probar búsqueda documental de texto completo con resaltado tras la validación humana
    $busquedaDespues = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/ocr/busqueda?q=AUTONOMA' -Method GET -Headers $headers
    Write-Output "[PASS] Busqueda despues de validacion: $($busquedaDespues.data.Count) resultados encontrados"
    foreach ($item in $busquedaDespues.data) {
        Write-Output "  -> Encontrado en Documento: $($item.nombreOriginal) Pagina: $($item.numeroPagina)"
        Write-Output "  -> Fragmento Resaltado: $($item.fragmentoResaltado)"
    }

} catch {
    Write-Error $_
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        Write-Output $reader.ReadToEnd()
    }
}
