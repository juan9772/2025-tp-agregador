# 🧪 Pruebas End-to-End (E2E) - Consistencia Eventual

## 📋 Objetivo

Validar que todos los módulos del sistema se comunican correctamente mediante eventos de RabbitMQ, garantizando **consistencia eventual** entre:
- **Fuente** (datos maestros de hechos)
- **ProcesadorPdI** (procesamiento OCR + etiquetado IA)
- **Agregador** (índice de búsqueda en MongoDB)
- **Solicitudes** (gestión de borrados)

## 🎯 Escenarios de Prueba

### Test 1: Crear Hecho → Indexación Automática

**Flujo:**
```
POST /api/hechos (Fuente)
    ↓
Evento: HECHO_CREADO
    ↓
IndexadorWorker (Agregador)
    ↓
Hecho indexado en MongoDB
```

**Validaciones:**
- ✅ Hecho creado correctamente en módulo Fuente
- ✅ Evento `HECHO_CREADO` emitido a RabbitMQ
- ✅ IndexadorWorker recibe evento y procesa
- ✅ Hecho aparece en búsqueda del agregador
- ✅ Tiempo de propagación < 5 segundos

---

### Test 2: Crear PDI → Procesamiento → Re-indexación con Tags

**Flujo:**
```
POST /api/pdis (ProcesadorPdI)
    ↓
PDI guardado → ID publicado en cola
    ↓
PdiWorker procesa (OCR + etiquetado IA)
    ↓
Evento: PDI_PROCESADO
    ↓
IndexadorWorker (Agregador)
    ↓
Hecho re-indexado con tags automáticos
```

**Validaciones:**
- ✅ PDI creado y encolado correctamente
- ✅ Worker procesa PDI (OCR + etiquetas)
- ✅ PDI guardado con `ocrTexto` y `etiquetas_auto`
- ✅ Evento `PDI_PROCESADO` emitido
- ✅ Hecho re-indexado con tags en MongoDB
- ✅ Búsqueda por tags funciona correctamente
- ✅ Tiempo de procesamiento < 15 segundos

---

### Test 3: Solicitud Borrado → Desindexación

**Flujo:**
```
POST /api/solicitudes (Solicitudes)
    ↓
PATCH /api/solicitudes/{id} (estado: ACEPTADA)
    ↓
Evento: HECHO_BORRADO
    ↓
IndexadorWorker (Agregador)
    ↓
Hecho marcado como fueBorrado=true
```

**Validaciones:**
- ✅ Solicitud creada correctamente
- ✅ Solicitud aceptada (estado: ACEPTADA)
- ✅ Evento `HECHO_BORRADO` emitido
- ✅ Hecho NO aparece en búsquedas o marcado como borrado
- ✅ Tiempo de propagación < 5 segundos

---

## 🚀 Ejecución de Pruebas

### Prerrequisitos

Todos los módulos deben estar corriendo:

```powershell
# Terminal 1: Fuente (puerto 8081)
cd 2025-tp-fuente
$env:SERVER_PORT=8081
./mvnw spring-boot:run

# Terminal 2: Procesador (puerto 8083)
cd 2025-dds-tp-procesadorPdI
$env:SERVER_PORT=8083
./mvnw spring-boot:run

# Terminal 3: Agregador (puerto 8080)
cd 2025-tp-agregador
$env:SERVER_PORT=8080
./mvnw spring-boot:run

# Terminal 4: Solicitudes (puerto 8082)
cd 2025-tp-solicitudes
$env:SERVER_PORT=8082
./mvnw spring-boot:run
```

**Verificar:**
- ✅ RabbitMQ está corriendo y accesible
- ✅ MongoDB Atlas está conectado
- ✅ ApiLayer API key configurada

### Ejecutar Script Automatizado

```powershell
# Desde el directorio Project
cd "c:\Users\LENOVO#1\Desktop\UTN\DS\Project"

# Ejecutar con puertos por defecto
.\test-e2e.ps1

# O especificar URLs personalizadas
.\test-e2e.ps1 `
    -FuenteUrl "http://localhost:8081" `
    -ProcesadorUrl "http://localhost:8083" `
    -AgregadorUrl "http://localhost:8080" `
    -SolicitudesUrl "http://localhost:8082"
```

### Salida Esperada

