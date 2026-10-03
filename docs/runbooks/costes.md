# Runbook: ingeniería de costes

Referencia operativa del ADR-015. Todo lo configurable va por variables de entorno con valor por defecto en `application.yml`.

## Variables (nombres reservados; se activan con el módulo `aigen`)
| Variable | Qué limita | Defecto propuesto |
|---|---|---|
| `AIGEN_TEACHER_MONTHLY_GENERATIONS` | Generaciones de texto/fichas por profesor y mes | 200 |
| `AIGEN_TEACHER_MONTHLY_AUDIO_CHARS` | Caracteres sintetizados por profesor y mes | 60000 |
| `AIGEN_TEACHER_MONTHLY_CHAT_MESSAGES` | Mensajes de chat IA por profesor y mes | 300 |
| `AIGEN_GLOBAL_DAILY_LIMIT` | Fusible global diario de llamadas de pago | 500 |
| `AIGEN_MONTHLY_BUDGET_EUR` | Presupuesto mensual estimado; aviso en log al 80 % | 30 |
| `AIGEN_PRICE_DEEPSEEK_IN_PER_MTOK` / `_OUT_PER_MTOK` | Tarifa estimada por millón de tokens (EUR) | según proveedor |
| `AIGEN_PRICE_TTS_PER_MCHAR` | Tarifa estimada por millón de caracteres (EUR) | según proveedor |
| `AIGEN_AUDIO_CACHE_MAX_MB` | Tamaño máximo de la caché persistente de audio | 500 |

## Reglas
1. Solo el módulo `aigen` llama a `ai-service`, `tts-service` y `ocr-service`. Verificado por el test de modularidad.
2. Reservar antes de gastar: el contador se incrementa en transacción propia antes de la llamada externa; si la llamada falla, se compensa.
3. Generar por profesor/grupo, no por alumno; los materiales y audios se guardan y se reutilizan.
4. Caché de audio persistente (`audio_cache`, clave = hash de texto + voz), no solo en memoria.
5. Cada llamada queda en `ai_usage` con coste estimado y `correlation_id`.

## Cómo revisar el gasto
- Panel admin `/api/admin/usage` (cuando exista): coste estimado por profesor y total del mes frente al presupuesto.
- Proveedores: consola de DeepSeek y facturación de Google Cloud (TTS/Vision). Revisar mensualmente y ajustar las tarifas de las variables de precio.
- Si el coste por profesor se acerca al margen (≈110 € netos/año con el precio hipótesis), bajar cuotas por variable, sin redeploy de código.
