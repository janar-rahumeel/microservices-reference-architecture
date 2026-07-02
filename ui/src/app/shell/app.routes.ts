import {Routes} from '@angular/router';
import {AuthCallbackComponent} from '../platform/security/auth-callback.component';
import {LandingComponent} from "../platform/landing/landing.component";

export const routes: Routes = [
    {
        path: '', component: LandingComponent,
    },
    {
        path: 'auth/callback', component: AuthCallbackComponent
    },
];