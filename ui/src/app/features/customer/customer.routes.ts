import { Routes } from '@angular/router';
import { authGuard } from '@core/auth/auth.guard';
import { CustomerListComponent } from './pages/customer-list/customer-list.component';

export const customerRoutes: Routes = [
    {
        path: '',
        component: CustomerListComponent,
        canActivate: [authGuard],
    },
];
