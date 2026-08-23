import { HttpEvent, HttpHandlerFn, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { Observable } from 'rxjs';
import { OAuthService } from 'angular-oauth2-oidc';
import { authConfig } from '@core/auth/auth.config';

const TOKEN_ELIGIBLE_API_PATTERN: RegExp = /\/api\/v\d+(?:\/|$)/;
const ISSUER_ORIGIN: string = new URL(authConfig.issuer!).origin;

export const authInterceptor: HttpInterceptorFn = (
    request: HttpRequest<unknown>,
    next: HttpHandlerFn,
): Observable<HttpEvent<unknown>> => {
    if (request.url.startsWith(ISSUER_ORIGIN)) {
        // Experimental token-microservice support: use the browser session
        // instead of exposing authentication tokens to the browser
        request = request.clone({ withCredentials: true });
    }

    if (!TOKEN_ELIGIBLE_API_PATTERN.test(request.url)) {
        return next(request);
    }

    // Experimental token-microservice support: use the browser session
    // instead of exposing authentication tokens to the browser
    request = request.clone({ withCredentials: true });

    const token: string = inject(OAuthService).getAccessToken();
    if (!token) {
        return next(request);
    }

    return next(
        request.clone({
            setHeaders: {
                Authorization: `Bearer ${token}`,
            },
        }),
    );
};
