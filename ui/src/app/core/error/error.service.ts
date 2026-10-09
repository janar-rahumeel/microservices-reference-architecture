import { Injectable } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { ValidationErrors } from '@angular/forms';
import { ApiError } from '@core/models/api-error.model';
import { translateErrorCode } from '@core/error/error-messages';

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
        this.push(error?.errorCode ? error : this.buildError('EXCEPTION', 'Unknown error'));
      }
    }
  }

  private buildError(errorCode: string, message: string): ApiError {
    return { errorCode: errorCode, message: message };
  }

  private push(error: ApiError): void {
    console.error(`${new Date().toISOString()} [ERROR_ID:${error.id ?? 'UNKNOWN'}] ${error.message ?? 'Unknown error'}`);
    this.globalErrorsSubject.next(error);
  }

  private handleFieldErrors(httpErrorResponse: HttpErrorResponse): void {
    const error: ApiError | undefined = httpErrorResponse.error as ApiError | undefined;
    const validationErrors: ValidationErrors = {};
    for (const fieldError of error?.fieldErrors ?? []) {
      validationErrors[fieldError.field] = translateErrorCode(fieldError.code, fieldError.parameters);
    }
    if (Object.keys(validationErrors).length === 0) {
      validationErrors['general'] = translateErrorCode(error?.errorCode ?? '', undefined, error?.message ?? 'Unprocessable request');
    }
    this.entityFieldValidationErrorsSubject.next(validationErrors);
  }
}
