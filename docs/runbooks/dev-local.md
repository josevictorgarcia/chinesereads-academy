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

## Puertos
| Servicio | Puerto |
|---|---|
| ChineseReads backend (Docker) | 8080 |
| ChineseReads frontend (`ng serve`) | 4200 |
| Academy backend | 8081 (actuator 8082) |
| Academy frontend (`ng serve`) | 4300 |
| MySQL | 3306 |
| Stub endpoint interno (WireMock) | 8089 |
