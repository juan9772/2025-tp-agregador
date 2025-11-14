# Script de Pruebas End-to-End para Consistencia Eventual
# Prueba el flujo completo: Fuente → Procesador → Agregador → Solicitudes

param(
    [string]$FuenteUrl = "http://localhost:8081",
    [string]$ProcesadorUrl = "http://localhost:8083",
    [string]$AgregadorUrl = "http://localhost:8080",
    [string]$SolicitudesUrl = "http://localhost:8082"
)

$ErrorActionPreference = "Continue"
$testsPassed = 0
$testsFailed = 0

Write-Host "╔══════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  🧪 PRUEBAS E2E - CONSISTENCIA EVENTUAL                     ║" -ForegroundColor Cyan
Write-Host "╚══════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""
Write-Host "📍 URLs configuradas:" -ForegroundColor Yellow
Write-Host "   Fuente:      $FuenteUrl" -ForegroundColor White
Write-Host "   Procesador:  $ProcesadorUrl" -ForegroundColor White
Write-Host "   Agregador:   $AgregadorUrl" -ForegroundColor White
Write-Host "   Solicitudes: $SolicitudesUrl" -ForegroundColor White
Write-Host ""

# Función auxiliar para esperar
function Wait-Seconds {
    param([int]$Seconds, [string]$Reason)
    Write-Host "⏳ Esperando $Seconds segundos ($Reason)..." -ForegroundColor Yellow
    Start-Sleep -Seconds $Seconds
}

# Función para mostrar resultado de test
function Show-TestResult {
    param([bool]$Success, [string]$TestName)
    if ($Success) {
        Write-Host "✅ PASS: $TestName" -ForegroundColor Green
        $script:testsPassed++
    } else {
        Write-Host "❌ FAIL: $TestName" -ForegroundColor Red
        $script:testsFailed++
    }
}

Write-Host "══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "TEST 1: CREAR HECHO Y VERIFICAR INDEXACIÓN" -ForegroundColor Cyan
Write-Host "══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

# Generar ID único para el test
$timestamp = Get-Date -Format "yyyyMMddHHmmss"
$hechoId = "e2e-test-hecho-$timestamp"

Write-Host "1.1) Creando hecho en módulo FUENTE..." -ForegroundColor Yellow
try {
    $hechoBody = @{
        id = $hechoId
        descripcion = "Hecho de prueba E2E para consistencia eventual"
        nombreColeccion = "TestCollection"
    } | ConvertTo-Json

    $hechoResponse = Invoke-RestMethod -Uri "$FuenteUrl/api/hechos" `
        -Method Post `
        -ContentType "application/json" `
        -Body $hechoBody

    Write-Host "   Hecho creado: $($hechoResponse.id)" -ForegroundColor Green
    Show-TestResult -Success $true -TestName "Crear hecho en Fuente"
} catch {
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
    Show-TestResult -Success $false -TestName "Crear hecho en Fuente"
}

Wait-Seconds -Seconds 5 -Reason "Propagación del evento HECHO_CREADO al agregador"

Write-Host "1.2) Verificando indexación en módulo AGREGADOR..." -ForegroundColor Yellow
try {
    $busquedaResponse = Invoke-RestMethod -Uri "$AgregadorUrl/api/busqueda?query=$hechoId&page=0&size=10" `
        -Method Get

    if ($busquedaResponse.content.Count -gt 0 -and $busquedaResponse.content[0].id -eq $hechoId) {
        Write-Host "   ✓ Hecho encontrado en índice de búsqueda" -ForegroundColor Green
        Write-Host "   ✓ Display: $($busquedaResponse.content[0].displayNombre)" -ForegroundColor Green
        Show-TestResult -Success $true -TestName "Hecho indexado en MongoDB (evento HECHO_CREADO)"
    } else {
        Write-Host "   ✗ Hecho NO encontrado en índice" -ForegroundColor Red
        Show-TestResult -Success $false -TestName "Hecho indexado en MongoDB"
    }
} catch {
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
    Show-TestResult -Success $false -TestName "Verificar indexación"
}

Write-Host ""
Write-Host "══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "TEST 2: CREAR PDI, PROCESAR Y VERIFICAR TAGS EN BÚSQUEDA" -ForegroundColor Cyan
Write-Host "══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