```
╔══════════════════════════════════════════════════════════════╗
║  🧪 PRUEBAS E2E - CONSISTENCIA EVENTUAL                     ║
╚══════════════════════════════════════════════════════════════╝

📍 URLs configuradas:
   Fuente:      http://localhost:8081
   Procesador:  http://localhost:8083
   Agregador:   http://localhost:8080
   Solicitudes: http://localhost:8082

══════════════════════════════════════════════════════════════
TEST 1: CREAR HECHO Y VERIFICAR INDEXACIÓN
══════════════════════════════════════════════════════════════

1.1) Creando hecho en módulo FUENTE...
   Hecho creado: e2e-test-hecho-20251114103045
✅ PASS: Crear hecho en Fuente
⏳ Esperando 5 segundos (Propagación del evento HECHO_CREADO al agregador)...
1.2) Verificando indexación en módulo AGREGADOR...
   ✓ Hecho encontrado en índice de búsqueda
   ✓ Display: Hecho de prueba E2E para consistencia eventual
✅ PASS: Hecho indexado en MongoDB (evento HECHO_CREADO)

══════════════════════════════════════════════════════════════
TEST 2: CREAR PDI, PROCESAR Y VERIFICAR TAGS EN BÚSQUEDA
══════════════════════════════════════════════════════════════

2.1) Creando PDI para el hecho...
   PDI creado: ID=123
   Estado inicial: OCR Pendiente de procesamiento
✅ PASS: Crear PDI en Procesador
⏳ Esperando 15 segundos (Procesamiento asíncrono (OCR + etiquetado IA))...
2.2) Verificando procesamiento del PDI...
   ✓ OCR procesado
     Texto: Lorem ipsum dolor sit amet, consectetur adipiscing...
   ✓ Etiquetas automáticas generadas: 4
     Tags: outdoor, building, architecture, urban
✅ PASS: PDI procesado con OCR y tags
⏳ Esperando 5 segundos (Propagación del evento PDI_PROCESADO al agregador)...
2.3) Verificando re-indexación con tags en AGREGADOR...
   ✓ Hecho re-indexado con tags automáticos
     Tags en índice: outdoor, building, architecture, urban
✅ PASS: Re-indexación con tags (evento PDI_PROCESADO)
2.4) Probando búsqueda por tags...
   ✓ Hecho encontrado usando búsqueda por tag 'outdoor'
✅ PASS: Búsqueda por tags funcional

══════════════════════════════════════════════════════════════
TEST 3: SOLICITUD DE BORRADO Y VERIFICAR DESINDEXACIÓN
══════════════════════════════════════════════════════════════

3.1) Creando solicitud de borrado...
   Solicitud creada: ID=abc-456
✅ PASS: Crear solicitud de borrado
3.2) Aceptando solicitud de borrado...
   Solicitud aceptada: Estado=ACEPTADA
✅ PASS: Aceptar solicitud de borrado
⏳ Esperando 5 segundos (Propagación del evento HECHO_BORRADO al agregador)...
3.3) Verificando que el hecho NO aparece en búsqueda...
   ✓ Hecho correctamente marcado como borrado o no visible
✅ PASS: Desindexación (evento HECHO_BORRADO)

══════════════════════════════════════════════════════════════
📊 RESUMEN DE PRUEBAS
══════════════════════════════════════════════════════════════

Tests ejecutados: 10
✅ Pasados: 10
❌ Fallidos: 0

🎉 TODOS LOS TESTS PASARON - Sistema funcionando correctamente

✅ Consistencia eventual verificada:
   • Eventos HECHO_CREADO → Indexación automática
   • Eventos PDI_PROCESADO → Re-indexación con tags
   • Eventos HECHO_BORRADO → Desindexación
   • Búsqueda por texto y tags funcional
```

---

## 🔍 Pruebas Manuales

Si prefieres probar manualmente cada paso:

### Paso 1: Crear Hecho

```bash
curl -X POST http://localhost:8081/api/hechos \
  -H "Content-Type: application/json" \
  -d '{
    "id": "manual-test-1",
    "descripcion": "Test manual de consistencia",
    "nombreColeccion": "TestCollection"
  }'
```

**Esperar 5 segundos**, luego buscar:

```bash
curl "http://localhost:8080/api/busqueda?query=manual-test-1&page=0&size=10"
```

### Paso 2: Crear PDI

```bash
curl -X POST http://localhost:8083/api/pdis \
  -H "Content-Type: application/json" \
  -d '{
    "hechoId": "manual-test-1",
    "descripcion": "PDI de prueba",
    "lugar": "Buenos Aires",
    "momento": "2025-11-14T10:00:00",
    "contenido": "Contenido de prueba",
    "imagenUrl": "https://picsum.photos/400/300"
  }'
```

**Esperar 15-20 segundos** (procesamiento), luego consultar:

