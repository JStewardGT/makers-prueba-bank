# Sistema de Gestión de Préstamos Bancarios - Makers

Solución integral de desarrollo para la prueba técnica de **Spring Boot (Java 21)** y **Front-end (Angular 22)** con arquitectura hexagonal, seguridad JWT, caché Ehcache 3, WebFlux reactivo, Angular Material 22.2.6 y contenedores Docker para desarrollo.

---

## 🏛️ Arquitectura y Tecnologías

### Backend (`/backend`)

- **Lenguaje & Framework:** Java 21 (LTS) & Spring Boot 3.3.5 (Spring 6.1, Jakarta EE 10).
- **Arquitectura:** Hexagonal / Clean Architecture (Dominio desacoplado, Puertos de Entrada/Salida y Adaptadores).
- **Seguridad:** Spring Security 6 + JJWT 0.12.6 (Stateless, control de accesos RBAC: `ROLE_USER` y `ROLE_ADMIN`, CORS y filtros).
- **Persistencia:** Spring Data JPA + PostgreSQL 16 (con perfil H2 para tests herméticos).
- **Caché:** Ehcache 3 (JSR-107 JCache) con `@Cacheable` y `@CacheEvict` atómico.
- **Soporte Reactivo (Spring WebFlux):**
  - Streaming Server-Sent Events (SSE) en tiempo real en `/api/v1/loans/stream`.
  - Simulación de scoring crediticio asíncrono no bloqueante con `WebClient`.
- **Documentación:** Swagger / OpenAPI 3 interactivo con autenticación Bearer JWT.
- **Testing:** 30 pruebas unitarias y de integración (JUnit 5, Mockito, Spring Security Test).

### Frontend (`/frontend`)

- **Framework & UI:** Angular 22 standalone components & Angular Material 22.2.6.
- **Gestión de Estado:** **Angular Signals** (`signal`, `computed`, `effect`) nativo y reactivo.
- **Separador de Miles:** Directiva `[appCurrencyMask]` (formato en tiempo real `$ 1,000,000`) y pipe `currencyFormat`.
- **Seguridad en Rutas:** `jwtInterceptor` (inyección de Bearer Token), `authGuard` y `adminGuard`.
- **Testing:** 11 pruebas unitarias con Vitest.

---

## 🔑 Credenciales Semilla Preconfiguradas

| Perfil               | Correo Electrónico | Contraseña | Rol          | Funcionalidades                                                                    |
| -------------------- | ------------------ | ---------- | ------------ | ---------------------------------------------------------------------------------- |
| **Usuario Estándar** | `usuario@test.com` | `123`      | `ROLE_USER`  | Solicitar préstamos con monto y plazo; consultar historial propio de solicitudes.  |
| **Administrador**    | `admin@test.com`   | `123`      | `ROLE_ADMIN` | Listar todas las solicitudes del sistema, aprobar o rechazar préstamos con motivo. |

---

## 🚀 Instrucciones para Levantar el Entorno Local

Puedes iniciar todo el sistema de dos formas: mediante Docker Compose desde la carpeta raíz o ejecutando los proyectos de forma local.

### Opción 1: Levantar todo con Docker Compose (Recomendado)

Desde la carpeta raíz del repositorio:

```bash
# Construir y levantar todos los contenedores (PostgreSQL, Backend y Frontend)
docker compose up --build
```

O para ejecutar en segundo plano (detached):

```bash
docker compose up -d --build
```

Para detener los servicios:

```bash
docker compose down
```

---

### Opción 2: Ejecución Local Independiente

#### 1. Iniciar Base de Datos PostgreSQL

```bash
# Desde la raíz o la carpeta backend
docker compose up -d postgres
```

#### 2. Iniciar el Backend (Spring Boot)

```bash
cd backend
./mvnw spring-boot:run
```

_El backend iniciará en el puerto `8081`._

#### 3. Iniciar el Frontend (Angular)

```bash
cd frontend
npm install
npm start
```

_El frontend iniciará en `http://localhost:4200` y redirigirá las peticiones `/api/_`al backend mediante`proxy.conf.json`.\*

