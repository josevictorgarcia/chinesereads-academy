#!/usr/bin/env bash
# Prueba de fuego del entorno local (docs/runbooks/dev-local.md), sin navegador:
#   1. login REAL en el backend de ChineseReads (:8080) → cookie AuthToken
#   2. la misma cookie en Academy (:8081) → /api/me
#   3. alta de profesor → TRIAL → grupo → código
#   4. un segundo usuario se une con el código → asiento → rol STUDENT
#   5. el stub WireMock (:8089) ha recibido POST /api/internal/premium-grant
#   6. baja del alumno → DELETE al stub
#
# Uso: TEACHER_EMAIL=... TEACHER_PASSWORD=... STUDENT_EMAIL=... STUDENT_PASSWORD=... scripts/e2e-local.sh
# (las credenciales son las de los usuarios de desarrollo del repo matriz; nunca se escriben aquí)
set -euo pipefail

CR=${CR_URL:-http://localhost:8080}
AC=${ACADEMY_URL:-http://localhost:8081}
STUB=${STUB_URL:-http://localhost:8089}
: "${TEACHER_EMAIL:?}" "${TEACHER_PASSWORD:?}" "${STUDENT_EMAIL:?}" "${STUDENT_PASSWORD:?}"
TMP=$(mktemp -d); trap 'rm -rf "$TMP"' EXIT
PASS=0; FAIL=0
ok()   { echo "  ✓ $*"; PASS=$((PASS+1)); }
fail() { echo "  ✗ $*"; FAIL=$((FAIL+1)); }
json() { python3 -c "import sys,json; d=json.load(sys.stdin); print(eval('d'+sys.argv[1]))" "$1"; }

login() { # $1 email, $2 password, $3 cookiejar
  local code
  code=$(curl -s -o /dev/null -w '%{http_code}' -c "$3" -H 'Content-Type: application/json' \
    -d "{\"username\":\"$1\",\"password\":\"$2\"}" "$CR/api/auth/login")
  [ "$code" = "200" ] && grep -q AuthToken "$3"
}
ac() { # $1 cookiejar, $2 method, $3 path, [$4 body]
  curl -s -b "$1" -X "$2" -H 'Content-Type: application/json' -H 'X-Requested-With: XMLHttpRequest' \
    ${4:+-d "$4"} -w '\n%{http_code}' "$AC$3"
}
status() { tail -n1; }
body()   { sed '$d'; }

echo "1. Login en ChineseReads ($CR)"
login "$TEACHER_EMAIL" "$TEACHER_PASSWORD" "$TMP/t.jar" && ok "profesor autenticado, cookie AuthToken recibida" || { fail "login profesor"; exit 1; }
login "$STUDENT_EMAIL" "$STUDENT_PASSWORD" "$TMP/s.jar" && ok "alumno autenticado" || { fail "login alumno"; exit 1; }

echo "2. Sesión compartida en Academy ($AC)"
R=$(ac "$TMP/t.jar" GET /api/me); [ "$(echo "$R" | status)" = 200 ] && ok "GET /api/me = 200: $(echo "$R" | body | json "['email']")" || fail "GET /api/me → $(echo "$R" | status)"
[ "$(curl -s -o /dev/null -w '%{http_code}' "$AC/api/me")" = 401 ] && ok "sin cookie → 401" || fail "sin cookie no da 401"

echo "3. Profesor: alta, trial, grupo, código"
R=$(ac "$TMP/t.jar" POST /api/teachers/me '{"displayName":"Profe E2E"}'); S=$(echo "$R" | status)
{ [ "$S" = 201 ] || [ "$S" = 409 ]; } && ok "alta de profesor ($S)" || fail "alta de profesor → $S"
for i in 1 2 3 4 5 6 7 8 9 10; do R=$(ac "$TMP/t.jar" GET /api/teacher/seats); T=$(echo "$R" | body | json "['total']" 2>/dev/null || echo 0); [ "$T" -gt 0 ] && break; sleep 1; done
[ "${T:-0}" -gt 0 ] && ok "TRIAL creado por evento: $T asientos" || fail "sin suscripción TRIAL"
R=$(ac "$TMP/t.jar" POST /api/teacher/groups '{"name":"Grupo E2E","level":"HSK1"}'); GID=$(echo "$R" | body | json "['id']")
[ "$(echo "$R" | status)" = 201 ] && ok "grupo creado id=$GID" || fail "crear grupo"
R=$(ac "$TMP/t.jar" POST "/api/teacher/groups/$GID/invite"); CODE=$(echo "$R" | body | json "['code']")
[ "$(echo "$R" | status)" = 201 ] && ok "código de invitación $CODE" || fail "crear código"

echo "4. Alumno se une"
R=$(ac "$TMP/s.jar" POST /api/student/join "{\"code\":\"$CODE\"}"); [ "$(echo "$R" | status)" = 200 ] && ok "join → $(echo "$R" | body | json "['groupName']")" || fail "join → $(echo "$R" | status) $(echo "$R" | body)"
R=$(ac "$TMP/s.jar" GET /api/me); echo "$R" | body | grep -q STUDENT && ok "rol STUDENT en /api/me" || fail "sin rol STUDENT"
R=$(ac "$TMP/t.jar" GET "/api/teacher/groups/$GID"); EID=$(echo "$R" | body | json "['members'][0]['enrollmentId']"); ok "el profesor ve al alumno (enrollment $EID)"

echo "5. Stub del endpoint interno ($STUB)"
sleep 2
N=$(curl -s "$STUB/__admin/requests" | python3 -c "import sys,json; r=json.load(sys.stdin)['requests']; print(sum(1 for x in r if x['request']['method']=='POST' and 'premium-grant' in x['request']['url']))")
[ "$N" -ge 1 ] && ok "ChineseReads (stub) recibió $N POST /api/internal/premium-grant" || fail "el stub no recibió la concesión de premium"

echo "6. Baja del alumno"
R=$(ac "$TMP/t.jar" DELETE "/api/teacher/enrollments/$EID"); [ "$(echo "$R" | status)" = 204 ] && ok "baja → 204" || fail "baja → $(echo "$R" | status)"
sleep 2
D=$(curl -s "$STUB/__admin/requests" | python3 -c "import sys,json; r=json.load(sys.stdin)['requests']; print(sum(1 for x in r if x['request']['method']=='DELETE' and 'premium-grant' in x['request']['url']))")
[ "$D" -ge 1 ] && ok "stub recibió $D DELETE /api/internal/premium-grant" || fail "el stub no recibió la retirada"
R=$(ac "$TMP/s.jar" GET /api/me); echo "$R" | body | grep -q STUDENT && fail "el alumno conserva el rol STUDENT" || ok "el alumno ya no es STUDENT"

echo; echo "Resultado: $PASS correctas, $FAIL fallidas"; [ $FAIL -eq 0 ]
