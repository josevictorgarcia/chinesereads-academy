import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { TranslocoService } from '@jsverse/transloco';
import { catchError, throwError } from 'rxjs';
import { ProblemDetail } from '../api/models';
import { ToastService } from './toast/toast.service';

export const REQUEST_ID_HEADER = 'X-Request-Id';
export const XHR_HEADER = 'X-Requested-With';
const MUTATING = new Set(['POST', 'PUT', 'PATCH', 'DELETE']);

function newRequestId(): string {
  const c = globalThis.crypto;
  if (c && typeof c.randomUUID === 'function') return c.randomUUID();
  return 'req-' + Date.now().toString(36) + '-' + Math.random().toString(36).slice(2, 10);
}

/**
 * Único interceptor de la API: credenciales (cookie compartida), identificador de petición,
 * cabecera anti-CSRF en mutaciones y conversión de ProblemDetail en un aviso traducido por `code`.
 * El 401 de `/api/me` es el caso normal de un visitante anónimo y no muestra nada.
 */
export const apiInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.url.startsWith('/api/')) return next(req);

  const toasts = inject(ToastService);
  const transloco = inject(TranslocoService);
  const isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

  let headers = req.headers.set(REQUEST_ID_HEADER, newRequestId());
  if (MUTATING.has(req.method)) headers = headers.set(XHR_HEADER, 'XMLHttpRequest');
  const prepared = req.clone({ withCredentials: true, headers });

  return next(prepared).pipe(
    catchError((err: unknown) => {
      if (err instanceof HttpErrorResponse && isBrowser) {
        const problem = (err.error ?? {}) as Partial<ProblemDetail>;
        const silent401 = err.status === 401 && req.url === '/api/me';
        if (!silent401 && err.status !== 400) {
          const code =
            problem.code &&
            transloco.translate('errors.' + problem.code) !== 'errors.' + problem.code
              ? problem.code
              : 'generic';
          toasts.error(transloco.translate('errors.' + code), problem.errorId);
        }
      }
      return throwError(() => err);
    }),
  );
};
