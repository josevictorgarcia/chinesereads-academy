# Módulos del backend

Paquete raíz `com.chinesereads.academy`; un subpaquete por módulo (Spring Modulith). Un módulo solo usa de otro lo que está en su paquete raíz (API); lo que está en `internal/` es privado y el test de modularidad falla si alguien lo toca.

```
<modulo>/
├── package-info.java      @ApplicationModule(allowedDependencies = {...})
├── <API pública>          servicios de aplicación, DTOs (records), eventos
├── internal/              entidades JPA, repositorios, adaptadores
└── web/                   controladores REST del módulo
```

| Módulo | Responsabilidad | Depende de | Tablas |
|---|---|---|---|
| `shared` (abierto) | ProblemDetail + manejador global de errores, `ErrorCode`, filtro de correlación, propiedades validadas, `Clock`, cliente HTTP base | — | `event_publication` |
| `identity` | Verificación del JWT compartido, vista de solo lectura de `chinesereads.user`, usuario actual, perfil de profesor, SPI `RoleContributor` | shared | `teacher_profile` |
| `billing` | Suscripción del profesor: TRIAL automático al alta (listener de `TeacherRegistered`, 10 asientos, 30 días), cuota de asientos (`SeatQuotaService`); Stripe y webhook idempotente en un plan posterior | identity, shared | `teacher_subscription`, `stripe_event` |
| `classroom` | Grupos, altas de alumnos (asiento = alumno↔profesor, uno por profesor aunque esté en varios grupos), miembros, códigos de invitación (8 caracteres, 30 días); publica `SeatActivated`/`SeatDeactivated`; aporta el rol STUDENT | identity, billing (solo cuota), shared | `study_group`, `enrollment`, `group_member`, `invite_code` |
| `access` | Frontera saliente con ChineseReads (concesión/retirada de premium por asiento): puerto `PremiumAccessPort`, cliente HTTP real (o no-op en tests), listener de `SeatActivated`/`SeatDeactivated`, cola de reintentos cada 5 min; tests de contrato contra los mappings WireMock compartidos | classroom, billing, identity, shared | `premium_grant` (action, status PENDING/DONE/FAILED, attempts) |
| `aigen` | Único adaptador a los servicios de IA/TTS/OCR; cuotas, fusible, caché de audio, contabilidad | identity, shared | `ai_usage`, `audio_cache` |
| `studyplan` | Plan de estudios → sesión → materiales | identity, classroom, aigen, shared | (futuro) |
| `materials` | Fichas, hojas de caracteres, exportación PDF | identity, aigen, studyplan, shared | (futuro) |
| `exercises` | Registro de tipos de ejercicio e intentos | identity, classroom, aigen, shared | (futuro) |

Grafo sin ciclos: `shared ← identity ← billing ← classroom ← access`. `billing` nunca conoce `classroom`: recibe hechos por evento a través de `access`.

Los diagramas generados por el test de modularidad se copian a esta carpeta en cada PR que toque módulos.

## Diagramas generados
`docs/arquitectura/generado/*.puml` los produce `ModularityTest` (Spring Modulith `Documenter`) en cada build; se copian aquí en las PR que tocan módulos. Se visualizan con cualquier renderizador PlantUML (p. ej. la extensión de VS Code o plantuml.com).

## Flujo del corte vertical (PR5)
1. Usuario de ChineseReads → `POST /api/teachers/me` → `teacher_profile` + evento `TeacherRegistered` → billing crea `teacher_subscription` TRIAL.
2. Profesor → `POST /api/teacher/groups` → `POST /api/teacher/groups/{id}/invite` → código.
3. Alumno → `POST /api/student/join {code}` → cuota (`SeatQuotaService`) → `enrollment` ACTIVE + `group_member` → evento `SeatActivated(premiumUntil = fin del periodo)`.
4. access → fila `premium_grant` GRANT → `POST /api/internal/premium-grant` en ChineseReads (stub en local) → DONE o FAILED (reintentos).
5. Profesor → `DELETE /api/teacher/enrollments/{id}` → `SeatDeactivated` → `premium_grant` REVOKE → `DELETE /api/internal/premium-grant/{userId}`.
