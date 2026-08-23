import { inject, Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly matSnackBar: MatSnackBar = inject(MatSnackBar);

  public onSuccess(message: string): void {
    this.matSnackBar.open(message, undefined, {
      verticalPosition: 'top',
      panelClass: ['success-snackbar'],
    });
  }

  public onError(message: string): void {
    this.matSnackBar.open(message, undefined, {
      verticalPosition: 'top',
      panelClass: ['error-snackbar'],
    });
  }
}
