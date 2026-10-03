import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslocoPipe } from '@jsverse/transloco';
import { IdentityApiService } from '../../api/identity-api.service';
import { SessionService } from '../../core/session.service';
import { ToastService } from '../../core/toast/toast.service';

@Component({
  selector: 'app-teacher-home',
  imports: [FormsModule, TranslocoPipe],
  template: `
    <section class="container py-5 col-lg-8">
      <h1 class="fw-semibold">{{ 'teacher.title' | transloco }}</h1>
      <p class="lead">{{ 'teacher.welcome' | transloco: { name: session.user()?.name } }}</p>

      @if (session.isTeacher()) {
        <div class="alert alert-success">{{ 'teacher.registered' | transloco }}</div>
        <p class="text-body-secondary">{{ 'teacher.groupsSoon' | transloco }}</p>
      } @else {
        <div class="card border-0 shadow-sm">
          <div class="card-body">
            <h2 class="h5">{{ 'teacher.notTeacherTitle' | transloco }}</h2>
            <p>{{ 'teacher.notTeacherText' | transloco }}</p>
            <form (ngSubmit)="register()" #f="ngForm" class="row g-2 align-items-end">
              <div class="col-sm-8">
                <label class="form-label" for="displayName">{{
                  'teacher.displayName' | transloco
                }}</label>
                <input
                  id="displayName"
                  name="displayName"
                  class="form-control"
                  required
                  maxlength="120"
                  [(ngModel)]="displayName"
                  [disabled]="saving()"
                />
              </div>
              <div class="col-sm-4">
                <button
                  class="btn btn-primary w-100"
                  type="submit"
                  [disabled]="f.invalid || saving()"
                >
                  {{ 'teacher.register' | transloco }}
                </button>
              </div>
            </form>
          </div>
        </div>
      }
    </section>
  `,
})
export class TeacherHome {
  readonly session = inject(SessionService);
  private readonly api = inject(IdentityApiService);
  private readonly toasts = inject(ToastService);

  displayName = this.session.user()?.name ?? '';
  readonly saving = signal(false);

  register(): void {
    if (!this.displayName.trim() || this.saving()) return;
    this.saving.set(true);
    this.api.registerTeacher({ displayName: this.displayName.trim() }).subscribe({
      next: () => {
        this.session.refresh().finally(() => this.saving.set(false));
      },
      error: () => this.saving.set(false),
    });
  }
}
