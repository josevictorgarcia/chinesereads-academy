import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoPipe } from '@jsverse/transloco';
import { LocalizeLinkPipe } from '../../i18n/localize-link.pipe';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-footer',
  imports: [RouterLink, TranslocoPipe, LocalizeLinkPipe],
  template: `
    <footer class="border-top mt-auto py-3 small text-body-secondary">
      <div class="container d-flex flex-wrap gap-3 justify-content-between">
        <a class="link-secondary" [href]="parentUrl" rel="noopener">{{
          'footer.parent' | transloco
        }}</a>
        <span>
          <a class="link-secondary me-3" [routerLink]="'/privacy-policy' | localizeLink">{{
            'footer.privacy' | transloco
          }}</a>
          <a class="link-secondary" [routerLink]="'/terms-of-use' | localizeLink">{{
            'footer.terms' | transloco
          }}</a>
        </span>
      </div>
    </footer>
  `,
})
export class Footer {
  readonly parentUrl = environment.chinesereadsUrl;
}
