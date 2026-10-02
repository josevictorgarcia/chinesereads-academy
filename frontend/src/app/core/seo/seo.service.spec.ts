import { DOCUMENT } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { SeoService, SITE_URL } from './seo.service';

describe('SeoService', () => {
  let seo: SeoService;
  let doc: Document;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    seo = TestBed.inject(SeoService);
    doc = TestBed.inject(DOCUMENT);
  });

  const attr = (selector: string, name: string) =>
    doc.querySelector(selector)?.getAttribute(name) ?? null;

  it('writes title, canonical, hreflang alternates and html lang for Spanish', () => {
    seo.update({ title: 'Precios', description: 'desc', path: '/precios' }, 'es');
    expect(doc.title).toBe('Precios');
    expect(doc.documentElement.lang).toBe('es');
    expect(attr("link[rel='canonical']", 'href')).toBe(SITE_URL + '/es/precios');
    expect(attr("link[rel='alternate'][hreflang='en']", 'href')).toBe(SITE_URL + '/precios');
    expect(attr("link[rel='alternate'][hreflang='es']", 'href')).toBe(SITE_URL + '/es/precios');
    expect(attr("link[rel='alternate'][hreflang='x-default']", 'href')).toBe(SITE_URL + '/precios');
    expect(attr("meta[name='robots']", 'content')).toBe('index, follow');
    expect(attr("meta[property='og:locale']", 'content')).toBe('es_ES');
  });

  it('removes alternates and sets noindex on private pages', () => {
    seo.update({ title: 'Pub', description: 'd', path: '/precios' }, 'en');
    seo.update({ title: 'Priv', description: 'd', path: '/teacher', noindex: true }, 'en');
    expect(attr("meta[name='robots']", 'content')).toBe('noindex, nofollow');
    expect(doc.querySelector("link[rel='alternate']")).toBeNull();
  });

  it('clears page JSON-LD on the next navigation', () => {
    seo.update({ title: 'A', description: 'd', path: '/' }, 'en');
    seo.setPageJsonLd({ '@type': 'Thing' });
    expect(doc.getElementById('seo-page-jsonld')).not.toBeNull();
    seo.update({ title: 'B', description: 'd', path: '/precios' }, 'en');
    expect(doc.getElementById('seo-page-jsonld')).toBeNull();
  });
});
