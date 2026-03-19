import { Component, OnInit }  from '@angular/core';
import { CommonModule }       from '@angular/common';
import { UserService, UserResponse } from '../../services/user.service';
import { AuthService }        from '../../services/auth.service';
import { Observable }         from 'rxjs';

@Component({
  selector:    'app-user-profile',
  standalone:  true,
  imports:     [CommonModule],
  templateUrl: './user-profile.component.html',
})
export class UserProfileComponent implements OnInit {

  user$!: Observable<UserResponse>;

  constructor(
    private userService: UserService,
    public  authService: AuthService
  ) {}

  ngOnInit(): void {
    // Appel GET /api/users/me → token JWT ajouté automatiquement
    this.user$ = this.userService.getMyProfile();
  }
}
