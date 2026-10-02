/**
 * Helpers puros del esquema de idioma por prefijo de URL, idéntico al de ChineseReads:
 * inglés en la raíz (`/precios`), español bajo `/es` (`/es/precios`).
 * El idioma activo se deriva SOLO de la URL (nunca de navigator/localStorage), de modo que
 * servidor (prerender) y cliente coinciden y no hay desajustes de hidratación.
 */
export type Lang = 'en' | 'es';

export const LANGS: readonly Lang[] = ['en', 'es'];

export function langFromUrl(url: string): Lang {
  const path = url.split('?')[0].split('#')[0];
  return path === '/es' || path.startsWith('/es/') ? 'es' : 'en';
}

export function stripLangPrefix(url: string): string {
  if (url === '/es') return '/';
  if (url.startsWith('/es/')) return url.slice(3);
  if (url.startsWith('/es?') || url.startsWith('/es#')) return '/' + url.slice(3);
  return url;
}

export function addLangPrefix(path: string, lang: Lang): string {
  const p = path.startsWith('/') ? path : '/' + path;
  if (lang !== 'es') return p;
  if (p === '/') return '/es';
  if (p.startsWith('/?') || p.startsWith('/#')) return '/es' + p.slice(1);
  return '/es' + p;
}
