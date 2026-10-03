import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { LocalizeLinkPipe } from '../../i18n/localize-link.pipe';
import { addLangPrefix, Lang, stripLangPrefix } from '../../i18n/locale.util';
import { SessionService } from '../session.service';

@Component({
  selector: 'app-header',
  imports: [RouterLink, RouterLinkActive, TranslocoPipe, LocalizeLinkPipe],
  templateUrl: './header.html',
  styleUrl: './header.scss',
})
export class Header {
  readonly session = inject(SessionService);
  private readonly transloco = inject(TranslocoService);
  private readonly router = inject(Router);

  get lang(): Lang {
    return this.transloco.getActiveLang() as Lang;
  }

  get loginUrl(): string {
    return this.session.loginUrl(this.lang);
  }

  switchLang(target: Lang): void {
    if (this.lang === target) return;
    const stripped = stripLangPrefix(this.router.url);
    this.transloco.setActiveLang(target);
    void this.router.navigateByUrl(addLangPrefix(stripped, target));
  }
}
