# Guía de desarrollo — ChineseReads Academy

## Qué es este proyecto
Producto B2B para profesores particulares de chino: el profesor paga una suscripción anual, organiza a sus alumnos, genera materiales (plan de estudios, fichas, dictados, ejercicios) y sus alumnos obtienen acceso a ChineseReads (https://chinesereads.com) y a esta web (`academy.chinesereads.com`). Repo hermano de `2025-ChineseTexts` (el proyecto matriz, en producción), que **no se modifica desde aquí** salvo por la PR de integración descrita en `docs/integracion-chinesereads.md`.

Lee antes de proponer nada: `docs/valoracion.md`, `docs/decisiones.md` (ADR), `docs/precio-hipotesis.md`, `docs/integracion-chinesereads.md` y `docs/integracion-chinesereads/CONTRACT.md`.

## Reglas no negociables
- **Secretos jamás en git** ni mostrados en pantalla: `.env`, `credentials.json`, claves de Stripe/DeepSeek/Brevo, `JWT_SECRET`. Los comandos que usan credenciales o acceso al servidor los ejecuta el dueño del proyecto.
- **Nunca romper lo existente**: cada PR pasa `make verify` completo antes de proponer el merge. El dueño mergea.
- **Una PR por funcionalidad**, rama `feature/<nombre>`, `fix/<nombre>`, `chore/<nombre>` o `docs/<nombre>`; commits con Conventional Commits en inglés (`feat(classroom): ...`). Autoría: únicamente la identidad git del dueño; sin coautorías ni menciones a herramientas en commits, PRs, código o documentación.
- **Nunca tocar producción ni el repo matriz desde este repo.** Lo que el proyecto matriz deba cambiar se escribe en `docs/integracion-chinesereads/CHANGELOG-frontera.md` como mensaje listo para llevar a ese repo.
- **Avisar al principio** de cualquier tarea si una parte requerirá una acción del dueño (credenciales, DNS, Node, dashboards externos, servidor).
- **Planificar antes de codificar** en cualquier cambio que toque backend + frontend, base de datos, pagos, autenticación, despliegue o la frontera con ChineseReads.
- **Frontera con ChineseReads**: si una tarea toca `identity/internal/SharedJwt*`, el módulo `access`, `docs/integracion-chinesereads/` o variables `CHINESEREADS_*`, hay que actualizar `CONTRACT.md` y `CHANGELOG-frontera.md` en la misma PR.

## Metodología
1. **CI desde el primer commit**: GitHub Actions con build + tests en cada PR. No se mergea en rojo.
2. **Una especificación de una página por herramienta de IA** en `docs/specs/` (plantilla `spec-template.md`): entrada, salida, ejemplo real, criterio de "está bien", coste estimado. Se escribe antes que el código y nace de una petición de un profesor real.
3. **Un ADR corto por decisión de arquitectura** en `docs/decisiones.md` (qué, por qué, qué lo invalidaría).
4. **Tests de contrato** para la frontera con ChineseReads (JWT compartido, columnas leídas de `chinesereads.user`, endpoint interno): si uno de los dos lados cambia, un test o el comprobador de frontera avisa.
5. **Fronteras entre módulos verificadas en el build** (Spring Modulith): un módulo solo usa la API pública de otro; los ciclos rompen el build.
6. **Esquema de base de datos solo por migraciones Flyway**; nunca `ddl-auto=update`. Tests de base de datos contra MySQL real (Testcontainers), nunca H2.
7. **Errores con identificador**: toda respuesta de error lleva `code` estable y `errorId` correlacionado con el log. La interfaz traduce por `code`; el backend no traduce.
8. **Ingeniería de costes** (ADR-015): toda llamada de pago (IA, TTS, OCR) pasa por el módulo `aigen`, reserva cuota antes de gastar y queda contabilizada.
9. **Menores**: el producto es para alumnos de 16+ / adultos (ADR-005). Ninguna funcionalidad asume menores.
10. **Contenido con copyright** (letras de canciones, audio comercial): prohibido salvo dominio público o subido por el profesor (ADR-007).

## Stack
Java 21 · Spring Boot 4.1 · Spring Modulith 2.1 · Flyway · MySQL 8 (instancia compartida, esquema propio `academy`) · Angular 22 standalone + Transloco (EN raíz / `/es`) + prerender de rutas públicas · Node 22 LTS (`.nvmrc`; Node 24 no publica binarios para macOS 13.1, la versión del equipo de desarrollo) · Docker Compose unido a la red `app-network` · Caddy del proyecto matriz (bloque `academy.chinesereads.com`) · reutiliza `ai-service`, `tts-service` y `ocr-service` de ChineseReads por HTTP interno.

## Verificación estándar antes de cualquier PR
```bash
make verify            # = check-no-ai-refs + check-contract + backend + frontend
# o por partes:
cd backend && ./mvnw -B verify
cd frontend && npm run lint && npm run build && npm test -- --watch=false
scripts/check-chinesereads-contract.sh --source auto
```

## Entorno local
`docs/runbooks/dev-local.md`. El login en local es real: se construye el backend de ChineseReads desde el repo hermano (`CHINESEREADS_REPO_PATH`, por defecto `../2025-ChineseTexts`) y ambas webs comparten la cookie en `localhost`.
