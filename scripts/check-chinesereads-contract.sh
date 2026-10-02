#!/usr/bin/env bash
# Comprueba que los ficheros del repositorio matriz (ChineseReads) de los que depende Academy
# no han cambiado desde la última revisión del contrato de frontera.
#
#   scripts/check-chinesereads-contract.sh [--source auto|local|github] [--update]
#
# --source auto    (defecto) usa el clon local si CHINESEREADS_REPO_PATH (o ../2025-ChineseTexts) existe; si no, GitHub.
# --source local   solo clon local.
# --source github  descarga cada fichero en crudo del repositorio público, rama main.
# --update         reescribe contract-lock.json con los hashes actuales (tras revisar el contrato).
#
# Salida: 0 si todo coincide; 1 si algún fichero cambió o no se pudo leer; imprime la lista.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOCK="$ROOT/docs/integracion-chinesereads/contract-lock.json"
SOURCE="auto"
UPDATE=0
for arg in "$@"; do
  case "$arg" in
    --source) ;;
    auto|local|github) SOURCE="$arg" ;;
    --source=*) SOURCE="${arg#--source=}" ;;
    --update) UPDATE=1 ;;
    -h|--help) sed -n '2,14p' "$0"; exit 0 ;;
    *) echo "argumento desconocido: $arg" >&2; exit 2 ;;
  esac
done

command -v python3 >/dev/null || { echo "python3 es necesario" >&2; exit 2; }

REPO_SLUG="$(python3 -c "import json;print(json.load(open('$LOCK'))['repository'])")"
BRANCH="$(python3 -c "import json;print(json.load(open('$LOCK'))['branch'])")"
LOCAL_PATH="${CHINESEREADS_REPO_PATH:-$ROOT/../2025-ChineseTexts}"

if [ "$SOURCE" = "auto" ]; then
  if [ -d "$LOCAL_PATH/.git" ]; then SOURCE="local"; else SOURCE="github"; fi
fi
if [ "$SOURCE" = "local" ] && [ ! -d "$LOCAL_PATH/.git" ]; then
  echo "No existe el clon local en $LOCAL_PATH (define CHINESEREADS_REPO_PATH)" >&2; exit 2
fi
echo "Fuente: $SOURCE ($( [ "$SOURCE" = local ] && echo "$LOCAL_PATH" || echo "https://github.com/$REPO_SLUG@$BRANCH" ))"

sha_of() {  # $1 = ruta relativa en el repo matriz → sha256 del contenido (vacío si no se puede leer)
  local rel="$1"
  if [ "$SOURCE" = "local" ]; then
    [ -f "$LOCAL_PATH/$rel" ] && shasum -a 256 "$LOCAL_PATH/$rel" | cut -d' ' -f1 || true
  else
    local tmp; tmp="$(mktemp)"
    local code; code="$(curl -sS -L -o "$tmp" -w '%{http_code}' "https://raw.githubusercontent.com/$REPO_SLUG/$BRANCH/$rel" || echo 000)"
    if [ "$code" = "200" ]; then shasum -a 256 "$tmp" | cut -d' ' -f1; fi
    rm -f "$tmp"
  fi
}

STATUS=0
RESULT_JSON="$(mktemp)"
echo "[" > "$RESULT_JSON"
first=1
while IFS=$'\t' read -r rel expected why; do
  actual="$(sha_of "$rel")"
  if [ -z "$actual" ]; then
    echo "  ✗ NO LEGIBLE  $rel"; STATUS=1
  elif [ "$actual" != "$expected" ]; then
    echo "  ✗ CAMBIÓ      $rel"; echo "      motivo de vigilancia: $why"; STATUS=1
  else
    echo "  ✓ igual       $rel"
  fi
  [ $first -eq 1 ] || echo "," >> "$RESULT_JSON"; first=0
  printf '  {"path": "%s", "sha256": "%s", "why": "%s"}' "$rel" "${actual:-$expected}" "$why" >> "$RESULT_JSON"
done < <(python3 -c "
import json
for f in json.load(open('$LOCK'))['files']:
    print(f['path'], f['sha256'], f['why'], sep='\t')
")
echo "]" >> "$RESULT_JSON"

if [ $UPDATE -eq 1 ]; then
  python3 - "$LOCK" "$RESULT_JSON" <<'PY'
import json, sys, datetime
lock_path, result_path = sys.argv[1], sys.argv[2]
lock = json.load(open(lock_path))
lock['files'] = json.load(open(result_path))
lock['reviewedOn'] = datetime.date.today().isoformat()
json.dump(lock, open(lock_path, 'w'), indent=2, ensure_ascii=False)
open(lock_path, 'a').write('\n')
print(f"Lock actualizado: {lock_path} (reviewedOn={lock['reviewedOn']}). Anota el cambio en CHANGELOG-frontera.md.")
PY
  STATUS=0
fi
rm -f "$RESULT_JSON"

if [ $STATUS -ne 0 ]; then
  cat <<MSG

La frontera con ChineseReads ha cambiado. Pasos:
  1. Lee el diff del fichero en el repositorio matriz y compáralo con docs/integracion-chinesereads/CONTRACT.md.
  2. Si el contrato sigue siendo válido, regenera el lock:  scripts/check-chinesereads-contract.sh --source $SOURCE --update
  3. Si no, actualiza CONTRACT.md (sube la versión) y el código afectado, y anota la entrada en CHANGELOG-frontera.md.
MSG
fi
exit $STATUS
