import { ApplicationConfig, APP_INITIALIZER } from '@angular/core';
import { provideRouter }                       from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { KeycloakService }                     from 'keycloak-angular';
import { routes }                              from './app.routes';
import { environment }                         from '../environments/environment';

function initializeKeycloak(keycloak: KeycloakService) {
  return () => keycloak.init({
    config: {
      url:      environment.keycloak.url,
      realm:    environment.keycloak.realm,
      clientId: environment.keycloak.clientId,
    },
    initOptions: {
      onLoad: 'check-sso',
      silentCheckSsoRedirectUri:
        window.location.origin + '/silent-check-sso.html', // ← public/ pas assets/
      pkceMethod: 'S256',
      checkLoginIframe: false, // ← désactiver l'iframe check (évite le timeout)
    },
    enableBearerInterceptor: true,
    bearerExcludedUrls: ['/assets', '/public'],
  }).catch(err => {
    // Si Keycloak n'est pas encore lancé → l'app continue quand même
    console.warn('Keycloak init failed (is it running?)', err);
  });
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptorsFromDi()),
    KeycloakService,
    {
      provide:    APP_INITIALIZER,
      useFactory: initializeKeycloak,
      multi:      true,
      deps:       [KeycloakService],
    },
  ],
};
