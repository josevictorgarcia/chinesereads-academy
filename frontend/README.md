# Academy frontend

Angular 22 (standalone, Vitest, ESLint + Prettier). Rutas públicas prerenderizadas, privadas en cliente (`src/app/app.routes.server.ts`). Idiomas como en ChineseReads: inglés en raíz, español bajo `/es`.

```bash
nvm use                 # Node 22 (.nvmrc)
npm ci
npm start               # http://localhost:4300, /api → backend en :8081
npm run lint && npm run build && npm test -- --watch=false
```

En macOS 13 exporta `NG_BUILD_SASS_EMBEDDED=0` antes de compilar (ver `docs/runbooks/dev-local.md`).
