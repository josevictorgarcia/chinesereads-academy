# Spec: <nombre de la herramienta>

> Una página. Se escribe antes que el código. Nace de una petición concreta de un profesor (anotar quién y cuándo, sin datos personales).

## Problema que resuelve
Qué tarea del profesor ahorra o mejora, en una frase.

## Entrada
Campos exactos que el profesor rellena (nivel HSK, tema, nº de ítems, idioma de explicación, grupo destino...). Valores por defecto.

## Salida
Estructura exacta del resultado (JSON del backend y qué ve el profesor). Formato exportable (PDF, impresión, interactivo).

## Ejemplo real
Una entrada real y la salida esperada, completa.

## Criterio de "está bien"
Lista verificable: p. ej. "todas las palabras pertenecen al nivel indicado", "el pinyin lleva tonos", "ninguna frase supera 20 caracteres".

## Coste
Llamadas a IA/TTS por generación, tokens o caracteres estimados, coste estimado por uso, cuota que consume (ADR-015). Qué se cachea y reutiliza.

## Fuera de alcance
Lo que deliberadamente no hace en esta versión.
