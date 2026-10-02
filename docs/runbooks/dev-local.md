# Runbook: entorno local

> Se completa en la PR del entorno Docker (PR4). Lo que sigue es el diseño acordado.

## Requisitos
- Docker Desktop (Compose v2), Node 22 LTS vía `nvm` (`nvm use` lee `.nvmrc`), JDK 21 (Temurin recomendado), `make`.
- El repo hermano `2025-ChineseTexts` clonado al lado (`../2025-ChineseTexts`) o `CHINESEREADS_REPO_PATH` apuntando a él.
- `docker/.env` creado a partir de `docker/.env.example`; `JWT_SECRET` generado con `openssl rand -base64 48`.

## Arranque
```bash
make dev-up            # MySQL (dos esquemas) + backend de ChineseReads + stub del endpoint interno
cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev   # :8081 (actuator :8082)
cd frontend && npm start                                               # :4300
make dev-cr-frontend                                                   # ChineseReads en :4200
```

## Cómo funciona el login en local
Inicias sesión en `http://localhost:4200` (ChineseReads). Su backend escribe la cookie `AuthToken` para el host `localhost`. Las cookies no distinguen puerto, así que el navegador la envía también a `http://localhost:4300`; el proxy de desarrollo la reenvía al backend de Academy, que la valida con el mismo `JWT_SECRET`. No hace falta tocar el repo matriz.

## Compilador Sass en macOS 13
Angular 22 usa por defecto `sass-embedded`, cuyo binario Dart exige macOS 14 o superior. En macOS 13 el build **se queda colgado** (sin error). Solución local, sin tocar el proyecto: exportar `NG_BUILD_SASS_EMBEDDED=0` para que Angular use el compilador Sass en JavaScript (más lento, mismo resultado). En CI y en Docker (Linux) no hace falta.
```bash
echo 'export NG_BUILD_SASS_EMBEDDED=0' >> ~/.bash_profile   # una vez
```
También conviene `export NG_CLI_ANALYTICS=false` para que la CLI no pregunte nada.

## Puertos
| Servicio | Puerto |
|---|---|
| ChineseReads backend (Docker) | 8080 |
| ChineseReads frontend (`ng serve`) | 4200 |
| Academy backend | 8081 (actuator 8082) |
| Academy frontend (`ng serve`) | 4300 |
| MySQL | 3306 |
| Stub endpoint interno (WireMock) | 8089 |
