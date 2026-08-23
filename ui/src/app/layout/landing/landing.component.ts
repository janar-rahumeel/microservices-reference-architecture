import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatTabsModule } from '@angular/material/tabs';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '@core/auth/auth.service';

@Component({
    selector: 'mra-landing',
    templateUrl: './landing.component.html',
    styleUrl: './landing.component.scss',
    imports: [
        RouterLink,
        RouterLinkActive,
        RouterOutlet,
        MatToolbarModule,
        MatIconModule,
        MatButtonModule,
        MatTooltipModule,
        MatTabsModule,
    ],
})
export class LandingComponent {
    protected readonly tabs: { title: string; url: string }[] = [
        { title: 'Authentication', url: '/authentication' },
        { title: 'Customers', url: '/customers' },
    ];

    private readonly authService: AuthService = inject(AuthService);

    public get isLoggedIn(): boolean {
        return this.authService.isLoggedIn;
    }

    public get userName(): string {
        return this.authService.userName;
    }

    public login(): void {
        this.authService.login();
    }

    public logout(): void {
        this.authService.logout();
    }
}
