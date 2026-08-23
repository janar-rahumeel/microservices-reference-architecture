import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
    selector: 'mra-app-root',
    imports: [RouterOutlet],
    template: '<router-outlet />',
})
export class AppComponent {}
