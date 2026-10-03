# Changelog de frontera

Cada entrada es un mensaje listo para llevar al otro repositorio. Formato: fecha · versión del contrato · qué cambió · qué debe hacer el otro lado · estado.

## 2026-10-02 · contrato 1.0.1 · precisión sobre el algoritmo HMAC
**Qué cambió en Academy:** el contrato decía "HS256"; en realidad jjwt 0.11.5 elige HS256/HS384/HS512 según la longitud del secreto (`signWith(Key)`). El verificador de Academy (`identity/internal/SharedJwtVerifier`) replica esa regla y el test de contrato lo cubre con secretos de 44, 48 y 64 bytes.

**Mensaje para el repositorio de ChineseReads:** ninguna acción. Solo tenerlo en cuenta si algún día se fija el algoritmo explícitamente en `JwtTokenProvider.buildToken` (p. ej. `signWith(key, HS256)`): avisar, porque Academy asume la regla por longitud.

**Estado:** sin acción pendiente.

## 2026-10-02 · contrato 1.0.0 · creación del contrato
**Qué cambió en Academy:** se formaliza la frontera (cookie `AuthToken`, claims, columnas leídas, endpoint interno, Caddy, Stripe). Nada en código todavía.

**Mensaje para el repositorio de ChineseReads (pendiente, no urgente; no tocar producción en picos de tráfico):**
> En Academy existe `docs/integracion-chinesereads/CONTRACT.md` v1.0.0. Cuando toque la PR de integración, implementa exactamente: (1) `Domain=.chinesereads.com` + `SameSite=Lax` + `Secure` configurables en `buildTokenCookie`/`removeTokenCookie`; (2) `POST/DELETE /api/internal/premium-grant` con cabecera `X-Service-Secret` según §3 del contrato, con test que valide el fixture `jwt-fixture.txt`; (3) Caddy: bloque `academy.chinesereads.com` y `handle /api/internal/* { respond 404 }` antes de `handle /api/*`; (4) el webhook de Stripe ignora `metadata.product = "academy"`; (5) `docs/integracion-academy/contract-lock.json` vigilando `docs/integracion-chinesereads/*` de Academy. Antes de nada, comprueba en el servidor que el backend recibe `JWT_SECRET` (en local el `.env` dice `JWT_SECRET_KEY`).

**Estado:** pendiente de la PR de integración (después de los cimientos de Academy).
