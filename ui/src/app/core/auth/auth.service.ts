import {inject, Injectable} from '@angular/core';
import {filter} from 'rxjs';
import {OAuthService} from 'angular-oauth2-oidc';
import {authConfig} from '@core/auth/auth.config';

@Injectable({providedIn: 'root'})
export class AuthService {
    private readonly oAuthService: OAuthService = inject(OAuthService);

    private initialized = false;

    public async initialize(): Promise<void> {
        if (this.initialized) {
            return;
        }

        this.oAuthService.configure(authConfig);
        await this.oAuthService.loadDiscoveryDocumentAndTryLogin();
        this.oAuthService.setupAutomaticSilentRefresh();
        this.oAuthService.events.pipe(filter((event) => event.type === 'token_refresh_error')).subscribe((): void => {
            this.oAuthService.initLoginFlow();
        });
        this.initialized = true;
    }

    public login(): void {
        this.oAuthService.initLoginFlow();
    }

    public logout(): void {
        this.oAuthService.logOut();
    }

    public get token(): unknown {
        if (!this.oAuthService.getAccessToken()) {
            return null;
        }

        return JSON.parse(atob(this.oAuthService.getAccessToken().split('.')[1]));
    }

    public get isLoggedIn(): boolean {
        return this.oAuthService.hasValidAccessToken();
    }

    public get userName(): string {
        return this.oAuthService.getIdentityClaims()['name'] as string
    }
}