```bash
curl http://localhost:8083/api/pdis/1
```

Verificar que tiene `ocrTexto` y `etiquetas_auto` poblados.

**Esperar 5 segundos más**, luego buscar nuevamente:

```bash
curl "http://localhost:8080/api/busqueda?query=manual-test-1&page=0&size=10"
```

Ahora debería incluir tags en el campo `tags[]`.

### Paso 3: Solicitud de Borrado

```bash
# Crear solicitud
curl -X POST http://localhost:8082/api/solicitudes \
  -H "Content-Type: application/json" \
  -d '{
    "hechoId": "manual-test-1",
    "motivo": "Prueba manual"
  }'

# Aceptar solicitud (reemplazar {id} con el ID recibido)
curl -X PATCH http://localhost:8082/api/solicitudes/{id} \
  -H "Content-Type: application/json" \
  -d '{
    "estado": "ACEPTADA",
    "descripcion": "Test aprobado"
  }'
```

**Esperar 5 segundos**, luego buscar:

```bash
curl "http://localhost:8080/api/busqueda?query=manual-test-1&page=0&size=10"
```

El hecho NO debería aparecer o debería tener `fueBorrado: true`.

---

## 🐛 Troubleshooting

### Hecho no se indexa (Test 1 falla)

**Posibles causas:**
1. RabbitMQ no está corriendo o no accesible
2. Agregador no está conectado a RabbitMQ
3. MongoDB Atlas no accesible

**Solución:**
```powershell
# Verificar logs del agregador
# Debería ver: "Listening on queue: agregador-indexacion-queue"

# Verificar RabbitMQ Management UI
# Queue "agregador-indexacion-queue" debe tener 1 consumer
```

### PDI no se procesa (Test 2 falla)

**Posibles causas:**
1. PdiWorker no está corriendo
2. ApiLayer API key inválida o expirada
3. Imagen URL no accesible

**Solución:**
```powershell
# Verificar logs del procesador
# Debería ver: "Procesando PdI recibido: {id}"

# Verificar variable de entorno
echo $env:APILAYER_KEY
```

### Tags no aparecen en búsqueda (Test 2.3 falla)

**Posibles causas:**
1. Evento PDI_PROCESADO no se emitió
2. IndexadorWorker no recibió el evento
3. Error al extraer tags en IndexadorServiceImpl

**Solución:**
```powershell
# Verificar logs del agregador
# Debería ver: "[IndexadorWorker] Re-indexando hecho ... por PDI procesado"

# Verificar que IndexadorServiceImpl extrae etiquetas_auto
# Ver código en IndexadorServiceImpl.java líneas 70-85
```

### Borrado no funciona (Test 3 falla)

**Posibles causas:**
1. Evento HECHO_BORRADO no se emitió
2. EventPublisher no está inyectado en Fachada (solicitudes)

**Solución:**
```powershell
# Verificar logs de solicitudes
# Debería ver: "✅ Evento HECHO_BORRADO emitido para hecho: {id}"

# Verificar logs del agregador
# Debería ver: "[IndexadorWorker] Marcando hecho como borrado: {id}"
```

---

## 📊 Métricas de Prueba

Durante las pruebas E2E, las siguientes métricas deberían incrementarse:

- `dds.busqueda.requests` - Búsquedas realizadas
- `dds.pdi.processed` - PDIs procesados
- `dds.pdi.tags.count` - Tags generados

Puedes verificar en Datadog (si configurado) o en los logs de cada módulo.

---

## ✅ Checklist de Validación

- [ ] Test 1: Hecho se indexa automáticamente tras creación
- [ ] Test 2.1: PDI se crea y encola correctamente
- [ ] Test 2.2: PDI se procesa con OCR y etiquetas IA
- [ ] Test 2.3: Hecho se re-indexa con tags automáticos
- [ ] Test 2.4: Búsqueda por tags encuentra el hecho
- [ ] Test 3: Hecho se marca como borrado tras aceptar solicitud
- [ ] Todos los eventos se propagan en < 5 segundos
- [ ] Procesamiento de PDI completa en < 20 segundos
- [ ] No hay errores en logs de ningún módulo

---

## 🎯 Resultado Esperado

**100% de tests pasados** indica que:
- ✅ Arquitectura event-driven funcionando correctamente
- ✅ Consistencia eventual garantizada
- ✅ Workers procesando mensajes asíncronamente
- ✅ Sistema listo para producción

---

**Nota**: Los tiempos de espera (5s, 15s) son configurables en el script. Ajusta según la latencia de tu entorno.
