import { DOCUMENT } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { translocoTesting } from '../i18n/transloco-testing';
import { authGuard, teacherGuard } from './guards';
import { SessionService } from './session.service';

describe('guards', () => {
  const route = {} as ActivatedRouteSnapshot;
  const state = {} as RouterStateSnapshot;
  let session: SessionService;
  let assign: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    assign = vi.fn();
    TestBed.configureTestingModule({
      imports: [translocoTesting()],
      providers: [
        {
          provide: SessionService,
          useValue: {
            ready: vi.fn(),
            isTeacher: vi.fn(),
            loginUrl: (l: string) => 'https://cr.test/' + l,
          },
        },
        { provide: DOCUMENT, useValue: { location: { assign } } },
      ],
    });
    session = TestBed.inject(SessionService);
  });

  it('authGuard lets a logged-in user through', async () => {
    (session.ready as ReturnType<typeof vi.fn>).mockResolvedValue({ userId: 1 });
    const result = await TestBed.runInInjectionContext(() => authGuard(route, state));
    expect(result).toBe(true);
    expect(assign).not.toHaveBeenCalled();
  });

  it('authGuard sends anonymous users to the ChineseReads login', async () => {
    (session.ready as ReturnType<typeof vi.fn>).mockResolvedValue(null);
    const result = await TestBed.runInInjectionContext(() => authGuard(route, state));
    expect(result).toBe(false);
    expect(assign).toHaveBeenCalledWith('https://cr.test/en');
  });

  it('teacherGuard redirects non-teachers to the teacher area', async () => {
    (session.ready as ReturnType<typeof vi.fn>).mockResolvedValue({ userId: 1 });
    (session.isTeacher as unknown as ReturnType<typeof vi.fn>).mockReturnValue(false);
    const result = (await TestBed.runInInjectionContext(() =>
      teacherGuard(route, state),
    )) as UrlTree;
    expect(TestBed.inject(Router).serializeUrl(result)).toBe('/teacher');
  });
});
