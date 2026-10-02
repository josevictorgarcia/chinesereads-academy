import { Component, inject } from '@angular/core';
import { TranslocoPipe } from '@jsverse/transloco';
import { ToastService } from './toast.service';

@Component({
  selector: 'app-toast',
  imports: [TranslocoPipe],
  template: `
    <div class="toast-container position-fixed bottom-0 end-0 p-3" aria-live="polite">
      @for (m of toasts.messages(); track m.id) {
        <div
          class="toast show align-items-center border-0"
          [class.text-bg-danger]="m.kind === 'error'"
          [class.text-bg-success]="m.kind === 'success'"
          [class.text-bg-dark]="m.kind === 'info'"
          role="alert"
        >
          <div class="d-flex">
            <div class="toast-body">
              {{ m.text }}
              @if (m.errorId) {
                <div class="small opacity-75 mt-1">
                  {{ 'common.errorIdHint' | transloco: { id: m.errorId } }}
                </div>
              }
            </div>
            <button
              type="button"
              class="btn-close btn-close-white me-2 m-auto"
              aria-label="Close"
              (click)="toasts.dismiss(m.id)"
            ></button>
          </div>
        </div>
      }
    </div>
  `,
})
export class Toast {
  readonly toasts = inject(ToastService);
}
