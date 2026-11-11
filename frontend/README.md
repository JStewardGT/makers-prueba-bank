# Frontend - Sistema de Gestión de Préstamos Bancarios

Aplicación web desarrollada en **Angular 22** con **Angular Material 22.2.6** y **Angular Signals** para el consumo de la API REST bancaria.

---

## 🚀 Tecnologías y Características

- **Framework:** Angular 22 (Standalone Components, Signals & Router).
- **Componentes UI:** Angular Material 22.2.6 (`@angular/material`, `@angular/cdk`).
- **Gestión de Estado:** **Angular Signals** (`signal`, `computed`, `effect`) nativo para un flujo reactivo predecible, seguro y sin sobrecarga de boilerplate.
- **Separador de Miles (`currency`):**
  - Directiva personalizada `[appCurrencyMask]` que formatea el monto en tiempo real con separador de miles (ej. `$ 1,000,000`) mientras se escribe, enviando al `FormControl` el valor numérico puro.
  - Pipe `currencyFormat` para despliegue visual de montos en listas y resúmenes.
- **Seguridad y JWT:**
  - `jwtInterceptor`: Inyecta automáticamente el token Bearer en las cabeceras HTTP de peticiones protegidas y redirige a `/login` ante respuestas `401 Unauthorized`.
  - `authGuard`: Protege rutas que requieren inicio de sesión.
  - `adminGuard`: Protege rutas exclusivas para administradores (`ROLE_ADMIN`).
  - `guestGuard`: Redirige usuarios ya autenticados a su respectivo portal.
- **Formularios Reactivos (Reactive Forms):** Validaciones en tiempo real para límites de montos (\$100 a \$100,000,000), plazos (1 a 72 meses) y correos válidos.
- **Streaming en Tiempo Real:** Servicio conectado a Server-Sent Events (SSE) `/api/v1/loans/stream` para reflejar aprobaciones y rechazos en vivo sin recargar la página.
- **Diseño Fiel:** Vistas de Iniciar Sesión, Portal de Usuario y Portal de Administrador fieles a las especificaciones y capturas de la prueba técnica.

---

## 📁 Estructura del Proyecto

```text
frontend/src/app/
├── core/
│   ├── guards/              # authGuard, adminGuard, guestGuard
│   ├── interceptors/        # jwtInterceptor
│   ├── models/              # auth.model.ts, loan.model.ts
│   └── services/            # AuthService (Signals), LoanService (Signals & SSE)
├── shared/
│   ├── directives/          # CurrencyMaskDirective (separador de miles en tiempo real)
│   ├── pipes/               # CurrencyFormatPipe
│   └── components/          # RejectDialogComponent (Modal Material)
└── features/
    ├── auth/login/          # Vista Iniciar Sesión (con accesos directos de prueba)
    ├── user/user-dashboard/ # Vista Usuario: Solicitar préstamo + Mis Préstamos
    └── admin/admin-dashboard/# Vista Administrador: Gestionar Solicitudes (Aprobar/Rechazar)
```

---

## 🔑 Credenciales para Pruebas Rápidas

En la pantalla de inicio de sesión se incluyen botones de un clic para autocompletar credenciales:

| Rol | Correo | Contraseña | Redirección |
|---|---|---|---|
| **Usuario** | `usuario@test.com` | `123` | `/user/loans` |
| **Administrador** | `admin@test.com` | `123` | `/admin/loans` |

---

## 💻 Puesta en Marcha

### 1. Iniciar el Backend
Asegúrate de que el backend esté en ejecución (puerto `8081`):
```bash
cd ../backend
docker compose up -d
./mvnw spring-boot:run
```

### 2. Iniciar el Servidor de Desarrollo Frontend
```bash
cd frontend
npm start
```
*El servidor arrancará en `http://localhost:4200` utilizando el proxy configurado en `proxy.conf.json` para redirigir peticiones `/api/*` al backend en el puerto `8081` sin inconvenientes de CORS.*

### 3. Ejecutar Pruebas Unitarias
```bash
npm test -- --watch=false
```
*Ejecuta la suite de pruebas unitarias con Vitest.*

### 4. Compilar para Producción
```bash
npm run build
```
