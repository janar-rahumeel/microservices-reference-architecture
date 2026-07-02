import { AuthConfig } from 'angular-oauth2-oidc';

export const authConfig: AuthConfig = {
    issuer: 'http://localhost:8190/realms/mra',
    redirectUri: window.location.origin + '/auth/callback',
    clientId: 'mra-ui',
    responseType: 'code',
    scope: 'openid profile email',
    strictDiscoveryDocumentValidation: false,
    requireHttps: false,
    showDebugInformation: true,
};