import { Injectable, Signal, signal, WritableSignal } from '@angular/core';

/**
 * Per-edit-form UI state shared between an {@link AbstractEditEntityDirective} subclass and the
 * container hosting it (currently `EditItemDialogComponent`).
 *
 * Deliberately NOT `providedIn: 'root'`: one instance per edit container. The container provides
 * it, the hosted edit component resolves the container's instance via
 * `AbstractEditEntityDirective.injectParentOrSelf` and falls back to its own when hosted
 * standalone.
 */
@Injectable()
export class EditEntityStateService {
  private readonly generalErrorMessageSignal: WritableSignal<string | null> = signal(null);

  public readonly generalErrorMessage: Signal<string | null> = this.generalErrorMessageSignal.asReadonly();

  public onGeneralErrorMessage(generalErrorMessage: string): void {
    this.generalErrorMessageSignal.set(generalErrorMessage);
  }

  public resetGeneralErrorMessage(): void {
    this.generalErrorMessageSignal.set(null);
  }
}
