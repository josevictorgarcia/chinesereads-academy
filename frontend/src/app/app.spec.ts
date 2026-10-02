import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';
import { routes } from './app.routes';
import { translocoTesting } from './i18n/transloco-testing';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App, translocoTesting()],
      providers: [provideRouter(routes), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
  });

  it('renders the header with the brand and the login link', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('.navbar-brand')?.textContent).toContain('ChineseReads Academy');
    expect(el.querySelector('a.btn-light')?.textContent).toContain('Log in with ChineseReads');
  });
});
