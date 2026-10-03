# Stub del endpoint interno de ChineseReads

Mappings de WireMock que implementan el §3 de `../CONTRACT.md`. Los usan a la vez el entorno local (`docker-compose.dev.yml`, servicio `premium-grant-stub`) y el test de contrato del módulo `access`, de modo que hay una sola definición del comportamiento esperado.

El secreto `dev-internal-secret-change-me-0123456789abcdef` es solo de desarrollo y pruebas; en producción el valor viene de `CHINESEREADS_INTERNAL_SECRET`.
