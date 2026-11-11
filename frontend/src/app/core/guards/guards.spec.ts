import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { signal } from '@angular/core';
import { vi } from 'vitest';
import { AuthService } from '../services/auth.service';
import { authGuard } from './auth.guard';
import { adminGuard } from './admin.guard';
import { User } from '../models/auth.model';

describe('Guards', () => {
  let routerMock: { navigate: any };
  let authServiceMock: {
    isAuthenticated: any;
    isAdmin: any;
    currentUser: ReturnType<typeof signal<User | null>>;
  };

  beforeEach(() => {
    routerMock = {
      navigate: vi.fn()
    };
    authServiceMock = {
      isAuthenticated: vi.fn(),
      isAdmin: vi.fn(),
      currentUser: signal<User | null>(null)
    };

    TestBed.configureTestingModule({
      providers: [
        { provide: Router, useValue: routerMock },
        { provide: AuthService, useValue: authServiceMock }
      ]
    });
  });

  describe('authGuard', () => {
    it('debe permitir acceso si está autenticado', () => {
      authServiceMock.isAuthenticated.mockReturnValue(true);
      const result = TestBed.runInInjectionContext(() => authGuard({} as any, {} as any));
      expect(result).toBe(true);
      expect(routerMock.navigate).not.toHaveBeenCalled();
    });

    it('debe bloquear y redirigir a /login si no está autenticado', () => {
      authServiceMock.isAuthenticated.mockReturnValue(false);
      const result = TestBed.runInInjectionContext(() => authGuard({} as any, {} as any));
      expect(result).toBe(false);
      expect(routerMock.navigate).toHaveBeenCalledWith(['/login']);
    });
  });

  describe('adminGuard', () => {
    it('debe permitir acceso si es administrador', () => {
      authServiceMock.isAuthenticated.mockReturnValue(true);
      authServiceMock.isAdmin.mockReturnValue(true);
      const result = TestBed.runInInjectionContext(() => adminGuard({} as any, {} as any));
      expect(result).toBe(true);
    });

    it('debe redirigir a /user/loans si está autenticado pero no es admin', () => {
      authServiceMock.isAuthenticated.mockReturnValue(true);
      authServiceMock.isAdmin.mockReturnValue(false);
      const result = TestBed.runInInjectionContext(() => adminGuard({} as any, {} as any));
      expect(result).toBe(false);
      expect(routerMock.navigate).toHaveBeenCalledWith(['/user/loans']);
    });
  });
});
