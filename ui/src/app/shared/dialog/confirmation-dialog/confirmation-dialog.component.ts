import { ChangeDetectionStrategy, Component, DestroyRef, inject } from '@angular/core';
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogClose,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle,
} from '@angular/material/dialog';
import { MatButton } from '@angular/material/button';
import { Observable, take } from 'rxjs';
import { ValidationErrors } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NotificationService } from '@core/notification/notification.service';
import { ErrorService } from '@core/error/error.service';
import { OnSuccessfulSubmit } from '@shared/models/submit-result.model';

@Component({
  selector: 'mra-confirmation-dialog',
  templateUrl: './confirmation-dialog.component.html',
  imports: [MatButton, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogTitle],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ConfirmationDialogComponent {
  protected readonly context: ConfirmationDialogContext = inject<ConfirmationDialogContext>(MAT_DIALOG_DATA);

  private readonly notificationService: NotificationService = inject(NotificationService);
  private readonly errorService: ErrorService = inject(ErrorService);
  private readonly matDialogReference: MatDialogRef<ConfirmationDialogComponent> =
    inject<MatDialogRef<ConfirmationDialogComponent>>(MatDialogRef);
  private readonly destroyReference: DestroyRef = inject(DestroyRef);

  protected onActionButtonClicked(): void {
    this.context.action.pipe(take(1)).subscribe((onSuccessfulSubmit: OnSuccessfulSubmit): void => {
      this.notificationService.onSuccess(onSuccessfulSubmit.feedbackMessage);
      if (onSuccessfulSubmit.closeContainer) {
        setTimeout((): void => {
          this.matDialogReference.close();
        }, 1000);
      }
    });
    this.errorService.entityFieldValidationErrors$
      .pipe(takeUntilDestroyed(this.destroyReference))
      .subscribe((validationErrors: ValidationErrors): void => {
        const generalErrorMessage: unknown = validationErrors['general'];
        if (typeof generalErrorMessage === 'string') {
          this.notificationService.onError(generalErrorMessage);
        }
      });
  }
}

export interface ConfirmationDialogContext {
  title: string;
  message: string;
  actionButtonLabel: string;
  action: Observable<OnSuccessfulSubmit>;
}
