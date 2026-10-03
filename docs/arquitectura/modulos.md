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
| `billing` | Suscripción del profesor (TRIAL/MANUAL/STRIPE), cuota de asientos, webhook idempotente | identity, shared | `teacher_subscription`, `stripe_event` |
| `classroom` | Grupos, altas de alumnos (asientos), miembros, códigos de invitación; publica `SeatActivated`/`SeatDeactivated` | identity, billing (solo cuota), shared | `study_group`, `enrollment`, `group_member`, `invite_code` |
| `access` | Frontera saliente con ChineseReads (concesión de premium por asiento), auditoría; tests de contrato | classroom, billing, identity, shared | `premium_grant` |
| `aigen` | Único adaptador a los servicios de IA/TTS/OCR; cuotas, fusible, caché de audio, contabilidad | identity, shared | `ai_usage`, `audio_cache` |
| `studyplan` | Plan de estudios → sesión → materiales | identity, classroom, aigen, shared | (futuro) |
| `materials` | Fichas, hojas de caracteres, exportación PDF | identity, aigen, studyplan, shared | (futuro) |
| `exercises` | Registro de tipos de ejercicio e intentos | identity, classroom, aigen, shared | (futuro) |

Grafo sin ciclos: `shared ← identity ← billing ← classroom ← access`. `billing` nunca conoce `classroom`: recibe hechos por evento a través de `access`.

Los diagramas generados por el test de modularidad se copian a esta carpeta en cada PR que toque módulos.

## Diagramas generados
`docs/arquitectura/generado/*.puml` los produce `ModularityTest` (Spring Modulith `Documenter`) en cada build; se copian aquí en las PR que tocan módulos. Se visualizan con cualquier renderizador PlantUML (p. ej. la extensión de VS Code o plantuml.com).
