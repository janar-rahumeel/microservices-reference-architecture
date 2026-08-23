import { AbstractControl, FormGroup, ValidationErrors } from '@angular/forms';
import { Observable, Subject, take } from 'rxjs';
import { DestroyRef, Directive, effect, inject, OnInit, ProviderToken, Signal, signal, WritableSignal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { EntityService } from '@shared/data-access/entity-service.model';
import { OnSuccessfulSubmit } from '@shared/models/submit-result.model';
import { EditEntityStateService } from './edit-entity-state.service';
import { ErrorService } from '@core/error/error.service';

@Directive()
export abstract class AbstractEditEntityDirective<
  S extends EntityService<E, I>,
  E extends {
    id?: I;
  },
  I,
> implements OnInit, EditEntityComponent<E, I>
{
  protected abstract readonly entityForm: Signal<FormGroup>;
  protected readonly errorService: ErrorService = inject(ErrorService);
  protected readonly onSuccessfulSubmitSubject: Subject<OnSuccessfulSubmit> = new Subject<OnSuccessfulSubmit>();
  protected readonly editEntityStateService: EditEntityStateService =
    AbstractEditEntityDirective.injectParentOrSelf(EditEntityStateService);
  protected entity: WritableSignal<E | undefined> = signal(undefined);

  private readonly destroyReference: DestroyRef = inject(DestroyRef);
  private entityCallback: (entity: E) => void = (): void => undefined;
  private id?: I;

  protected constructor(protected readonly entityService: S) {
    effect((): void => {
      const entity: E | undefined = this.entity();
      if (entity) {
        this.entityForm().patchValue(entity);
        this.entityCallback(entity);
      }
    });
  }

  protected static injectParentOrSelf<T>(providerToken: ProviderToken<T>): T {
    const parent: T | null = inject(providerToken, { skipSelf: true, optional: true });

    if (parent !== null) {
      return parent;
    }

    return inject(providerToken);
  }

  protected abstract onInit(): void;

  protected abstract getTitle(): string;

  public ngOnInit(): void {
    this.subscribe(this.errorService.entityFieldValidationErrors$, (validationErrors: ValidationErrors): void => {
      const form: FormGroup = this.entityForm();
      Object.entries(validationErrors).forEach(([fieldName, errorMessage]): void => {
        if (form.contains(fieldName)) {
          const formFieldControl: AbstractControl | null = form.get(fieldName);
          if (formFieldControl) {
            formFieldControl.markAsDirty({ onlySelf: true });
            formFieldControl.setErrors({ server: errorMessage });
          }
        } else {
          this.editEntityStateService.onGeneralErrorMessage(errorMessage as string);
        }
      });
    });
    this.onInit();
  }

  public set(idOrEntity: I | E): void {
    if (typeof idOrEntity === 'object' && idOrEntity !== null) {
      const entity: E = idOrEntity as E;
      this.id = entity.id;
      this.entity.set(entity);
      return;
    }

    const id: I = idOrEntity as I;
    this.id = id;
    if (this.entityService.get) {
      this.subscribeOnce(this.entityService.get(id), (entity: E): void => this.entity.set(entity));
    }
  }

  public getFormTitle(): string {
    if (this.id) {
      return this.getTitle() + ' (Edit)';
    }
    return this.getTitle() + ' (New)';
  }

  public getDisplayWidth(): number {
    return 400; // Default
  }

  public onSubmitButtonClicked(): Subject<OnSuccessfulSubmit> {
    this.editEntityStateService.resetGeneralErrorMessage();

    const form: FormGroup = this.entityForm();
    if (form.valid) {
      const entity: E = this.getFormEntity();
      if (entity.id === undefined) {
        this.subscribeOnce(this.entityService.insert(entity), (): void =>
          this.onSuccessfulSubmitSubject.next({
            feedbackMessage: 'Created',
            closeContainer: true,
          }),
        );
      } else {
        this.subscribeOnce(this.entityService.update(entity), (updatedEntity: E): void => {
          this.entity.set(updatedEntity);
          this.onSuccessfulSubmitSubject.next({
            feedbackMessage: 'Updated',
            closeContainer: false,
          });
        });
      }
    }

    if (form.invalid) {
      Object.values(form.controls).forEach((control: AbstractControl): void => {
        if (control.invalid) {
          control.markAsDirty({ onlySelf: true });
          control.updateValueAndValidity({ onlySelf: true });
        }
      });
    }

    return this.onSuccessfulSubmitSubject;
  }

  protected getFormEntity(): E {
    return this.entityForm().getRawValue() as E;
  }

  protected onEntity(entityCallback: (entity: E) => void = (): void => undefined): void {
    this.onEntityOrElse(entityCallback);
  }

  protected onEntityOrElse(
    entityCallback: (entity: E) => void = (): void => undefined,
    elseCallback: () => void = (): void => undefined,
  ): void {
    if (this.id) {
      this.entityCallback = entityCallback;
    } else {
      elseCallback();
    }
  }

  protected subscribe<T>(observable: Observable<T>, callback: (object: T) => void): void {
    observable.pipe(takeUntilDestroyed(this.destroyReference)).subscribe(callback);
  }

  protected subscribeOnce<T>(observable: Observable<T>, callback: (object: T) => void): void {
    observable.pipe(take(1)).subscribe(callback);
  }
}

export interface EditEntityComponent<E extends { id?: I }, I> {
  set(idOrEntity: I | E): void;

  getFormTitle(): string;

  getDisplayWidth(): number;

  onSubmitButtonClicked(): Observable<OnSuccessfulSubmit>;
}
