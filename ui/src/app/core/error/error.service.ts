import { Injectable } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { ValidationErrors } from '@angular/forms';
import { ApiError } from '@core/models/api-error.model';

@Injectable({ providedIn: 'root' })
export class ErrorService {
  private readonly globalErrorsSubject: Subject<ApiError> = new Subject<ApiError>();
  private readonly entityFieldValidationErrorsSubject: Subject<ValidationErrors> = new Subject<ValidationErrors>();

  public readonly globalErrors$: Observable<ApiError> = this.globalErrorsSubject.asObservable();
  public readonly entityFieldValidationErrors$: Observable<ValidationErrors> = this.entityFieldValidationErrorsSubject.asObservable();

  public handleHttpErrorResponse(httpErrorResponse: HttpErrorResponse): void {
    if (httpErrorResponse.error instanceof ErrorEvent) {
      const error: ApiError = this.buildError('CLIENT', httpErrorResponse.error.message);
      this.push(error);
      return;
    }

    switch (httpErrorResponse.status) {
      case 0: {
        const error: ApiError = this.buildError('NETWORK', 'Connection error');
        this.push(error);
        break;
      }
      case 422:
        this.handleFieldErrors(httpErrorResponse);
        break;
      default: {
        const error: ApiError | undefined = httpErrorResponse.error as ApiError | undefined;
        this.push(error?.entityFieldValidationErrors ? error : this.buildError('EXCEPTION', 'Unknown error'));
      }
    }
  }

  private buildError(uuid: string, message: string): ApiError {
    return {
      uuid: uuid,
      timestamp: new Date(),
      message: message,
      entityFieldValidationErrors: [],
    };
  }

  private push(error: ApiError): void {
    console.error(`${String(error.timestamp)} [ERROR_UUID:${error.uuid ?? 'UNKNOWN'}] ${error.message ?? 'Unknown error'}`);
    this.globalErrorsSubject.next(error);
  }

  private handleFieldErrors(httpErrorResponse: HttpErrorResponse): void {
    const error: ApiError = httpErrorResponse.error as ApiError;
    const validationErrors: ValidationErrors = {};
    for (const fieldError of error.entityFieldValidationErrors) {
      validationErrors[fieldError.fieldName] = fieldError.validationErrorMessage;
    }
    this.entityFieldValidationErrorsSubject.next(validationErrors);
  }
}
