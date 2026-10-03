import { DOCUMENT, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { TranslocoService } from '@jsverse/transloco';
import { addLangPrefix, Lang } from '../i18n/locale.util';
import { SessionService } from './session.service';

/** Exige sesión. Sin ella, en navegador envía a iniciar sesión en ChineseReads; en servidor no renderiza. */
export const authGuard: CanActivateFn = async () => {
  const session = inject(SessionService);
  const transloco = inject(TranslocoService);
  const doc = inject(DOCUMENT);
  const isBrowser = isPlatformBrowser(inject(PLATFORM_ID));
  if (!isBrowser) return false;
  const user = await session.ready();
  if (user) return true;
  doc.location.assign(session.loginUrl(transloco.getActiveLang() as Lang));
  return false;
};

/** Exige rol de profesor; si no lo tiene, lleva al área de profesor (donde puede registrarse). */
export const teacherGuard: CanActivateFn = async (): Promise<boolean | UrlTree> => {
  const session = inject(SessionService);
  const router = inject(Router);
  const transloco = inject(TranslocoService);
  const isBrowser = isPlatformBrowser(inject(PLATFORM_ID));
  if (!isBrowser) return false;
  await session.ready();
  if (session.isTeacher()) return true;
  return router.parseUrl(addLangPrefix('/teacher', transloco.getActiveLang() as Lang));
};

/** Exige rol de alumno; si no, al área de alumno (que explica cómo unirse). */
export const studentGuard: CanActivateFn = async (): Promise<boolean | UrlTree> => {
  const session = inject(SessionService);
  const router = inject(Router);
  const transloco = inject(TranslocoService);
  const isBrowser = isPlatformBrowser(inject(PLATFORM_ID));
  if (!isBrowser) return false;
  await session.ready();
  if (session.isStudent()) return true;
  return router.parseUrl(addLangPrefix('/student', transloco.getActiveLang() as Lang));
};
