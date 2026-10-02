import { Component } from '@angular/core';
import { TranslocoPipe } from '@jsverse/transloco';

@Component({
  selector: 'app-for-teachers',
  imports: [TranslocoPipe],
  template: `
    <article class="container py-5 col-lg-8">
      <h1 class="fw-semibold">{{ 'forTeachers.title' | transloco }}</h1>
      <p class="lead">{{ 'forTeachers.intro' | transloco }}</p>
      <ul class="list-group list-group-flush my-4">
        @for (p of points; track p) {
          <li class="list-group-item px-0">{{ 'forTeachers.' + p | transloco }}</li>
        }
      </ul>
      <p class="alert alert-light border">{{ 'forTeachers.status' | transloco }}</p>
    </article>
  `,
})
export class ForTeachers {
  readonly points = ['point1', 'point2', 'point3', 'point4'];
}
