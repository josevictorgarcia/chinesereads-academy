import { DOCUMENT, inject, Injectable } from '@angular/core';
import { Meta, Title } from '@angular/platform-browser';
import { addLangPrefix, Lang } from '../../i18n/locale.util';

/** Metadatos SEO de una página. `path` es siempre la ruta sin prefijo (inglés). */
export interface SeoConfig {
  title: string;
  description: string;
  keywords?: string;
  path?: string;
  image?: string;
  noindex?: boolean;
}

export const SITE_URL = 'https://academy.chinesereads.com';
const SITE_NAME = 'ChineseReads Academy';
const PAGE_JSONLD_ID = 'seo-page-jsonld';
const DEFAULT_KEYWORDS =
  'chinese teacher tools, teach mandarin, chinese class materials, HSK worksheets, ' +
  'chinese study plan generator, chinese dictation, profesor de chino, materiales clase de chino';
const OG_LOCALE: Record<Lang, string> = { en: 'en_US', es: 'es_ES' };

/**
 * Centraliza title, description, canonical, Open Graph, Twitter, hreflang y `<html lang>`.
 * Solo usa Title/Meta/DOCUMENT, que funcionan en prerender: los rastreadores reciben las
 * etiquetas correctas por ruta e idioma en el HTML inicial. Portado del proyecto matriz.
 */
@Injectable({ providedIn: 'root' })
export class SeoService {
  private readonly titleService = inject(Title);
  private readonly meta = inject(Meta);
  private readonly doc = inject(DOCUMENT);

  update(config: SeoConfig, lang: Lang = 'en'): void {
    const enPath = this.normalizePath(config.path);
    const url = SITE_URL + addLangPrefix(enPath, lang);
    const robots = config.noindex ? 'noindex, nofollow' : 'index, follow';

    this.doc.documentElement.lang = lang;
    this.titleService.setTitle(config.title);

    this.meta.updateTag({ name: 'description', content: config.description });
    this.meta.updateTag({ name: 'keywords', content: config.keywords ?? DEFAULT_KEYWORDS });
    this.meta.updateTag({ name: 'robots', content: robots });
    this.meta.updateTag({ name: 'googlebot', content: robots });

    this.meta.updateTag({ property: 'og:type', content: 'website' });
    this.meta.updateTag({ property: 'og:site_name', content: SITE_NAME });
    this.meta.updateTag({ property: 'og:title', content: config.title });
    this.meta.updateTag({ property: 'og:description', content: config.description });
    this.meta.updateTag({ property: 'og:url', content: url });
    this.meta.updateTag({ property: 'og:locale', content: OG_LOCALE[lang] });
    this.meta.updateTag({ name: 'twitter:card', content: 'summary' });
    this.meta.updateTag({ name: 'twitter:title', content: config.title });
    this.meta.updateTag({ name: 'twitter:description', content: config.description });
    if (config.image) {
      this.meta.updateTag({ property: 'og:image', content: config.image });
      this.meta.updateTag({ name: 'twitter:image', content: config.image });
    } else {
      this.meta.removeTag("property='og:image'");
      this.meta.removeTag("name='twitter:image'");
    }

    this.setCanonical(url);
    this.setAlternates(enPath, config.noindex === true);
    this.clearPageJsonLd();
    this.setJsonLdLanguage(lang);
  }

  setPageJsonLd(data: object): void {
    let script = this.doc.getElementById(PAGE_JSONLD_ID) as HTMLScriptElement | null;
    if (!script) {
      script = this.doc.createElement('script');
      script.type = 'application/ld+json';
      script.id = PAGE_JSONLD_ID;
      this.doc.head.appendChild(script);
    }
    script.textContent = JSON.stringify(data);
  }

  clearPageJsonLd(): void {
    this.doc.getElementById(PAGE_JSONLD_ID)?.remove();
  }

  private setCanonical(url: string): void {
    let link = this.doc.querySelector("link[rel='canonical']") as HTMLLinkElement | null;
    if (!link) {
      link = this.doc.createElement('link');
      link.setAttribute('rel', 'canonical');
      this.doc.head.appendChild(link);
    }
    link.setAttribute('href', url);
  }

  private setAlternates(enPath: string, noindex: boolean): void {
    const alternates: [string, string][] = [
      ['en', SITE_URL + enPath],
      ['es', SITE_URL + addLangPrefix(enPath, 'es')],
      ['x-default', SITE_URL + enPath],
    ];
    for (const [hreflang, href] of alternates) {
      const selector = `link[rel='alternate'][hreflang='${hreflang}']`;
      let link = this.doc.querySelector(selector) as HTMLLinkElement | null;
      if (noindex) {
        link?.remove();
        continue;
      }
      if (!link) {
        link = this.doc.createElement('link');
        link.setAttribute('rel', 'alternate');
        link.setAttribute('hreflang', hreflang);
        this.doc.head.appendChild(link);
      }
      link.setAttribute('href', href);
    }
  }

  private setJsonLdLanguage(lang: Lang): void {
    try {
      const script = this.doc.querySelector(
        `script[type="application/ld+json"]:not(#${PAGE_JSONLD_ID})`,
      );
      if (!script?.textContent) return;
      const data = JSON.parse(script.textContent);
      const nodes = Array.isArray(data['@graph']) ? data['@graph'] : [data];
      let changed = false;
      for (const node of nodes) {
        if (node && typeof node === 'object' && 'inLanguage' in node) {
          node.inLanguage = lang;
          changed = true;
        }
      }
      if (changed) script.textContent = JSON.stringify(data);
    } catch {
      // Dejar el JSON-LD estático intacto ante cualquier error de parseo.
    }
  }

  private normalizePath(path?: string): string {
    if (!path || path === '/') return '/';
    return path.startsWith('/') ? path : `/${path}`;
  }
}
