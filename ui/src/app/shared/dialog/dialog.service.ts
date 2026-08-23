import { Injectable, inject, ViewContainerRef } from '@angular/core';
import { MatDialog, MatDialogConfig, MatDialogRef } from '@angular/material/dialog';
import { ComponentType } from '@angular/cdk/overlay';
import { Observable } from 'rxjs';
import { OnSuccessfulSubmit } from '@shared/models/submit-result.model';
import { EditEntityComponent } from '@shared/edit-entity/abstract-edit-entity.directive';
import { EditEntityDialogData, EditItemDialogComponent } from '@shared/edit-entity/edit-item-dialog/edit-item-dialog.component';
import { ConfirmationDialogComponent } from './confirmation-dialog/confirmation-dialog.component';

@Injectable({ providedIn: 'root' })
export class DialogService {
  private readonly dialog: MatDialog = inject(MatDialog);

  public openCreateEntityDialog<E extends { id?: I }, I>(
    componentType: ComponentType<EditEntityComponent<E, I>>,
    viewContainerReference: ViewContainerRef | undefined = undefined,
  ): MatDialogRef<object> {
    const dialogConfig: MatDialogConfig<EditEntityDialogData<E, I>> = new MatDialogConfig<EditEntityDialogData<E, I>>();
    dialogConfig.maxWidth = '100%';
    dialogConfig.autoFocus = true;
    dialogConfig.data = { componentType: componentType };
    dialogConfig.viewContainerRef = viewContainerReference;
    return this.dialog.open(EditItemDialogComponent<EditEntityDialogData<E, I>, E, I>, dialogConfig);
  }

  public openEditEntityDialog<E extends { id?: I }, I>(
    componentType: ComponentType<EditEntityComponent<E, I>>,
    idOrEntity: I | E,
    viewContainerReference: ViewContainerRef | undefined = undefined,
  ): MatDialogRef<object> {
    const dialogConfig: MatDialogConfig<EditEntityDialogData<E, I>> = new MatDialogConfig<EditEntityDialogData<E, I>>();
    dialogConfig.maxWidth = '100%';
    dialogConfig.autoFocus = true;
    dialogConfig.data = { componentType: componentType, idOrEntity: idOrEntity };
    dialogConfig.viewContainerRef = viewContainerReference;
    return this.dialog.open(EditItemDialogComponent<EditEntityDialogData<E, I>, E, I>, dialogConfig);
  }

  public openDeleteConfirmationDialog(action: Observable<OnSuccessfulSubmit>): MatDialogRef<ConfirmationDialogComponent> {
    const dialogConfig: MatDialogConfig = new MatDialogConfig();
    dialogConfig.width = '300px';
    dialogConfig.data = {
      title: 'Deleting ...',
      message: 'Are you sure?',
      actionButtonLabel: 'Yes',
      action: action,
    };
    return this.dialog.open(ConfirmationDialogComponent, dialogConfig);
  }
}
