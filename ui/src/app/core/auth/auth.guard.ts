import { inject } from '@angular/core';
import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = (): boolean | UrlTree => {
    if (inject(AuthService).isLoggedIn) {
        return true;
    }

    return inject(Router).createUrlTree(['/authentication']);
};
