import { ApplicationConfig, APP_INITIALIZER } from '@angular/core';
import { provideRouter }       from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { HTTP_INTERCEPTORS }   from '@angular/common/http';
import { KeycloakService, KeycloakBearerInterceptor } from 'keycloak-angular';
import { routes }              from './app.routes';
import { environment }         from '../environments/environment';

function initializeKeycloak(keycloak: KeycloakService) {
  return () => keycloak.init({
    config: {
      url:      environment.keycloak.url,
      realm:    environment.keycloak.realm,
      clientId: environment.keycloak.clientId,
    },
    initOptions: {
      // login-required = forcer la connexion avant que l'app charge
      // le token est garanti disponible dès le démarrage
      onLoad:           'login-required',
      pkceMethod:       'S256',
      checkLoginIframe: false,
    },
    enableBearerInterceptor: true,
    bearerExcludedUrls: ['/assets', '/public'],
  }).catch(err => {
    console.warn('Keycloak init failed:', err);
  });
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptorsFromDi()),
    KeycloakService,
    {
      provide:  HTTP_INTERCEPTORS,
      useClass: KeycloakBearerInterceptor,
      multi:    true,
    },
    {
      provide:    APP_INITIALIZER,
      useFactory: initializeKeycloak,
      multi:      true,
      deps:       [KeycloakService],
    },
  ],
};
