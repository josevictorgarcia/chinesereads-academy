import { Component, inject, input, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { ClassroomApiService } from '../../api/classroom-api.service';
import { StudentGroup } from '../../api/models';
import { SessionService } from '../../core/session.service';
import { ToastService } from '../../core/toast/toast.service';

/** Área del alumno: sus grupos y el formulario para unirse con un código (también vía /join?code=XXXX). */
@Component({
  selector: 'app-student-home',
  imports: [FormsModule, TranslocoPipe, DatePipe],
  templateUrl: './student-home.html',
})
export class StudentHome implements OnInit {
  readonly session = inject(SessionService);
  private readonly api = inject(ClassroomApiService);
  private readonly toasts = inject(ToastService);
  private readonly transloco = inject(TranslocoService);

  /** Query param ?code= (ruta /join). */
  readonly code = input<string>();
  codeInput = '';
  readonly groups = signal<StudentGroup[]>([]);
  readonly busy = signal(false);

  ngOnInit(): void {
    this.load();
    const prefilled = this.code();
    if (prefilled) {
      this.codeInput = prefilled.toUpperCase();
      this.join();
    }
  }

  join(): void {
    const code = this.codeInput.trim().toUpperCase();
    if (code.length !== 8 || this.busy()) return;
    this.busy.set(true);
    this.api.join(code).subscribe({
      next: (r) => {
        this.toasts.success(
          this.transloco.translate(r.alreadyMember ? 'join.already' : 'join.success', {
            group: r.groupName,
          }),
        );
        this.codeInput = '';
        this.busy.set(false);
        this.load();
        this.session.refresh();
      },
      error: () => this.busy.set(false),
    });
  }

  private load(): void {
    this.api.myGroups().subscribe({ next: (g) => this.groups.set(g) });
  }
}
