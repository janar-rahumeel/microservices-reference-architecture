import {bootstrapApplication} from '@angular/platform-browser';
import {AppComponent} from './app/shell/app.component';
import {routes} from './app/shell/app.routes';
import {provideHttpClient, withInterceptorsFromDi} from '@angular/common/http';
import {provideAnimations} from '@angular/platform-browser/animations';
import {provideRouter, withViewTransitions} from '@angular/router';
import {ErrorStateMatcher, ShowOnDirtyErrorStateMatcher} from '@angular/material/core';
import {MAT_FORM_FIELD_DEFAULT_OPTIONS} from '@angular/material/form-field';
import {MAT_PROGRESS_BAR_DEFAULT_OPTIONS} from '@angular/material/progress-bar';
import {MAT_SNACK_BAR_DEFAULT_OPTIONS} from '@angular/material/snack-bar';
import {inject, provideAppInitializer, provideZonelessChangeDetection} from '@angular/core';
import {provideOAuthClient} from "angular-oauth2-oidc";
import {AuthService} from "./app/platform/security/auth.service";

bootstrapApplication(AppComponent, {
    providers: [
        provideHttpClient(withInterceptorsFromDi()),
        provideOAuthClient(),
        provideAnimations(),
        provideRouter(routes, withViewTransitions()),
        provideZonelessChangeDetection(),
        provideAppInitializer(async () => {
            await inject(AuthService).initialize();
        }),
        {provide: MAT_FORM_FIELD_DEFAULT_OPTIONS, useValue: {appearance: 'fill'}},
        {provide: MAT_PROGRESS_BAR_DEFAULT_OPTIONS, useValue: {color: 'primary', mode: 'indeterminate'}},
        {provide: MAT_SNACK_BAR_DEFAULT_OPTIONS, useValue: {duration: 3000}},
        {provide: ErrorStateMatcher, useClass: ShowOnDirtyErrorStateMatcher},
    ],
});
