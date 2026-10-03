# Puntos de integración con ChineseReads (hechos verificados en el repo matriz)

Verificado el 03-09-2026 y re-verificado el 02-10-2026 sobre `codeurjc-students/2025-ChineseTexts` en `main` (commit `fa664f3`). Re-verificar rutas y líneas antes de tocar nada: el proyecto matriz sigue evolucionando. El contrato formal y versionado está en `docs/integracion-chinesereads/CONTRACT.md`; este documento es la explicación.

## Identidad / sesión
- Cookies JWT **`AuthToken`** (acceso) y **`RefreshToken`** (refresco), HttpOnly, `Path=/`, **sin atributo `Domain`** ni `Secure`/`SameSite` → hoy solo valen para `chinesereads.com`. Ambas duran **7 días** (`Security/jwt/TokenType.java`).
  Fichero: `backend/src/main/java/com/chinesereads/backend/Security/jwt/UserLoginService.java`, métodos `buildTokenCookie` y `removeTokenCookie`.
  **Cambio necesario en ChineseReads:** `Domain=.chinesereads.com` y `SameSite=Lax` en ambos métodos, configurables por propiedad para que en local siga funcionando sin dominio.
- Firma HS256 con `jwt.secret=${JWT_SECRET:}` (`application.properties`), clave `Keys.hmacShaKeyFor(secret.trim().getBytes(UTF_8))`, librería `io.jsonwebtoken` **0.11.5**. Si el secreto llega vacío, `JwtTokenProvider` genera una clave aleatoria por arranque.
  **Hallazgo 02-10-2026:** en la copia local del repo matriz, `docker/.env` define la variable como `JWT_SECRET_KEY`, pero `docker-compose.yml` y `application.properties` leen `JWT_SECRET`. Si producción tiene el mismo desajuste, la clave es aleatoria en cada arranque: cada redeploy invalida todas las sesiones y la sesión compartida con Academy es imposible. Comprobación y arreglo: ver `CONTRACT.md` §Prerrequisitos.
- Claims del token: `sub` = **email** del usuario, `iat`, `exp`, `roles` = autoridades serializadas (`[{"authority":"ROLE_USER"}]`), `type` = `ACCESS` | `REFRESH`.
- Academy debe recibir el **mismo `JWT_SECRET`** y validar con la misma lógica (portada literal, misma versión de librería). Academy solo acepta `type = ACCESS` (ADR-013) y no confía en `roles` del token para autorizar.
- Roles en ChineseReads: `USER`, `ADMIN`. Premium **no es un rol**: es `User.premiumUntil` (LocalDateTime) + `User.isPremiumActive()`.

## Datos de usuario
- Tabla **`chinesereads.user`** (singular: la entidad `User` no declara `@Table`), roles en `user_roles(user_id, roles)`. Columnas que Academy lee: `id`, `email`, `name`, `language`, `blocked`, `premium_until`. Contrato SQL en `docs/integracion-chinesereads/chinesereads-user.contract.sql`.
- Academy lee con el usuario MySQL `academy_app` (solo SELECT en esas dos tablas, ADR-011). **Nunca escribe tablas de ChineseReads.**
- El esquema del matriz se gestiona con `spring.jpa.hibernate.ddl-auto=update`: si cambia una columna leída, Academy lo detecta al arrancar (`ddl-auto=validate`) y el comprobador de frontera avisa antes.

## Acceso de los alumnos a ChineseReads
- Reutilizar `premiumUntil`: un asiento activo → ChineseReads fija `premiumUntil` del alumno hasta el fin de la suscripción del profesor. Ya existe `PATCH /api/users/{id}/premium` (solo admin) que hace exactamente eso.
- **Endpoint interno a crear en ChineseReads:** `POST /api/internal/premium-grant {userId, until}` y `DELETE /api/internal/premium-grant/{userId}`, protegido por la cabecera `X-Service-Secret` y **no expuesto por Caddy**. Contrato exacto en `CONTRACT.md` y stub WireMock en `docs/integracion-chinesereads/wiremock/`.
- **Hallazgo 02-10-2026:** el Caddyfile hace `handle /api/* → backend` sin excepciones, así que `/api/internal/*` quedaría expuesto a Internet. La PR de integración debe añadir `handle /api/internal/* { respond 404 }` **antes** de ese bloque, además del secreto de servicio (defensa doble).

## Servicios reutilizables (HTTP interno, red `app-network`)
- `ai-service` (Flask, puerto 5001, DeepSeek `deepseek-chat` vía SDK OpenAI): generación de textos por nivel/tema, títulos, traducciones, palabras faltantes, chat tutor. Rutas en `ai-service/deepseekService.py`.
- `tts-service` (Flask, 5002, Google WaveNet `cmn-CN-Wavenet-A`, `MAX_CHARS=1600`): `POST /synthesize`.
- `ocr-service` (Flask, 5000, Google Vision): `POST /ocr`.
- Credenciales (`DEEPSEEK_API_KEY`, `credentials.json`) ya montadas en esos contenedores; Academy no las necesita.

## Pagos
- ChineseReads: `StripeService.createCheckoutSession` (modo `SUBSCRIPTION`), webhook `POST /api/premium/webhook` verificado por firma, lee JSON crudo. Precios `STRIPE_PRICE_MONTHLY` / `STRIPE_PRICE_YEARLY`.
- Academy: Stripe propio (ADR-009). **La PR de integración debe hacer que el webhook de ChineseReads ignore los eventos con `metadata.product = "academy"`.**

## Infraestructura
- Servidor: Hetzner Cloud 8 GB, Ubuntu, compartido con otro proceso (publicador de contenido). 7 contenedores: caddy, frontend-ssr, backend, db, ai-service, ocr-service, tts-service. **Medir `free -m` y `docker stats` antes de añadir dos más.** Ventana prohibida de despliegue: 19:00–20:15 Madrid.
- Caddy: `docker/Caddyfile`, bloque actual `chinesereads.com, www.chinesereads.com`. Añadir bloque `academy.chinesereads.com` (ver `CONTRACT.md`). Certificado automático.
- DNS: registro `A` `academy` → misma IP. Coste 0 €.
- Red Docker: `app-network` (bridge). Academy se une como red externa.
- Deploy del matriz: `docker/deploy.sh` (backup BD → build frontend → `up -d` → force-recreate caddy/frontend-ssr). Academy tendrá el suyo; coordinar para no recrear Caddy a la vez.

## PR de integración en el proyecto matriz (pequeña, una sola, cuando toque)
1. `Domain` y `SameSite` configurables en las cookies JWT.
2. Endpoint interno de premium por asiento + test de contrato con el fixture de `docs/integracion-chinesereads/jwt-fixture.txt`.
3. Bloque `academy.chinesereads.com` en el Caddyfile + `handle /api/internal/* { respond 404 }` en el bloque principal.
4. El webhook de Stripe ignora `metadata.product = "academy"`.
5. Enlace "Academy" en cabecera + campo/badge "Profesor colaborador" en Hall of Fame (ya es admin-editable).
6. Espejo del contrato: `docs/integracion-academy/` con su `contract-lock.json` apuntando a `docs/integracion-chinesereads/*` de Academy.
7. (Recomendado, no bloqueante) seed de usuarios de desarrollo solo bajo un perfil `dev`.
Todo lo demás vive en el repo de Academy.
