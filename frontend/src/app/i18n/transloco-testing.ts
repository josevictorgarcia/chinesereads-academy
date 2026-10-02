import { TranslocoTestingModule, TranslocoTestingOptions } from '@jsverse/transloco';
import en from '../../assets/i18n/en.json';
import es from '../../assets/i18n/es.json';

/** Diccionarios reales disponibles de forma síncrona en un TestBed. Añadir a `imports`. */
export function translocoTesting(options: TranslocoTestingOptions = {}) {
  return TranslocoTestingModule.forRoot({
    langs: { en, es },
    translocoConfig: { availableLangs: ['en', 'es'], defaultLang: 'en' },
    preloadLangs: true,
    ...options,
  });
}
