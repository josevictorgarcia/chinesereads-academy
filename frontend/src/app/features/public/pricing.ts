import { Component } from '@angular/core';
import { TranslocoPipe } from '@jsverse/transloco';

@Component({
  selector: 'app-pricing',
  imports: [TranslocoPipe],
  template: `
    <section class="container py-5 text-center">
      <h1 class="fw-semibold">{{ 'pricing.title' | transloco }}</h1>
      <p class="lead">{{ 'pricing.intro' | transloco }}</p>
      <div class="card mx-auto shadow-sm border-0" style="max-width: 26rem">
        <div class="card-body p-4">
          <h2 class="h5">{{ 'pricing.planName' | transloco }}</h2>
          <p class="display-6 fw-semibold mb-0">{{ 'pricing.price' | transloco }}</p>
          <p class="text-body-secondary">{{ 'pricing.priceNote' | transloco }}</p>
          <ul class="list-unstyled text-start">
            <li>
              <i class="bi bi-check2 text-success me-2"></i>{{ 'pricing.includes1' | transloco }}
            </li>
            <li>
              <i class="bi bi-check2 text-success me-2"></i>{{ 'pricing.includes2' | transloco }}
            </li>
            <li>
              <i class="bi bi-check2 text-success me-2"></i>{{ 'pricing.includes3' | transloco }}
            </li>
          </ul>
        </div>
      </div>
      <p class="small text-body-secondary mt-3">{{ 'pricing.hypothesis' | transloco }}</p>
    </section>
  `,
})
export class Pricing {}
