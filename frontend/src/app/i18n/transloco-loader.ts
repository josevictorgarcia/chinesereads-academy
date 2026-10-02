import { Injectable } from '@angular/core';
import { Translation, TranslocoLoader } from '@jsverse/transloco';
import { of } from 'rxjs';

import en from '../../assets/i18n/en.json';
import es from '../../assets/i18n/es.json';

/**
 * Loader síncrono: los diccionarios van dentro del bundle (import estático), así `setActiveLang('es')`
 * se aplica en el mismo tick y el HTML prerenderizado sale en el idioma correcto.
 */
@Injectable({ providedIn: 'root' })
export class InlineTranslocoLoader implements TranslocoLoader {
  private readonly dictionaries: Record<string, Translation> = { en, es };

  getTranslation(lang: string) {
    return of(this.dictionaries[lang] ?? this.dictionaries['en']);
  }
}
