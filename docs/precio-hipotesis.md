# Precio anual — hipótesis de partida (2026-09-03)

**Pregunta del fundador:** ¿20 €/año está bien o es demasiado poco?
**Respuesta:** demasiado poco, por un orden de magnitud. Razones:

1. **Regala más de lo que cobra.** Con 10 alumnos incluidos, el valor de venta del acceso premium que reciben es 10 × 6,99 € × 12 = 838 €/año. Cobrar 20 € por eso convierte la línea B2B en un agujero: cualquier alumno que fuera a pagar 6,99 €/mes pasa a entrar gratis vía su profesor.
2. **No cubre costes variables.** Stripe se queda 0,25 € + 1,5 % (0,55 € = 2,75 %), e IA/TTS por uso. Con 20 € el margen para pagar la IA que el profesor consume es de céntimos.
3. **Está fuera de mercado.** 🟡 (referencias de memoria, no verificadas hoy) las herramientas para profesores rondan 8-12 €/mes (MagicSchool, Twee, Diffit) o 36-100 €/año (Quizlet/Kahoot planes docentes). Un profesor de italki cobra 15-25 €/hora: si Academy le ahorra una hora al mes, vale más de 150 €/año para él.
4. **El precio señala.** 20 €/año dice "hobby"; un profesor no organiza sus clases alrededor de un hobby.

## Hipótesis a validar (🔴 sin dato propio todavía)

| Plan | Precio | Incluye |
|---|---|---|
| Profesor mensual | 14,99 €/mes | herramientas IA + hasta 10 alumnos con acceso a ChineseReads |
| **Profesor anual** | **119 €/año** (≈ 9,90 €/mes, dos meses gratis) | igual |
| Asientos extra | +1 €/alumno/mes o pack de 10 por 79 €/año | escalable a academias sin cambiar el modelo |

Por qué esas cifras: 119 €/año con 10 alumnos = 0,99 €/alumno/mes, un descuento B2B razonable frente a 6,99 € retail sin regalar el producto; deja ~110 € netos tras Stripe para cubrir IA y ganar dinero; y 9,90 €/mes está en la franja de las herramientas docentes.

## Cómo validar sin código
En los 10 emails de la fase 0, cerrar con una pregunta de precio concreta: *"¿Pagarías 119 € al año por esto con tus 10 alumnos incluidos?"*. Tres respuestas posibles y su lectura: "sí" → mantener; "sí pero X" → el precio no es el problema; "no, pero pagaría Y" → tu dato real. Cambiar el precio en Stripe es un minuto; adivinarlo antes de preguntar, no.

## Regla de cannibalización
Limitar siempre los alumnos incluidos (10). Sin tope, cada profesor se convierte en un canal de acceso gratuito a ChineseReads.
