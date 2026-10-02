import { Component } from '@angular/core';
import { TranslocoPipe } from '@jsverse/transloco';

@Component({
  selector: 'app-privacy-policy',
  imports: [TranslocoPipe],
  template: `
    <article class="container py-5 col-lg-8">
      <h1 class="fw-semibold">{{ 'legal.privacyTitle' | transloco }}</h1>
      <p>{{ 'legal.placeholder' | transloco }}</p>
      <p>{{ 'legal.adults' | transloco }}</p>
    </article>
  `,
})
export class PrivacyPolicy {}
