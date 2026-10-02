import { inject, Pipe, PipeTransform } from '@angular/core';
import { TranslocoService } from '@jsverse/transloco';
import { addLangPrefix, Lang } from './locale.util';

/**
 * Hace que un `routerLink` absoluto respete el idioma activo: en español antepone `/es`.
 * Uso: `[routerLink]="'/precios' | localizeLink"`. Impuro porque depende del idioma activo.
 */
@Pipe({ name: 'localizeLink', pure: false })
export class LocalizeLinkPipe implements PipeTransform {
  private readonly transloco = inject(TranslocoService);

  transform(commands: unknown[] | string): unknown[] {
    const arr = Array.isArray(commands) ? [...commands] : [commands];
    const lang = this.transloco.getActiveLang() as Lang;
    if (lang !== 'es' || arr.length === 0) return arr;
    const [first, ...rest] = arr;
    if (typeof first === 'string' && first.startsWith('/')) {
      return [addLangPrefix(first, 'es'), ...rest];
    }
    return arr;
  }
}
