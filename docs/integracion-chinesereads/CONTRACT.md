# Contrato de frontera ChineseReads ↔ Academy

**Versión del contrato:** 1.0.1 (2026-10-02)
**Repo matriz:** `codeurjc-students/2025-ChineseTexts`, rama `main`, commit de referencia `fa664f3`.

Este documento y los ficheros de esta carpeta son la **única fuente de verdad** de lo que un proyecto espera del otro. Los tests de contrato de Academy leen de aquí; la PR de integración del repo matriz debe leer de aquí también. Cualquier cambio exige subir la versión, anotarlo en `CHANGELOG-frontera.md` y actualizar `contract-lock.json`.

## 1. Sesión compartida (navegador → ambos backends)

| Elemento | Valor |
|---|---|
| Cookie de acceso | `AuthToken` |
| Cookie de refresco | `RefreshToken` (Academy la ignora) |
| Atributos hoy | `HttpOnly`, `Path=/`, sin `Domain`, sin `Secure`, sin `SameSite` |
| Atributos tras la PR de integración | + `Domain=.chinesereads.com`, `SameSite=Lax`, `Secure` (configurables; en local sin `Domain`) |
| Algoritmo | HMAC con clave `Keys.hmacShaKeyFor(JWT_SECRET.trim().getBytes(UTF_8))` (≥ 32 bytes). **jjwt elige el algoritmo por la longitud del secreto**: ≥ 64 bytes → HS512, ≥ 48 → HS384, si no HS256. Un secreto generado con `openssl rand -base64 48` tiene 64 caracteres → **HS512**. El verificador de Academy aplica la misma regla |
| Librería emisora | `io.jsonwebtoken:jjwt` 0.11.5 (`Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)`) |
| Claims | `sub` = email · `iat` · `exp` (7 días) · `roles` = `[{"authority":"ROLE_USER"}, ...]` · `type` = `ACCESS` \| `REFRESH` |
| Variable de entorno | `JWT_SECRET` (**mismo valor** en `docker/.env` de ambos proyectos) |
| Qué hace Academy | Verifica firma y expiración; exige `type == "ACCESS"`; carga el usuario por `sub` desde `chinesereads.user`; `blocked = 1` → anónimo; no confía en `roles` del token |
| Fixture | `jwt-fixture.txt`: token firmado con el secreto de pruebas `JWT_TEST_SECRET` de `jwt-fixture.txt` (cabecera del fichero); `exp` en 2099. Ambos repos deben validarlo con ese secreto |

## 2. Lectura de usuarios (Academy → MySQL)

| Elemento | Valor |
|---|---|
| Tabla | `chinesereads.user` (singular) y `chinesereads.user_roles(user_id, roles)`. Nota JDBC: en MySQL una base de datos es un **catálogo**, no un schema; la entidad de Academy usa `@Table(catalog = "chinesereads")` |
| Columnas leídas | `id BIGINT`, `email VARCHAR`, `name VARCHAR`, `language VARCHAR`, `blocked BIT/TINYINT(1)`, `premium_until DATETIME(6) NULL` |
| Usuario MySQL | `academy_app`: `ALL ON academy.*`, `SELECT ON chinesereads.user`, `SELECT ON chinesereads.user_roles` (en producción, con las tablas ya creadas; en el entorno local el GRANT es `SELECT ON chinesereads.*` porque MySQL 8 no admite GRANT sobre tablas inexistentes). Nunca INSERT/UPDATE/DELETE sobre `chinesereads` |
| Contrato SQL | `chinesereads-user.contract.sql` (lo ejecutan los tests de Academy en Testcontainers) |
| Regla | Academy **nunca** escribe en el esquema `chinesereads` |

## 3. Concesión de premium por asiento (Academy → ChineseReads, red interna)

**Aún no existe en ChineseReads.** Hasta que exista, Academy habla con el stub WireMock de `wiremock/`. Este es el contrato que la PR de integración debe implementar.

