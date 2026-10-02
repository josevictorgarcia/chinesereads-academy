import { Component, inject } from '@angular/core';
import { TranslocoPipe } from '@jsverse/transloco';
import { SessionService } from '../../core/session.service';

@Component({
  selector: 'app-student-home',
  imports: [TranslocoPipe],
  template: `
    <section class="container py-5 col-lg-8">
      <h1 class="fw-semibold">{{ 'student.title' | transloco }}</h1>
      <p class="lead">{{ 'student.welcome' | transloco: { name: session.user()?.name } }}</p>
      <p class="alert alert-light border">{{ 'student.noGroups' | transloco }}</p>
    </section>
  `,
})
export class StudentHome {
  readonly session = inject(SessionService);
}