Write-Host "2.1) Creando PDI para el hecho..." -ForegroundColor Yellow
try {
    $pdiBody = @{
        hechoId = $hechoId
        descripcion = "PDI de prueba E2E con imagen para etiquetado"
        lugar = "Buenos Aires, Argentina"
        momento = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss")
        contenido = "Contenido de prueba para OCR y etiquetado automático"
        imagenUrl = "https://picsum.photos/seed/e2e-test/400/300"
    } | ConvertTo-Json

    $pdiResponse = Invoke-RestMethod -Uri "$ProcesadorUrl/api/pdis" `
        -Method Post `
        -ContentType "application/json" `
        -Body $pdiBody

    Write-Host "   PDI creado: ID=$($pdiResponse.id)" -ForegroundColor Green
    Write-Host "   Estado inicial: $($pdiResponse.ocrTexto)" -ForegroundColor Gray
    $pdiId = $pdiResponse.id
    Show-TestResult -Success $true -TestName "Crear PDI en Procesador"
} catch {
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
    Show-TestResult -Success $false -TestName "Crear PDI"
    $pdiId = $null
}

if ($pdiId) {
    Wait-Seconds -Seconds 15 -Reason "Procesamiento asíncrono (OCR + etiquetado IA)"

    Write-Host "2.2) Verificando procesamiento del PDI..." -ForegroundColor Yellow
    try {
        $pdiProcesadoResponse = Invoke-RestMethod -Uri "$ProcesadorUrl/api/pdis/$pdiId" `
            -Method Get

        $tieneOcr = $pdiProcesadoResponse.ocrTexto -and $pdiProcesadoResponse.ocrTexto -ne "OCR Pendiente de procesamiento"
        $tieneTags = $pdiProcesadoResponse.etiquetas_auto -and $pdiProcesadoResponse.etiquetas_auto.Count -gt 0

        if ($tieneOcr) {
            Write-Host "   ✓ OCR procesado" -ForegroundColor Green
            Write-Host "     Texto: $($pdiProcesadoResponse.ocrTexto.Substring(0, [Math]::Min(50, $pdiProcesadoResponse.ocrTexto.Length)))..." -ForegroundColor Gray
        } else {
            Write-Host "   ✗ OCR no procesado" -ForegroundColor Red
        }

        if ($tieneTags) {
            Write-Host "   ✓ Etiquetas automáticas generadas: $($pdiProcesadoResponse.etiquetas_auto.Count)" -ForegroundColor Green
            Write-Host "     Tags: $($pdiProcesadoResponse.etiquetas_auto -join ', ')" -ForegroundColor Gray
        } else {
            Write-Host "   ✗ No se generaron etiquetas" -ForegroundColor Red
        }

        Show-TestResult -Success ($tieneOcr -and $tieneTags) -TestName "PDI procesado con OCR y tags"
    } catch {
        Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
        Show-TestResult -Success $false -TestName "Verificar procesamiento PDI"
    }

    Wait-Seconds -Seconds 5 -Reason "Propagación del evento PDI_PROCESADO al agregador"

    Write-Host "2.3) Verificando re-indexación con tags en AGREGADOR..." -ForegroundColor Yellow
    try {
        $busquedaConTagsResponse = Invoke-RestMethod -Uri "$AgregadorUrl/api/busqueda?query=$hechoId&page=0&size=10" `
            -Method Get

        if ($busquedaConTagsResponse.content.Count -gt 0) {
            $hechoIndexado = $busquedaConTagsResponse.content[0]
            $tieneTags = $hechoIndexado.tags -and $hechoIndexado.tags.Count -gt 0

            if ($tieneTags) {
                Write-Host "   ✓ Hecho re-indexado con tags automáticos" -ForegroundColor Green
                Write-Host "     Tags en índice: $($hechoIndexado.tags -join ', ')" -ForegroundColor Gray
                Show-TestResult -Success $true -TestName "Re-indexación con tags (evento PDI_PROCESADO)"
            } else {
                Write-Host "   ✗ Hecho indexado pero SIN tags" -ForegroundColor Red
                Show-TestResult -Success $false -TestName "Re-indexación con tags"
            }
        } else {
            Write-Host "   ✗ Hecho no encontrado en índice" -ForegroundColor Red
            Show-TestResult -Success $false -TestName "Verificar re-indexación"
        }
    } catch {
        Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
        Show-TestResult -Success $false -TestName "Verificar indexación con tags"
    }

    Write-Host "2.4) Probando búsqueda por tags..." -ForegroundColor Yellow
    try {
        $primerTag = $pdiProcesadoResponse.etiquetas_auto[0]
        $busquedaPorTagResponse = Invoke-RestMethod -Uri "$AgregadorUrl/api/busqueda?query=tag:$primerTag&page=0&size=10" `
            -Method Get

        $encontrado = $false
        foreach ($item in $busquedaPorTagResponse.content) {
            if ($item.id -eq $hechoId) {
                $encontrado = $true
                break
            }
        }

        if ($encontrado) {
            Write-Host "   ✓ Hecho encontrado usando búsqueda por tag '$primerTag'" -ForegroundColor Green
            Show-TestResult -Success $true -TestName "Búsqueda por tags funcional"
        } else {
            Write-Host "   ✗ Hecho NO encontrado con búsqueda por tag" -ForegroundColor Red
            Show-TestResult -Success $false -TestName "Búsqueda por tags"
        }
    } catch {
        Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
        Show-TestResult -Success $false -TestName "Búsqueda por tags"
    }
}

