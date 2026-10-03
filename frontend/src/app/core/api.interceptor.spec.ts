import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { translocoTesting } from '../i18n/transloco-testing';
import { apiInterceptor, REQUEST_ID_HEADER, XHR_HEADER } from './api.interceptor';
import { ToastService } from './toast/toast.service';

describe('apiInterceptor', () => {
  let http: HttpClient;
  let ctrl: HttpTestingController;
  let toasts: ToastService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [translocoTesting()],
      providers: [
        provideHttpClient(withInterceptors([apiInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    ctrl = TestBed.inject(HttpTestingController);
    toasts = TestBed.inject(ToastService);
  });

  it('adds credentials, a request id and the XHR header on mutations', () => {
    http.post('/api/teachers/me', { displayName: 'x' }).subscribe();
    const req = ctrl.expectOne('/api/teachers/me');
    expect(req.request.withCredentials).toBe(true);
    expect(req.request.headers.get(REQUEST_ID_HEADER)).toMatch(/.{8,}/);
    expect(req.request.headers.get(XHR_HEADER)).toBe('XMLHttpRequest');
    req.flush({});
  });

  it('does not add the XHR header on GET and leaves non-API calls alone', () => {
    http.get('/api/me').subscribe({ error: () => undefined });
    const me = ctrl.expectOne('/api/me');
    expect(me.request.headers.has(XHR_HEADER)).toBe(false);
    me.flush({});
    http.get('/assets/x.json').subscribe();
    const asset = ctrl.expectOne('/assets/x.json');
    expect(asset.request.headers.has(REQUEST_ID_HEADER)).toBe(false);
    asset.flush({});
  });

  it('shows a translated toast with the errorId for a ProblemDetail', () => {
    http.get('/api/teachers/me').subscribe({ error: () => undefined });
    ctrl
      .expectOne('/api/teachers/me')
      .flush(
        { code: 'IDENTITY_TEACHER_REQUIRED', errorId: 'abcd1234', status: 403 },
        { status: 403, statusText: 'Forbidden' },
      );
    expect(toasts.messages().length).toBe(1);
    expect(toasts.messages()[0].text).toBe('This area is for teachers.');
    expect(toasts.messages()[0].errorId).toBe('abcd1234');
  });

  it('stays silent for the anonymous 401 of /api/me and for validation errors', () => {
    http.get('/api/me').subscribe({ error: () => undefined });
    ctrl
      .expectOne('/api/me')
      .flush({ code: 'UNAUTHENTICATED' }, { status: 401, statusText: 'Unauthorized' });
    http.post('/api/teachers/me', {}).subscribe({ error: () => undefined });
    ctrl
      .expectOne('/api/teachers/me')
      .flush({ code: 'VALIDATION_FAILED' }, { status: 400, statusText: 'Bad Request' });
    expect(toasts.messages().length).toBe(0);
  });

  it('falls back to the generic message for unknown codes', () => {
    http.get('/api/x').subscribe({ error: () => undefined });
    ctrl
      .expectOne('/api/x')
      .flush({ code: 'SOMETHING_NEW' }, { status: 500, statusText: 'Server Error' });
    expect(toasts.messages()[0].text).toBe('Something went wrong.');
  });
});
