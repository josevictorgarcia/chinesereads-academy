import { Component, inject } from '@angular/core';
import { NavigationEnd, NavigationStart, Router, RouterOutlet } from '@angular/router';
import { TranslocoService } from '@jsverse/transloco';
import { filter } from 'rxjs/operators';
import { Footer } from './core/layout/footer';
import { Header } from './core/layout/header';
import { resolveSeo } from './core/seo/seo.config';
import { SeoService } from './core/seo/seo.service';
import { Toast } from './core/toast/toast';
import { langFromUrl, stripLangPrefix } from './i18n/locale.util';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Header, Footer, Toast],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  private readonly router = inject(Router);
  private readonly transloco = inject(TranslocoService);
  private readonly seo = inject(SeoService);

  constructor() {
    // Idioma desde el prefijo de la URL ANTES de renderizar la ruta (servidor y cliente coinciden).
    this.router.events
      .pipe(filter((e): e is NavigationStart => e instanceof NavigationStart))
      .subscribe((e) => this.transloco.setActiveLang(langFromUrl(e.url)));

    // SEO por ruta e idioma en cada navegación (también durante el prerender).
    this.router.events
      .pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd))
      .subscribe((e) => {
        const url = e.urlAfterRedirects;
        const lang = langFromUrl(url);
        this.seo.update(resolveSeo(stripLangPrefix(url), lang), lang);
      });
  }
}
