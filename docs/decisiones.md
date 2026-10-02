# ChineseReads Academy — decisiones tomadas (registro ADR)

Cada decisión: qué se decidió, por qué, y qué la invalidaría. Añade una entrada por cada decisión nueva.

## ADR-001 · Subdominio `academy.chinesereads.com`, no dominio `.academy` (2026-09-03)
**Decisión:** el producto vive en `academy.chinesereads.com`. Si se compra `chinesereads.academy` es solo para redirigir.
**Por qué:** la cookie JWT de ChineseReads se emite sin `Domain`; con `Domain=.chinesereads.com` (una línea) la sesión se comparte entre ambas webs. Un TLD distinto no puede compartir cookies y obligaría a un flujo de traspaso tipo OAuth. Coste del subdominio: 0 € (registro DNS + certificado Let's Encrypt automático vía Caddy).
**Se revisaría si:** la marca exigiera un dominio propio y se aceptara construir el traspaso de sesión.

## ADR-002 · Segundo monolito modular, no microservicios (2026-09-03)
**Decisión:** Academy = un backend Spring Boot + un frontend Angular, repo propio, `docker-compose` propio unido a la red `app-network` existente. Reutiliza `ai-service`, `tts-service`, `ocr-service`. ChineseReads NO se migra.
**Por qué:** un desarrollador; microservicios resuelven escalado organizativo, no técnico. Cada servicio extra es un despliegue, un log y un modo de fallo más.

## ADR-003 · Identidad y cobro en ChineseReads; clases en Academy (2026-09-03)
**Decisión:** una instancia MySQL, dos esquemas. `chinesereads.user` es de ChineseReads; Academy lee por id y nunca escribe sus tablas: para dar/quitar acceso a un alumno llama a un endpoint interno de ChineseReads (secreto de servicio). El acceso del alumno a ChineseReads = `premiumUntil` fijado por asiento activo.
**Por qué:** reutiliza toda la maquinaria premium ya probada; "existir en ambas webs" es automático al compartir la tabla de usuarios; nadie rompe la base del otro.

## ADR-004 · Segmento inicial: profesores particulares (2026-09-03)
**Decisión:** primero profesores autónomos (italki/Preply, 5-20 alumnos). Academias pequeñas y escuelas más adelante; el modelo de asientos ya lo permite.
**Por qué:** deciden solos y en días; una institución tarda meses y desanima.

## ADR-005 · Solo adultos / mayores de 16 (2026-09-03, preferencia; confirmar en términos)
**Decisión:** Academy se declara para alumnos de 16+ (o adultos) en sus términos de uso.
**Por qué:** menores de 14 exigen consentimiento parental (GDPR/LOPDGDD) y las instituciones piden contrato de encargado de tratamiento. Evitarlo desde el día 1 ahorra todo ese aparato legal. Los alumnos de profesores de italki son mayoritariamente adultos.

## ADR-006 · "Profesor colaborador de ChineseReads", no "certificado" (2026-09-03)
**Decisión:** el PDF se llama "Profesor colaborador" (o similar). Nunca "certificado" a secas ni "certificado oficial".
**Por qué:** no es una acreditación reconocida; prometerla sería engañoso.

## ADR-007 · Letras de canciones: la última, y solo con contenido lícito (2026-09-03)
**Decisión:** la herramienta de letra sincronizada con pinyin/traducción se pospone al final del roadmap. Si se hace: solo canciones de dominio público, infantiles tradicionales, o letras subidas por el propio profesor bajo su responsabilidad. Nada de reproducir audio con copyright.
**Por qué:** las letras tienen copyright; mostrarlas requiere licencia. Es la herramienta más vistosa y la única con riesgo legal real.

## ADR-008 · Fase 0 obligatoria: validar con 10 profesores antes de código (2026-09-03)
**Decisión:** no se escribe código de Academy hasta enviar 10 emails a profesores (Cuenta Profesor + pregunta abierta "¿qué querrías que hiciera?"). Luz verde con ≥ 2 dispuestos a pagar.
**Por qué:** hoy no hay ninguna evidencia de demanda. Es el mayor riesgo y no es técnico.

## ADR-009 · Stripe propio en Academy (opción A) (2026-10-02)
**Decisión:** Academy tiene su propio `StripeService` y su propio webhook (`POST /api/billing/webhook`, secreto `STRIPE_WEBHOOK_SECRET_ACADEMY`), un tercer precio (`STRIPE_PRICE_TEACHER_YEARLY`) y marca sus sesiones de checkout con `client_reference_id = "academy:<teacherId>"` y `metadata.product = "academy"`. Para dar o quitar premium a un alumno solo usa el endpoint interno de ChineseReads (ADR-003).
**Por qué:** el dueño pidió que los proyectos sean lo más independientes posible; la lógica de asientos vive donde están los datos; la PR de integración en el proyecto matriz se mantiene mínima.
**Consecuencia obligatoria:** la misma cuenta de Stripe envía cada evento a todos los endpoints suscritos, así que el webhook de ChineseReads debe **ignorar** los eventos con `metadata.product = "academy"` (hoy intentaría casarlos con un usuario). Va en la PR de integración.
**Se revisaría si:** Stripe exigiera una sola integración por cuenta (no es el caso) o se separaran las cuentas.

## ADR-010 · Spring Modulith + Flyway + Testcontainers (2026-10-02)
**Decisión:** el backend es un monolito modular con Spring Modulith (fronteras entre módulos verificadas en el build, eventos persistidos y reintentables). El esquema `academy` se gestiona solo con migraciones Flyway (`ddl-auto=validate`). Los tests de base de datos corren contra MySQL 8 real con Testcontainers.
**Por qué:** un desarrollador único necesita que los errores de estructura se detecten al compilar, no en producción; `ddl-auto=update` oculta cambios de esquema y H2 arrastra diferencias de dialecto (el proyecto matriz necesita `NON_KEYWORDS=USER` por ello).
**Se revisaría si:** Modulith dejara de soportar la versión de Spring Boot elegida; entonces se mantendrían las mismas fronteras con ArchUnit.

## ADR-011 · Usuario MySQL de mínimo privilegio y sin claves foráneas entre esquemas (2026-10-02)
**Decisión:** Academy se conecta con el usuario `academy_app`: `ALL ON academy.*`, `SELECT ON chinesereads.user`, `SELECT ON chinesereads.user_roles`, nada más. Las referencias a `chinesereads.user.id` son columnas `*_user_id` sin FK física.
**Por qué:** "Academy nunca escribe tablas de ChineseReads" (ADR-003) pasa de convención a imposibilidad técnica; una FK entre esquemas acoplaría los despliegues y los backups.
**Se revisaría si:** ambos esquemas pasaran a gestionarse como una sola unidad de despliegue.

## ADR-012 · Angular 22 + Node 24, SSR híbrido nativo e i18n idéntica al proyecto matriz (2026-10-02)
**Decisión:** frontend en Angular 22 (última estable) con Node 24 LTS fijado en `.nvmrc`; el proyecto matriz sigue en Angular 17 / Node 20 en su propia carpeta (nvm cambia por directorio, sin conflicto). SEO con prerender de las rutas públicas y renderizado en cliente de las privadas, declarado por ruta con `RenderMode`; SSR en vivo (contenedor Node) solo cuando existan páginas públicas dinámicas. Internacionalización exactamente como en ChineseReads: Transloco con loader inline, inglés en raíz y español bajo `/es`, idioma derivado solo de la URL. Servicios de API escritos a mano y tipados.
**Por qué:** Angular 17 ya no recibe parches de seguridad; la configuración nativa de rutas evita los tres ficheros acoplados a mano del proyecto matriz (lista de prerender, script de limpieza, matcher de Caddy); la coherencia de idiomas entre ambas webs reduce errores y conocimiento duplicado.
**Se revisaría si:** una librería imprescindible no soportara Angular 22 (fallback: Angular 21 LTS con Node 22) o si la superficie de API superara ~30 endpoints (valorar generador OpenAPI).

## ADR-013 · Academy no refresca tokens (2026-10-02)
**Decisión:** Academy solo acepta la cookie `AuthToken` con `type = ACCESS`. Ignora `RefreshToken`. Cuando el token expira, responde 401 y el frontend redirige a chinesereads.com para iniciar sesión.
**Por qué:** en ChineseReads ambas cookies duran 7 días, así que el refresco no aporta nada; reimplementarlo duplicaría lógica de identidad fuera de su dueño (ADR-003).
**Se revisaría si:** ChineseReads acortara la vida del token de acceso.

## ADR-014 · Cimientos sin completar la fase 0 (2026-10-02)
**Decisión:** se construyen los cimientos técnicos (repo, CI, esqueletos, entorno local, primer corte vertical de grupos y asientos) sin haber completado la validación con 10 profesores (ADR-008). Las herramientas de IA siguen exigiendo una spec y una petición real de un profesor antes de escribirse.
**Por qué:** decisión consciente del dueño. Los cimientos son reutilizables pase lo que pase con la demanda; lo que se pospone es el coste de construir herramientas que nadie pidió.
**Se revisaría si:** tras la fase 0 ningún profesor mostrara interés: entonces se detiene antes de las herramientas.

## ADR-015 · Ingeniería de costes (2026-10-02)
**Decisión:** toda llamada de pago (IA, TTS, OCR) pasa por el módulo `aigen`, único autorizado a depender de esos clientes (verificado por el test de modularidad). Se reserva cuota antes de gastar; hay cuotas mensuales por profesor y un fusible global diario; el audio generado se cachea de forma persistente; cada llamada se contabiliza en `ai_usage` con coste estimado y hay presupuesto mensual con aviso. Los materiales se generan por profesor/grupo y se reutilizan para todos sus alumnos. Cero infraestructura nueva por defecto (sin dominio, sin servidor, sin contenedor Node, sin SaaS de pago).
**Por qué:** el coste debe escalar con quien paga (profesores), no con quien no paga (alumnos); el proyecto matriz ya demostró que límites configurables por entorno y reserva previa evitan sorpresas.
**Se revisaría si:** los márgenes reales medidos permitieran relajar cuotas, o si un proveedor cambiara su modelo de precios.

## ADR-016 · La frontera con ChineseReads es un contrato versionado (2026-10-02)
**Decisión:** `docs/integracion-chinesereads/CONTRACT.md` y sus ficheros adjuntos (fixture JWT, contrato SQL de `chinesereads.user`, mappings del endpoint interno) son la única fuente de verdad de la frontera. `contract-lock.json` guarda el hash de cada fichero del proyecto matriz del que depende Academy; `scripts/check-chinesereads-contract.sh` lo comprueba en cada PR y un flujo nocturno abre una issue si algo cambió. Cada cambio de frontera se anota en `CHANGELOG-frontera.md` como mensaje listo para llevar al otro repo. La PR de integración del proyecto matriz monta el espejo.
**Por qué:** dos repos y una sola persona: nada puede depender de recordar; una máquina avisa.
**Se revisaría si:** ambos proyectos pasaran a un único repositorio.

## Hipótesis abiertas (no son decisiones todavía)
- **Precio:** ver `precio-hipotesis.md`. Se valida en los emails de la fase 0.
- **Herramienta núcleo:** plan de estudios / plan por alumno + calendario generados y modificables por IA. Candidata a ser el esqueleto del modelo de datos (alumno → plan → sesión → materiales). Confirmar con los profesores que es lo que quieren.
- **Error tracking:** Sentry (SaaS, dato a tercero) frente a GlitchTip (autoalojado, RAM). Hasta decidir, `ErrorReporter` sin implementación.
- **Visibilidad del repositorio:** hoy público. Decidir si pasa a privado o lleva licencia restrictiva.
