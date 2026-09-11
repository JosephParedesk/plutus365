# Mata el proceso que esté escuchando en cada puerto de los microservicios de Plutus365.
# Uso: .\liberar-puertos.ps1

$puertos = 8080..8092  # auth..gateway..contabilidad..nomina, ver tabla de puertos en CLAUDE.md

foreach ($puerto in $puertos) {
    $conexion = Get-NetTCPConnection -LocalPort $puerto -State Listen -ErrorAction SilentlyContinue
    if (-not $conexion) {
        Write-Host "Puerto $puerto - libre" -ForegroundColor DarkGray
        continue
    }
    foreach ($pid_ in ($conexion.OwningProcess | Select-Object -Unique)) {
        $proceso = Get-Process -Id $pid_ -ErrorAction SilentlyContinue
        $nombre = if ($proceso) { $proceso.ProcessName } else { "?" }
        Stop-Process -Id $pid_ -Force -ErrorAction SilentlyContinue
        Write-Host "Puerto $puerto - matado PID $pid_ ($nombre)" -ForegroundColor Yellow
    }
}

Write-Host "`nListo." -ForegroundColor Green
