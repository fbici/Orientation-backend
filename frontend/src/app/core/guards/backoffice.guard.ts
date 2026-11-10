import {inject} from '@angular/core';
import {CanActivateFn,CanActivateChildFn,Router} from '@angular/router';
import {AuthService} from '../services/auth.service';
export const backofficeGuard: CanActivateFn & CanActivateChildFn = () => {
 const auth=inject(AuthService),router=inject(Router);
 return auth.hasAnyRole(['SUPER_ADMIN','ADMIN','MODERATEUR','REPRESENTANT_UNIVERSITAIRE'])
   ? true : router.createUrlTree(['/mobile-access']);
};
