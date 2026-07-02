import {Component, inject, OnInit} from '@angular/core';
import {Router} from '@angular/router';

@Component({
    selector: 'mra-auth-callback',
    template: `<p>Logging in/out ...</p>`
})
export class AuthCallbackComponent implements OnInit {

    private readonly router: Router = inject(Router);

    public ngOnInit() {
        this.router.navigate(['/']);
    }
}