import en from '../../assets/i18n/en.json';
import es from '../../assets/i18n/es.json';

function keys(obj: Record<string, unknown>, prefix = ''): string[] {
  return Object.entries(obj).flatMap(([k, v]) =>
    v && typeof v === 'object'
      ? keys(v as Record<string, unknown>, prefix + k + '.')
      : [prefix + k],
  );
}

describe('i18n dictionaries', () => {
  it('have exactly the same keys in English and Spanish', () => {
    expect(keys(es).sort()).toEqual(keys(en).sort());
  });

  it('have no empty translations', () => {
    const empty = (d: Record<string, unknown>) =>
      keys(d).filter((k) => {
        const v = k.split('.').reduce<unknown>((o, p) => (o as Record<string, unknown>)[p], d);
        return typeof v !== 'string' || v.trim() === '';
      });
    expect(empty(en)).toEqual([]);
    expect(empty(es)).toEqual([]);
  });
});
