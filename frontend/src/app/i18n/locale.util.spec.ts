import { addLangPrefix, langFromUrl, stripLangPrefix } from './locale.util';

describe('locale.util', () => {
  it('derives the language from the URL prefix only', () => {
    expect(langFromUrl('/')).toBe('en');
    expect(langFromUrl('/precios')).toBe('en');
    expect(langFromUrl('/es')).toBe('es');
    expect(langFromUrl('/es/precios?x=1#h')).toBe('es');
    expect(langFromUrl('/escuela')).toBe('en');
  });

  it('strips and adds the prefix symmetrically', () => {
    for (const path of ['/', '/precios', '/teacher', '/?ref=X']) {
      expect(stripLangPrefix(addLangPrefix(path, 'es'))).toBe(path);
      expect(addLangPrefix(path, 'en')).toBe(path);
    }
    expect(addLangPrefix('/', 'es')).toBe('/es');
    expect(addLangPrefix('/?ref=X', 'es')).toBe('/es?ref=X');
    expect(stripLangPrefix('/es?ref=X')).toBe('/?ref=X');
  });
});
