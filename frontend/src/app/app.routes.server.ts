import { RenderMode, ServerRoute } from '@angular/ssr';

/**
 * Qué se renderiza dónde (ADR-012): las páginas públicas se prerenderizan en el build (SEO, sin
 * servidor Node en producción); las privadas se renderizan en el navegador (no hay nada que indexar
 * y no se expone contenido privado en HTML). `RenderMode.Server` queda para el día en que existan
 * páginas públicas con datos de base de datos.
 */
const PUBLIC = ['', 'para-profesores', 'precios', 'privacy-policy', 'terms-of-use'];

const prerendered: ServerRoute[] = PUBLIC.flatMap((p) => [
  { path: p, renderMode: RenderMode.Prerender },
  { path: p ? `es/${p}` : 'es', renderMode: RenderMode.Prerender },
]);

export const serverRoutes: ServerRoute[] = [
  ...prerendered,
  { path: 'teacher', renderMode: RenderMode.Client },
  { path: 'teacher/**', renderMode: RenderMode.Client },
  { path: 'student', renderMode: RenderMode.Client },
  { path: 'join', renderMode: RenderMode.Client },
  { path: 'es/teacher', renderMode: RenderMode.Client },
  { path: 'es/teacher/**', renderMode: RenderMode.Client },
  { path: 'es/student', renderMode: RenderMode.Client },
  { path: 'es/join', renderMode: RenderMode.Client },
  { path: '**', renderMode: RenderMode.Client },
];
