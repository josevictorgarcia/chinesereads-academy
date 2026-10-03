import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { MeResponse } from '../api/models';
import { SessionService } from './session.service';

const ME: MeResponse = {
  userId: 1,
  email: 'a@b.c',
  name: 'Ana',
  language: 'es',
  roles: ['TEACHER'],
  teacher: { id: 7, displayName: 'Ana' },
};

describe('SessionService', () => {
  let session: SessionService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    session = TestBed.inject(SessionService);
    http = TestBed.inject(HttpTestingController);
  });

  it('loads the user from /api/me and derives roles', async () => {
    const p = session.load();
    http.expectOne('/api/me').flush(ME);
    await p;
    expect(session.isLoggedIn()).toBe(true);
    expect(session.isTeacher()).toBe(true);
    expect(session.isStudent()).toBe(false);
    expect(session.resolved()).toBe(true);
  });

  it('resolves to anonymous on 401 without throwing', async () => {
    const p = session.load();
    http
      .expectOne('/api/me')
      .flush({ code: 'UNAUTHENTICATED' }, { status: 401, statusText: 'Unauthorized' });
    expect(await p).toBeNull();
    expect(session.isLoggedIn()).toBe(false);
    expect(session.resolved()).toBe(true);
  });

  it('shares one in-flight request between concurrent callers', async () => {
    const a = session.load();
    const b = session.ready();
    http.expectOne('/api/me').flush(ME);
    await Promise.all([a, b]);
    http.verify();
  });

  it('builds the ChineseReads login URL in the active language', () => {
    expect(session.loginUrl('es').endsWith('/es')).toBe(true);
    expect(session.loginUrl('en').endsWith('/')).toBe(true);
  });
});
