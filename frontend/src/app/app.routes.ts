import { Routes }              from '@angular/router';
import { AuthGuard }           from './guards/auth.guard';
import { LoginComponent }      from './components/login/login.component';
import { UserProfileComponent } from './components/user-profile/user-profile.component';

export const routes: Routes = [
  // Page publique
  { path: 'login',   component: LoginComponent },

  // Page protégée — nécessite d'être connecté
  {
    path:        'profile',
    component:   UserProfileComponent,
    canActivate: [AuthGuard],
    data:        { roles: [] }   // [] = connecté mais pas de rôle spécifique requis
  },

  // Redirection par défaut
  { path: '',   redirectTo: '/profile', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' },
];
