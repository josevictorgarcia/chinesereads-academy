import { inject, Injectable } from '@angular/core';
import { NavigationExtras, Router } from '@angular/router';
import { TranslocoService } from '@jsverse/transloco';
import { addLangPrefix, Lang } from './locale.util';

/** Navegación imperativa que mantiene al usuario en su idioma (`/es/...`). */
@Injectable({ providedIn: 'root' })
export class LocaleNavService {
  private readonly router = inject(Router);
  private readonly transloco = inject(TranslocoService);

  navigate(commands: unknown[], extras?: NavigationExtras): Promise<boolean> {
    const localized = this.localize(commands);
    return extras ? this.router.navigate(localized, extras) : this.router.navigate(localized);
  }

  /** Ruta absoluta (sin prefijo) → ruta en el idioma activo, como string. */
  localizedUrl(path: string): string {
    return addLangPrefix(path, this.transloco.getActiveLang() as Lang);
  }

  private localize(commands: unknown[]): unknown[] {
    const lang = this.transloco.getActiveLang() as Lang;
    if (lang !== 'es' || commands.length === 0) return commands;
    const [first, ...rest] = commands;
    if (typeof first === 'string' && first.startsWith('/')) {
      return [addLangPrefix(first, 'es'), ...rest];
    }
    return commands;
  }
}
