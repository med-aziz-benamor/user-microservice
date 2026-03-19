import { Component }    from '@angular/core';
import { AuthService }  from '../../services/auth.service';

@Component({
  selector:   'app-login',
  standalone: true,
  template: `
    <div class="login-container">
      <h1>User Microservice</h1>
      <p>Connectez-vous pour accéder à votre profil</p>
      <button class="btn-login" (click)="authService.login()">
        Se connecter avec Keycloak
      </button>
    </div>
  `,
})
export class LoginComponent {
  constructor(public authService: AuthService) {}
}
