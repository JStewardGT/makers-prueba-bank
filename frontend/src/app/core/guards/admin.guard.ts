import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const adminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated() && authService.isAdmin()) {
    return true;
  }

  // Si está autenticado pero no es admin, redirigir a su portal
  if (authService.isAuthenticated()) {
    router.navigate(['/user/loans']);
  } else {
    router.navigate(['/login']);
  }
  return false;
};
