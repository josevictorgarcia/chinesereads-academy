#!/usr/bin/env bash
# El repositorio versionado no debe contener referencias a herramientas de asistencia ni coautorías.
# Revisa los ficheros rastreados por git y los mensajes de commit de la rama actual que no estén en main.
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
PATTERN='[Cc]laude|[Aa]nthropic|[Cc]o-[Aa]uthored-[Bb]y|Generated with'
STATUS=0
echo "Revisando ficheros versionados…"
if git grep -n -I -E "$PATTERN" -- . ':(exclude)scripts/check-no-ai-refs.sh' ; then
  echo "✗ Referencias encontradas en ficheros versionados."; STATUS=1
else
  echo "✓ Ficheros limpios."
fi
echo "Revisando mensajes de commit no presentes en main…"
if git rev-parse --verify -q origin/main >/dev/null 2>&1; then RANGE="origin/main..HEAD"; elif git rev-parse --verify -q main >/dev/null 2>&1; then RANGE="main..HEAD"; else RANGE="HEAD"; fi
if git log --format='%H %an <%ae>%n%B' "$RANGE" 2>/dev/null | grep -n -E "$PATTERN"; then
  echo "✗ Referencias encontradas en mensajes o autoría de commits."; STATUS=1
else
  echo "✓ Commits limpios."
fi
exit $STATUS
