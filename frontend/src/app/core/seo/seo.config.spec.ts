import { PUBLIC_PATHS, resolveSeo } from './seo.config';

describe('resolveSeo', () => {
  it('returns indexable metadata in both languages for every public path', () => {
    for (const path of PUBLIC_PATHS) {
      const en = resolveSeo(path, 'en');
      const es = resolveSeo(path, 'es');
      expect(en.noindex).toBeUndefined();
      expect(en.title).not.toEqual(es.title);
      expect(en.path).toBe(path);
      expect(es.path).toBe(path);
    }
  });

  it('marks private areas and unknown routes as noindex', () => {
    expect(resolveSeo('/teacher', 'en').noindex).toBe(true);
    expect(resolveSeo('/student/groups/1', 'es').noindex).toBe(true);
    expect(resolveSeo('/does-not-exist', 'en').noindex).toBe(true);
  });

  it('ignores query strings and trailing slashes', () => {
    expect(resolveSeo('/precios/?utm=1', 'en').title).toBe(resolveSeo('/precios', 'en').title);
  });
});
