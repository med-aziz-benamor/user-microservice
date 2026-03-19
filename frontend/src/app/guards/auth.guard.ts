import { Injectable }                                         from '@angular/core';
import { ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { KeycloakAuthGuard, KeycloakService }                 from 'keycloak-angular';

/**
 * Ce guard protège les routes privées.
 * Si tu n'es pas connecté → Keycloak te redirige vers la page de login.
 * Si tu n'as pas le bon rôle → redirection vers /forbidden.
 *
 * Utilisation dans app.routes.ts :
 * { path: 'profile', canActivate: [AuthGuard], data: { roles: [] } }
 * { path: 'admin',   canActivate: [AuthGuard], data: { roles: ['ROLE_ADMIN'] } }
 */
@Injectable({ providedIn: 'root' })
export class AuthGuard extends KeycloakAuthGuard {

  constructor(router: Router, keycloak: KeycloakService) {
    super(router, keycloak);
  }

  async isAccessAllowed(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Promise<boolean> {

    // Pas connecté → rediriger vers Keycloak login
    if (!this.authenticated) {
      await this.keycloakAngular.login({
        redirectUri: window.location.href
      });
      return false;
    }

    // Vérifier les rôles requis (définis dans data.roles de la route)
    const requiredRoles = route.data['roles'] as string[];

    // Pas de rôles requis → accès autorisé
    if (!requiredRoles || requiredRoles.length === 0) return true;

    // Vérifier que l'utilisateur a AU MOINS UN des rôles requis
    return requiredRoles.some(role => this.roles.includes(role));
  }
}
