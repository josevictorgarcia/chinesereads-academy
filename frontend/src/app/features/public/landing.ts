import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { LocalizeLinkPipe } from '../../i18n/localize-link.pipe';
import { Lang } from '../../i18n/locale.util';
import { SessionService } from '../../core/session.service';

@Component({
  selector: 'app-landing',
  imports: [RouterLink, TranslocoPipe, LocalizeLinkPipe],
  template: `
    <section class="hero text-center py-5">
      <div class="container">
        <h1 class="display-5 fw-semibold">{{ 'landing.title' | transloco }}</h1>
        <p class="lead mx-auto col-lg-8">{{ 'landing.subtitle' | transloco }}</p>
        <div class="d-flex justify-content-center gap-2 flex-wrap mt-4">
          <a class="btn btn-primary btn-lg" [routerLink]="'/teacher' | localizeLink">{{
            'landing.ctaTeacher' | transloco
          }}</a>
          <a class="btn btn-outline-primary btn-lg" [routerLink]="'/student' | localizeLink">{{
            'landing.ctaStudent' | transloco
          }}</a>
        </div>
        @if (!session.isLoggedIn()) {
          <p class="mt-3 small">
            <a [href]="session.loginUrl(lang)" rel="noopener">{{
              'landing.ctaLogin' | transloco
            }}</a>
          </p>
        }
      </div>
    </section>
    <section class="container py-5">
      <div class="row g-4">
        @for (f of features; track f) {
          <div class="col-md-4">
            <div class="card h-100 border-0 shadow-sm">
              <div class="card-body">
                <h2 class="h5 card-title">{{ 'landing.' + f + 'Title' | transloco }}</h2>
                <p class="card-text text-body-secondary">
                  {{ 'landing.' + f + 'Text' | transloco }}
                </p>
              </div>
            </div>
          </div>
        }
      </div>
    </section>
  `,
  styles: `
    .hero {
      background: var(--academy-hero-bg);
    }
  `,
})
export class Landing {
  readonly session = inject(SessionService);
  private readonly transloco = inject(TranslocoService);
  readonly features = ['feature1', 'feature2', 'feature3'];
  get lang(): Lang {
    return this.transloco.getActiveLang() as Lang;
  }
}
