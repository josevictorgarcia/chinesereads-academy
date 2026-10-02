# Atajos de desarrollo. Cada objetivo imprime lo que ejecuta.
SHELL := /bin/bash
.DEFAULT_GOAL := help
COMPOSE_DEV := docker compose -f docker/docker-compose.dev.yml --env-file docker/.env

.PHONY: help verify verify-backend verify-frontend check-contract check-no-ai-refs dev-up dev-up-ai dev-down dev-reset dev-logs dev-ps dev-cr-frontend

help: ## Lista los objetivos disponibles
	@grep -E '^[a-zA-Z_-]+:.*?## ' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-18s\033[0m %s\n", $$1, $$2}'

verify: check-no-ai-refs check-contract verify-backend verify-frontend ## Todo lo que exige una PR

verify-backend: ## Build + tests del backend (si existe)
	@if [ -f backend/pom.xml ]; then cd backend && ./mvnw -B -ntp verify; else echo "backend/ aún no existe"; fi

verify-frontend: ## Lint + build + tests del frontend (si existe)
	@if [ -f frontend/package.json ]; then cd frontend && npm run lint && npm run build && npm test -- --watch=false; else echo "frontend/ aún no existe"; fi

check-contract: ## Comprueba que la frontera con ChineseReads no ha cambiado (lock de hashes)
	@scripts/check-chinesereads-contract.sh --source auto

check-no-ai-refs: ## El repositorio no contiene referencias a herramientas de IA
	@scripts/check-no-ai-refs.sh

dev-up: ## Arranca MySQL + backend de ChineseReads + stub del endpoint interno (desde PR4)
	@test -f docker/.env || { echo "Falta docker/.env (copia docker/.env.example)"; exit 1; }
	$(COMPOSE_DEV) up -d --build

dev-up-ai: ## Igual que dev-up más ai-service y tts-service del matriz (necesitan credenciales)
	@test -f docker/.env || { echo "Falta docker/.env (copia docker/.env.example)"; exit 1; }
	$(COMPOSE_DEV) --profile ai up -d --build

dev-down: ## Para el entorno local (conserva la base de datos)
	$(COMPOSE_DEV) down

dev-reset: ## Para el entorno local y BORRA la base de datos local (vuelve a ejecutar los scripts de init)
	$(COMPOSE_DEV) down -v

dev-ps: ## Estado de los contenedores del entorno local
	$(COMPOSE_DEV) ps

dev-logs: ## Logs del entorno local
	$(COMPOSE_DEV) logs -f --tail=100

dev-cr-frontend: ## Arranca el frontend de ChineseReads (repo hermano) en :4200 para hacer login en local
	@cd "$${CHINESEREADS_REPO_PATH:-../2025-ChineseTexts}/frontend" && npm start
