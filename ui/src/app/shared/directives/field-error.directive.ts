import { ChangeDetectorRef, DestroyRef, Directive, ElementRef, inject, input, InputSignal, OnInit } from '@angular/core';
import { AbstractControl, FormControlStatus, FormGroup, FormGroupDirective, NgForm, ValidationErrors } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Directive({
  selector: '[mraFormControlName]',
})
export class FieldErrorDirective implements OnInit {
  public controlName: InputSignal<string> = input.required({ alias: 'mraFormControlName' });

  private readonly formGroupDirective: FormGroupDirective | null = inject(FormGroupDirective, { optional: true });
  private readonly ngForm: NgForm | null = inject(NgForm, { optional: true });
  private readonly elementReference: ElementRef<HTMLElement> = inject(ElementRef<HTMLElement>);
  private readonly changeDetectorReference: ChangeDetectorRef = inject(ChangeDetectorRef);
  private readonly destroyReference: DestroyRef = inject(DestroyRef);

  public ngOnInit(): void {
    const form: FormGroup | undefined = this.formGroupDirective?.form ?? this.ngForm?.form;
    if (!form) {
      return;
    }

    const control: AbstractControl | null = form.get(this.controlName());
    if (!control) {
      return;
    }

    control.statusChanges.pipe(takeUntilDestroyed(this.destroyReference)).subscribe((formControlStatus: FormControlStatus): void => {
      if (formControlStatus === 'VALID') {
        this.elementReference.nativeElement.textContent = '';
        this.changeDetectorReference.markForCheck();
      }

      if (formControlStatus === 'INVALID') {
        const validationErrors: ValidationErrors | null = control.errors;
        if (validationErrors) {
          const validationErrorKey: string = Object.keys(validationErrors)[0];
          this.elementReference.nativeElement.textContent = FieldErrorDirective.resolveErrorMessage(
            validationErrorKey,
            validationErrors[validationErrorKey],
          );
          this.changeDetectorReference.markForCheck();
        }
      }
    });
  }

  private static resolveErrorMessage(key: string, value: unknown): string {
    if (key === 'required') {
      return 'Required';
    }

    return typeof value === 'string' ? value : 'Invalid input';
  }
}
