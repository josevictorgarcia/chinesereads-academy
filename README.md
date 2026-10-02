# ChineseReads Academy

Herramientas para profesores particulares y academias de chino mandarín, integradas con [ChineseReads](https://chinesereads.com): el profesor organiza a sus alumnos, genera materiales (planes de estudio, fichas, dictados, ejercicios) y sus alumnos obtienen acceso a ChineseReads y a esta web.

> Estado: en construcción. No desplegado.

## Arquitectura en una frase
Un backend Spring Boot (monolito modular) y un frontend Angular, con su propio esquema MySQL `academy` en la misma instancia que ChineseReads. La identidad (sesión compartida por cookie) y el cobro del acceso de los alumnos viven en ChineseReads; las clases, los asientos y las herramientas viven aquí. Detalle en `docs/decisiones.md` y `docs/integracion-chinesereads.md`.

## Documentación
| Documento | Contenido |
|---|---|
| `docs/guia-desarrollo.md` | Reglas del proyecto, metodología y cómo verificar antes de una PR |
| `docs/decisiones.md` | Registro de decisiones de arquitectura (ADR) |
| `docs/valoracion.md` | Valoración estratégica y hoja de ruta |
| `docs/precio-hipotesis.md` | Hipótesis de precio y regla de canibalización |
| `docs/integracion-chinesereads.md` | Hechos verificados sobre la frontera con ChineseReads |
| `docs/integracion-chinesereads/` | Contrato versionado de la frontera, lock de hashes y changelog |
| `docs/arquitectura/` | Módulos, dependencias permitidas y diagramas |
| `docs/specs/` | Una especificación de una página por herramienta de IA |
| `docs/runbooks/` | Procedimientos: entorno local, costes, despliegue |

## Arrancar en local
Ver `docs/runbooks/dev-local.md`. Resumen: `make help`.

## Verificación antes de cualquier PR
```bash
make verify
```
