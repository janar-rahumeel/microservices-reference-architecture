import { Routes } from '@angular/router';
import { AuthenticationComponent } from './pages/authentication/authentication.component';

export const authRoutes: Routes = [
    {
        path: '',
        component: AuthenticationComponent,
    },
];
