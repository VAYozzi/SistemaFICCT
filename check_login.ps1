$loginBody = @{ correoInstitucional = 'nuevo.coordinador@uagrm.edu.bo'; password = 'Coord2026!' } | ConvertTo-Json
$loginRes = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/auth/login' -Method POST -ContentType 'application/json' -Body $loginBody
Write-Output "LoginResponse: $($loginRes | ConvertTo-Json -Compress)"
$token = $loginRes.data.token
Write-Output "Extracted token: $token"

if ($token) {
    $perfil = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/auth/perfil' -Method GET -Headers @{ Authorization = "Bearer $token" }
    Write-Output "Perfil: $($perfil | ConvertTo-Json -Compress)"
}

