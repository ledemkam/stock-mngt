import {inject} from '@angular/core';
import {TokenService} from '@app/core/token/token-service';
import {HttpHeaders, HttpInterceptorFn} from '@angular/common/http';

export const httpInterceptorInterceptor: HttpInterceptorFn = (req, next) => {
  const tokentService = inject(TokenService);
  let authReq = req;
  const token = tokentService.token();

  if (token) {
    authReq = req.clone({
      headers: new HttpHeaders({
        Authorization: `Bearer ${token}`,
      }),
    });
    return next(authReq);
  }

  return next(req);
};
