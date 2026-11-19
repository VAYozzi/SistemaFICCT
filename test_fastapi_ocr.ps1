$health = Invoke-RestMethod -Uri 'http://127.0.0.1:8000/health' -Method GET
Write-Output "Health: $($health | ConvertTo-Json -Compress)"

$body = @{
    ruta_relativa = 'test_ocr_sample.png'
    idioma = 'spa'
} | ConvertTo-Json

$headers = @{
    'X-Internal-Api-Key' = 'ficct_super_secret_internal_key_2026'
    'Content-Type' = 'application/json'
}

$ocrRes = Invoke-RestMethod -Uri 'http://127.0.0.1:8000/internal/v1/ocr/extract' -Method POST -Headers $headers -Body $body
Write-Output "OCR Response: $($ocrRes | ConvertTo-Json -Compress)"

