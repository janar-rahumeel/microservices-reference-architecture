import {
  afterNextRender,
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ComponentRef,
  inject,
  Injector,
  OnDestroy,
  Signal,
  signal,
  Type,
  viewChild,
  ViewContainerRef,
  WritableSignal,
} from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogRef, MatDialogTitle } from '@angular/material/dialog';
import { MatButton } from '@angular/material/button';
import { MatError } from '@angular/material/form-field';
import { take } from 'rxjs';
import { NotificationService } from '@core/notification/notification.service';
import { OnSuccessfulSubmit } from '@shared/models/submit-result.model';
import { EditEntityComponent } from '../abstract-edit-entity.directive';
import { EditEntityStateService } from '../edit-entity-state.service';

@Component({
  selector: 'mra-edit-item-dialog',
  templateUrl: './edit-item-dialog.component.html',
  imports: [MatButton, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogTitle, MatError],
  providers: [EditEntityStateService],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EditItemDialogComponent<D extends EditEntityDialogData<E, I>, E extends { id?: I }, I> implements AfterViewInit, OnDestroy {
  protected readonly contentViewContainerReference: Signal<ViewContainerRef> = viewChild.required('content', { read: ViewContainerRef });
  protected readonly title: WritableSignal<string> = signal('');
  protected readonly generalErrorMessage: Signal<string | null> = inject(EditEntityStateService).generalErrorMessage;

  private readonly data: D = inject<D>(MAT_DIALOG_DATA);
  private readonly notificationService: NotificationService = inject(NotificationService);
  private readonly injector: Injector = inject(Injector);
  private readonly matDialogReference: MatDialogRef<EditItemDialogComponent<EditEntityDialogData<E, I>, E, I>> =
    inject<MatDialogRef<EditItemDialogComponent<EditEntityDialogData<E, I>, E, I>>>(MatDialogRef);
  private componentReference?: ComponentRef<EditEntityComponent<E, I>>;

  public ngAfterViewInit(): void {
    const componentReference: ComponentRef<EditEntityComponent<E, I>> = this.contentViewContainerReference().createComponent(
      this.data.componentType,
    );
    this.componentReference = componentReference;

    if (this.data.idOrEntity) {
      componentReference.instance.set(this.data.idOrEntity);
    }

    this.matDialogReference.updateSize(componentReference.instance.getDisplayWidth() + 48 + 'px');

    // The hosted component only knows its title once it has been initialised, so defer the read
    // past the current render instead of writing to a signal mid-check.
    afterNextRender((): void => this.title.set(componentReference.instance.getFormTitle()), { injector: this.injector });
  }

  public ngOnDestroy(): void {
    this.componentReference?.destroy();
  }

  protected onSaveClicked(): void {
    this.componentReference?.instance
      .onSubmitButtonClicked()
      .pipe(take(1))
      .subscribe((onSuccessfulSubmit: OnSuccessfulSubmit): void => {
        this.notificationService.onSuccess(onSuccessfulSubmit.feedbackMessage);
        if (onSuccessfulSubmit.closeContainer) {
          setTimeout((): void => {
            this.matDialogReference.close();
          }, 1000);
        }
      });
  }
}

export interface EditEntityDialogData<E extends { id?: I }, I> {
  componentType: Type<EditEntityComponent<E, I>>;

  idOrEntity?: I | E;
}
