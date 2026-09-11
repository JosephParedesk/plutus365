# Apaga lo que esté corriendo en los puertos de Plutus365 y levanta los 13
# microservicios desde cero, cada uno en su propia ventana de PowerShell.
# Uso: .\levantar-todo.ps1

# UsaEnvFile: true en el servicio cuyo .env de carpeta hay que inyectar como
# variables de entorno antes de bootRun (contraseñas reales de DB/mail/JWT que
# ya no están hardcodeadas en application.properties — ver MIGRACION-MONOLITO-MODULAR.md).
# inventario y subscription-service TAMBIÉN tienen un .env en su carpeta, pero
# apunta a un puerto y una base de datos que no son los reales — cargarlo
# rompería cosas, se deja fuera hasta que se audite ese .env aparte.
$servicios = @(
    @{ Nombre = "auth-service";          Carpeta = "auth";                  Puerto = 8080; UsaEnvFile = $true  }
    @{ Nombre = "subscription-service";  Carpeta = "subscription-service";  Puerto = 8081; UsaEnvFile = $false }
    @{ Nombre = "inventario-service";    Carpeta = "inventario";            Puerto = 8082; UsaEnvFile = $false }
    @{ Nombre = "categoria-service";     Carpeta = "categoria";             Puerto = 8083; UsaEnvFile = $false }
    @{ Nombre = "proveedor-service";     Carpeta = "proveedor";             Puerto = 8084; UsaEnvFile = $false }
    @{ Nombre = "compra-service";        Carpeta = "compra";                Puerto = 8085; UsaEnvFile = $false }
    @{ Nombre = "cliente-service";       Carpeta = "cliente-service";       Puerto = 8086; UsaEnvFile = $false }
    @{ Nombre = "venta-service";         Carpeta = "venta-service";         Puerto = 8087; UsaEnvFile = $true  }
    @{ Nombre = "empresa-service";       Carpeta = "empresa-service";       Puerto = 8088; UsaEnvFile = $false }
    @{ Nombre = "facturacion-service";   Carpeta = "facturacion-service";   Puerto = 8089; UsaEnvFile = $true  }
    @{ Nombre = "gateway";               Carpeta = "gateway";               Puerto = 8090; UsaEnvFile = $true  }
    @{ Nombre = "contabilidad-service";  Carpeta = "contabilidad-service";  Puerto = 8091; UsaEnvFile = $false }
    @{ Nombre = "nomina-service";        Carpeta = "nomina";                Puerto = 8092; UsaEnvFile = $false }
)

$raiz = "C:\plutus365\pos-backend"

# Lee un .env (líneas KEY=VALUE, ignora blancas y comentarios) y arma el bloque
# de comandos $env:KEY='VALOR' que se antepone al bootRun de ese servicio.
function Comandos-EnvFile($rutaEnv) {
    $lineas = Get-Content $rutaEnv | Where-Object { $_.Trim() -and -not $_.Trim().StartsWith('#') }
    $comandos = @()
    foreach ($linea in $lineas) {
        $partes = $linea -split '=', 2
        if ($partes.Count -eq 2) {
            $clave = $partes[0].Trim()
            $valor = $partes[1].Trim()
            $comandos += "`$env:$clave = '$valor'"
        }
    }
    return $comandos -join '; '
}

# ── 1. Apagar todo lo que ya esté corriendo en esos puertos ────────────────
Write-Host "Apagando servicios que ya estén corriendo..." -ForegroundColor Cyan
foreach ($s in $servicios) {
    $conexion = Get-NetTCPConnection -LocalPort $s.Puerto -State Listen -ErrorAction SilentlyContinue
    if ($conexion) {
        foreach ($pid_ in ($conexion.OwningProcess | Select-Object -Unique)) {
            Stop-Process -Id $pid_ -Force -ErrorAction SilentlyContinue
            Write-Host "  Puerto $($s.Puerto) ($($s.Nombre)) - matado PID $pid_" -ForegroundColor Yellow
        }
    }
}

# Deja que el sistema operativo libere los sockets antes de volver a bindear.
Start-Sleep -Seconds 3

# ── 2. Levantar cada servicio en su propia ventana ──────────────────────────
Write-Host "`nLevantando los 13 microservicios..." -ForegroundColor Cyan
foreach ($s in $servicios) {
    $carpeta = Join-Path $raiz $s.Carpeta
    if (-not (Test-Path $carpeta)) {
        Write-Host "  $($s.Nombre): carpeta no encontrada ($carpeta), se salta" -ForegroundColor Red
        continue
    }
    $prefijoEnv = ""
    if ($s.UsaEnvFile) {
        $rutaEnv = Join-Path $carpeta ".env"
        if (Test-Path $rutaEnv) {
            $prefijoEnv = (Comandos-EnvFile $rutaEnv) + "; "
        }
    }

    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$carpeta'; $prefijoEnv.\gradlew.bat bootRun" `
        -WindowStyle Normal
    Write-Host "  $($s.Nombre) (puerto $($s.Puerto)) - arrancando..." -ForegroundColor Green
}

Write-Host "`nListo. Cada servicio abrio su propia ventana, mira ahi sus logs." -ForegroundColor Green
Write-Host "El primero en estar listo suele tardar ~10-20s por servicio (arranque de Spring Boot + Gradle)." -ForegroundColor DarkGray
