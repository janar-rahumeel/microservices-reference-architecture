import { AuthConfig } from 'angular-oauth2-oidc';

export const authConfig: AuthConfig = {
    issuer: 'https://kc.mra.local:8443/realms/mra',
    redirectUri: window.location.origin + '/auth/callback',
    clientId: 'mra-ui',
    responseType: 'code',
    scope: 'profile email',
    requireHttps: true
};