| Elemento | Valor |
|---|---|
| Base URL | `http://backend:8080` dentro de `app-network` (`CHINESEREADS_INTERNAL_BASE_URL`) |
| Autenticación | Cabecera `X-Service-Secret: <CHINESEREADS_INTERNAL_SECRET>` (≥ 32 bytes aleatorios; mismo valor en ambos `.env`) |
| Correlación | Cabecera `X-Request-Id` propagada; ChineseReads la devuelve y la loguea |
| Conceder | `POST /api/internal/premium-grant` · body `{"userId": 123, "until": "2027-10-02T00:00:00Z", "source": "academy", "reference": "enrollment:456"}` · `204 No Content` si se aplicó; `404` si el usuario no existe; `401` sin/mal secreto; `422` si `until` no es futuro |
| Retirar | `DELETE /api/internal/premium-grant/{userId}?source=academy` · `204`; `404` si el usuario no existe; `401` sin/mal secreto |
| Semántica | Conceder = `premiumUntil := max(premiumUntil actual, until)`. Retirar = si el premium vigente procede de Academy (`source`), `premiumUntil := null`; si procede de Stripe propio del usuario, no tocar (204 igualmente). Idempotente en ambos casos |
| Exposición | **No** accesible desde Internet: Caddy añade `handle /api/internal/* { respond 404 }` antes de `handle /api/*` |
| Reintentos | Academy reintenta con backoff (evento persistido); `premium_grant.status` refleja `PENDING/GRANTED/FAILED` |

## 4. Caddy (bloques que la PR de integración añade)

```
academy.chinesereads.com {
    encode gzip
    handle /api/* {
        reverse_proxy academy-backend:8080
    }
    handle {
        reverse_proxy academy-frontend:80
    }
}
```
Y en el bloque `chinesereads.com, www.chinesereads.com`, **antes** de `handle /api/*`:
```
    handle /api/internal/* {
        respond 404
    }
```

## 5. Stripe

| Elemento | Valor |
|---|---|
| Cuenta | La misma. Cada endpoint de webhook recibe todos los eventos |
| Marca de Academy | `metadata.product = "academy"` y `client_reference_id = "academy:<teacherId>"` en Checkout; la suscripción resultante hereda `metadata.product` |
| Obligación de ChineseReads | Su webhook `POST /api/premium/webhook` **ignora** (200 sin efecto) los eventos cuyo objeto tenga `metadata.product = "academy"` |
| Obligación de Academy | Su webhook ignora los eventos sin `metadata.product = "academy"` |

## 6. Variables de entorno compartidas

| Variable | Dónde | Notas |
|---|---|---|
| `JWT_SECRET` | ambos `docker/.env` | Mismo valor. Generar con `openssl rand -base64 48` |
| `CHINESEREADS_INTERNAL_SECRET` | ambos `docker/.env` | Mismo valor. Solo para el endpoint interno |
| Red Docker | `app-network` | Creada por el compose del matriz; externa para Academy |

## Prerrequisitos en producción (acción del dueño del proyecto, en ventana tranquila)
1. Comprobar que el backend del matriz recibe `JWT_SECRET` (y no `JWT_SECRET_KEY`):
   ```bash
   cd /root/2025-ChineseTexts && cut -d= -f1 docker/.env | grep -i jwt          # debe imprimir JWT_SECRET
   docker compose -f docker/docker-compose.yml exec backend sh -c 'test -n "$JWT_SECRET" && echo OK || echo VACIO'
   ```
   Si imprime `JWT_SECRET_KEY` o `VACIO`: renombrar la línea en `.env` y `docker compose up -d backend`. Efecto: una sola vez todos vuelven a iniciar sesión; los datos no se tocan.
2. Medir RAM libre antes de añadir contenedores: `free -m`, `docker stats --no-stream`.

## Ficheros del repo matriz vigilados
Ver `contract-lock.json`. Si cualquiera cambia, `scripts/check-chinesereads-contract.sh` falla y el flujo nocturno abre una issue. Revisar el cambio, actualizar este contrato si procede, regenerar el lock con `scripts/check-chinesereads-contract.sh --update` y anotar en el changelog.
