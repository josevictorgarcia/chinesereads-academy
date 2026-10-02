# ChineseReads Academy — valoración honesta y arquitectura recomendada

> Esto es una valoración estratégica, no un plan de código. No se ejecuta nada: no vamos a empezar ahora.

## Contexto

Conseguir suscriptores individuales cuesta: 376 visitantes/mes, ~24 registros, 0 pagos. La idea es abrir una línea B2B: el **profesor** compra una suscripción anual, sus alumnos obtienen acceso a ChineseReads y a una nueva web para clases ("Academy") con herramientas de IA más potentes, un certificado PDF y presencia en el Hall of Fame. Proyecto y repositorio aparte para no engordar el TFG, pero con base de datos común y acceso bidireccional (un usuario existe en ambas webs). Se quiere empezar poco a poco antes de la defensa.

## Veredicto en una frase

**La dirección es correcta y es realista para una persona, pero el orden importa más que la arquitectura: valida con 10 profesores antes de escribir la primera línea, y constrúyelo como un segundo monolito modular en un subdominio, nunca como microservicios.**

---

## 1. Las tres respuestas directas

### ¿Cuesta dinero `chinesereads.academy`?
Sí. **`.academy` no es un subdominio, es un dominio nuevo** (un TLD distinto). Precio verificado hoy: ~12 $ el primer año en oferta, **~38 $/año la renovación** (Porkbun). Un **subdominio** real, `academy.chinesereads.com`, cuesta **0 €** (un registro DNS).

Y la diferencia no es solo el precio. La cookie JWT de ChineseReads se emite sin atributo `Domain` (`UserLoginService.buildTokenCookie`, `backend/.../Security/jwt/UserLoginService.java:106`), es decir, solo vale para `chinesereads.com`:
- Con **`academy.chinesereads.com`**: añadir `Domain=.chinesereads.com` a esa cookie (una línea) y la sesión se comparte. El alumno entra en ChineseReads y ya está dentro de Academy. Exactamente el "bidireccional" que pides.
- Con **`chinesereads.academy`**: los navegadores **no comparten cookies entre dominios distintos**. Harías falta un flujo de traspaso de token tipo OAuth (redirección, código temporal, cookie propia en el otro dominio). Es una semana de trabajo delicado en seguridad y una fuente permanente de bugs de sesión.

**Recomendación:** `academy.chinesereads.com` para el producto. Si quieres la marca `.academy` para marketing, compra el dominio (38 $/año) y haz que **redirija** al subdominio. Tienes lo mejor de ambos.

### ¿Microservicios para Academy?
**No.** Los motivos que te di el 06-08 para ChineseReads se aplican igual: microservicios resuelven un problema de organización (muchos equipos tocando un repo) que no tienes. Para un desarrollador solo, cada servicio extra es un despliegue, un log, una red y un modo de fallo más. Lo que sí tiene sentido y ya hace ChineseReads: **extraer solo lo que tiene un perfil de coste o fallo distinto** (IA, OCR, TTS ya son servicios). Academy reutilizará esos mismos tres servicios; no necesita duplicarlos.

### ¿Migrar ChineseReads a microservicios?
**No, y con más razón ahora.** Sería semanas de trabajo que no mueven ninguna métrica, sobre un producto con 0 pagos, justo antes de defenderlo. Ante un tribunal, "monolito modular con los servicios de IA extraídos por una razón concreta" se defiende mejor que "microservicios para 25 usuarios". Lo que ChineseReads sí necesita para Academy es pequeño: exponer un par de endpoints internos (ver §3).

---

## 2. Qué debe pasar ANTES de empezar (fase 0, sin código, 2-3 semanas)

Tu respuesta a "¿qué demanda tienes?" fue: ninguna todavía. Es el único dato que decide si esto merece meses de trabajo. El test sin código que ya diseñamos sigue siendo la vía:

1. **10 emails a profesores de chino** de italki/Preply que enseñen en español, cada uno con un texto generado a su nivel y tema (una URL pública de ChineseReads) y la oferta "Cuenta Profesor: un mes gratis, luego 14,99 €/mes".
2. Añade **una pregunta** al final: *"Si existiera una versión para tus clases con fichas y dictados generados por IA, ¿qué es lo primero que querrías que hiciera?"*. Su respuesta ordena el roadmap de Academy mejor que cualquier lista nuestra.
3. **Criterio de decisión pactado de antemano:**
   - 0 respuestas de 10 → problema de mensaje o canal; repite con otro texto antes de concluir nada.
   - Prueban pero nadie paga → problema de valor; Academy tal como está pensada no lo arregla.
   - **≥ 2 dicen que pagarían o piden funciones concretas → luz verde para la fase 1.**

Sin este paso, Academy sería el segundo producto construido a ciegas mientras el primero sigue en 0 pagos. Es el mayor riesgo de toda la idea y no es técnico.

---

## 3. Arquitectura recomendada (si la fase 0 da luz verde)

### Principio: identidad y cobro viven en ChineseReads; las clases viven en Academy

