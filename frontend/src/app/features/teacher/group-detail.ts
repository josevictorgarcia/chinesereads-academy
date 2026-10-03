import { Component, inject, input, OnInit, signal } from '@angular/core';
import { DatePipe, DOCUMENT } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TranslocoPipe, TranslocoService } from '@jsverse/transloco';
import { ClassroomApiService } from '../../api/classroom-api.service';
import { GroupDetail } from '../../api/models';
import { LocalizeLinkPipe } from '../../i18n/localize-link.pipe';
import { addLangPrefix, Lang } from '../../i18n/locale.util';
import { SITE_URL } from '../../core/seo/seo.service';

@Component({
  selector: 'app-group-detail',
  imports: [RouterLink, TranslocoPipe, LocalizeLinkPipe, DatePipe],
  templateUrl: './group-detail.html',
})
export class GroupDetailPage implements OnInit {
  readonly id = input.required<string>();
  private readonly api = inject(ClassroomApiService);
  private readonly transloco = inject(TranslocoService);
  private readonly doc = inject(DOCUMENT);

  readonly group = signal<GroupDetail | null>(null);
  readonly busy = signal(false);
  readonly copied = signal(false);

  ngOnInit(): void {
    this.reload();
  }

  get joinLink(): string {
    const code = this.group()?.activeInvite?.code;
    const origin = this.doc.location?.origin || SITE_URL;
    return code
      ? `${origin}${addLangPrefix('/join', this.transloco.getActiveLang() as Lang)}?code=${code}`
      : '';
  }

  generateInvite(): void {
    this.run(this.api.createInvite(Number(this.id())));
  }

  revokeInvite(): void {
    this.run(this.api.revokeInvite(Number(this.id())));
  }

  remove(enrollmentId: number): void {
    if (!this.doc.defaultView?.confirm(this.transloco.translate('group.removeConfirm'))) return;
    this.run(this.api.deactivateEnrollment(enrollmentId));
  }

  copy(): void {
    const text = this.group()?.activeInvite?.code ?? '';
    this.doc.defaultView?.navigator?.clipboard?.writeText(text).then(() => {
      this.copied.set(true);
      setTimeout(() => this.copied.set(false), 2000);
    });
  }

  private run(call: { subscribe: (o: { next: () => void; error: () => void }) => unknown }): void {
    if (this.busy()) return;
    this.busy.set(true);
    call.subscribe({ next: () => this.reload(), error: () => this.busy.set(false) });
  }

  private reload(): void {
    this.api.groupDetail(Number(this.id())).subscribe({
      next: (g) => {
        this.group.set(g);
        this.busy.set(false);
      },
      error: () => this.busy.set(false),
    });
  }
}
