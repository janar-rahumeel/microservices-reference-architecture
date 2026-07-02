import {inject, Injectable} from '@angular/core';
import {OAuthService} from 'angular-oauth2-oidc';
import {authConfig} from './auth.config';

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

        this.initialized = true;
    }

    public login(): void {
        this.oAuthService.initLoginFlow();
    }

    public logout(): void {
        this.oAuthService.logOut();
    }

    get token(): unknown {
        if (!this.oAuthService.getAccessToken()) {
            return null;
        }

        return JSON.parse(atob(this.oAuthService.getAccessToken().split('.')[1]));
    }

    get isLoggedIn(): boolean {
        return this.oAuthService.hasValidAccessToken();
    }
}