import { Component, OnInit }       from '@angular/core';
import { CommonModule, DatePipe }  from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { KeycloakService }         from 'keycloak-angular';
import { environment }             from '../../../environments/environment';

export interface UserResponse {
  id: string;
  keycloakId: string;
  email: string;
  firstName: string;
  lastName: string;
  status: string;
  createdAt: string;
}

@Component({
  selector:    'app-user-profile',
  standalone:  true,
  imports:     [CommonModule, DatePipe],
  templateUrl: './user-profile.component.html',
})
export class UserProfileComponent implements OnInit {

  user: UserResponse | null = null;
  error: string | null = null;
  loading = true;

  constructor(
    private http:     HttpClient,
    private keycloak: KeycloakService
  ) {}

  async ngOnInit(): Promise<void> {
    try {
      const token = await this.keycloak.getToken();
      this.http.get<UserResponse>(
        `${environment.apiUrl}/users/me`,
        { headers: new HttpHeaders({ Authorization: `Bearer ${token}` }) }
      ).subscribe({
        next:  (user) => { this.user = user;  this.loading = false; },
        error: (err)  => { this.error = 'Erreur chargement profil'; this.loading = false; }
      });
    } catch (err) {
      this.error   = 'Erreur authentification';
      this.loading = false;
    }
  }

  logout(): void {
    this.keycloak.logout(window.location.origin);
  }
}
