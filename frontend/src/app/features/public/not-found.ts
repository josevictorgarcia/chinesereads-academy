import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoPipe } from '@jsverse/transloco';
import { LocalizeLinkPipe } from '../../i18n/localize-link.pipe';

@Component({
  selector: 'app-not-found',
  imports: [RouterLink, TranslocoPipe, LocalizeLinkPipe],
  template: `
    <section class="container py-5 text-center">
      <h1 class="fw-semibold">{{ 'notFound.title' | transloco }}</h1>
      <p>{{ 'notFound.text' | transloco }}</p>
      <a class="btn btn-primary" [routerLink]="'/' | localizeLink">{{
        'notFound.home' | transloco
      }}</a>
    </section>
  `,
})
export class NotFound {}
