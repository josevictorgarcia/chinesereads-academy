import { Component } from '@angular/core';
import { TranslocoPipe } from '@jsverse/transloco';

@Component({
  selector: 'app-terms-of-use',
  imports: [TranslocoPipe],
  template: `
    <article class="container py-5 col-lg-8">
      <h1 class="fw-semibold">{{ 'legal.termsTitle' | transloco }}</h1>
      <p>{{ 'legal.placeholder' | transloco }}</p>
      <p>{{ 'legal.adults' | transloco }}</p>
    </article>
  `,
})
export class TermsOfUse {}
