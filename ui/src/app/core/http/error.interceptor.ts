import { HttpErrorResponse, HttpEvent, HttpHandlerFn, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, Observable, throwError } from 'rxjs';
import { ErrorService } from '@core/error/error.service';

/**
 * Feeds every failed response into {@link ErrorService}, which fans 422 responses out as
 * per-field validation errors that the edit-entity components subscribe to.
 */
export const errorInterceptor: HttpInterceptorFn = (
    request: HttpRequest<unknown>,
    next: HttpHandlerFn,
): Observable<HttpEvent<unknown>> => {
    const errorService: ErrorService = inject(ErrorService);

    return next(request).pipe(
        catchError((httpErrorResponse: HttpErrorResponse): Observable<never> => {
            errorService.handleHttpErrorResponse(httpErrorResponse);
            return throwError((): HttpErrorResponse => httpErrorResponse);
        }),
    );
};
