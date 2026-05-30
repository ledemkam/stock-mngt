import {inject, Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {LoginReponse} from '@app/api-services/models/login-reponse';
import {JwtHelperService} from '@auth0/angular-jwt';

@Injectable({
  providedIn: 'root',
})
export class TokenService {
  readonly #router = inject(Router);

  saveToken(loginResponse:LoginReponse){
    localStorage.setItem('token', loginResponse.accessToken!);
  }

  token(){
    return localStorage.getItem('token');
  }

  get userRole(): string{
    const token = this.token()
    if (token){
      const jwtHelper = new JwtHelperService();
      const decodedToken = jwtHelper.decodeToken(token);
      return decodedToken['role'];
    }
    return '';
  }

  get isPlatformAdmin(): boolean {
    return  this.userRole === 'ROLE_PLATFORM_ADMIN';
  }

  get isCompagnyAdmin(): boolean {
    return  this.userRole === 'ROLE_COMPAGNY_ADMIN';
  }

  get isAdministrator(): boolean {
    return  this.userRole === 'ROLE_ADMINISTRATOR';
  }

  get isUser(): boolean {
    return  this.userRole === 'ROLE_USER';
  }

  get isSalesOperator(): boolean {
    return  this.userRole === 'ROLE_SALES_OPERATOR';
  }

  get isTokenValid(): boolean {
    const token = this.token();
    if (token) {
      const jwtHelper = new JwtHelperService();
      const isExpired = jwtHelper.isTokenExpired(token);
      if (isExpired){
        this.logout();
        return false;
      }
    }
    return true;
  }

  async  logout() {
    await this.#router.navigate(['login']);
    return localStorage.clear()
  }

  clearToken(){
    localStorage.clear()
  }
}
