import { Injectable } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';
import { from, Observable } from 'rxjs';

/**
 * Façade sur KeycloakService — simplifie l'utilisation dans les composants.
 * Au lieu d'injecter KeycloakService partout, on injecte juste AuthService.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {

  constructor(private keycloak: KeycloakService) {}

  login(): void {
    this.keycloak.login();
  }

  logout(): void {
    // Rediriger vers la page d'accueil après déconnexion
    this.keycloak.logout(window.location.origin);
  }

  isLoggedIn(): boolean {
    return this.keycloak.isLoggedIn();
  }

  getUsername(): string {
    return this.keycloak.getUsername();
  }

  getUserRoles(): string[] {
    return this.keycloak.getUserRoles();
  }

  hasRole(role: string): boolean {
    return this.keycloak.getUserRoles().includes(role);
  }

  isAdmin(): boolean {
    return this.hasRole('ROLE_ADMIN');
  }

  // getToken() est async — on le transforme en Observable
  getToken(): Observable<string> {
    return from(this.keycloak.getToken());
  }

  getUserProfile(): Observable<Keycloak.KeycloakProfile> {
    return from(this.keycloak.loadUserProfile());
  }
}
