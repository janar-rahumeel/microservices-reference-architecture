import { Component, inject } from '@angular/core';
import { MatDividerModule } from '@angular/material/divider';
import { MatCardModule } from '@angular/material/card';
import { JsonPipe } from '@angular/common';
import { AuthService } from '@core/auth/auth.service';

@Component({
    selector: 'mra-authentication',
    templateUrl: './authentication.component.html',
    styleUrl: './authentication.component.scss',
    imports: [JsonPipe, MatCardModule, MatDividerModule],
})
export class AuthenticationComponent {
    private readonly authService: AuthService = inject(AuthService);

    public get isLoggedIn(): boolean {
        return this.authService.isLoggedIn;
    }

    public get token(): unknown {
        return this.authService.token;
    }
}
