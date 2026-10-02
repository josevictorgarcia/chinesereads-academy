import { computed, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { IdentityApiService } from '../api/identity-api.service';
import { AcademyRole, MeResponse } from '../api/models';
import { environment } from '../../environments/environment';
import { addLangPrefix, Lang } from '../i18n/locale.util';

/**
 * Estado de sesión. Fuente de verdad: `GET /api/me` (la cookie de ChineseReads viaja sola).
 * No se guarda nada en localStorage. En prerender no se carga (no hay cookie): la página pública
 * se renderiza como anónimo y el navegador la completa al hidratar.
 */
@Injectable({ providedIn: 'root' })
export class SessionService {
  private readonly api = inject(IdentityApiService);

  readonly user = signal<MeResponse | null>(null);
  /** true cuando la primera consulta a /api/me ha terminado (con o sin usuario). */
  readonly resolved = signal(false);
  readonly isLoggedIn = computed(() => this.user() !== null);
  readonly isTeacher = computed(() => this.hasRole('TEACHER'));
  readonly isStudent = computed(() => this.hasRole('STUDENT'));

  private pending: Promise<MeResponse | null> | null = null;

  /** Carga (o recarga) el usuario. Devuelve null si no hay sesión válida. */
  load(): Promise<MeResponse | null> {
    if (!this.pending) {
      this.pending = firstValueFrom(this.api.me())
        .then((me) => {
          this.user.set(me);
          return me;
        })
        .catch(() => {
          this.user.set(null);
          return null;
        })
        .finally(() => {
          this.resolved.set(true);
          this.pending = null;
        });
    }
    return this.pending;
  }

  /** Espera a la primera resolución; no vuelve a llamar si ya se resolvió. */
  ready(): Promise<MeResponse | null> {
    return this.resolved() ? Promise.resolve(this.user()) : this.load();
  }

  refresh(): Promise<MeResponse | null> {
    this.resolved.set(false);
    return this.load();
  }

  clear(): void {
    this.user.set(null);
    this.resolved.set(true);
  }

  /** URL de inicio de sesión en ChineseReads, respetando el idioma. */
  loginUrl(lang: Lang): string {
    return environment.chinesereadsUrl + addLangPrefix('/', lang);
  }

  private hasRole(role: AcademyRole): boolean {
    return this.user()?.roles.includes(role) ?? false;
  }
}
