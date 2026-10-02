import {Injectable} from '@angular/core';
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {AuthService} from '../services/auth.service';
import {Observable} from 'rxjs';
import {Globals} from '../global/globals';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService, private globals: Globals) {
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const authUri = this.globals.backendUri + '/authentication';
    const signupUri = this.globals.backendUri + '/signup';
    const positionUrl = this.globals.backendUri + '/tool/position';

    // Do not intercept requests
    if (req.url === authUri || req.url === signupUri || req.url.startsWith(positionUrl) || !req.url.startsWith(this.globals.backendUri)) {
      return next.handle(req);
    }

    console.log('intercepting request.');

    const authReq = req.clone({
      headers: req.headers.set('Authorization', 'Bearer ' + this.authService.getToken())
    });

    return next.handle(authReq);
  }
}
