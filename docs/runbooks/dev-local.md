# Runbook: entorno local

Objetivo: trabajar en Academy con **login real de ChineseReads** sin tocar el repo matriz ni producción.

## Requisitos
- Docker Desktop (Compose v2) abierto.
- Node 22 LTS vía `nvm` (`nvm use` lee `.nvmrc`) y JDK 21 (Temurin recomendado; el 23 también compila con `--release 21`).
- El repo hermano `2025-ChineseTexts` clonado al lado (`../2025-ChineseTexts`) o `CHINESEREADS_REPO_PATH` apuntando a él en `docker/.env`.
- `docker/.env` creado desde `docker/.env.example`; `JWT_SECRET` generado con `openssl rand -base64 48`.

## Compilador Sass en macOS 13
Angular 22 usa por defecto `sass-embedded`, cuyo binario Dart exige macOS 14 o superior. En macOS 13 el build **se queda colgado** (sin error). Solución local, sin tocar el proyecto: exportar `NG_BUILD_SASS_EMBEDDED=0` para que Angular use el compilador Sass en JavaScript (más lento, mismo resultado). En CI y en Docker (Linux) no hace falta.
```bash
echo 'export NG_BUILD_SASS_EMBEDDED=0' >> ~/.bash_profile   # una vez
echo 'export NG_CLI_ANALYTICS=false' >> ~/.bash_profile      # la CLI no pregunta nada
```

## Arranque (cuatro terminales)
```bash
# 1. Infraestructura: MySQL (dos esquemas) + backend de ChineseReads + stub del endpoint interno
make dev-up            # primera vez: construye la imagen del backend del matriz (varios minutos, ~1 GB de disco)
make dev-ps            # db healthy, chinesereads-backend y premium-grant-stub "Up"

# 2. Backend de Academy (:8081, actuator :8082)
cd backend && ACADEMY_DB_PASSWORD=academy-dev-password JWT_SECRET=<el de docker/.env> \
  ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# 3. Frontend de Academy (:4300, /api → :8081)
cd frontend && nvm use && npm start

# 4. Frontend de ChineseReads (:4200) para hacer login
make dev-cr-frontend
```

Comprobaciones rápidas:
```bash
curl -si localhost:8081/api/me | head -1          # HTTP/1.1 401 (ProblemDetail, code UNAUTHENTICATED)
curl -s  localhost:8082/actuator/health           # {"status":"UP"}
curl -s  localhost:8089/__admin/mappings | head -c 300   # el stub ha cargado los mappings del contrato
```

## Cómo funciona el login en local
1. Entra en `http://localhost:4200` (ChineseReads) con uno de los dos usuarios de desarrollo que siembra su `DatabaseInitializer` (ver ese fichero en el repo matriz; nunca copiar contraseñas aquí).
2. Su backend escribe la cookie `AuthToken` para el host `localhost`, sin `Domain`.
3. Las cookies no distinguen puerto: el navegador la envía también a `http://localhost:4300`. El proxy de desarrollo la reenvía al backend de Academy, que la valida con el mismo `JWT_SECRET` de `docker/.env`.
4. En `http://localhost:4300` la cabecera muestra tu nombre; "Soy profesor" crea tu perfil (`POST /api/teachers/me`).

El `Domain=.chinesereads.com` real solo se prueba cuando exista la PR de integración del matriz.

## Puertos
| Servicio | Puerto |
|---|---|
| ChineseReads backend (Docker) | 8080 |
| ChineseReads frontend (`ng serve`, repo matriz) | 4200 |
| Academy backend | 8081 (actuator 8082) |
| Academy frontend (`ng serve`) | 4300 |
| MySQL | 3306 |
| Stub endpoint interno (WireMock) | 8089 |
| ai-service / tts-service (perfil `ai`, opcional) | 5001 / 5002 |

## Mantenimiento
- `make dev-down` para todo y conserva la base de datos; `make dev-reset` la borra (vuelve a ejecutar `docker/dev/mysql-init`).
- Tras reconstruir imágenes: `docker builder prune -af` para recuperar disco.
- Los servicios de IA (`make dev-up-ai`) consumen crédito de DeepSeek/Google: arrancarlos solo cuando se trabaje en `aigen`.
