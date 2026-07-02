import {Component, inject} from '@angular/core';
import { AuthService } from '../security/auth.service';
import {MatDividerModule} from "@angular/material/divider";
import {MatButtonModule} from "@angular/material/button";
import {MatCardModule} from "@angular/material/card";
import {MatToolbarModule} from "@angular/material/toolbar";
import {JsonPipe} from "@angular/common";
import {MatIconModule} from "@angular/material/icon";
import {MatTooltipModule} from "@angular/material/tooltip";

@Component({
    selector: 'mra-home',
    imports: [
        JsonPipe,
        MatToolbarModule,
        MatIconModule,
        MatButtonModule,
        MatTooltipModule,
        MatCardModule,
        MatButtonModule,
        MatDividerModule
    ],
    templateUrl: './landing.component.html',
    styleUrl: './landing.component.scss'
})
export class LandingComponent {

    private readonly authService: AuthService = inject(AuthService);

    public login(): void {
        this.authService.login();
    }

    public logout(): void {
        this.authService.logout();
    }

    public get isLoggedIn(): boolean {
        return this.authService.isLoggedIn;
    }

    public get token(): unknown {
        return this.authService.token;
    }

}