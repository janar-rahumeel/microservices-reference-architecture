import {Component, inject} from '@angular/core';
import { AuthService } from '../platform/security/auth.service';
import {RouterOutlet} from "@angular/router";

@Component({
    selector: 'mra-app-root',
    imports: [RouterOutlet],
    template: '<router-outlet />'
})
export class AppComponent {
}