```
                 academy.chinesereads.com            chinesereads.com
                ┌──────────────────────────┐    ┌──────────────────────────┐
  navegador ──► │ academy-frontend (Angular)│    │ frontend + frontend-ssr  │
   (misma       │ academy-backend (Spring)  │    │ backend (Spring)         │
    cookie)     │  · clases, asientos,      │──► │  · users, premiumUntil,  │
                │    tareas, certificados   │API │    Stripe, Hall of Fame  │
                └───────────┬──────────────┘    └───────────┬──────────────┘
                            │ verifica el MISMO JWT          │
                            │ (JWT_SECRET compartido)        │
                            ▼                                ▼
                     ┌──────────────── MySQL (una instancia) ────────────────┐
                     │ esquema chinesereads (dueño: backend)                 │
                     │ esquema academy      (dueño: academy-backend)         │
                     └───────────────────────────────────────────────────────┘
                        ai-service · tts-service · ocr-service (compartidos)
```

- **Repositorio aparte** (`chinesereads-academy`): frontend Angular + backend Spring Boot + su propio `docker-compose` que se une a la red `app-network` existente. Cumple tu objetivo de no engordar el TFG.
- **Base de datos común, tablas separadas.** Una sola instancia MySQL, dos esquemas. Academy **nunca escribe** en las tablas de ChineseReads; solo lee `users` por id y, para cambiar algo (dar premium a un alumno), llama a un **endpoint interno** de ChineseReads protegido por un secreto de servicio. Así "existir en ambas" es automático (misma tabla de usuarios) y nadie rompe a nadie.
- **Sesión compartida:** la cookie JWT pasa a `Domain=.chinesereads.com`; `academy-backend` reutiliza la clase de validación JWT con el mismo `JWT_SECRET`. Cero login doble.
- **Caddy** añade un bloque `academy.chinesereads.com` que enruta a los dos contenedores nuevos. Let's Encrypt emite el certificado solo.
- **El acceso de los alumnos a ChineseReads reutiliza `premiumUntil`.** Un "asiento" activo de un profesor = ChineseReads pone `premiumUntil` al alumno hasta el fin de la suscripción del profesor. No hay que inventar un rol nuevo ni tocar los guards de cuotas: toda la maquinaria de premium ya existe y está probada.
- **Cobro:** un tercer precio en Stripe (anual, profesor) por el mismo `StripeService`; el webhook ya sabe fijar `premiumUntil`. Lo nuevo es el concepto de asientos (`class`, `seat`) en el esquema de Academy.

### Cambios mínimos en ChineseReads (una PR pequeña, cuando toque)
1. `Domain=.chinesereads.com` en `buildTokenCookie` / `removeTokenCookie`.
2. Endpoint interno `POST /api/internal/premium-grant` (secreto de servicio, no expuesto por Caddy al exterior) que Academy usa al activar/cancelar un asiento.
3. Un badge/campo "Profesor certificado" en el Hall of Fame (ya es admin-editable; puede ser manual al principio).
4. Enlace "Academy" en la cabecera.

### Servidor
La Hetzner de 8 GB ya corre 7 contenedores. Dos más (otro Spring Boot ~500-700 MB + un Node/Caddy estático) **probablemente caben**, pero hay que medir `free -m` y `docker stats` antes; si no, un segundo servidor pequeño (~5-10 €/mes) y MySQL se queda donde está. Coste incremental esperado de Academy: **0-10 €/mes de servidor + 38 $/año si quieres el `.academy` + coste de IA por uso** (DeepSeek/TTS, hoy pagado por consumo y protegido por cuotas).

---

## 4. Las herramientas de IA, ordenadas por coste real

| Herramienta | Dificultad | Qué reutiliza | Nota honesta |
|---|---|---|---|
| **Fichas / apuntes / ejercicios por tema y nivel** | Baja | `AiService.generateFullText`, mismo patrón de prompts | Es el núcleo del valor B2B (ahorra horas de preparación). **Primera a construir.** |
| **Dictados de pinyin con audio** | Baja | `tts-service`, el mismo WaveNet | Generar frases por nivel + audio + hueco de respuesta + corrección automática. **Segunda.** |
| **Certificado PDF "profesor certificado"** | Trivial | Una plantilla HTML → PDF | Cuidado con la palabra "certificado": no es una acreditación oficial; llámalo "Profesor colaborador de ChineseReads" o similar para no prometer lo que no es. |
| **Orden de trazos** | Media-baja | Librería **Hanzi Writer** (JS, 10 kB, 9.000+ caracteres, animación + quiz) | Gratis y probada. 🟡 Verificar la licencia de sus datos de trazos (proyecto Make Me a Hanzi) antes de publicar. |
| **Letra de canciones sincronizada con pinyin y traducción** | **Alta + legal** | Nada reutilizable; necesitaría ASR y alineación temporal | **Las letras tienen copyright.** Mostrarlas requiere licencia (los servicios tipo Musixmatch la pagan). Reproducir la canción, otra licencia más. Es la herramienta más vistosa y la única que puede traer un problema legal. **Déjala para el final** y, si se hace, solo con canciones de dominio público, infantiles tradicionales o letras subidas por el propio profesor bajo su responsabilidad. |

