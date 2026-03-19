import { Injectable }       from '@angular/core';
import { HttpClient }       from '@angular/common/http';
import { Observable }       from 'rxjs';
import { environment }      from '../../environments/environment';

// Interface TypeScript = type du UserResponse Java
export interface UserResponse {
  id:          string;
  keycloakId:  string;
  email:       string;
  firstName:   string;
  lastName:    string;
  status:      string;
  createdAt:   string;
}

export interface CreateUserRequest {
  email:     string;
  firstName: string;
  lastName:  string;
  password:  string;
}

export interface UpdateUserRequest {
  firstName?: string;
  lastName?:  string;
}

/**
 * Ce service appelle l'API Spring Boot.
 * Le token JWT est ajouté AUTOMATIQUEMENT par keycloak-angular
 * sur toutes les requêtes (configuré dans app.config.ts).
 */
@Injectable({ providedIn: 'root' })
export class UserService {

  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // GET /api/users/me — profil de l'utilisateur connecté
  getMyProfile(): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.apiUrl}/users/me`);
  }

  // POST /api/users — inscription (public, pas de token requis)
  register(request: CreateUserRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(`${this.apiUrl}/users`, request);
  }

  // GET /api/users — liste tous les users (admin seulement)
  getAllUsers(page = 0, size = 20): Observable<any> {
    return this.http.get(`${this.apiUrl}/users?page=${page}&size=${size}`);
  }

  // PATCH /api/users/{id}
  updateUser(keycloakId: string, request: UpdateUserRequest): Observable<UserResponse> {
    return this.http.patch<UserResponse>(
      `${this.apiUrl}/users/${keycloakId}`, request);
  }

  // DELETE /api/users/{id}
  deleteUser(keycloakId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/users/${keycloakId}`);
  }
}
