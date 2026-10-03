import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TranslocoPipe } from '@jsverse/transloco';
import { ClassroomApiService } from '../../api/classroom-api.service';
import { IdentityApiService } from '../../api/identity-api.service';
import { GroupSummary, SeatUsage } from '../../api/models';
import { SessionService } from '../../core/session.service';
import { LocalizeLinkPipe } from '../../i18n/localize-link.pipe';

@Component({
  selector: 'app-teacher-home',
  imports: [FormsModule, RouterLink, TranslocoPipe, LocalizeLinkPipe, DatePipe],
  templateUrl: './teacher-home.html',
})
export class TeacherHome implements OnInit {
  readonly session = inject(SessionService);
  private readonly identity = inject(IdentityApiService);
  private readonly classroom = inject(ClassroomApiService);

  displayName = this.session.user()?.name ?? '';
  newGroupName = '';
  newGroupLevel = '';
  readonly saving = signal(false);
  readonly groups = signal<GroupSummary[]>([]);
  readonly seats = signal<SeatUsage | null>(null);

  ngOnInit(): void {
    if (this.session.isTeacher()) this.loadAll();
  }

  register(): void {
    if (!this.displayName.trim() || this.saving()) return;
    this.saving.set(true);
    this.identity.registerTeacher({ displayName: this.displayName.trim() }).subscribe({
      next: () =>
        this.session.refresh().finally(() => {
          this.saving.set(false);
          this.loadAll();
        }),
      error: () => this.saving.set(false),
    });
  }

  createGroup(): void {
    if (!this.newGroupName.trim() || this.saving()) return;
    this.saving.set(true);
    this.classroom
      .createGroup({
        name: this.newGroupName.trim(),
        level: this.newGroupLevel.trim() || undefined,
      })
      .subscribe({
        next: (g) => {
          this.groups.update((list) => [g, ...list]);
          this.newGroupName = '';
          this.newGroupLevel = '';
          this.saving.set(false);
        },
        error: () => this.saving.set(false),
      });
  }

  private loadAll(): void {
    this.classroom.listGroups().subscribe({ next: (g) => this.groups.set(g) });
    this.classroom.seats().subscribe({ next: (s) => this.seats.set(s) });
  }
}