Write-Host ""
Write-Host "══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "TEST 3: SOLICITUD DE BORRADO Y VERIFICAR DESINDEXACIÓN" -ForegroundColor Cyan
Write-Host "══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

Write-Host "3.1) Creando solicitud de borrado..." -ForegroundColor Yellow
try {
    $solicitudBody = @{
        hechoId = $hechoId
        motivo = "Prueba E2E de consistencia eventual - borrado"
    } | ConvertTo-Json

    $solicitudResponse = Invoke-RestMethod -Uri "$SolicitudesUrl/api/solicitudes" `
        -Method Post `
        -ContentType "application/json" `
        -Body $solicitudBody

    Write-Host "   Solicitud creada: ID=$($solicitudResponse.id)" -ForegroundColor Green
    $solicitudId = $solicitudResponse.id
    Show-TestResult -Success $true -TestName "Crear solicitud de borrado"
} catch {
    Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
    Show-TestResult -Success $false -TestName "Crear solicitud"
    $solicitudId = $null
}

if ($solicitudId) {
    Write-Host "3.2) Aceptando solicitud de borrado..." -ForegroundColor Yellow
    try {
        $aceptarBody = @{
            estado = "ACEPTADA"
            descripcion = "Prueba E2E - solicitud aceptada"
        } | ConvertTo-Json

        $aceptarResponse = Invoke-RestMethod -Uri "$SolicitudesUrl/api/solicitudes/$solicitudId" `
            -Method Patch `
            -ContentType "application/json" `
            -Body $aceptarBody

        Write-Host "   Solicitud aceptada: Estado=$($aceptarResponse.estado)" -ForegroundColor Green
        Show-TestResult -Success $true -TestName "Aceptar solicitud de borrado"
    } catch {
        Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
        Show-TestResult -Success $false -TestName "Aceptar solicitud"
    }

    Wait-Seconds -Seconds 5 -Reason "Propagación del evento HECHO_BORRADO al agregador"

    Write-Host "3.3) Verificando que el hecho NO aparece en búsqueda..." -ForegroundColor Yellow
    try {
        $busquedaFinalResponse = Invoke-RestMethod -Uri "$AgregadorUrl/api/busqueda?query=$hechoId&page=0&size=10" `
            -Method Get

        $hechoVisible = $false
        foreach ($item in $busquedaFinalResponse.content) {
            if ($item.id -eq $hechoId -and -not $item.fueBorrado) {
                $hechoVisible = $true
                break
            }
        }

        if (-not $hechoVisible) {
            Write-Host "   ✓ Hecho correctamente marcado como borrado o no visible" -ForegroundColor Green
            Show-TestResult -Success $true -TestName "Desindexación (evento HECHO_BORRADO)"
        } else {
            Write-Host "   ✗ Hecho todavía visible en búsqueda" -ForegroundColor Red
            Show-TestResult -Success $false -TestName "Desindexación"
        }
    } catch {
        Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Red
        Show-TestResult -Success $false -TestName "Verificar desindexación"
    }
}

Write-Host ""
Write-Host "══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "📊 RESUMEN DE PRUEBAS" -ForegroundColor Cyan
Write-Host "══════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
Write-Host "Tests ejecutados: $($testsPassed + $testsFailed)" -ForegroundColor White
Write-Host "✅ Pasados: $testsPassed" -ForegroundColor Green
Write-Host "❌ Fallidos: $testsFailed" -ForegroundColor Red
Write-Host ""

if ($testsFailed -eq 0) {
    Write-Host "🎉 TODOS LOS TESTS PASARON - Sistema funcionando correctamente" -ForegroundColor Green
    Write-Host ""
    Write-Host "✅ Consistencia eventual verificada:" -ForegroundColor Green
    Write-Host "   • Eventos HECHO_CREADO → Indexación automática" -ForegroundColor White
    Write-Host "   • Eventos PDI_PROCESADO → Re-indexación con tags" -ForegroundColor White
    Write-Host "   • Eventos HECHO_BORRADO → Desindexación" -ForegroundColor White
    Write-Host "   • Búsqueda por texto y tags funcional" -ForegroundColor White
    exit 0
} else {
    Write-Host "⚠️  ALGUNOS TESTS FALLARON - Revisar logs de los módulos" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Checklist de troubleshooting:" -ForegroundColor Yellow
    Write-Host "  [ ] Verificar que RabbitMQ está corriendo" -ForegroundColor White
    Write-Host "  [ ] Verificar que todos los módulos están levantados" -ForegroundColor White
    Write-Host "  [ ] Revisar logs de cada módulo por errores" -ForegroundColor White
    Write-Host "  [ ] Verificar conectividad a MongoDB Atlas" -ForegroundColor White
    Write-Host "  [ ] Verificar API key de ApiLayer" -ForegroundColor White
    exit 1
}
