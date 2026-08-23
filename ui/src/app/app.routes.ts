import { Routes } from '@angular/router';
import { LandingComponent } from '@layout/landing/landing.component';

export const routes: Routes = [
    {
        path: '',
        component: LandingComponent,
        children: [
            {
                path: '',
                pathMatch: 'full',
                redirectTo: 'authentication',
            },
            {
                path: 'authentication',
                loadChildren: (): Promise<Routes> => import('@features/auth/auth.routes').then((m): Routes => m.authRoutes),
            },
            {
                path: 'customers',
                loadChildren: (): Promise<Routes> => import('@features/customer/customer.routes').then((m): Routes => m.customerRoutes),
            },
        ],
    },
    {
        path: 'auth/callback',
        loadComponent: () =>
            import('@features/auth/pages/auth-callback/auth-callback.component').then((m) => m.AuthCallbackComponent),
    },
];
