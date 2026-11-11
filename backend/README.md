# API REST de Gestión de Préstamos Bancarios

Sistema bancario backend desarrollado con **Spring Boot 3.3.5** y **Java 21 (LTS)** aplicando **Arquitectura Hexagonal (Clean Architecture)**.

---

## 🛠️ Tecnologías y Características

- **Lenguaje:** Java 21 (LTS) - Uso de Records para DTOs/Value Objects inmutables, Pattern Matching y Switch Expressions.
- **Framework:** Spring Boot 3.3.5 (Spring Framework 6.1, Jakarta EE 10).
- **Arquitectura:** Hexagonal / Clean Architecture (Dominio desacoplado, Puertos y Adaptadores).
- **Seguridad:** Spring Security 6 + JJWT 0.12.6 (Autenticación sin estado, RBAC con `ROLE_USER` y `ROLE_ADMIN`, expiración configurable y CORS).
- **Persistencia:** Spring Data JPA + Hibernate 6.
- **Base de Datos:** PostgreSQL 16 (vía Docker Compose) + perfil de prueba H2 en memoria.
- **Caché:** Ehcache 3 (JSR-107 JCache) con desalojo atómico en mutaciones (`@Cacheable`, `@CacheEvict`).
- **Reactividad (Spring WebFlux):**
  - Streaming Server-Sent Events (SSE) en tiempo real (`Flux<LoanStatusEvent>`) en `/api/v1/loans/stream`.
  - Servicio de scoring crediticio reactivo no bloqueante con `WebClient`.
- **Manejo de Errores:** Centralizado con RFC 7807 (`ProblemDetail`) y códigos HTTP homogéneos.
- **Documentación Interactiva:** OpenAPI 3 / Swagger UI con botón de autenticación Bearer JWT.
- **Testing:** 30 tests unitarios y de integración con JUnit 5, Mockito y Spring Security Test.

---

## 🏛️ Estructura del Proyecto (Arquitectura Hexagonal)

```text
backend/src/main/java/com/makers/loansystem/
├── domain/                      # NÚCLEO: Lógica pura de negocio (Sin dependencias externas)
│   ├── model/                  # Entidades y Enums (Loan, User, LoanStatus, Role)
│   ├── exception/              # Excepciones de negocio (DomainException, InvalidStatusTransitionException, etc.)
│   └── port/                   # Puertos de salida (LoanRepositoryPort, UserRepositoryPort)
│
├── application/                 # CASOS DE USO Y ORQUESTACIÓN
│   ├── port/in/                # Puertos de entrada (CreateLoanUseCase, ApproveLoanUseCase, etc.)
│   ├── service/                # Implementación de Casos de Uso, @Transactional, Caché y WebFlux
│   └── dto/                    # Records Java 21 (CreateLoanRequest, LoanResponse, AuthResponse, etc.)
│
└── infrastructure/              # ADAPTADORES EXTERNOS
    ├── adapter/
    │   ├── in/web/             # Controladores REST, SSE Flux y GlobalExceptionHandler (RFC 7807)
    │   └── out/persistence/    # Entidades JPA, Repositorios Spring Data y Adaptadores de Repositorio
    ├── config/                 # SecurityConfig, CacheConfig (Ehcache), OpenApiConfig, DataInitializer
    └── security/               # Filtro JWT, TokenProvider, CustomUserDetailsService
```

---

## 🔑 Credenciales Semilla Preconfiguradas

Al arrancar la aplicación, se siembran automáticamente los usuarios y solicitudes de prueba:

| Rol | Correo Electrónico | Contraseña | Permisos |
|---|---|---|---|
| **Administrador** | `admin@test.com` | `123` | Aprobar, rechazar y consultar todas las solicitudes del sistema. |
| **Usuario Estándar** | `usuario@test.com` | `123` | Solicitar préstamos y consultar únicamente sus préstamos. |

---

## 🚀 Puesta en Marcha

### 1. Iniciar Base de Datos PostgreSQL con Docker Compose
```bash
docker compose up -d
```

### 2. Ejecutar la Aplicación Spring Boot
```bash
./mvnw spring-boot:run
```
*El servidor iniciará en el puerto `8081` (o en el configurado en la variable de entorno `PORT`).*

### 3. Acceder a Swagger UI (Documentación Interactiva)
Abre en tu navegador:
👉 **[http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)**

Para probar endpoints protegidos desde Swagger:
1. Ejecuta el endpoint `POST /api/v1/auth/login` con las credenciales de `admin@test.com` o `usuario@test.com`.
2. Copia el token de la respuesta.
3. Haz clic en el botón superior verde **Authorize** y pega el token JWT.

---

## 📡 Endpoints de la API

### Autenticación
| Método | Endpoint | Acceso | Descripción |
|---|---|---|---|
| `POST` | `/api/v1/auth/login` | Público | Iniciar sesión y obtener token JWT Bearer. |
| `POST` | `/api/v1/auth/register` | Público | Registrar un nuevo usuario (`ROLE_USER`). |

### Préstamos Bancarios
| Método | Endpoint | Acceso | Descripción |
|---|---|---|---|
| `POST` | `/api/v1/loans` | USER / ADMIN | Solicitar un nuevo préstamo (Monto: \$100 - \$100,000,000, Plazo: 1 - 72 meses). |
| `GET` | `/api/v1/loans/my-loans` | USER / ADMIN | Consultar historial y estado de préstamos del usuario autenticado. |
| `GET` | `/api/v1/loans/{id}` | Propietario / ADMIN | Consultar detalle de préstamo por ID (Optimizado con Ehcache). |
| `GET` | `/api/v1/loans` | ADMIN | Listar todas las solicitudes del sistema. |
| `PATCH` | `/api/v1/loans/{id}/approve` | ADMIN | Aprobar solicitud (Transaccional, valida estado `PENDING`, invalida caché). |
| `PATCH` | `/api/v1/loans/{id}/reject` | ADMIN | Rechazar solicitud con motivo opcional (Transaccional, invalida caché). |
| `GET` | `/api/v1/loans/stream` | Público / Cliente | Stream reactivo SSE (`Flux<LoanStatusEvent>`) para notificaciones en vivo. |

### Gestión de Usuarios (Admin)
| Método | Endpoint | Acceso | Descripción |
|---|---|---|---|
| `GET` | `/api/v1/users` | ADMIN | Listar todos los usuarios registrados. |
| `GET` | `/api/v1/users/{id}` | ADMIN | Consultar usuario por ID. |
| `DELETE` | `/api/v1/users/{id}` | ADMIN | Eliminar usuario por ID. |

---

## 🧪 Ejecución de Pruebas Automatizadas

Se incluyen 30 pruebas unitarias y de integración que cubren invariantes de dominio, control de estados de préstamos, autenticación JWT, transaccionalidad y seguridad por roles:

```bash
./mvnw clean test
```