Regla que propongo: **cada herramienta nace de una petición de un profesor real** (fase 0), no de nuestra lista. La lista de arriba es el orden por defecto si nadie pide otra cosa.

---

## 5. Un riesgo que no habías mencionado: menores

Si los alumnos de un profesor son **menores de 14 años**, el consentimiento GDPR lo dan los padres, y si el cliente es un colegio o academia, te pedirán un **contrato de encargado de tratamiento**. ChineseReads hoy está pensada para adultos. Decidir desde el principio "Academy es para alumnos de 16+ / adultos" (y decirlo en los términos) evita construir todo un aparato legal. Si el segmento son profesores particulares de italki, sus alumnos son mayoritariamente adultos y el problema desaparece.

---

## 6. "Metodología con IA más seria": qué cambiar respecto a ChineseReads

Lo que ya haces bien (una rama por feature, PR, nunca romper lo existente, verificación con build y tests) se mantiene. Lo que añadiría desde el día 1 del nuevo repo:

1. **CI en GitHub Actions desde el primer commit** (build + tests en cada PR). Es además el objetivo T11 pendiente del TFG: puedes estrenarlo aquí y luego llevarlo a ChineseReads.
2. **Un documento de decisiones (ADR) corto por cada decisión de arquitectura** — "subdominio y no dominio nuevo, porque la cookie", "BD compartida con esquemas separados, porque..." — de 10 líneas cada uno. Es lo que mejor sobrevive al paso del tiempo y lo que un tribunal o un futuro colaborador agradece.
3. **Plan escrito antes del código en cada feature** y una **especificación de una página** por herramienta de IA: entrada, salida, ejemplo real, criterio de "está bien".
4. **Guía de desarrollo del repo** (`docs/guia-desarrollo.md`) con las reglas del proyecto (secretos, autoría, cómo verificar) para que cualquier trabajo arranque con el contexto correcto.
5. **Tests de contrato** para la frontera con ChineseReads (el JWT compartido y el endpoint interno): si uno de los dos cambia, el test avisa.

---

## 7. Roadmap propuesto (sin presión, antes de la defensa)

| Fase | Qué | Código | Señal para pasar a la siguiente |
|---|---|---|---|
| **0. Validar** | 10 emails a profesores con la Cuenta Profesor + la pregunta abierta | Ninguno | ≥ 2 profesores dispuestos a pagar o pidiendo funciones concretas |
| **1. Cimientos** | Repo nuevo, CI, subdominio en Caddy, cookie compartida, esquema `academy` (clase, asiento), precio anual en Stripe, panel del profesor con código de invitación, `premiumUntil` a los alumnos | ChineseReads: PR pequeña (§3). Academy: esqueleto | Un profesor real crea una clase y un alumno entra en ambas webs |
| **2. Primera herramienta** | Generador de fichas/ejercicios por tema y nivel, exportable a PDF | Academy | El profesor la usa dos semanas seguidas sin que se lo pidas |
| **3. Segunda herramienta** | Dictados con audio y autocorrección | Academy + `tts-service` | Idem |
| **4. Certificado + Hall of Fame** | PDF y badge de profesor | Trivial | — |
| **5. Trazos** | Hanzi Writer integrado en fichas y dictados | Academy | — |
| **6. Canciones** | Solo si hay licencias o contenido propio | Academy | Decisión legal previa |

Cada fase es independiente y desplegable; si en cualquier punto la señal no llega, se para sin haber perdido lo anterior.

---

## 8. Lo que te pido decidir tú (no ahora)

1. **¿Subdominio `academy.chinesereads.com` (recomendado) o dominio `chinesereads.academy` con redirección?**
2. **¿Alumnos solo adultos / 16+?** Cambia todo el aparato legal.
3. **¿Qué nombre dar al "certificado"** para no prometer una acreditación oficial?
4. **¿Cuándo envías los 10 emails?** Es el único paso que decide si todo lo demás merece hacerse.

## Verificación de esta valoración

- Cookie sin `Domain`: `backend/src/main/java/com/chinesereads/backend/Security/jwt/UserLoginService.java:106-112` (leído hoy).
- Precio `.academy`: porkbun.com/tld/academy (consultado hoy: 11,84 $ primer año, 37,59 $ renovación).
- Hanzi Writer: hanziwriter.org (consultado hoy: open source, 9.000+ caracteres, 10 kB gzip; datos de Make Me a Hanzi).
- Tarifas PayPal/Stripe y el resto de cifras de negocio: sesión de hoy y memoria del proyecto.

## Correcciones posteriores (02-10-2026)
- La cookie de acceso de ChineseReads se llama `AuthToken` (no `AccessToken`); la de refresco, `RefreshToken`. Ambas duran 7 días.
- La tabla de usuarios es `chinesereads.user` (singular), con roles en `user_roles`.
- El SSR híbrido de Academy se hace con la configuración nativa de rutas de Angular (`RenderMode`), no con la lista manual de prerender + script + matcher de Caddy del proyecto matriz (ADR-012).
- La fase 0 no se completó antes de empezar los cimientos; se registra como decisión consciente en ADR-014.
