try {
    # 1. Login Director
    $loginBody = @{ correoInstitucional = 'nuevo.coordinador@uagrm.edu.bo'; password = 'Coord2026!' } | ConvertTo-Json
    $loginRes = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/auth/login' -Method POST -ContentType 'application/json' -Body $loginBody
    $token = $loginRes.data.token
    Write-Output "[PASS] Login exitoso como Director. Token obtenido."

    # 2. Crear archivo dummy para subir
    $testFilePath = 'C:\Users\vajho\Desktop\SistemaFICCT\storage\test_doc.pdf'
    [System.IO.File]::WriteAllBytes($testFilePath, [System.Text.Encoding]::UTF8.GetBytes('%PDF-1.4 test document content for FICCT'))

    # Subir documento usando curl.exe con part 'file'
    $headers = @{ Authorization = "Bearer $token" }
    $uploadOutput = & curl.exe -s -X POST "http://localhost:8080/api/v1/documentos/upload" `
        -H "Authorization: Bearer $token" `
        -F "file=@$testFilePath"

    Write-Output "Upload response: $uploadOutput"
    $uploadJson = $uploadOutput | ConvertFrom-Json
    $uploadRes = $uploadJson.data
    Write-Output "[PASS] Documento subido ID: $($uploadRes.id) Hash: $($uploadRes.hashSha256) Ruta: $($uploadRes.rutaAlmacenamiento)"

    # 3. Registrar correspondencia con este documento
    $corrPayload = @{
        codigoCite = 'CITE-FICCT-2026-001'
        tipo = 'ENTRANTE'
        remitenteInstitucion = 'Decanato FICCT'
        remitentePersona = 'Ing. Decano de Facultad'
        destinatario = 'Dirección de Sistemas'
        referencia = 'Aprobación de calendario académico gestión 2026'
        observaciones = 'Adjunto documento oficial'
        documentoAdjuntoId = $uploadRes.id
    } | ConvertTo-Json -Compress

    $corrBytes = [System.Text.Encoding]::UTF8.GetBytes($corrPayload)
    $corrResWrapper = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/correspondencia' -Method POST -ContentType 'application/json; charset=utf-8' -Body $corrBytes -Headers $headers
    $corrRes = $corrResWrapper.data
    Write-Output "[PASS] Correspondencia registrada ID: $($corrRes.id) CITE: $($corrRes.codigoCite) Estado: $($corrRes.estado)"

    # 4. Listar correspondencia
    $listCorr = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/correspondencia' -Method GET -Headers $headers
    Write-Output "[PASS] Correspondencias listadas total: $($listCorr.data.totalElements)"

} catch {
    Write-Error $_
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        Write-Output $reader.ReadToEnd()
    }
}