---

## 🌐 URLs de Acceso

- **Aplicación Frontend:** [http://localhost:4200](http://localhost:4200)
- **Documentación Swagger UI (Backend):** [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)
- **Especificación OpenAPI (JSON):** [http://localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs)
- **Stream Reactivo SSE:** [http://localhost:8081/api/v1/loans/stream](http://localhost:8081/api/v1/loans/stream)

---

## 🧪 Ejecución de Pruebas Automatizadas

### Backend (30 tests)

```bash
cd backend
./mvnw clean test
```

### Frontend (11 tests)

```bash
cd frontend
npm test -- --watch=false
```

---

## 📝 Parte Teórica

1.  Optimización en sistemas financieros:

    a. Imagina que trabajas en una aplicación bancaria que debe procesar transacciones en tiempo real. ¿Qué estrategias aplicarías para mejorar el rendimiento y la escalabilidad? Considera aspectos como concurrencia, caché y patrones de diseño.

    > En una aplicación bancaria en tiempo real, los dos retos principales son controlar la latencia de cola (p99) y mitigar la contención en base de datos. Antes de aplicar cualquier optimización, implementaría observabilidad con OpenTelemetry para medir métricas por endpoint y separar las cargas de lectura y escritura desde el diseño inicial.
    >
    > En cuanto a concurrencia, utilizaría Virtual Threads de Java 21 para optimizar operaciones I/O-bound (consultas a base de datos y llamadas a servicios externos) sin saturar la memoria, reservando los hilos tradicionales para tareas CPU-bound y utilizando `ReentrantLock` en lugar de bloques `synchronized` para evitar el _pinning_. Como el cuello de botella se traslada al pool de conexiones (HikariCP), lo dimensionaría en función de los núcleos disponibles y mediría el p99 de adquisición. Para proteger la integridad transaccional frente a la contención de datos, aplicaría bloqueo optimista (`@Version`) en cuentas de baja colisión y bloqueo pesimista (`SELECT ... FOR UPDATE` con ordenamiento de IDs para prevenir _deadlocks_) en operaciones críticas. Para cuentas corporativas de alto tráfico, evitaría el bloqueo directo mediante un modelo contable _append-only_ o encolamiento secuencial en Kafka particionado por `account_id`, exigiendo siempre un `Idempotency-Key` para evitar transacciones duplicadas ante reintentos de red.
    >
    > Para la estrategia de caché, implementaría una arquitectura de dos niveles: una caché local en memoria (Caffeine) para catálogos estáticos e inmutables, y una caché distribuida (Redis Cluster) para sesiones, _rate limiting_, llaves de idempotencia y tasas de cambio. Priorizaría la invalidación por eventos mediante brokers con garantía de entrega (Kafka o Redis Streams) por encima del simple uso de TTL. Por consistencia financiera, los saldos nunca se almacenan en caché en el cliente ni se utilizan desde caché para autorizar operaciones; toda validación de fondos se ejecuta contra la base de datos transaccional.
    >
    > Respecto a patrones de diseño y base de datos, implementaría Circuit Breaker y Bulkhead (con Resilience4j) para aislar fallos de integraciones externas según el tipo de operación, Transactional Outbox para garantizar la publicación confiable de eventos sin _dual-writes_, y CQRS para desacoplar las escrituras transaccionales de las consultas pesadas mediante réplicas de lectura. A nivel de persistencia, definiría índices compuestos basados en planes de ejecución reales (`EXPLAIN ANALYZE`) y particionamiento por `account_id` cuando el volumen lo requiera.
    >
    > Finalmente, en el frontend con Angular, prevendría el envío duplicado de transacciones aplicando el operador `exhaustMap` de RxJS en las acciones de pago, y optimizaría el renderizado de datos en tiempo real mediante `ChangeDetectionStrategy.OnPush` y Signals.

2.  Seguridad en APIs financieras:

    a. Explica cómo protegerías una API que maneja información sensible de cuentas bancarias contra ataques como inyección SQL, CSRF, XSS y otros ataques comunes.

    > Para proteger una API financiera que maneja información crítica, aplicaría una estrategia de defensa en profundidad bajo un enfoque Zero Trust, asegurando cada capa desde el cliente en Angular hasta la persistencia en Java con Spring Security.
    >
    > Para prevenir la inyección SQL (SQLi), prohibiría por completo la concatenación de cadenas en consultas dinámicas, utilizando exclusivamente consultas parametrizadas mediante Spring Data JPA, Criteria API o PreparedStatement. Esto lo complementaría con una validación estricta en la capa de entrada usando Bean Validation (@Valid, @Pattern, @Positive) y tipado fuerte en los DTOs antes de procesar cualquier lógica de negocio. Adicionalmente, aplicaría el principio de mínimo privilegio en la base de datos: el usuario de la aplicación solo tendría permisos operativos (SELECT, INSERT, UPDATE), bloqueando comandos DDL y restringiendo el acceso directo a tablas de auditoría.
    >
    > Frente a ataques de Cross-Site Scripting (XSS) y Cross-Site Request Forgery (CSRF), el diseño entre Angular y el backend debe trabajar en conjunto. En el frontend aprovecharía la sanitización automática del motor de renderizado de Angular, evitando el uso de DomSanitizer (bypassSecurityTrustHtml) con datos externos, e implementaría una cabecera Content-Security-Policy (CSP) estricta para impedir la ejecución de scripts no autorizados. Para proteger la sesión, descartaría el almacenamiento de tokens en localStorage o sessionStorage por ser vulnerables a robo vía XSS; en su lugar, gestionaría los JWT o tokens de sesión en cookies con los atributos HttpOnly, Secure y SameSite=Strict. Al usar cookies, mitigaría el riesgo de CSRF habilitando CookieCsrfTokenRepository en Spring Security junto con withXsrfConfiguration() en el HttpClient de Angular para validar el header X-XSRF-TOKEN en cada operación de escritura, sumado a una política de CORS restrictiva.
    >
    > Más allá de las vulnerabilidades tradicionales, en banca el riesgo más crítico es el Broken Object Level Authorization (BOLA/IDOR), donde un atacante manipula el accountId en la petición para consultar o mover fondos de terceros. Para mitigarlo, nunca confiaría en los identificadores enviados por el cliente; toda operación validaría en base de datos que la cuenta pertenezca efectivamente al usuario autenticado extraído del SecurityContextHolder.
    >
    > Finalmente, blindaría la infraestructura y el transporte mediante OAuth2/OIDC con tokens de vida corta, MFA transaccional para movimientos de dinero y mTLS para la comunicación interna entre microservicios. En el API Gateway implementaría Rate Limiting distribuido con Redis para frenar ataques de fuerza bruta o denegación de servicio, validación de Timestamp y Nonce para evitar ataques de repetición (Replay Attacks), cifrado TLS 1.3 en tránsito con HSTS y enmascaramiento automático de datos sensibles (PII/PCI) en los logs de aplicación para evitar fugas de información interna.

3.  Transacciones en sistemas distribuidos:

    a. En un sistema bancario distribuido, ¿cómo implementarías la consistencia y el manejo de errores en una API que procesa transferencias entre cuentas en diferentes servicios?

    > En un sistema bancario distribuido, donde las cuentas de origen y destino residen en diferentes microservicios o bases de datos, descartaría el protocolo de Two-Phase Commit (2PC / XA Transactions) porque introduce bloqueos distribuidos que degradan la latencia y crean un punto único de fallo. En su lugar, implementaría consistencia eventual mediante el patrón Saga por Orquestación combinado con un modelo contable de reserva en dos fases (Reserve-Commit/Cancel).
    >
    > Para coordinar el flujo en el backend con Java, un servicio orquestador gestionaría la máquina de estados de la transferencia en tres pasos: primero, solicita al servicio de origen una retención de fondos (Hold), que descuenta del saldo disponible pero no del saldo contable real; segundo, solicita el abono en el servicio de destino; y tercero, confirma el débito definitivo en origen. Este enfoque evita la anomalía de lectura sucia (dirty read) típica de las Sagas básicas: si el dinero se debitara directamente y luego la transacción fallara, la compensación parecería un ingreso nuevo o el usuario podría quedarse sin fondos temporalmente sin rastro claro. Si el servicio de destino rechaza el abono (por ejemplo, por cuenta bloqueada o inexistente), el orquestador ejecuta una transacción compensatoria que libera la retención en la cuenta origen y marca la transferencia como fallida.
    >
    > Para garantizar que ningún mensaje se pierda entre la base de datos y el broker de mensajería (Kafka), aplicaría el patrón Transactional Outbox en cada microservicio: el cambio de saldo y el evento a publicar se guardan en la misma transacción local ACID, y un proceso asíncrono (o Debezium CDC) publica el evento hacia el broker. Del lado del consumidor, todos los endpoints y listeners deben ser estrictamente idempotentes utilizando el transaction_id o Idempotency-Key almacenado en una tabla de control (Inbox pattern) o en Redis; así, si Kafka reentrega un mensaje o hay un reintento de red, el abono o la compensación se ejecuta exactamente una vez.
    >
    > En el manejo de errores, separaría las fallas transitorias de las definitivas. Para errores de red o timeouts, aplicaría reintentos con Exponential Backoff y Jitter junto con un Circuit Breaker (Resilience4j). Si un mensaje agota sus reintentos por un fallo inesperado, se envía a una Dead Letter Queue (DLQ) con alertas inmediatas para intervención operativa, acompañado de un proceso automático de conciliación contable (Reconciliation Job) que audita periódicamente que cada débito tenga su contrapartida de crédito (partida doble) o su respectiva liberación.
    >
    > Finalmente, en el frontend con Angular, dado que la operación entre servicios es asíncrona por naturaleza, evitaría dejar una petición HTTP bloqueada esperando la resolución completa si involucra sistemas externos lentos. Respondería con un 202 Accepted junto al identificador de la transferencia, reflejaría el estado PENDIENTE en la interfaz y actualizaría la resolución final en tiempo real mediante Server-Sent Events (SSE) o WebSockets, utilizando Signals y ChangeDetectionStrategy.OnPush para informar al usuario si la transferencia fue exitosa o reversada sin obligarlo a recargar ni arriesgar timeouts en el navegador.

4.  Pruebas unitarias y de integración:

    a. Describe cómo diseñarías una suite de pruebas que asegure la correcta operación de una API bancaria, considerando tanto pruebas unitarias como de integración. ¿Qué herramientas utilizarías?

    > Para asegurar la calidad de una API bancaria sin caer en suites de pruebas lentas o frágiles, diseñaría una estrategia basada en la pirámide de pruebas, priorizando la velocidad y el aislamiento en la lógica financiera crítica, y utilizando entornos reales contenerizados para validar la persistencia y las integraciones.
    >
    > En el nivel de pruebas unitarias en Java, el objetivo es validar las reglas de negocio puras en milisegundos sin levantar el contexto de Spring. Utilizaría JUnit 5 (con @ParameterizedTest para probar múltiples casos borde de montos, divisas y comisiones), Mockito para aislar dependencias externas y AssertJ para aserciones fluidas. En banca es crítico verificar cálculos con BigDecimal (redondeos bancarios HALF_EVEN, nunca double), transiciones de estado en las transacciones y validaciones de saldo insuficiente. Además, incluiría ArchUnit para asegurar por código que la arquitectura no se degrade (por ejemplo, que los controladores no accedan directamente a los repositorios o que las entidades de dominio no se expongan en los endpoints).
    >
    > Para las pruebas de integración en el backend, descartaría el uso de bases de datos en memoria como H2, ya que no replican el comportamiento real de bloqueos pesimistas (SELECT ... FOR UPDATE), índices específicos ni niveles de aislamiento del motor productivo. En su lugar, utilizaría Testcontainers para levantar instancias efímeras de PostgreSQL/Oracle, Redis y Kafka en Docker. Aplicaría Testing Slices de Spring Boot para no levantar toda la aplicación innecesariamente: @DataJpaTest para validar queries nativas y bloqueos concurrentes (usando CountDownLatch o CompletableFuture para simular hilos compitiendo por el mismo saldo y verificar que no ocurran condiciones de carrera ni cobros duplicados), @WebMvcTest junto con spring-security-test (@WithMockUser) para probar la capa HTTP, serialización JSON y reglas de autorización (BOLA/CSRF), y WireMock para simular respuestas, latencias y caídas (errores 500 o timeouts) de pasarelas bancarias externas y verificar que los Circuit Breakers e idempotencia respondan correctamente.
    >
    > Para evitar que cambios en el backend rompan la integración con otros microservicios o con el frontend, implementaría Consumer-Driven Contract Testing con Spring Cloud Contract o Pact, garantizando que los esquemas JSON y códigos de respuesta se respeten antes de desplegar.
    >
    > Finalmente, en el frontend con Angular, dividiría las pruebas en dos niveles: pruebas unitarias rápidas con Jest (o Vitest) y Angular Testing Library para validar pipes de formato de moneda, validadores reactivos de formularios de transferencia, interceptores HTTP (manejo de tokens, cabeceras de idempotencia y errores 401/403) y el estado en Signals o servicios RxJS (usando Marble Testing o HttpTestingController para asegurar que operadores como exhaustMap bloqueen dobles envíos). Para los flujos críticos de punta a punta (login, transferencia y descarga de comprobante), utilizaría Playwright automatizado en el pipeline de CI/CD, junto con Pitest (Mutation Testing) en los módulos financieros del backend para asegurar que los tests realmente detecten errores lógicos y no solo inflen el porcentaje de cobertura en SonarQube.

5.  Front-end:

    a. En una aplicación bancaria que muestra el saldo de las cuentas, ¿cómo gestionarías el estado y la autenticación en el front-end, garantizando la seguridad y la coherencia de los datos?

    > Para gestionar el estado y la autenticación en una aplicación bancaria en Angular, diseñaría una arquitectura reactiva centrada en la memoria volátil (en RAM) y en el principio de que el servidor es la única fuente de verdad, evitando persistir información financiera o credenciales en el almacenamiento local del navegador.
    >
    > En cuanto a la autenticación y seguridad de sesión, descartaría por completo guardar tokens en localStorage o sessionStorage para no exponer la cuenta ante un eventual ataque XSS. Utilizaría el patrón BFF (Backend for Frontend) o cookies seguras gestionadas por el servidor (HttpOnly, Secure y SameSite=Strict) junto con la configuración withXsrfConfiguration() en el HttpClient de Angular para mitigar ataques CSRF. Si la arquitectura exige manejar un Access Token JWT en el cliente, lo mantendría exclusivamente en memoria dentro de un servicio de Angular (con vida corta de 5 a 10 minutos) renovándolo de forma transparente mediante un Refresh Token en cookie HttpOnly. Para el control de flujo, implementaría Functional Route Guards (canActivateFn) que validen permisos antes de cargar módulos, un Functional HTTP Interceptor que gestione la renovación de sesión evitando múltiples llamadas simultáneas con un BehaviorSubject o shareReplay(1) cuando expira el token (error 401), y un control de inactividad (usando NgZone.runOutsideAngular para escuchar eventos del DOM sin disparar detección de cambios innecesaria) que cierre la sesión y limpie la memoria tras pocos minutos sin interacción.
    >
    > Para la gestión del estado y la coherencia del saldo, evitaría meter Redux complejo (NgRx Store tradicional) si no hay una justificación fuerte, prefiriendo NgRx SignalStore o servicios reactivos con Angular Signals (signal, computed) combinados con ChangeDetectionStrategy.OnPush. A diferencia de otras aplicaciones, en banca nunca aplicaría actualizaciones optimistas (Optimistic UI) sobre el saldo: el frontend jamás debe restar ni sumar dinero por su cuenta antes de que el backend confirme la transacción. Al ejecutar una transferencia, bloquearía la acción con exhaustMap para impedir dobles envíos, esperaría la confirmación del servidor y actualizaría el estado únicamente con el saldo real retornado por la API o mediante eventos en tiempo real (Server-Sent Events / WebSockets) cuando se procesen movimientos asíncronos.
    >
    > Finalmente, para garantizar la coherencia visual y proteger la privacidad del usuario, invalidaría y recargaría el estado del saldo automáticamente cuando la pestaña recupere el foco (visibilitychange / window:focus) para evitar que el usuario tome decisiones con datos obsoletos tras dejar la ventana abierta. Además, al cerrar sesión o detectar un error 401/403, ejecutaría un reinicio estricto de todos los SignalStores y cachés en memoria para que ningún rastro de saldos o movimientos quede accesible en el navegador ni al usar el botón "Atrás" del historial.

6.  SPRING BOOT

    a. Que pasa en una aplicación internamente cuando usas la anotación @SpringBootApplication y cómo afecta el arranque de una aplicación?

    > La anotación `@SpringBootApplication` es el punto de entrada de cualquier aplicación Spring Boot y actúa como una meta-anotación que agrupa tres anotaciones fundamentales de Spring Framework: **`@SpringBootConfiguration`**, **`@ComponentScan`** y **`@EnableAutoConfiguration`**.
    >
    > Internamente, su presencia desencadena tres procesos clave sobre el contenedor de Inversión de Control (IoC):
    >
    > 1. **`@SpringBootConfiguration` (especialización de `@Configuration`):** Registra la clase principal como una fuente de definición de beans dentro del contexto de la aplicación, permitiendo declarar métodos anotados con `@Bean` directamente en ella.
    > 2. **`@ComponentScan`:** Le indica a Spring que escanee recursivamente el paquete donde se ubica la clase principal y todos sus subpaquetes en busca de estereotipos (`@Component`, `@Service`, `@Repository`, `@Controller`, `@RestController`) para registrarlos como definiciones de beans (`BeanDefinition`). Por diseño, si colocas clases fuera de ese paquete raíz sin declararlo explícitamente, Spring las ignorará.
    > 3. **`@EnableAutoConfiguration`:** Es el motor real de Spring Boot. Utiliza `AutoConfigurationImportSelector` para leer el archivo `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` dentro de las dependencias del _classpath_. A partir de esa lista, evalúa condiciones dinámicas (`@ConditionalOnClass`, `@ConditionalOnMissingBean`, `@ConditionalOnProperty`) y configura automáticamente la infraestructura (por ejemplo, si detecta `spring-boot-starter-web`, configura Tomcat embebido y el `DispatcherServlet`; si detecta `HikariCP` y un driver JDBC, instancia el `DataSource` y el `EntityManagerFactory`).
    >
    > En cuanto a **cómo afecta el arranque de la aplicación**, cuando se invoca `SpringApplication.run()`, esta anotación dirige el ciclo de vida de inicialización en fases secuenciales que impactan directamente el tiempo de inicio (_startup time_) y el consumo de memoria:
    >
    > - **Preparación del entorno y reflexión:** Spring deduce el tipo de aplicación (`SERVLET`, `REACTIVE` o `NONE`), carga los `ApplicationContextInitializer`, construye el `Environment` (leyendo `application.yml`, variables de entorno y perfiles activos) y ejecuta el escaneo del _classpath_. Un `@ComponentScan` sobre paquetes demasiado amplios o con cientos de dependencias degrada el arranque debido al uso intensivo de reflexión e inspección de _bytecode_ (ASM).
    > - **Creación y refresco del contexto (`refresh()`):** Se instancian todos los beans _singleton_ de forma ansiosa (_eager_) por defecto, se resuelven las inyecciones de dependencias, se generan los proxies dinámicos de AOP (para aspectos como `@Transactional` o `@Async`) y, en el caso de aplicaciones web, se levanta el servidor embebido (Tomcat/Netty) en el puerto configurado antes de ejecutar los `CommandLineRunner` o `ApplicationRunner`.
    >
    > Para mitigar el impacto en el arranque en entornos productivos o de microservicios, se suele restringir el alcance del `@ComponentScan`, excluir autoconfiguraciones innecesarias (`@SpringBootApplication(exclude = ...)`), o aplicar compilación anticipada (**AOT - Ahead-Of-Time**) y **CDS (Class Data Sharing)** en Java 21+ para reemplazar la reflexión en tiempo de arranque por código pregenerado.

    b. ¿Cómo funciona el ciclo de vida de un beans en Spring y como podrías intervenir en él?

    > El ciclo de vida de un bean en Spring abarca desde que el contenedor de Inversión de Control (IoC) lee su definición hasta que la instancia es destruida al apagar la aplicación. A diferencia de un objeto Java tradicional instanciado con `new`, un bean atraviesa una serie de fases administradas por el `ApplicationContext` que permiten inyectar dependencias, aplicar proxys de programación orientada a aspectos (AOP) y ejecutar lógica de inicialización o limpieza.
    >
    > Internamente, el flujo ocurre en seis etapas principales:
    >
    > 1. **Carga de metadatos (`BeanDefinition`):** Spring escanea las clases (vía `@ComponentScan`, `@Configuration` o XML) y crea las definiciones de los beans en memoria antes de instanciar cualquier objeto.
    > 2. **Instanciación y resolución de dependencias (DI):** El contenedor instancia la clase (idealmente mediante su constructor) e inyecta las dependencias requeridas. Si se usa inyección por constructor —la práctica recomendada—, la instanciación y la inyección ocurren en un solo paso garantizando inmutabilidad; si se usa `@Autowired` en campos o _setters_, se resuelven inmediatamente después de crear el objeto.
    > 3. **Notificación de interfaces `*Aware`:** Si el bean implementa interfaces como `BeanNameAware` o `ApplicationContextAware`, Spring las invoca para pasarle referencias de la propia infraestructura del contenedor.
    > 4. **Pre-inicialización (`BeanPostProcessor.postProcessBeforeInitialization`):** Antes de ejecutar los métodos de inicio del bean, Spring pasa la instancia por todos los `BeanPostProcessor` registrados (por ejemplo, para procesar anotaciones personalizadas o validar propiedades).
    > 5. **Inicialización del Bean:** Se ejecuta la lógica de preparación del componente en un orden estricto: primero los métodos anotados con **`@PostConstruct`**, luego el método `afterPropertiesSet()` si implementa `InitializingBean`, y finalmente el método `initMethod` configurado en `@Bean`.
    > 6. **Post-inicialización y creación de Proxies (`BeanPostProcessor.postProcessAfterInitialization`):** Este es el punto más crítico en Spring. Aquí el contenedor envuelve el bean original en un **Proxy dinámico (CGLIB o JDK Dynamic Proxy)** si detecta anotaciones como `@Transactional`, `@Async`, `@Cacheable` o `@Secured`. Una vez superado este paso, el bean queda listo para usarse en la aplicación.
    >
    > Cuando el contexto se cierra (`ConfigurableApplicationContext.close()`), los beans con alcance _singleton_ entran en la **fase de destrucción**, ejecutando primero **`@PreDestroy`**, luego `DisposableBean.destroy()` y por último el `destroyMethod` personalizado. (Nota: los beans con `@Scope("prototype")` no son destruidos por Spring; una vez entregados al cliente, el recolector de basura de la JVM se encarga de ellos).
    >
    > **¿Cómo intervenir en el ciclo de vida?**
    >
    > Para intervenir a **nivel de un bean individual** (por ejemplo, calentar una caché local, abrir un socket o cerrar un pool de recursos), el estándar moderno es usar las anotaciones de Jakarta **`@PostConstruct`** y **`@PreDestroy`**, ya que no acoplan el código a interfaces propias de Spring (`InitializingBean` / `DisposableBean`). Si el bean pertenece a una librería de terceros cuyo código fuente no puedes modificar con anotaciones, se interviene declarando `@Bean(initMethod = "iniciar", destroyMethod = "cerrar")` en una clase `@Configuration`. Un detalle técnico fundamental es que dentro de `@PostConstruct` aún no están activos los proxies de `@Transactional`, por lo que si necesitas ejecutar consultas transaccionales al arrancar la aplicación, es mejor intervenir escuchando el evento `ApplicationReadyEvent` con `@EventListener`.
    >
    > Para intervenir de **forma transversal o global** sobre múltiples beans, Spring ofrece dos mecanismos avanzados: **`BeanFactoryPostProcessor`**, que permite interceptar y modificar los metadatos (`BeanDefinition`) _antes_ de que cualquier bean sea instanciado, y **`BeanPostProcessor`**, que permite interceptar las instancias ya creadas para auditarlas, inyectar lógica transversal o envolverlas en un proxy personalizado antes de que entren en operación.

    c. ¿Cómo personalizarías el comportamiento de la auto-configuración en Spring Boot sin romper la filosofía de 'convención sobre configuración'?

    > Para personalizar la auto-configuración en Spring Boot respetando la filosofía de **convención sobre configuración**, la clave es no deshabilitar el motor de arranque ni reescribir desde cero lo que el framework ya resuelve, sino intervenir de forma gradual aprovechando sus mecanismos condicionales. Aplicaría esta personalización en cuatro niveles según la necesidad, de menor a mayor intrusión:
    >
    > **1. Configuración declarativa y `@ConfigurationProperties**`El primer nivel consiste en ajustar las propiedades expuestas en`application.yml`(por ejemplo, tamaños de pool en`spring.datasource.hikari._`o serialización en`spring.jackson._`). Como las clases de auto-configuración de Spring Boot enlazan estos valores internamente mediante `@EnableConfigurationProperties`, puedes alterar el comportamiento sin tocar una sola línea de código Java.
    >
    > **2. Uso de _Customizers_ en lugar de reemplazar el Bean completo**
    > Un error común es declarar un `@Bean` de `ObjectMapper`, `RestTemplate` o `WebClient` desde cero solo para agregar un módulo o un _header_, lo que apaga toda la configuración optimizada de Spring Boot. La forma limpia es registrar beans de tipo **`*Customizer`** (como `Jackson2ObjectMapperBuilderCustomizer`, `RestClientCustomizer` o `WebServerFactoryCustomizer`). Estos actúan como _hooks_ que modifican únicamente la propiedad deseada conservando el resto de las convenciones intactas.
    >
    > **3. Aprovechar `@ConditionalOnMissingBean` (Reemplazo selectivo)**
    > Casi todas las auto-configuraciones nativas de Spring Boot están protegidas con la anotación `@ConditionalOnMissingBean`. Como el contenedor evalúa primero los beans definidos por el desarrollador en sus clases `@Configuration` y luego procesa las auto-configuraciones, basta con declarar tu propio `@Bean` específico (por ejemplo, un `PasswordEncoder` o un `SecurityFilterChain` a medida). Spring Boot detectará que ese componente ya existe en el contexto y cederá el control ("_backs away_") automáticamente para esa pieza sin afectar el resto del módulo.
    >
    > **4. Creación de Auto-configuraciones propias y Exclusión controlada**
    > En arquitecturas empresariales con múltiples microservicios (por ejemplo, para estandarizar trazabilidad, seguridad o clientes HTTP corporativos), crearía un _starter_ interno registrando la clase en el archivo `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` con la anotación `@AutoConfiguration(before = ... / after = ...)`. Dentro de ella combinaría anotaciones como `@ConditionalOnClass`, `@ConditionalOnProperty` y `@ConditionalOnMissingBean` para que los demás equipos obtengan el comportamiento por defecto solo con agregar la dependencia, manteniendo la opción de sobreescribirlo si lo necesitan. Finalmente, si una auto-configuración específica no aplica al proyecto (como `DataSourceAutoConfiguration` en un servicio sin base de datos), se excluye puntualmente mediante `@SpringBootApplication(exclude = ...)` o en propiedades con `spring.autoconfigure.exclude`.
