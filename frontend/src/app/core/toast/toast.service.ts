import { Injectable, signal } from '@angular/core';

export interface ToastMessage {
  id: number;
  kind: 'error' | 'success' | 'info';
  text: string;
  /** Código corto que el usuario puede copiar al reportar un problema. */
  errorId?: string;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  readonly messages = signal<ToastMessage[]>([]);
  private seq = 0;

  error(text: string, errorId?: string): void {
    this.push('error', text, errorId);
  }

  success(text: string): void {
    this.push('success', text);
  }

  dismiss(id: number): void {
    this.messages.update((list) => list.filter((m) => m.id !== id));
  }

  private push(kind: ToastMessage['kind'], text: string, errorId?: string): void {
    const id = ++this.seq;
    this.messages.update((list) => [...list, { id, kind, text, errorId }]);
    setTimeout(() => this.dismiss(id), 8000);
  }
}
