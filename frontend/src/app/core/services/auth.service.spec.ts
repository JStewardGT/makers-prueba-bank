import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'login', component: class {} }])
      ]
    });

    service = TestBed.inject(AuthService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
    localStorage.clear();
  });

  it('debe iniciar sin autenticación si no hay token en localStorage', () => {
    expect(service.isAuthenticated()).toBe(false);
    expect(service.currentUser()).toBeNull();
  });

  it('debe actualizar los signals tras login exitoso', () => {
    service.login({ email: 'usuario@test.com', password: '123' }).subscribe((res) => {
      expect(res.token).toBe('test-token');
      expect(service.isAuthenticated()).toBe(true);
      expect(service.currentUser()?.email).toBe('usuario@test.com');
      expect(service.isAdmin()).toBe(false);
    });

    const req = httpTesting.expectOne('/api/v1/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush({
      token: 'test-token',
      tokenType: 'Bearer',
      userId: 1,
      email: 'usuario@test.com',
      fullName: 'Usuario Demo',
      role: 'ROLE_USER'
    });
  });

  it('debe limpiar signals y localStorage al cerrar sesión', () => {
    service.login({ email: 'admin@test.com', password: '123' }).subscribe();
    const req = httpTesting.expectOne('/api/v1/auth/login');
    req.flush({
      token: 'admin-token',
      tokenType: 'Bearer',
      userId: 2,
      email: 'admin@test.com',
      fullName: 'Admin',
      role: 'ROLE_ADMIN'
    });

    expect(service.isAuthenticated()).toBe(true);
    expect(service.isAdmin()).toBe(true);

    service.logout();

    expect(service.isAuthenticated()).toBe(false);
    expect(service.currentUser()).toBeNull();
    expect(service.token()).toBeNull();
  });
});
